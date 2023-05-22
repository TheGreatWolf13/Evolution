package tgw.evolution.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.patches.IMinecraftPatch;

import java.net.Proxy;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin extends MinecraftServer {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private boolean paused;
    @Shadow
    private int previousSimulationDistance;

    public IntegratedServerMixin(Thread pServerThread,
                                 LevelStorageSource.LevelStorageAccess pStorageSource,
                                 PackRepository pPackRepository,
                                 WorldStem p_206549_,
                                 Proxy pProxy,
                                 DataFixer pFixerUpper,
                                 @Nullable MinecraftSessionService pSessionService,
                                 @Nullable GameProfileRepository pProfileRepository,
                                 @Nullable GameProfileCache pProfileCache,
                                 ChunkProgressListenerFactory pProgressListenerFactory) {
        super(pServerThread, pStorageSource, pPackRepository, p_206549_, pProxy, pFixerUpper, pSessionService, pProfileRepository, pProfileCache,
              pProgressListenerFactory);
    }

    @Shadow
    protected abstract void tickPaused();

    /**
     * @author TheGreatWolf
     * @reason Increase stats
     */
    @Override
    @Overwrite
    public void tickServer(BooleanSupplier hasTime) {
        PlayerList playerList = this.getPlayerList();
        List<ServerPlayer> players = playerList.getPlayers();
        for (int i = 0, len = players.size(); i < len; i++) {
            players.get(i).awardStat(EvolutionStats.TIME_WITH_WORLD_OPEN);
        }
        boolean wasPaused = this.paused;
        this.paused = this.minecraft.isPaused() && !((IMinecraftPatch) this.minecraft).isMultiplayerPaused();
        ProfilerFiller profiler = this.getProfiler();
        if (!wasPaused && this.paused) {
            profiler.push("autoSave");
            LOGGER.info("Saving and pausing game...");
            this.saveEverything(false, false, false);
            profiler.pop();
        }
        boolean isConnected = this.minecraft.getConnection() != null;
        if (isConnected && this.paused) {
            this.tickPaused();
        }
        else {
            super.tickServer(hasTime);
            int renderDistance = Math.max(2, this.minecraft.options.renderDistance);
            if (renderDistance != playerList.getViewDistance()) {
                LOGGER.info("Changing view distance to {}, from {}", renderDistance, playerList.getViewDistance());
                playerList.setViewDistance(renderDistance);
            }
            int simDistance = Math.max(2, this.minecraft.options.simulationDistance);
            if (simDistance != this.previousSimulationDistance) {
                LOGGER.info("Changing simulation distance to {}, from {}", simDistance, this.previousSimulationDistance);
                playerList.setSimulationDistance(simDistance);
                this.previousSimulationDistance = simDistance;
            }
        }
    }
}
