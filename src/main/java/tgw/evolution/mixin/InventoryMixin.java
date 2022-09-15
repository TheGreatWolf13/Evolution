package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.IMinecraftPatch;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void onSwapPaint(double scrollAmount, CallbackInfo ci) {
        if (((IMinecraftPatch) Minecraft.getInstance()).isMultiplayerPaused() || ClientEvents.getInstance().isInSpecialAttack()) {
            ci.cancel();
        }
    }
}
