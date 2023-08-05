package tgw.evolution.patches;

import net.minecraft.world.phys.shapes.VoxelShape;

public interface PatchCollisionContext {

    default boolean isAbove_(VoxelShape shape, int x, int y, int z, boolean canAscend) {
        throw new AbstractMethodError();
    }
}
