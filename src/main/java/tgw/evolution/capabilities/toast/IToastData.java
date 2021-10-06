package tgw.evolution.capabilities.toast;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IToastData extends INBTSerializable<CompoundNBT> {

    void trigger(ServerPlayerEntity player, ItemStack stack);
}
