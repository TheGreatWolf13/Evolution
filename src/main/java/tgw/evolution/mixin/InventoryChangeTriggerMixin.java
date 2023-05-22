package tgw.evolution.mixin;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.capabilities.toast.CapabilityToast;
import tgw.evolution.capabilities.toast.IToastData;
import tgw.evolution.init.EvolutionCapabilities;

@Mixin(InventoryChangeTrigger.class)
public abstract class InventoryChangeTriggerMixin extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {

    @Shadow
    protected abstract void trigger(ServerPlayer pPlayer, Inventory pInventory, ItemStack pStack, int pFull, int pEmpty, int pOccupied);

    /**
     * @author TheGreatWolf
     * @reason Add hook for IToast
     */
    @Overwrite
    public void trigger(ServerPlayer player, Inventory inventory, ItemStack stack) {
        int i = 0;
        int j = 0;
        int k = 0;
        for (int l = 0; l < inventory.getContainerSize(); ++l) {
            ItemStack itemstack = inventory.getItem(l);
            if (itemstack.isEmpty()) {
                ++j;
            }
            else {
                ++k;
                if (itemstack.getCount() >= itemstack.getMaxStackSize()) {
                    ++i;
                }
            }
        }
        this.trigger(player, inventory, stack, i, j, k);
        IToastData toast = EvolutionCapabilities.getRevivedCapability(player, CapabilityToast.INSTANCE);
        toast.trigger(player, stack);
    }
}
