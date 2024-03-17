package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchLevelReader;
import tgw.evolution.world.util.LevelUtils;

@Mixin(LevelReader.class)
public interface MixinLevelReader extends BlockAndTintGetter, PatchLevelReader {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default boolean containsAnyLiquid(AABB bb) {
        Evolution.deprecatedMethod();
        return LevelUtils.containsAnyLiquid((LevelReader) this, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    @Shadow
    DimensionType dimensionType();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default Holder<Biome> getBiome(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBiome_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Shadow
    BiomeManager getBiomeManager();

    @Override
    default Holder<Biome> getBiome_(int x, int y, int z) {
        return this.getBiomeManager().getBiome_(x, y, z);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    default int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        Evolution.deprecatedMethod();
        return this.getBlockTint_(pos.getX(), pos.getY(), pos.getZ(), colorResolver);
    }

    @Override
    default int getBlockTint_(int x, int y, int z, ColorResolver colorResolver) {
        return colorResolver.getColor(this.getBiome_(x, y, z).value(), x, z);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Deprecated
    @Overwrite
    default float getBrightness(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBrightness_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    default float getBrightness_(int x, int y, int z) {
        return this.dimensionType().brightness(this.getMaxLocalRawBrightness_(x, y, z));
    }

    @Shadow
    ChunkAccess getChunk(int i, int j);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default int getDirectSignal(BlockPos pos, Direction dir) {
        Evolution.deprecatedMethod();
        return this.getDirectSignal_(pos.getX(), pos.getY(), pos.getZ(), dir);
    }

    @Override
    default int getDirectSignal_(int x, int y, int z, Direction direction) {
        return this.getBlockState_(x, y, z).getDirectSignal(this, new BlockPos(x, y, z), direction);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default int getMaxLocalRawBrightness(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getMaxLocalRawBrightness_(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default int getMaxLocalRawBrightness(BlockPos pos, int skyDarken) {
        Evolution.deprecatedMethod();
        return this.getMaxLocalRawBrightness_(pos.getX(), pos.getY(), pos.getZ(), skyDarken);
    }

    @Override
    default int getMaxLocalRawBrightness_(int x, int y, int z) {
        return this.getMaxLocalRawBrightness_(x, y, z, this.getSkyDarken());
    }

    @Override
    default int getMaxLocalRawBrightness_(int x, int y, int z, int skyDarken) {
        return x >= -30_000_000 && z >= -30_000_000 && x < 30_000_000 && z < 30_000_000 ?
               this.getRawBrightness_(BlockPos.asLong(x, y, z), skyDarken) :
               15;
    }

    @Shadow
    int getSkyDarken();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default boolean isEmptyBlock(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isEmptyBlock_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    default boolean isEmptyBlock_(int x, int y, int z) {
        return this.getBlockState_(x, y, z).isAir();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default boolean isWaterAt(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isWaterAt_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    default boolean isWaterAt_(int x, int y, int z) {
        return this.getFluidState_(x, y, z).is(FluidTags.WATER);
    }
}
