package tgw.evolution.world;

import net.minecraft.block.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.OverworldGenSettings;
import tgw.evolution.init.EvolutionBiomes;

public class EvWorldDefault extends WorldType {

    public EvWorldDefault() {
        super("ev_default");
    }

    @Override
    public ChunkGenerator<?> createChunkGenerator(World world) {
        if (world.getDimension().getType() == DimensionType.OVERWORLD) {
            OverworldGenSettings genSettings = new OverworldGenSettings();
            genSettings.setDefaultBlock(Blocks.STONE.getDefaultState());
            SingleBiomeProviderSettings settings = new SingleBiomeProviderSettings();
            settings.setBiome(EvolutionBiomes.FOREST.get());
            return new OverworldChunkGenerator(world, new SingleBiomeProvider(settings), genSettings);
        }
        if (world.getDimension().getType() == DimensionType.THE_NETHER) {
            return WorldType.DEFAULT.createChunkGenerator(world);
        }
        if (world.getDimension().getType() == DimensionType.THE_END) {
            return WorldType.DEFAULT.createChunkGenerator(world);
        }
        return super.createChunkGenerator(world);
    }

    @Override
    public float getCloudHeight() {
        return 260;
    }
}
