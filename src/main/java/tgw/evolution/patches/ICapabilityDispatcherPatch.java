package tgw.evolution.patches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public interface ICapabilityDispatcherPatch {

    INBTSerializable<Tag>[] getWriters();

    CompoundTag serializeNBTNoAlloc(CompoundTag nbt);
}
