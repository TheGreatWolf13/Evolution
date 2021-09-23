package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.test.TestCollection;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.Logger;
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

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends RecursiveEventLoop<TickDelayedTask> implements IMinecraftServerPatch {

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
    private Map<RegistryKey<World>, long[]> perWorldTickTimes;
    @Shadow
    private PlayerList playerList;
    @Shadow
    private IProfiler profiler;
    @Shadow
    @Final
    private Random random;
    @Shadow
    @Final
    private Snooper snooper;
    @Shadow
    @Final
    private ServerStatusResponse status;
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
    public abstract NetworkSystem getConnection();

    @Shadow
    public abstract FunctionManager getFunctions();

    @Shadow
    public abstract int getMaxPlayers();

    @Shadow
    public abstract int getPlayerCount();

    @Shadow
    public abstract PlayerList getPlayerList();

    @Shadow
    protected abstract ServerWorld[] getWorldArray();

    @Override
    public boolean isMultiplayerPaused() {
        return this.isMultiplayerPaused;
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServer(BooleanSupplier supplier, CallbackInfo ci) {
        if (!((Object) this instanceof IntegratedServer)) {
            for (ServerPlayerEntity player : this.getPlayerList().getPlayers()) {
                player.awardStat(EvolutionStats.TIME_WITH_WORLD_OPEN);
            }
        }
    }

    @Shadow
    public abstract boolean saveAllChunks(boolean p_213211_1_, boolean p_213211_2_, boolean p_213211_3_);

    @Override
    public void setMultiplayerPaused(boolean paused) {
        this.isMultiplayerPaused = paused;
        if (paused) {
            Evolution.LOGGER.info("Pausing Multiplayer Server");
        }
        else {
            Evolution.LOGGER.info("Resuming Multiplayer Server");
        }
        EvolutionNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new PacketSCMultiplayerPause(paused));
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle multiplayer pause.
     */
    @Overwrite
    protected void tickChildren(BooleanSupplier booleanSupplier) {
        this.profiler.push("commandFunctions");
        if (!this.isMultiplayerPaused) { //Added check for multiplayer pause
            this.getFunctions().tick();
        }
        this.profiler.popPush("levels");
        for (ServerWorld serverworld : this.getWorldArray()) {
            long tickStart = Util.getNanos();
            //noinspection ObjectAllocationInLoop
            this.profiler.push(() -> serverworld + " " + serverworld.dimension().location());
            if (this.tickCount % 20 == 0 || !this.wasPaused && this.isMultiplayerPaused) { //Added check for multiplayer pause
                this.profiler.push("timeSync");
                //noinspection ObjectAllocationInLoop
                this.playerList.broadcastAll(new SUpdateTimePacket(serverworld.getGameTime(),
                                                                   serverworld.getDayTime(),
                                                                   serverworld.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)),
                                             serverworld.dimension());
                this.profiler.pop();
            }
            this.profiler.push("tick");
            if (!this.isMultiplayerPaused) { //Added check for multiplayer pause
                BasicEventHooks.onPreWorldTick(serverworld);
                try {
                    serverworld.tick(booleanSupplier);
                }
                catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception ticking world");
                    serverworld.fillReportDetails(crashreport);
                    throw new ReportedException(crashreport);
                }
                BasicEventHooks.onPostWorldTick(serverworld);
            }
            this.profiler.pop();
            this.profiler.pop();
            //noinspection ObjectAllocationInLoop
            this.perWorldTickTimes.computeIfAbsent(serverworld.dimension(), k -> new long[100])[this.tickCount % 100] = Util.getNanos() - tickStart;
        }
        this.profiler.popPush("connection");
        this.getConnection().tick();
        this.profiler.popPush("players");
        this.playerList.tick();
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            TestCollection.singleton.tick();
        }
        this.profiler.popPush("server gui refresh");
        for (Runnable tickable : this.tickables) {
            tickable.run();
        }
        this.profiler.pop();
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle multiplayer pausing.
     */
    @Overwrite
    protected void tickServer(BooleanSupplier booleanSupplier) {
        long i = Util.getNanos();
        BasicEventHooks.onPreServerTick();
        if (!this.isMultiplayerPaused) { //Added check for multiplayer pause
            ++this.tickCount;
        }
        this.tickChildren(booleanSupplier);
        if (i - this.lastServerStatus >= 5_000_000_000L) {
            this.lastServerStatus = i;
            this.status.setPlayers(new ServerStatusResponse.Players(this.getMaxPlayers(), this.getPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.getPlayerCount(), 12)];
            int j = MathHelper.nextInt(this.random, 0, this.getPlayerCount() - agameprofile.length);
            for (int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = this.playerList.getPlayers().get(j + k).getGameProfile();
            }
            Collections.shuffle(Arrays.asList(agameprofile));
            this.status.getPlayers().setSample(agameprofile);
            this.status.invalidateJson();
        }
        if (this.tickCount % 6_000 == 0 || !this.wasPaused && this.isMultiplayerPaused) { //Added check for multiplayer pause
            LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.playerList.saveAll();
            this.saveAllChunks(true, false, false);
            this.profiler.pop();
            LOGGER.debug("Autosave finished");
        }
        this.profiler.push("snooper");
        if (!this.snooper.isStarted() && this.tickCount > 100) {
            this.snooper.start();
        }
        if (this.tickCount % 6_000 == 0) {
            this.snooper.prepare();
        }
        this.profiler.pop();
        this.profiler.push("tallying");
        long l = this.tickTimes[this.tickCount % 100] = Util.getNanos() - i;
        this.averageTickTime = this.averageTickTime * 0.8F + l / 1_000_000.0F * 0.199_999_99F;
        long i1 = Util.getNanos();
        this.frameTimer.logFrameDuration(i1 - i);
        this.profiler.pop();
        BasicEventHooks.onPostServerTick();
        this.wasPaused = this.isMultiplayerPaused; //Update wasPaused field
    }
}
