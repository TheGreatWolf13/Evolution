package tgw.evolution.patches;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public interface PatchLevelReader {

    default Holder<Biome> getBiome_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default float getBrightness_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default int getMaxLocalRawBrightness_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default int getMaxLocalRawBrightness_(int x, int y, int z, int skyDarken) {
        throw new AbstractMethodError();
    }
}
