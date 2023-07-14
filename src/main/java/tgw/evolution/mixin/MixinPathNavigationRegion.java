package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;

@Mixin(PathNavigationRegion.class)
public abstract class MixinPathNavigationRegion implements BlockGetter {

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Override
    @Overwrite
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        return this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)).getBlockEntity_(x, y, z);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Override
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        if (this.isOutsideBuildHeight(y)) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)).getBlockState_(x, y, z);
    }

    @Shadow
    protected abstract ChunkAccess getChunk(int i, int j);

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Override
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        if (this.isOutsideBuildHeight(y)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)).getFluidState_(x, y, z);
    }
}
