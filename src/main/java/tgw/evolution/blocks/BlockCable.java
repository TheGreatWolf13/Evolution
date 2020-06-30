//package tgw.evolution.blocks;
//
//import java.util.Map;
//
//import com.google.common.collect.Maps;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.ITileEntityProvider;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.BlockItemUseContext;
//import net.minecraft.state.BooleanProperty;
//import net.minecraft.state.EnumProperty;
//import net.minecraft.state.StateContainer.Builder;
//import net.minecraft.state.properties.BlockStateProperties;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.Direction;
//import net.minecraft.util.Hand;
//import net.minecraft.util.Util;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.BlockRayTraceResult;
//import net.minecraft.util.math.shapes.ISelectionContext;
//import net.minecraft.util.math.shapes.VoxelShape;
//import net.minecraft.util.math.shapes.VoxelShapes;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.World;
//import tgw.evolution.blocks.tileentities.TileEntityCable;
//import tgw.evolution.init.EvolutionItems;
//import tgw.evolution.util.DirectionNullable;
//import tgw.evolution.util.MathHelper;
//
//@SuppressWarnings("deprecation")
//public class BlockCable extends Block implements ITileEntityProvider {
//	
////	public static final BooleanProperty NORTH_CONNECTION = BlockStateProperties.NORTH;
////	public static final BooleanProperty SOUTH_CONNECTION = BlockStateProperties.SOUTH;
////	public static final BooleanProperty WEST_CONNECTION = BlockStateProperties.WEST;
////	public static final BooleanProperty EAST_CONNECTION = BlockStateProperties.EAST;
////	public static final BooleanProperty UP_CONNECTION = BlockStateProperties.UP;
////	public static final BooleanProperty DOWN_CONNECTION = BlockStateProperties.DOWN;
//	public static final EnumProperty<DirectionNullable> INPUT_CONNECTION = EnumProperty.create("input_connection", DirectionNullable.class);
//	public static final EnumProperty<DirectionNullable> OUTPUT_CONNECTION = EnumProperty.create("output_connection", DirectionNullable.class);
//	public static final VoxelShape CENTRE = Block.makeCuboidShape(7, 7, 7, 9, 9, 9);
//	public static final VoxelShape WEST = Block.makeCuboidShape(0, 7, 7, 9, 9, 9);
//	public static final VoxelShape EAST = Block.makeCuboidShape(7, 7, 7, 16, 9, 9);
//	public static final VoxelShape NORTH = Block.makeCuboidShape(7, 7, 0, 9, 9, 9);
//	public static final VoxelShape SOUTH = Block.makeCuboidShape(7, 7, 7, 9, 9, 16);
//	public static final VoxelShape UP = Block.makeCuboidShape(7, 7, 7, 9, 16, 9);
//	public static final VoxelShape DOWN = Block.makeCuboidShape(7, 0, 7, 9, 9, 9);
////	public static final Map<Direction, BooleanProperty> DIRECTION_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (map) -> {
////		map.put(Direction.NORTH, NORTH_CONNECTION);
////		map.put(Direction.EAST, EAST_CONNECTION);
////		map.put(Direction.SOUTH, SOUTH_CONNECTION);
////		map.put(Direction.WEST, WEST_CONNECTION);
////		map.put(Direction.UP, UP_CONNECTION);
////		map.put(Direction.DOWN, DOWN_CONNECTION);
////	});
//	public static final Map<DirectionNullable, VoxelShape> DIRECTION_TO_SHAPE = Util.make(Maps.newEnumMap(DirectionNullable.class), (map) -> {
//		map.put(DirectionNullable.NORTH, NORTH);
//		map.put(DirectionNullable.SOUTH, SOUTH);
//		map.put(DirectionNullable.WEST, WEST);
//		map.put(DirectionNullable.EAST, EAST);
//		map.put(DirectionNullable.UP, UP);
//		map.put(DirectionNullable.DOWN, DOWN);
//		map.put(DirectionNullable.NONE, CENTRE);
//	});
//
//	public BlockCable(Properties properties) {
//		super(properties);
//		this.setDefaultState(getDefaultState().with(INPUT_CONNECTION, DirectionNullable.NONE).with(OUTPUT_CONNECTION, DirectionNullable.NONE));
//	}
//	
//	@Override
//	public boolean hasTileEntity() {
//		return true;
//	}
//	
//	@Override
//	public TileEntity createNewTileEntity(IBlockReader worldIn) {
//		return new TileEntityCable();
//	}
//	
//	@Override
//	protected void fillStateContainer(Builder<Block, BlockState> builder) {
//		builder.add(INPUT_CONNECTION, OUTPUT_CONNECTION);
//	}
//	
//	@Override
//	public BlockState getStateForPlacement(BlockItemUseContext context) {
//		BlockState state = getDefaultState();
//		BlockState checkingState;
//		for (Direction dir : Direction.values()) {
//			checkingState = context.getWorld().getBlockState(context.getPos().offset(dir));
//			if (checkingState.getBlock() instanceof BlockAbstractGenerator) {
//				if (checkingState.get(BlockAbstractGenerator.FACING) == dir) {
//					state = state.with(OUTPUT_CONNECTION, DirectionNullable.fromDirection(dir));
//				}
//				else if (checkingState.get(BlockAbstractGenerator.FACING) == dir.getOpposite()) {
//					state = state.with(INPUT_CONNECTION, DirectionNullable.fromDirection(dir));
//				}
//			}
//		}
//		for (Direction dir : Direction.values()) {
//			checkingState = context.getWorld().getBlockState(context.getPos().offset(dir));
//			if (checkingState.getBlock() == this) {
//				if (state.get(OUTPUT_CONNECTION) == DirectionNullable.NONE) {
//					state = state.with(OUTPUT_CONNECTION, DirectionNullable.fromDirection(dir));
//				}
//				else {
//					state = state.with(INPUT_CONNECTION, DirectionNullable.fromDirection(dir));
//				}
//				break;
//			}
//		}
//		return state;
//	}
//	
//	@Override
//	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
//		return VoxelShapes.or(DIRECTION_TO_SHAPE.get(state.get(INPUT_CONNECTION)), DIRECTION_TO_SHAPE.get(state.get(OUTPUT_CONNECTION)));
//	}
//	
//	@Override
//	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {		
//		if (worldIn.getBlockState(facingPos).getBlock() instanceof BlockAbstractGenerator) {
//			if (worldIn.getBlockState(facingPos).get(BlockAbstractGenerator.FACING) == facing && stateIn.get(OUTPUT_CONNECTION) == DirectionNullable.NONE) {
//				return stateIn.with(OUTPUT_CONNECTION, DirectionNullable.fromDirection(facing));
//			}
//			if (worldIn.getBlockState(facingPos).get(BlockAbstractGenerator.FACING) == facing.getOpposite() && stateIn.get(INPUT_CONNECTION) == DirectionNullable.NONE) {
//				return stateIn.with(INPUT_CONNECTION, DirectionNullable.fromDirection(facing));
//			}
//		}
//		else if (worldIn.getBlockState(facingPos).getBlock() == this) {
//			if (worldIn.getBlockState(facingPos).get(OUTPUT_CONNECTION) == DirectionNullable.fromDirection(facing) && stateIn.get(INPUT_CONNECTION) == DirectionNullable.NONE) {
//				return stateIn.with(INPUT_CONNECTION, DirectionNullable.fromDirection(facing));
//			}
//			if (worldIn.getBlockState(facingPos).get(INPUT_CONNECTION) == DirectionNullable.fromDirection(facing) && stateIn.get(OUTPUT_CONNECTION) == DirectionNullable.NONE) {
//				return stateIn.with(OUTPUT_CONNECTION, DirectionNullable.fromDirection(facing));
//			}
//		}
//		return stateIn;
//	}
//	
//	@Override
//	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
//		return false;
//	}
//	
//	@Override
//	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
////		updateConnections(worldIn, pos, state);
//	}
//	
////	private void updateConnections(World worldIn, BlockPos pos, BlockState state) {
////		if (!(worldIn.getTileEntity(pos) instanceof TileEntityCable)) {
////			return;
////		}
////		TileEntityCable tileEntityCable = (TileEntityCable) worldIn.getTileEntity(pos);
////		BlockState checkingState;
////		for (Direction dir : Direction.values()) {
////			if (!state.get(DIRECTION_TO_PROPERTY_MAP.get(dir))) {
////				continue;
////			}
////			checkingState = worldIn.getBlockState(pos.offset(dir));
////			if (checkingState.getBlock() instanceof BlockAbstractGenerator) {
////				if (checkingState.get(BlockAbstractGenerator.FACING) == dir.getOpposite()) {
////					tileEntityCable.positiveConnection = 1;
////				}
////				else if (checkingState.get(BlockAbstractGenerator.FACING) == dir) {
////					tileEntityCable.negativeConnection = 1;
////				}
////			}
////			else if (checkingState.getBlock() == this) {
////				TileEntityCable checkingTile = (TileEntityCable) worldIn.getTileEntity(pos.offset(dir));
////				if (checkingTile.positiveConnection != 0) {
////					if (tileEntityCable.positiveConnection == 0) {
////						tileEntityCable.positiveConnection = checkingTile.positiveConnection + 1;
////					}
////					else {
////						tileEntityCable.positiveConnection = Math.min(tileEntityCable.positiveConnection, checkingTile.positiveConnection + 1);
////					}
////				}
////				if (checkingTile.negativeConnection != 0) {
////					if (tileEntityCable.negativeConnection == 0) {
////						tileEntityCable.negativeConnection = checkingTile.negativeConnection + 1;
////					}
////					else {
////						tileEntityCable.negativeConnection = Math.min(tileEntityCable.negativeConnection, checkingTile.negativeConnection + 1);
////					}	
////				}
////			}
////		}
////	}
//}
