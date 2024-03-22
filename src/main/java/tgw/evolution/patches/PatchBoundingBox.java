package tgw.evolution.patches;

import net.minecraft.world.level.levelgen.structure.BoundingBox;

public interface PatchBoundingBox {

    default BoundingBox encapsulate_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isInside_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
