package tgw.evolution.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.gen.ChunkGenerator;
import tgw.evolution.init.EvolutionBiomes;

public class EvWorldFlat extends WorldType {

    public EvWorldFlat() {
        super("ev_flat");
    }

    @Override
    public float getCloudHeight() {
        return 260;
    }

    @Override
    public ChunkGenerator<?> createChunkGenerator(World world) {
        SingleBiomeProviderSettings settings = new SingleBiomeProviderSettings();
        settings.setBiome(EvolutionBiomes.FOREST.get());
        return new EvFlatChunkGenerator(world, new SingleBiomeProvider(settings));
    }
}
