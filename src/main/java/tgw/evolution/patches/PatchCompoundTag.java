package tgw.evolution.patches;

import net.minecraft.nbt.Tag;
import tgw.evolution.util.collection.maps.O2OMap;

public interface PatchCompoundTag {

    default void clear() {
        throw new AbstractMethodError();
    }

    default O2OMap<String, Tag> tags() {
        throw new AbstractMethodError();
    }
}
