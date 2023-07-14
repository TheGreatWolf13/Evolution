package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchLevelReader;

@Mixin(LevelReader.class)
public interface MixinLevelReader extends BlockAndTintGetter, PatchLevelReader {

    @Shadow
    DimensionType dimensionType();

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

    @Overwrite
    default int getMaxLocalRawBrightness(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getMaxLocalRawBrightness_(pos.getX(), pos.getY(), pos.getZ());
    }

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
}
