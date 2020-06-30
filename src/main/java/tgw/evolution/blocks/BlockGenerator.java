//package tgw.evolution.blocks;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//
//public class BlockGenerator extends BlockAbstractGenerator {
//
//	public BlockGenerator(Properties properties, int density) {
//		super(properties, density);
//	}
//
//	@Override
//	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
//		updateWeight(worldIn, pos);
//	}
//	
//	@Override
//	public float electromotiveForce() {
//		return 10;
//	}
//}
