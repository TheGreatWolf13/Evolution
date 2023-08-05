package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;

public interface PatchWorldBorder {

    default boolean isInsideCloseToBorder_(Entity entity, double xSize, double zSize) {
        throw new AbstractMethodError();
    }

    default boolean isWithinBounds_(int x, int z) {
        throw new AbstractMethodError();
    }
}
