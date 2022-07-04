package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.IClientboundLoginPacketPatch;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Shadow
    private ClientLevel.ClientLevelData levelData;

    @Final
    @Shadow
    private Minecraft minecraft;

    @Redirect(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(DDD)V",
            ordinal = 0))
    private void handleMovePlayerProxy(Player player, double x, double y, double z) {
        //Cancel call to prevent player movement from resetting on login.
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;<init>" +
                                                                        "(Lnet/minecraft/client/multiplayer/ClientPacketListener;" +
                                                                        "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;" +
                                                                        "Lnet/minecraft/resources/ResourceKey;" +
                                                                        "Lnet/minecraft/world/level/dimension/DimensionType;" +
                                                                        "IILjava/util/function/Supplier;" +
                                                                        "Lnet/minecraft/client/renderer/LevelRenderer;ZJ)V"))
    private void onHandleLogin0(ClientboundLoginPacket packet, CallbackInfo ci) {
        this.levelData.setDayTime(((IClientboundLoginPacketPatch) (Object) packet).getDaytime());
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;firePlayerLogin" +
                                                                        "(Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;" +
                                                                        "Lnet/minecraft/client/player/LocalPlayer;" +
                                                                        "Lnet/minecraft/network/Connection;)V", ordinal = 0))
    private void onHandleLogin1(ClientboundLoginPacket packet, CallbackInfo ci) {
        this.minecraft.player.setDeltaMovement(((IClientboundLoginPacketPatch) (Object) packet).getMotion());
        this.minecraft.player.fallDistance = 1.0f;
    }
}