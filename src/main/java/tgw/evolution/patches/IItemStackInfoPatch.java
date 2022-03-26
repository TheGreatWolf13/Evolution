package tgw.evolution.patches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

public interface IItemStackInfoPatch {

    CompoundTag getCapNBT();

    int getCount();

    Item getItem();

    CompoundTag getTag();

    void setCapNBT(CompoundTag capNBT);
}
