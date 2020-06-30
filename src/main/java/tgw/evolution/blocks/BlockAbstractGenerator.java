//package tgw.evolution.blocks;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.item.BlockItemUseContext;
//import net.minecraft.state.DirectionProperty;
//import net.minecraft.state.StateContainer.Builder;
//import net.minecraft.state.properties.BlockStateProperties;
//import net.minecraft.util.Direction;
//
//public abstract class BlockAbstractGenerator extends BlockWeight {
//	
//	public static final DirectionProperty FACING = BlockStateProperties.FACING;
//			
//	public BlockAbstractGenerator(Properties properties, int density) {
//		super(properties, density);
//		this.setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
//	}
//
//	public abstract float electromotiveForce();
//	
//	@Override
//	protected void fillStateContainer(Builder<Block, BlockState> builder) {
//		builder.add(FACING);
//	}
//	
//	@Override
//	public BlockState getStateForPlacement(BlockItemUseContext context) {
//		return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
//	}
//}
