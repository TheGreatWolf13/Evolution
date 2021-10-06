package tgw.evolution.mixin;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.capabilities.toast.CapabilityToast;
import tgw.evolution.capabilities.toast.IToastData;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(InventoryChangeTrigger.class)
public abstract class InventoryChangeTriggerMixin extends AbstractCriterionTrigger<InventoryChangeTrigger.Instance> {

    @Inject(method = "trigger(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/entity/player/PlayerInventory;" +
                     "Lnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
    private void onTrigger(ServerPlayerEntity player, PlayerInventory inv, ItemStack stack, CallbackInfo ci) {
        IToastData toast = player.getCapability(CapabilityToast.INSTANCE).orElseThrow(IllegalStateException::new);
        toast.trigger(player, stack);
    }
}
