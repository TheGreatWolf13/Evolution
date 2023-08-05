package tgw.evolution.patches;

import net.minecraft.world.entity.ai.village.poi.PoiType;

public interface PatchPoiSection {

    default void add_(int x, int y, int z, PoiType poi) {
        throw new AbstractMethodError();
    }

    default void remove_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
