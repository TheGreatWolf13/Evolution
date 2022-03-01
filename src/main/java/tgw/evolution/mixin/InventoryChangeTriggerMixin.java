package tgw.evolution.mixin;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.capabilities.toast.CapabilityToast;
import tgw.evolution.capabilities.toast.IToastData;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(InventoryChangeTrigger.class)
public abstract class InventoryChangeTriggerMixin extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {

    @Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;" +
                     "Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void onTrigger(ServerPlayer player, Inventory inv, ItemStack stack, CallbackInfo ci) {
        IToastData toast = player.getCapability(CapabilityToast.INSTANCE).orElseThrow(IllegalStateException::new);
        toast.trigger(player, stack);
    }
}
