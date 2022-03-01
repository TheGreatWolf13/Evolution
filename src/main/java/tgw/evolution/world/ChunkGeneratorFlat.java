//package tgw.evolution.world;
//
//import com.mojang.serialization.Codec;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.Blocks;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.Blockreader;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.biome.provider.SingleBiomeProvider;
//import net.minecraft.world.chunk.IChunk;
//import net.minecraft.world.gen.ChunkGenerator;
//import net.minecraft.world.gen.FlatGenerationSettings;
//import net.minecraft.world.gen.Heightmap;
//import net.minecraft.world.gen.WorldGenRegion;
//import net.minecraft.world.gen.feature.structure.StructureManager;
//import tgw.evolution.init.EvolutionBiomes;
//
//import java.util.Arrays;
//
//public class ChunkGeneratorFlat extends ChunkGenerator {
//
//    public static final Codec<ChunkGeneratorFlat> CODEC = FlatGenerationSettings.CODEC.fieldOf("settings")
//                                                                                      .xmap(ChunkGeneratorFlat::new,
//                                                                                            ChunkGeneratorFlat::getGenSettings)
//                                                                                      .codec();
//    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
//    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
//    private final FlatGenerationSettings settings;
//
//    public ChunkGeneratorFlat(FlatGenerationSettings settings) {
//        super(new SingleBiomeProvider(EvolutionBiomes.FOREST), new SingleBiomeProvider(EvolutionBiomes.FOREST), settings.structureSettings(), 0L);
//        this.settings = settings;
//    }
//
//    @Override
//    public void buildSurfaceAndBedrock(WorldGenRegion region, IChunk chunk) {
//        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
//        for (int x = 0; x < 16; x++) {
//            for (int z = 0; z < 16; z++) {
//                chunk.setBlockState(mutablePos.set(x, 0, z), BEDROCK, false);
//                for (int y = 1; y < 65; y++) {
//                    mutablePos.setY(y);
//                    chunk.setBlockState(mutablePos, STONE, false);
//                }
//            }
//        }
//    }
//
//    @Override
//    protected Codec<? extends ChunkGenerator> codec() {
//        return CODEC;
//    }
//
//    @Override
//    public void fillFromNoise(IWorld world, StructureManager structureManager, IChunk chunk) {
//        BlockState[] states = this.settings.getLayers();
//        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
//        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Type.OCEAN_FLOOR_WG);
//        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Type.WORLD_SURFACE_WG);
//        for (int y = 0; y < states.length; y++) {
//            BlockState state = states[y];
//            if (state != null) {
//                for (int x = 0; x < 16; ++x) {
//                    for (int z = 0; z < 16; ++z) {
//                        chunk.setBlockState(mutablePos.set(x, y, z), state, false);
//                        oceanFloor.update(x, y, z, state);
//                        surface.update(x, y, z, state);
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    public IBlockReader getBaseColumn(int i, int j) {
//        return new Blockreader(Arrays.stream(this.settings.getLayers())
//                                     .map(state -> state == null ? Blocks.AIR.defaultBlockState() : state)
//                                     .toArray(BlockState[]::new));
//    }
//
//    @Override
//    public int getBaseHeight(int x, int z, Heightmap.Type heightmapType) {
//        BlockState[] states = this.settings.getLayers();
//        for (int i = states.length - 1; i >= 0; i--) {
//            BlockState state = states[i];
//            if (state != null && heightmapType.isOpaque().test(state)) {
//                return i + 1;
//            }
//        }
//        return 0;
//    }
//
//    public FlatGenerationSettings getGenSettings() {
//        return this.settings;
//    }
//
//    @Override
//    public int getSpawnHeight() {
//        return 64;
//    }
//
//    @Override
//    public ChunkGenerator withSeed(long seed) {
//        return this;
//    }
//}
