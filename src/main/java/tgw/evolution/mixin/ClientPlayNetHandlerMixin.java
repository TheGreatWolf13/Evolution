package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SJoinGamePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.ISJoinGamePacketPatch;

@Mixin(ClientPlayNetHandler.class)
public abstract class ClientPlayNetHandlerMixin {

    @Shadow
    private ClientWorld.ClientWorldInfo levelData;

    @Shadow
    private Minecraft minecraft;

    @Redirect(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setDeltaMovement(DDD)V",
            ordinal = 0))
    private void handleMovePlayerProxy(PlayerEntity player, double x, double y, double z) {

    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;<init>" +
                                                                        "(Lnet/minecraft/client/network/play/ClientPlayNetHandler;" +
                                                                        "Lnet/minecraft/client/world/ClientWorld$ClientWorldInfo;" +
                                                                        "Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;" +
                                                                        "ILjava/util/function/Supplier;" +
                                                                        "Lnet/minecraft/client/renderer/WorldRenderer;ZJ)V"))
    private void onHandleLogin0(SJoinGamePacket packet, CallbackInfo ci) {
        this.levelData.setDayTime(((ISJoinGamePacketPatch) packet).getDaytime());
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/ClientHooks;firePlayerLogin" +
                                                                        "(Lnet/minecraft/client/multiplayer/PlayerController;" +
                                                                        "Lnet/minecraft/client/entity/player/ClientPlayerEntity;" +
                                                                        "Lnet/minecraft/network/NetworkManager;)V", ordinal = 0))
    private void onHandleLogin1(SJoinGamePacket packet, CallbackInfo ci) {
        this.minecraft.player.setDeltaMovement(((ISJoinGamePacketPatch) packet).getMotion());
        this.minecraft.player.fallDistance = 1.0f;
    }
}
