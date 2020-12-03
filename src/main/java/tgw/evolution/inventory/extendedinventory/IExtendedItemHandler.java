package tgw.evolution.inventory.extendedinventory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IExtendedItemHandler extends IItemHandlerModifiable {

    boolean isItemValidForSlot(int slot, ItemStack stack, LivingEntity player);

    void setChanged(int slot, boolean change);
}
