package tgw.evolution.patches;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public interface PatchBiomeManager {

    default Holder<Biome> getBiome_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
