package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import static tgw.evolution.init.EvolutionBStates.SNOWY;

public abstract class BlockGenericSlowable extends BlockGravity implements ICollisionBlock {

    protected BlockGenericSlowable(Block.Properties builder, int mass) {
        super(builder, mass);
        this.registerDefaultState(this.defaultBlockState().setValue(SNOWY, false));
    }

    @Override
    public void collision(LivingEntity entity, double speed) {
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(SNOWY);
    }

    @Override
    public float getSlowdownSide(BlockState state) {
        return 1.0f;
    }

    @Override
    public float getSlowdownTop(BlockState state) {
        if (state.getValue(SNOWY)) {
            return 0.95f;
        }
        return 1.0f;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Block block = context.getLevel().getBlockState(context.getClickedPos().above()).getBlock();
        //TODO proper snow
        return this.defaultBlockState().setValue(SNOWY, block == Blocks.SNOW_BLOCK || block == Blocks.SNOW);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        if (facing != Direction.UP) {
            return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
        }
        Block block = facingState.getBlock();
        //TODO proper snow
        return state.setValue(SNOWY, block == Blocks.SNOW_BLOCK || block == Blocks.SNOW);
    }
}