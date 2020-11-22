package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class BlockSnowable extends BlockGravity {

    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    protected BlockSnowable(Block.Properties builder, int mass) {
        super(builder, mass);
        this.setDefaultState(this.getDefaultState().with(SNOWY, false));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld worldIn,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        if (facing != Direction.UP) {
            return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
        Block block = facingState.getBlock();
        //TODO proper snow
        return stateIn.with(SNOWY, block == Blocks.SNOW_BLOCK || block == Blocks.SNOW);
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        entityIn.fall(fallDistance, 0.95F);
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(SNOWY);
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
}