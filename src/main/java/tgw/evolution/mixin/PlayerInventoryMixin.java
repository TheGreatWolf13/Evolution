package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.IMinecraftPatch;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow
    @Final
    public PlayerEntity player;

    @Inject(method = "setCarried", at = @At("TAIL"))
    private void onSetCarried(ItemStack stack, CallbackInfo ci) {
        if (this.player instanceof ServerPlayerEntity) {
            CriteriaTriggers.INVENTORY_CHANGED.trigger((ServerPlayerEntity) this.player, (PlayerInventory) (Object) this, stack);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void onSwapPaint(double scrollAmount, CallbackInfo ci) {
        if (((IMinecraftPatch) Minecraft.getInstance()).isMultiplayerPaused()) {
            ci.cancel();
        }
    }
}
