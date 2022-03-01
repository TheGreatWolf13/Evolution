//package tgw.evolution.world;
//
//import net.minecraft.util.registry.Registry;
//import net.minecraft.world.biome.Biome;
//import net.minecraft.world.biome.provider.SingleBiomeProvider;
//import net.minecraft.world.gen.ChunkGenerator;
//import net.minecraft.world.gen.DimensionSettings;
//import net.minecraft.world.gen.NoiseChunkGenerator;
//import net.minecraftforge.common.world.ForgeWorldType;
//import tgw.evolution.init.EvolutionBiomes;
//
//public class WorldEvolutionDefault extends ForgeWorldType {
//
//    public WorldEvolutionDefault() {
//        super((biomeRegistry, dimensionSettingsRegistry, seed) -> new NoiseChunkGenerator(new SingleBiomeProvider(EvolutionBiomes.FOREST),
//                                                                                          seed,
//                                                                                          () -> dimensionSettingsRegistry.getOrThrow
//                                                                                          (DimensionSettings.OVERWORLD)));
//    }
//
//    @Override
//    public ChunkGenerator createChunkGenerator(Registry<Biome> biomeRegistry,
//                                               Registry<DimensionSettings> dimensionSettingsRegistry,
//                                               long seed,
//                                               String generatorSettings) {
////        if (world.getDimension().getType() == DimensionType.OVERWORLD) {
////            OverworldGenSettings genSettings = new OverworldGenSettings();
////            genSettings.setDefaultBlock(Blocks.STONE.getDefaultState());
////            genSettings.setDefaultFluid(EvolutionBlocks.FRESH_WATER.get().getDefaultState());
////            SingleBiomeProviderSettings settings = new SingleBiomeProviderSettings();
////            settings.setBiome(EvolutionBiomes.FOREST.get());
////            return new OverworldChunkGenerator(world, new SingleBiomeProvider(settings), genSettings);
////        }
////        if (world.getDimension().getType() == DimensionType.THE_NETHER) {
////            return WorldType.DEFAULT.createChunkGenerator(world);
////        }
////        if (world.getDimension().getType() == DimensionType.THE_END) {
////            return WorldType.DEFAULT.createChunkGenerator(world);
////        }
////        return super.createChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed, generatorSettings);
//        return new NoiseChunkGenerator(new SingleBiomeProvider(EvolutionBiomes.FOREST),
//                                       seed,
//                                       () -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.OVERWORLD));
//    }
//}
