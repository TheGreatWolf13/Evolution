package tgw.evolution.mixin;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import net.minecraft.util.concurrent.TickDelayedTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.init.EvolutionStats;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends RecursiveEventLoop<TickDelayedTask> {

    public MinecraftServerMixin(String p_i50401_1_) {
        super(p_i50401_1_);
    }

    @Shadow
    public abstract PlayerList getPlayerList();

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onTickServer(BooleanSupplier supplier, CallbackInfo ci) {
        if (!((Object) this instanceof IntegratedServer)) {
            for (ServerPlayerEntity player : this.getPlayerList().getPlayers()) {
                player.awardStat(EvolutionStats.TIME_WITH_WORLD_OPEN);
            }
        }
    }
}
