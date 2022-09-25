package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.TickTask;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.PacketSCMultiplayerPause;
import tgw.evolution.patches.IMinecraftServerPatch;

import java.util.*;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<TickTask> implements IMinecraftServerPatch {

    @Shadow
    @Final
    public static GameProfile ANONYMOUS_PLAYER_PROFILE;
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    public long[] tickTimes;
    @Shadow
    private float averageTickTime;
    @Shadow
    @Final
    private FrameTimer frameTimer;
    private boolean isMultiplayerPaused;
    @Shadow
    private long lastServerStatus;
    @Shadow
    private Map<ResourceKey<Level>, long[]> perWorldTickTimes;
    @Shadow
    private PlayerList playerList;
    @Shadow
    private ProfilerFiller profiler;
    @Shadow
    @Final
    private Random random;
    @Shadow
    @Final
    private ServerStatus status;
    @Shadow
    private int tickCount;
    @Shadow
    @Final
    private List<Runnable> tickables;
    private boolean wasPaused;

    public MinecraftServerMixin(String name) {
        super(name);
    }

    @Shadow
    @Nullable
    public abstract ServerConnectionListener getConnection();

    @Shadow
    public abstract ServerFunctionManager getFunctions();

    @Shadow
    public abstract int getMaxPlayers();

    @Shadow
    public abstract int getPlayerCount();

    @Shadow
    public abstract PlayerList getPlayerList();

    @Shadow
    protected abstract ServerLevel[] getWorldArray();

    @Shadow
    public abstract boolean hidesOnlinePlayers();

    @Override
    public boolean isMultiplayerPaused() {
        return this.isMultiplayerPaused;
    }

    @Shadow
    public abstract boolean isSingleplayer();

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServer(BooleanSupplier supplier, CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof DedicatedServer) {
            for (ServerPlayer player : this.getPlayerList().getPlayers()) {
                player.awardStat(EvolutionStats.TIME_WITH_WORLD_OPEN);
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
        }
        else {
            Evolution.info("Resuming Multiplayer Server");
        }
        EvolutionNetwork.sendToAll(new PacketSCMultiplayerPause(paused));
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle multiplayer pause.
     */
    @Overwrite
    public void tickChildren(BooleanSupplier booleanSupplier) {
        this.profiler.push("commandFunctions");
        if (!this.isMultiplayerPaused) { //Added check for multiplayer pause
            this.getFunctions().tick();
        }
        this.profiler.popPush("levels");
        for (ServerLevel level : this.getWorldArray()) {
            long tickStart = Util.getNanos();
            //noinspection ObjectAllocationInLoop
            this.profiler.push(() -> level + " " + level.dimension().location());
            if (this.tickCount % 20 == 0 || !this.wasPaused && this.isMultiplayerPaused) { //Added check for multiplayer pause
                this.profiler.push("timeSync");
                //noinspection ObjectAllocationInLoop
                this.playerList.broadcastAll(new ClientboundSetTimePacket(level.getGameTime(),
                                                                          level.getDayTime(),
                                                                          level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)),
                                             level.dimension());
                this.profiler.pop();
            }
            this.profiler.push("tick");
            if (!this.isMultiplayerPaused) { //Added check for multiplayer pause
                ForgeEventFactory.onPreWorldTick(level, booleanSupplier);
                try {
                    level.tick(booleanSupplier);
                }
                catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception ticking world");
                    level.fillReportDetails(crashreport);
                    throw new ReportedException(crashreport);
                }
                ForgeEventFactory.onPostWorldTick(level, booleanSupplier);
            }
            this.profiler.pop();
            this.profiler.pop();
            long[] tickTimes = this.perWorldTickTimes.get(level.dimension());
            if (tickTimes == null) {
                tickTimes = new long[100];
                this.perWorldTickTimes.put(level.dimension(), tickTimes);
            }
            tickTimes[this.tickCount % 100] = Util.getNanos() - tickStart;
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
        for (Runnable tickable : this.tickables) {
            tickable.run();
        }
        this.profiler.pop();
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle multiplayer pausing.
     */
    @Overwrite
    public void tickServer(BooleanSupplier booleanSupplier) {
        long i = Util.getNanos();
        ForgeEventFactory.onPreServerTick(booleanSupplier);
        if (!this.isMultiplayerPaused) { //Added check for multiplayer pause
            ++this.tickCount;
        }
        this.tickChildren(booleanSupplier);
        if (i - this.lastServerStatus >= 5_000_000_000L) {
            this.lastServerStatus = i;
            this.status.setPlayers(new ServerStatus.Players(this.getMaxPlayers(), this.getPlayerCount()));
            if (!this.hidesOnlinePlayers()) {
                GameProfile[] agameprofile = new GameProfile[Math.min(this.getPlayerCount(), 12)];
                int j = Mth.nextInt(this.random, 0, this.getPlayerCount() - agameprofile.length);
                for (int k = 0; k < agameprofile.length; ++k) {
                    ServerPlayer serverplayer = this.playerList.getPlayers().get(j + k);
                    if (serverplayer.allowsListing()) {
                        agameprofile[k] = serverplayer.getGameProfile();
                    }
                    else {
                        agameprofile[k] = ANONYMOUS_PLAYER_PROFILE;
                    }
                }
                Collections.shuffle(Arrays.asList(agameprofile));
                assert this.status.getPlayers() != null;
                this.status.getPlayers().setSample(agameprofile);
            }
            this.status.invalidateJson();
        }
        if (this.tickCount % 6_000 == 0 || !this.wasPaused && this.isMultiplayerPaused) { //Added check for multiplayer pause
            LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.saveEverything(true, false, false);
            this.profiler.pop();
            LOGGER.debug("Autosave finished");
        }
        this.profiler.push("tallying");
        long l = this.tickTimes[this.tickCount % 100] = Util.getNanos() - i;
        this.averageTickTime = this.averageTickTime * 0.8F + l / 1_000_000.0F * 0.199_999_99F;
        long i1 = Util.getNanos();
        this.frameTimer.logFrameDuration(i1 - i);
        this.profiler.pop();
        ForgeEventFactory.onPostServerTick(booleanSupplier);
        this.wasPaused = this.isMultiplayerPaused; //Update wasPaused field
    }
}
