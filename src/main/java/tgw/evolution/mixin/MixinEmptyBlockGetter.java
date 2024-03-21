package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;

@Mixin(EmptyBlockGetter.class)
public abstract class MixinEmptyBlockGetter implements BlockGetter {
    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos implementation
     */
    @Override
    @Overwrite
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        return null;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos implementation
     */
    @Override
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockState getBlockStateAtSide(int x, int y, int z, Direction direction) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        return Blocks.AIR.defaultBlockState();
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos implementation
     */
    @Override
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        return Fluids.EMPTY.defaultFluidState();
    }
}
