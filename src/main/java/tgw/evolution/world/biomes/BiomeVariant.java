package tgw.evolution.world.biomes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public abstract class BiomeVariant extends Biome {

    public BiomeVariant(Builder biomeBuilder) {
        super(biomeBuilder);
    }

    @Override
    public int getGrassColor(BlockPos pos) {
        return this.getBase().getGrassColor(pos);
    }

    @Override
    public int getFoliageColor(BlockPos pos) {
        return this.getBase().getFoliageColor(pos);
    }

    @Override
    public int getSkyColorByTemp(float currentTemperature) {
        return this.getBase().getSkyColorByTemp(currentTemperature);
    }

    public abstract Biome getBase();
}
