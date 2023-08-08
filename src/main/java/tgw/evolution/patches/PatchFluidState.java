package tgw.evolution.patches;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;
import java.util.random.RandomGenerator;

public interface PatchFluidState {

    default void animateTick_(Level level, int x, int y, int z, RandomGenerator random) {
        throw new AbstractMethodError();
    }

    default float getHeight_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default VoxelShape getShape_(BlockGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void randomTick_(Level level, int x, int y, int z, Random random) {
        throw new AbstractMethodError();
    }
}
