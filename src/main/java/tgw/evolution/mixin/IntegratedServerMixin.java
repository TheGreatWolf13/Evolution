package tgw.evolution.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
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

    public IntegratedServerMixin(Thread thread,
                                 DynamicRegistries.Impl registries,
                                 SaveFormat.LevelSave save,
                                 IServerConfiguration serverConfig,
                                 ResourcePackList resourcePacks,
                                 Proxy proxy,
                                 DataFixer dataFixer,
                                 DataPackRegistries dataPackRegistries,
                                 MinecraftSessionService sessionService,
                                 GameProfileRepository profileRepository,
                                 PlayerProfileCache playerProfileCache,
                                 IChunkStatusListenerFactory chunkStatus) {
        super(thread,
              registries,
              save,
              serverConfig,
              resourcePacks,
              proxy,
              dataFixer,
              dataPackRegistries,
              sessionService,
              profileRepository,
              playerProfileCache,
              chunkStatus);
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServer(BooleanSupplier supplier, CallbackInfo ci) {
        for (ServerPlayerEntity player : this.getPlayerList().getPlayers()) {
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
