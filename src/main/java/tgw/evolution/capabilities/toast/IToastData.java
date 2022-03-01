package tgw.evolution.capabilities.toast;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public interface IToastData extends INBTSerializable<CompoundTag> {

    void trigger(ServerPlayer player, ItemStack stack);
}
