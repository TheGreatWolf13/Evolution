package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;

public abstract class BlockXYZAxis extends BlockGravity {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	public BlockXYZAxis(Properties builder, int mass) {
		super(builder, mass);
		this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.Y));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		switch(rot) {
		case COUNTERCLOCKWISE_90:
		case CLOCKWISE_90:
			switch(state.get(AXIS)) {
			case X:
				return state.with(AXIS, Direction.Axis.Z);
			case Z:
				return state.with(AXIS, Direction.Axis.X);
			default:
				return state;
			}
		default:
			return state;
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(AXIS);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(AXIS, context.getFace().getAxis());
	}
}