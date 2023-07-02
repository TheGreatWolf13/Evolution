package tgw.evolution.patches;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public interface IBiomeManagerPatch {

    Holder<Biome> getBiome(int x, int y, int z);
}
