package tgw.evolution.world.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import tgw.evolution.util.EnumRockVariant;
import tgw.evolution.util.EnumVanillaRockVariant;
import tgw.evolution.util.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static tgw.evolution.util.EnumRockVariant.*;

public class StrataFeature extends Feature<NoFeatureConfig> {

    public static final List<EnumRockVariant> SURFACE_BLOCKS = ImmutableList.of(ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DOLOMITE, GNEISS, LIMESTONE, MARBLE, PHYLLITE, QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE);
    public static final List<EnumRockVariant> BOTTOM_BLOCKS = ImmutableList.of(DIORITE, GABBRO, GNEISS, GRANITE, MARBLE, PHYLLITE, QUARTZITE, SCHIST, SLATE);
    public static final Random RANDOM = new Random();
    public static List<EnumRockVariant> LAYER_SURFACE = new ArrayList<>(SURFACE_BLOCKS);
    public static List<EnumRockVariant> LAYER_BOTTOM = new ArrayList<>(BOTTOM_BLOCKS);
    public static Long seed;

    public StrataFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactoryIn) {
        super(configFactoryIn);
    }

    private static int getChunkUpperLine(int chunkX, int chunkZ) {
        return (int) (8 * MathHelper.sin(chunkX * MathHelper.PI / 9f - chunkZ * MathHelper.PI / 15f)) + 40;
    }

    private static int getChunkLowerLine(int chunkX, int chunkZ) {
        return (int) (5 * MathHelper.cos(chunkX * MathHelper.PI / 18f - chunkZ * 11 * MathHelper.PI / 90f)) + 15;
    }

    private static int getBlockModifier(int absX, int absZ) {
        return (int) (2 * MathHelper.sin(absX * MathHelper.PI / 9f - absZ * MathHelper.PI / 15f));
    }

    //Mine
    private static EnumRockVariant getSurfaceVariant(int absX, int absZ) {
        float smoothness = 0.125f; // The less the smoother
        float arrepioX = 10f;  //Arrepia along the Z axis
        float arrepioZ = 10f;  //Arrepia along the X axis
        float precisionMaybe = 0.0001f;
        int chosen = (int) ((MathHelper.sin(0.001f * absX) + MathHelper.cos(0.001f * absZ) + MathHelper.cos(smoothness * precisionMaybe * (100 * MathHelper.sin(arrepioX * absX * smoothness)) * (100 * MathHelper.sin(arrepioZ * absZ * smoothness)))) * SURFACE_BLOCKS.size());
        chosen = Math.abs(chosen) % LAYER_SURFACE.size();
        return LAYER_SURFACE.get(chosen);
    }

    private static EnumRockVariant getMiddleVariant(int absX, int absZ, EnumRockVariant surface) {
        int chosen = Math.abs((int) (absX * 0.017f - absZ * 0.006f) % LAYER_BOTTOM.size());
        EnumRockVariant attempt = LAYER_BOTTOM.get(chosen);
        if (attempt == surface) {
            if (chosen + 1 == LAYER_BOTTOM.size()) {
                return LAYER_BOTTOM.get(0);
            }
            return LAYER_BOTTOM.get(chosen + 1);
        }
        return attempt;
    }

    private static EnumRockVariant getBottomVariant(int absX, int absZ, EnumRockVariant middle) {
        int chosen = Math.abs((int) (absX * 0.02f + absZ * 0.008f) % LAYER_BOTTOM.size());
        EnumRockVariant attempt = LAYER_BOTTOM.get(chosen);
        if (attempt == middle) {
            if (chosen + 1 == LAYER_BOTTOM.size()) {
                return LAYER_BOTTOM.get(0);
            }
            return LAYER_BOTTOM.get(chosen + 1);
        }
        return attempt;
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (seed == null || seed != generator.getSeed()) {
            seed = generator.getSeed();
            Collections.copy(LAYER_SURFACE, SURFACE_BLOCKS);
            Collections.copy(LAYER_BOTTOM, BOTTOM_BLOCKS);
            RANDOM.setSeed(seed);
            Collections.shuffle(LAYER_SURFACE, RANDOM);
            Collections.shuffle(LAYER_BOTTOM, RANDOM);
        }
        IChunk currentChunk = worldIn.getChunk(pos);
        int startX = currentChunk.getPos().getXStart();
        int startZ = currentChunk.getPos().getZStart();
        int endX = currentChunk.getPos().getXEnd();
        int endZ = currentChunk.getPos().getZEnd();
        int chunkLowerLine = getChunkLowerLine(currentChunk.getPos().x, currentChunk.getPos().z);
        int chunkUpperLine = getChunkUpperLine(currentChunk.getPos().x, currentChunk.getPos().z);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = startX; x <= endX; x++) {
            mutablePos.setX(x);
            for (int z = startZ; z <= endZ; z++) {
                mutablePos.setZ(z);
                int columnModifier = getBlockModifier(x, z);
                EnumRockVariant surface = getSurfaceVariant(x, z);
                EnumRockVariant middle = getMiddleVariant(x, z, surface);
                EnumRockVariant bottom = getBottomVariant(x, z, middle);
                for (int y = 255; y > 0; y--) {
                    mutablePos.setY(y);
                    Block blockAtPos = worldIn.getBlockState(mutablePos).getBlock();
                    EnumVanillaRockVariant vanilla = EnumVanillaRockVariant.fromBlock(blockAtPos);
                    if (vanilla == null) {
                        continue;
                    }
                    if (y > chunkUpperLine + columnModifier) {
                        worldIn.setBlockState(mutablePos, surface.fromEnumVanillaRep(vanilla).getDefaultState(), 4);
                        continue;
                    }
                    if (y > chunkLowerLine + columnModifier) {
                        worldIn.setBlockState(mutablePos, middle.fromEnumVanillaRep(vanilla).getDefaultState(), 4);
                        continue;
                    }
                    worldIn.setBlockState(mutablePos, bottom.fromEnumVanillaRep(vanilla).getDefaultState(), 4);
                }
            }
        }
        return true;
    }
}
