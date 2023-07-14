package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;

@Mixin(EmptyLevelChunk.class)
public abstract class MixinEmptyLevelChunk extends LevelChunk {

    public MixinEmptyLevelChunk(Level pLevel, ChunkPos pPos) {
        super(pLevel, pPos);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        return null;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z, EntityCreationType creationType) {
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public BlockState getBlockState(BlockPos pos) {
        Evolution.warn("getBlockState(BlockPos) should not be called!");
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockStateAtSide(int x, int y, int z, Direction direction) {
        return Blocks.VOID_AIR.defaultBlockState();
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        return Blocks.VOID_AIR.defaultBlockState();
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public FluidState getFluidState(BlockPos pos) {
        Evolution.warn("getFluidState(BlockPos) should not be called!");
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        return Fluids.EMPTY.defaultFluidState();
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public int getLightEmission(BlockPos pos) {
        Evolution.warn("getLightEmission(BlockPos) should not be called!");
        return this.getLightEmission_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getLightEmission_(int x, int y, int z) {
        return 0;
    }
}
