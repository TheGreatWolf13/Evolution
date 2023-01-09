//package tgw.evolution.blocks;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockRenderType;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.SoundType;
//import net.minecraft.block.material.Material;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.Direction;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorldReader;
//import net.minecraft.world.World;
//import tgw.evolution.blocks.tileentities.TEShadowHound;
//
//import org.jetbrains.annotations.Nullable;
//
//public class BlockShadowHound extends BlockPhysics {
//
//    public BlockShadowHound() {
//        super(Block.Properties.create(Material.SHULKER).hardnessAndResistance(2F, 6F).sound(SoundType.SWEET_BERRY_BUSH), 500);
//    }
//
//    @Override
//    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
//        if (!worldIn.isRemote) {
//            if (!state.isValidPosition(worldIn, pos)) {
//                ((TEShadowHound) worldIn.getTileEntity(pos)).spawnShadowHound();
//            }
//        }
//        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
//    }
//
//    @Nullable
//    @Override
//    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
//        return new TEShadowHound();
//    }
//
//    @Override
//    public boolean hasTileEntity(BlockState state) {
//        return true;
//    }
//
//    @Override
//    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
//        BlockState up = worldIn.getBlockState(pos.up());
//        if (!BlockUtils.isReplaceable(up)) {
//            return false;
//        }
//        BlockPos posDown = pos.down();
//        BlockState down = worldIn.getBlockState(posDown);
//        return Block.hasSolidSide(down, worldIn, posDown, Direction.UP);
//    }
//
//    @Override
//    public BlockRenderType getRenderType(BlockState state) {
//        return BlockRenderType.ENTITYBLOCK_ANIMATED;
//    }
//
//    @Override
//    public boolean isSolid(BlockState state) {
//        return false;
//    }
//}
