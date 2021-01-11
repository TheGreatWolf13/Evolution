package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import static tgw.evolution.init.EvolutionBStates.SNOWY;

public abstract class BlockGenericSlowable extends BlockGravity implements ISoftBlock {

    protected BlockGenericSlowable(Block.Properties builder, int mass) {
        super(builder, mass);
        this.setDefaultState(this.getDefaultState().with(SNOWY, false));
    }

    @Override
    public void collision(LivingEntity entity, double speed) {
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(SNOWY);
    }

    @Override
    public float getSlowdownSide(BlockState state) {
        return 1.0f;
    }

    @Override
    public float getSlowdownTop(BlockState state) {
        if (state.get(SNOWY)) {
            return 0.95f;
        }
        return 1.0f;
    }

    @Override
    public BlockState getStateForPlacement(BlockState state,
                                           Direction facing,
                                           BlockState state2,
                                           IWorld world,
                                           BlockPos pos1,
                                           BlockPos pos2,
                                           Hand hand) {
        Block block = world.getBlockState(pos1.up()).getBlock();
        //TODO proper snow
        return this.getDefaultState().with(SNOWY, block == Blocks.SNOW_BLOCK || block == Blocks.SNOW);
    }

    @Override
    public BlockState updatePostPlacement(BlockState state,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld world,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        if (facing != Direction.UP) {
            return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
        }
        Block block = facingState.getBlock();
        //TODO proper snow
        return state.with(SNOWY, block == Blocks.SNOW_BLOCK || block == Blocks.SNOW);
    }
}