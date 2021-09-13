package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;

import static tgw.evolution.init.EvolutionBStates.AXIS;

public abstract class BlockXYZAxis extends BlockGravity {

    public BlockXYZAxis(Properties builder, int mass) {
        super(builder, mass);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(AXIS);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        switch (rot) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90: {
                switch (state.getValue(AXIS)) {
                    case X: {
                        return state.setValue(AXIS, Direction.Axis.Z);
                    }
                    case Z: {
                        return state.setValue(AXIS, Direction.Axis.X);
                    }
                    default: {
                        return state;
                    }
                }
            }
            default: {
                return state;
            }
        }
    }
}