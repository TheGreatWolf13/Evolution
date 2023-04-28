package tgw.evolution.patches;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface IItemStackPatch {

    void forceSerializeCaps();

    @Nullable CompoundTag getCapNBT();
}
