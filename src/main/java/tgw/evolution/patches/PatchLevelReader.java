package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public interface PatchLevelReader {

    default Holder<Biome> getBiome_(BlockPos pos) {
        return this.getBiome_(pos.getX(), pos.getY(), pos.getZ());
    }

    default Holder<Biome> getBiome_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default float getBrightness_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default int getDirectSignal_(int x, int y, int z, Direction direction) {
        throw new AbstractMethodError();
    }

    default int getMaxLocalRawBrightness_(int x, int y, int z, int skyDarken) {
        throw new AbstractMethodError();
    }

    default int getMaxLocalRawBrightness_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isEmptyBlock_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default boolean isEmptyBlock_(BlockPos pos) {
        return this.isEmptyBlock_(pos.getX(), pos.getY(), pos.getZ());
    }

    default boolean isWaterAt_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
