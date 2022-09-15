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
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.patches.IMinecraftPatch;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin extends MinecraftServer {

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

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServer(BooleanSupplier supplier, CallbackInfo ci) {
        for (ServerPlayer player : this.getPlayerList().getPlayers()) {
            player.awardStat(EvolutionStats.TIME_WITH_WORLD_OPEN);
        }
    }

    @Redirect(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isPaused()Z"))
    private boolean tickServerProxy(Minecraft minecraft) {
        if (!minecraft.isPaused()) {
            return false;
        }
        return !((IMinecraftPatch) minecraft).isMultiplayerPaused();
    }
}
