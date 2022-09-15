package tgw.evolution.capabilities.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IInventory extends INBTSerializable<CompoundTag>, IItemHandlerModifiable {

    boolean isItemValidForSlot(int slot, ItemStack stack, LivingEntity player);

    void setChanged(int slot, boolean change);
}
