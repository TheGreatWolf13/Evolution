package tgw.evolution.patches;

import net.minecraft.nbt.CompoundTag;

public interface PatchGameRules {

    default void loadFromTag(CompoundTag tag) {
        throw new AbstractMethodError();
    }
}
