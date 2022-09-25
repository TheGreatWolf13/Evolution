package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.IMinecraftPatch;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(Inventory.class)
public abstract class InventoryMixinClient {

    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void onSwapPaint(double scrollAmount, CallbackInfo ci) {
        if (((IMinecraftPatch) Minecraft.getInstance()).isMultiplayerPaused() || ClientEvents.getInstance().shouldRenderSpecialAttack()) {
            ci.cancel();
        }
    }
}
