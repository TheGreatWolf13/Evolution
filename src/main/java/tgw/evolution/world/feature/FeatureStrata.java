package tgw.evolution.world.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.RockVariant;
import tgw.evolution.util.VanillaRockVariant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static tgw.evolution.util.RockVariant.*;

public class FeatureStrata extends Feature<NoFeatureConfig> {

    public static final List<RockVariant> SURFACE_BLOCKS = ImmutableList.of(ANDESITE,
                                                                            BASALT,
                                                                            CHALK,
                                                                            CHERT,
                                                                            CONGLOMERATE,
                                                                            DACITE,
                                                                            DOLOMITE,
                                                                            GNEISS,
                                                                            LIMESTONE,
                                                                            MARBLE,
                                                                            PHYLLITE,
                                                                            QUARTZITE,
                                                                            RED_SANDSTONE,
                                                                            SANDSTONE,
                                                                            SCHIST,
                                                                            SHALE,
                                                                            SLATE);
    public static final List<RockVariant> BOTTOM_BLOCKS = ImmutableList.of(DIORITE,
                                                                           GABBRO,
                                                                           GNEISS,
                                                                           GRANITE,
                                                                           MARBLE,
                                                                           PHYLLITE,
                                                                           QUARTZITE,
                                                                           SCHIST,
                                                                           SLATE);
    public static final Random RANDOM = new Random();
    public static final List<RockVariant> LAYER_BOTTOM = new ArrayList<>(BOTTOM_BLOCKS);
    public static final List<RockVariant> LAYER_SURFACE = new ArrayList<>(SURFACE_BLOCKS);
    public static Long seed;

    public FeatureStrata(Codec<NoFeatureConfig> configFactory) {
        super(configFactory);
    }

    private static int getBlockModifier(int absX, int absZ) {
        return (int) (2 * MathHelper.sin(absX * MathHelper.PI / 9.0f - absZ * MathHelper.PI / 15.0f));
    }

    private static RockVariant getBottomVariant(int absX, int absZ, RockVariant middle) {
        int chosen = Math.abs((int) (absX * 0.02f + absZ * 0.008f) % LAYER_BOTTOM.size());
        RockVariant attempt = LAYER_BOTTOM.get(chosen);
        if (attempt == middle) {
            if (chosen + 1 == LAYER_BOTTOM.size()) {
                return LAYER_BOTTOM.get(0);
            }
            return LAYER_BOTTOM.get(chosen + 1);
        }
        return attempt;
    }

    private static int getChunkLowerLine(int chunkX, int chunkZ) {
        return (int) (5 * MathHelper.cos(chunkX * MathHelper.PI / 18.0f - chunkZ * 11 * MathHelper.PI / 90.0f)) + 15;
    }

    private static int getChunkUpperLine(int chunkX, int chunkZ) {
        return (int) (8 * MathHelper.sin(chunkX * MathHelper.PI / 9.0f - chunkZ * MathHelper.PI / 15.0f)) + 40;
    }

    private static RockVariant getMiddleVariant(int absX, int absZ, RockVariant surface) {
        int chosen = Math.abs((int) (absX * 0.017f - absZ * 0.006f) % LAYER_BOTTOM.size());
        RockVariant attempt = LAYER_BOTTOM.get(chosen);
        if (attempt == surface) {
            if (chosen + 1 == LAYER_BOTTOM.size()) {
                return LAYER_BOTTOM.get(0);
            }
            return LAYER_BOTTOM.get(chosen + 1);
        }
        return attempt;
    }

    //Mine
    private static RockVariant getSurfaceVariant(int absX, int absZ) {
        float smoothness = 0.125f; // The less the smoother
        float arrepioX = 10.0f;  //Arrepia along the Z axis
        float arrepioZ = 10.0f;  //Arrepia along the X axis
        float precisionMaybe = 0.000_1f;
        int chosen = (int) ((MathHelper.sin(0.001f * absX) +
                             MathHelper.cos(0.001f * absZ) +
                             MathHelper.cos(smoothness *
                                            precisionMaybe *
                                            (100 * MathHelper.sin(arrepioX * absX * smoothness)) *
                                            (100 * MathHelper.sin(arrepioZ * absZ * smoothness)))) * SURFACE_BLOCKS.size());
        chosen = Math.abs(chosen) % LAYER_SURFACE.size();
        return LAYER_SURFACE.get(chosen);
    }

    @Override
    public boolean place(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (seed == null || seed != world.getSeed()) {
            seed = world.getSeed();
            Collections.copy(LAYER_SURFACE, SURFACE_BLOCKS);
            Collections.copy(LAYER_BOTTOM, BOTTOM_BLOCKS);
            RANDOM.setSeed(seed);
            Collections.shuffle(LAYER_SURFACE, RANDOM);
            Collections.shuffle(LAYER_BOTTOM, RANDOM);
        }
        IChunk currentChunk = world.getChunk(pos);
        int startX = currentChunk.getPos().getMinBlockX();
        int startZ = currentChunk.getPos().getMinBlockZ();
        int endX = currentChunk.getPos().getMaxBlockX();
        int endZ = currentChunk.getPos().getMaxBlockZ();
        int chunkLowerLine = getChunkLowerLine(currentChunk.getPos().x, currentChunk.getPos().z);
        int chunkUpperLine = getChunkUpperLine(currentChunk.getPos().x, currentChunk.getPos().z);
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        for (int x = startX; x <= endX; x++) {
            mutablePos.setX(x);
            for (int z = startZ; z <= endZ; z++) {
                mutablePos.setZ(z);
                int columnModifier = getBlockModifier(x, z);
                RockVariant surface = getSurfaceVariant(x, z);
                RockVariant middle = getMiddleVariant(x, z, surface);
                RockVariant bottom = getBottomVariant(x, z, middle);
                for (int y = 255; y > 0; y--) {
                    mutablePos.setY(y);
                    Block blockAtPos = world.getBlockState(mutablePos).getBlock();
                    VanillaRockVariant vanilla = VanillaRockVariant.fromBlock(blockAtPos);
                    if (vanilla == null) {
                        continue;
                    }
                    if (y > chunkUpperLine + columnModifier) {
                        world.setBlock(mutablePos, surface.fromEnumVanillaRep(vanilla).defaultBlockState(), BlockFlags.NO_RERENDER);
                        continue;
                    }
                    if (y > chunkLowerLine + columnModifier) {
                        world.setBlock(mutablePos, middle.fromEnumVanillaRep(vanilla).defaultBlockState(), BlockFlags.NO_RERENDER);
                        continue;
                    }
                    world.setBlock(mutablePos, bottom.fromEnumVanillaRep(vanilla).defaultBlockState(), BlockFlags.NO_RERENDER);
                }
            }
        }
        return true;
    }
}
