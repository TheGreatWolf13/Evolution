//package tgw.evolution.world.feature.structures;
//
//import com.mojang.serialization.Codec;
//import net.minecraft.util.SharedSeedRandom;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.ChunkPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.util.registry.DynamicRegistries;
//import net.minecraft.world.biome.Biome;
//import net.minecraft.world.biome.provider.BiomeProvider;
//import net.minecraft.world.gen.ChunkGenerator;
//import net.minecraft.world.gen.Heightmap;
//import net.minecraft.world.gen.feature.IFeatureConfig;
//import net.minecraft.world.gen.feature.NoFeatureConfig;
//import net.minecraft.world.gen.feature.structure.Structure;
//import net.minecraft.world.gen.feature.structure.StructureStart;
//import net.minecraft.world.gen.feature.template.TemplateManager;
//import tgw.evolution.world.feature.structures.config.ConfigStructDummy;
//
//public class StructureTest extends Structure<NoFeatureConfig> {
//
//    public StructureTest(Codec<NoFeatureConfig> configFactoryIn) {
//        super(configFactoryIn);
//    }
//
//    /**
//     * hasStartAt
//     */
//    @Override
//    public boolean func_230363_a_(ChunkGenerator generator,
//                                  BiomeProvider biomeProvider,
//                                  long seed,
//                                  SharedSeedRandom random,
//                                  int posX,
//                                  int posZ,
//                                  Biome biome,
//                                  ChunkPos chunkPos,
//                                  NoFeatureConfig config) {
//        ChunkPos chunkpos = this.getStartPositionForPosition(random, seed, posX, posZ, 0, 0);
//        return posX == chunkpos.x && posZ == chunkpos.z;
//    }
//
//    protected int getSeedModifier() {
//        return 123_456_789;
//    }
//
////    @Override
////    public int getSize() {
////        return 2;
////    }
//
//    @Override
//    public IStartFactory<NoFeatureConfig> getStartFactory() {
//        return Start::new;
//    }
//
//    protected ChunkPos getStartPositionForPosition(SharedSeedRandom random, long seed, int x, int z, int spacingOffsetsX, int spacingOffsetsZ) {
//        int maxDistance = 16;
//        int xTemp = x + maxDistance * spacingOffsetsX;
//        int ztemp = z + maxDistance * spacingOffsetsZ;
//        int xTemp2 = xTemp < 0 ? xTemp - maxDistance + 1 : xTemp;
//        int zTemp2 = ztemp < 0 ? ztemp - maxDistance + 1 : ztemp;
//        int validChunkX = xTemp2 / maxDistance;
//        int validChunkZ = zTemp2 / maxDistance;
//        random.setLargeFeatureSeedWithSalt(seed, validChunkX, validChunkZ, this.getSeedModifier());
//        validChunkX *= maxDistance;
//        validChunkZ *= maxDistance;
//        int minDistance = 8;
//        validChunkX += random.nextInt(maxDistance - minDistance);
//        validChunkZ += random.nextInt(maxDistance - minDistance);
//        return new ChunkPos(validChunkX, validChunkZ);
//    }
//
//    @Override
//    public String getStructureName() {
//        return "evolution:structure_test";
//    }
//
//    private static class Start extends StructureStart {
//
//        public Start(Structure<?> structure, int chunkX, int chunkZ, MutableBoundingBox bb, int reference, long seed) {
//            super(structure, chunkX, chunkZ, bb, reference, seed);
//        }
//
//        @Override
//        public void func_230364_a_(DynamicRegistries registries,
//                                   ChunkGenerator generator,
//                                   TemplateManager templateManager,
//                                   int chunkX,
//                                   int chunkZ,
//                                   Biome biome,
//                                   IFeatureConfig config) {
//            int x = (chunkX << 4) + 8;
//            int z = (chunkZ << 4) + 8;
//            int surfaceY = generator.getHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG);
//            BlockPos pos = new BlockPos(x, surfaceY - 4, z);
//            StructureTestPieces.start(generator, templateManager, pos, this.components, this.rand, new ConfigStructDummy());
//            this.recalculateStructureSize();
//        }
//    }
//}
