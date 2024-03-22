package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public interface PatchWorldBorder {

    default boolean isInsideCloseToBorder_(Entity entity, double xSize, double zSize) {
        throw new AbstractMethodError();
    }

    default boolean isWithinBounds_(int x, int z) {
        throw new AbstractMethodError();
    }

    default boolean isWithinBounds_(AABB aabb) {
        return this.isWithinBounds_(aabb.minX, aabb.minZ, aabb.maxX, aabb.maxZ);
    }

    default boolean isWithinBounds_(double minX, double minZ, double maxX, double maxZ) {
        throw new AbstractMethodError();
    }
}
