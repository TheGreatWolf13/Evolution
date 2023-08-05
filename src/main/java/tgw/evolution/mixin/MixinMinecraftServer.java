package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.network.Message;
import tgw.evolution.network.PacketSCSimpleMessage;
import tgw.evolution.patches.PatchMinecraftServer;
import tgw.evolution.resources.BuiltinModResourcePackSource;
import tgw.evolution.resources.ModPackResources;
import tgw.evolution.util.time.RealTime;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements PatchMinecraftServer {

    @Shadow @Final public static GameProfile ANONYMOUS_PLAYER_PROFILE;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public long[] tickTimes;
    @Shadow private float averageTickTime;
    @Shadow private @Nullable MinecraftServer.TimeProfiler debugCommandProfiler;
    @Shadow private boolean debugCommandProfilerDelayStart;
    @Shadow private long delayedTasksMaxNextTickTime;
    @Shadow @Final private FrameTimer frameTimer;
    @Unique private boolean isMultiplayerPaused;
    @Shadow private volatile boolean isReady;
    @Shadow private long lastOverloadWarning;
    @Shadow private long lastServerStatus;
    @Shadow private boolean mayHaveDelayedTasks;
    @Shadow private @Nullable String motd;
    @Shadow private long nextTickTime;
    @Shadow private PlayerList playerList;
    @Shadow @Final private @Nullable GameProfileCache profileCache;
    @Shadow private ProfilerFiller profiler;
    @Shadow @Final private Random random;
    @Shadow private volatile boolean running;
    @Shadow @Final private ServerStatus status;
    @Shadow private boolean stopped;
    @Shadow private int tickCount;
    @Shadow @Final private List<Runnable> tickables;
    @Unique private boolean wasPaused;

    public MixinMinecraftServer(String name) {
        super(name);
    }

    @Shadow
    private static CrashReport constructOrExtractCrashReport(Throwable throwable) {
        throw new AbstractMethodError();
    }

    @Redirect(method = "configurePackRepository", at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"))
    private static boolean onCheckDisabled(List<String> list, Object o, PackRepository resourcePackManager) {
        String profileName = (String) o;
        boolean contains = list.contains(profileName);
        if (contains) {
            return true;
        }
        Pack profile = resourcePackManager.getPack(profileName);
        assert profile != null;
        if (profile.getPackSource() instanceof BuiltinModResourcePackSource) {
            PackResources pack = profile.open();
            // Prevents automatic load for built-in data packs provided by mods.
            return pack instanceof ModPackResources modPack && !modPack.getActivationType().isEnabledByDefault();
        }
        return false;
    }

    @Shadow
    protected abstract void endMetricsRecordingTick();

    @Shadow
    public abstract SystemReport fillSystemReport(SystemReport systemReport);

    @Shadow
    public abstract Iterable<ServerLevel> getAllLevels();

    @Shadow
    public abstract @Nullable ServerConnectionListener getConnection();

    @Shadow
    public abstract ServerFunctionManager getFunctions();

    @Shadow
    public abstract int getMaxPlayers();

    @Shadow
    public abstract int getPlayerCount();

    @Shadow
    public abstract PlayerList getPlayerList();

    @Shadow
    public abstract File getServerDirectory();

    @Shadow
    protected abstract boolean haveTime();

    @Shadow
    public abstract boolean hidesOnlinePlayers();

    @Shadow
    protected abstract boolean initServer() throws IOException;

    @Override
    public boolean isMultiplayerPaused() {
        return this.isMultiplayerPaused;
    }

    @Shadow
    public abstract boolean isSingleplayer();

    @Overwrite
    public boolean isUnderSpawnProtection(ServerLevel level, BlockPos pos, Player player) {
        Evolution.deprecatedMethod();
        return this.isUnderSpawnProtection_(level, pos.getX(), pos.getY(), pos.getZ(), player);
    }

    @Override
    public boolean isUnderSpawnProtection_(ServerLevel level, int x, int y, int z, Player player) {
        return false;
    }

    @Shadow
    protected abstract void onServerCrash(CrashReport crashReport);

    @Shadow
    public abstract void onServerExit();

    /**
     * @author TheGreatWolf
     * @reason Change tick rate
     */
    @Overwrite
    public void runServer() {
        try {
            if (this.initServer()) {
                this.nextTickTime = Util.getMillis();
                assert this.motd != null;
                this.status.setDescription(new TextComponent(this.motd));
                this.status.setVersion(new ServerStatus.Version(SharedConstants.getCurrentVersion().getName(),
                                                                SharedConstants.getCurrentVersion().getProtocolVersion()));
                this.updateStatusIcon(this.status);
                while (this.running) {
                    long mspt = TickrateChanger.getMSPT();
                    long msBehind = Util.getMillis() - this.nextTickTime;
                    if (msBehind > 2_000L && this.nextTickTime - this.lastOverloadWarning >= 15_000L) {
                        long m = msBehind / mspt;
                        LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", msBehind, m);
                        this.nextTickTime += m * mspt;
                        this.lastOverloadWarning = this.nextTickTime;
                    }
                    if (this.debugCommandProfilerDelayStart) {
                        this.debugCommandProfilerDelayStart = false;
                        //noinspection ObjectAllocationInLoop
                        this.debugCommandProfiler = new MinecraftServer.TimeProfiler(Util.getNanos(), this.tickCount);
                    }
                    this.nextTickTime += mspt;
                    this.startMetricsRecordingTick();
                    this.profiler.push("tick");
                    //noinspection ObjectAllocationInLoop
                    this.tickServer(this::haveTime);
                    this.profiler.popPush("nextTickWait");
                    this.mayHaveDelayedTasks = true;
                    this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + mspt, this.nextTickTime);
                    this.waitUntilNextTick();
                    this.profiler.pop();
                    this.endMetricsRecordingTick();
                    this.isReady = true;
                    JvmProfiler.INSTANCE.onServerTick(this.averageTickTime);
                }
            }
            else {
                //noinspection ConstantConditions
                this.onServerCrash(null);
            }
        }
        catch (Throwable t) {
            LOGGER.error("Encountered an unexpected exception", t);
            CrashReport crashReport = constructOrExtractCrashReport(t);
            this.fillSystemReport(crashReport.getSystemReport());
            File directory = new File(this.getServerDirectory(), "crash-reports");
            File file = new File(directory, "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt");
            if (crashReport.saveToFile(file)) {
                LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
            }
            else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }
            this.onServerCrash(crashReport);
        }
        finally {
            try {
                this.stopped = true;
                this.stopServer();
            }
            catch (Throwable t) {
                LOGGER.error("Exception stopping the server", t);
            }
            finally {
                if (this.profileCache != null) {
                    this.profileCache.clearExecutor();
                }
                this.onServerExit();
            }
        }
    }

    @Shadow
    public abstract boolean saveEverything(boolean p_195515_, boolean p_195516_, boolean p_195517_);

    @Override
    public void setMultiplayerPaused(boolean paused) {
        this.isMultiplayerPaused = paused;
        if (paused) {
            Evolution.info("Pausing Multiplayer Server");
            this.playerList.broadcastAll(new PacketSCSimpleMessage(Message.S2C.MULTIPLAYER_PAUSE));
        }
        else {
            Evolution.info("Resuming Multiplayer Server");
            this.playerList.broadcastAll(new PacketSCSimpleMessage(Message.S2C.MULTIPLAYER_RESUME));
        }
    }

    @Shadow
    protected abstract void startMetricsRecordingTick();

    @Shadow
    public abstract void stopServer();

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle multiplayer pause.
     */
    @Overwrite
    public void tickChildren(BooleanSupplier hasTimeLeft) {
        this.profiler.push("commandFunctions");
        if (!this.isMultiplayerPaused) {
            this.getFunctions().tick();
        }
        this.profiler.popPush("levels");
        for (ServerLevel level : this.getAllLevels()) {
            //noinspection ObjectAllocationInLoop
            this.profiler.push(() -> level + " " + level.dimension().location());
            if (this.tickCount % 20 == 0 || !this.wasPaused && this.isMultiplayerPaused) {
                this.profiler.push("timeSync");
                //noinspection ObjectAllocationInLoop
                this.playerList.broadcastAll(new ClientboundSetTimePacket(level.getGameTime(),
                                                                          level.getDayTime(),
                                                                          level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)),
                                             level.dimension());
                this.profiler.pop();
            }
            this.profiler.push("tick");
            if (!this.isMultiplayerPaused) {
                try {
                    level.tick(hasTimeLeft);
                }
                catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception ticking world");
                    level.fillReportDetails(crashreport);
                    throw new ReportedException(crashreport);
                }
            }
            this.profiler.pop();
            this.profiler.pop();
        }
        this.profiler.popPush("connection");
        assert this.getConnection() != null;
        this.getConnection().tick();
        this.profiler.popPush("players");
        this.playerList.tick();
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            GameTestTicker.SINGLETON.tick();
        }
        this.profiler.popPush("server gui refresh");
        for (int i = 0, len = this.tickables.size(); i < len; ++i) {
            this.tickables.get(i).run();
        }
        this.profiler.pop();
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle multiplayer pausing.
     */
    @Overwrite
    public void tickServer(BooleanSupplier hasTimeLeft) {
        long nanoTime = Util.getNanos();
        if (!this.isMultiplayerPaused) {
            ++this.tickCount;
        }
        this.tickChildren(hasTimeLeft);
        if (nanoTime - this.lastServerStatus >= 5 * RealTime.SEC_TO_NANO) {
            this.lastServerStatus = nanoTime;
            this.status.setPlayers(new ServerStatus.Players(this.getMaxPlayers(), this.getPlayerCount()));
            if (!this.hidesOnlinePlayers()) {
                GameProfile[] gameProfiles = new GameProfile[Math.min(this.getPlayerCount(), 12)];
                int i = Mth.nextInt(this.random, 0, this.getPlayerCount() - gameProfiles.length);
                for (int j = 0; j < gameProfiles.length; ++j) {
                    ServerPlayer serverPlayer = this.playerList.getPlayers().get(i + j);
                    if (serverPlayer.allowsListing()) {
                        gameProfiles[j] = serverPlayer.getGameProfile();
                    }
                    else {
                        gameProfiles[j] = ANONYMOUS_PLAYER_PROFILE;
                    }
                }
                Collections.shuffle(Arrays.asList(gameProfiles));
                assert this.status.getPlayers() != null;
                this.status.getPlayers().setSample(gameProfiles);
            }
        }
        if (this.tickCount % (300 * RealTime.SEC_TO_TICKS) == 0 || !this.wasPaused && this.isMultiplayerPaused) {
            LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.saveEverything(true, false, false);
            this.profiler.pop();
            LOGGER.debug("Autosave finished");
        }
        this.profiler.push("tallying");
        long l = this.tickTimes[this.tickCount % 100] = Util.getNanos() - nanoTime;
        this.averageTickTime = this.averageTickTime * 0.8F + l / 1_000_000.0F * 0.199_999_99F;
        long endTickTime = Util.getNanos();
        this.frameTimer.logFrameDuration(endTickTime - nanoTime);
        this.profiler.pop();
        this.wasPaused = this.isMultiplayerPaused;
    }

    @Shadow
    protected abstract void updateStatusIcon(ServerStatus serverStatus);

    @Shadow
    protected abstract void waitUntilNextTick();
}
