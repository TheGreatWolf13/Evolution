package tgw.evolution.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.init.EvolutionStats;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin extends MinecraftServer {

    public IntegratedServerMixin(Thread p_i232576_1_,
                                 DynamicRegistries.Impl p_i232576_2_,
                                 SaveFormat.LevelSave p_i232576_3_,
                                 IServerConfiguration p_i232576_4_,
                                 ResourcePackList p_i232576_5_,
                                 Proxy p_i232576_6_,
                                 DataFixer p_i232576_7_,
                                 DataPackRegistries p_i232576_8_,
                                 MinecraftSessionService p_i232576_9_,
                                 GameProfileRepository p_i232576_10_,
                                 PlayerProfileCache p_i232576_11_,
                                 IChunkStatusListenerFactory p_i232576_12_) {
        super(p_i232576_1_,
              p_i232576_2_,
              p_i232576_3_,
              p_i232576_4_,
              p_i232576_5_,
              p_i232576_6_,
              p_i232576_7_,
              p_i232576_8_,
              p_i232576_9_,
              p_i232576_10_,
              p_i232576_11_,
              p_i232576_12_);
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServer(BooleanSupplier supplier, CallbackInfo ci) {
        for (ServerPlayerEntity player : this.getPlayerList().getPlayers()) {
            player.awardStat(EvolutionStats.TIME_WITH_WORLD_OPEN);
        }
    }
}
