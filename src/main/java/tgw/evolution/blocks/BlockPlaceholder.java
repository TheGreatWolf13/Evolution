package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPlaceholder extends BlockMass {

    public BlockPlaceholder() {
        super(Properties.create(Material.IRON).hardnessAndResistance(2.0f, 3.0f).sound(SoundType.METAL), 1000000);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        //        if (worldIn.isRemote) {
        //            return;
        //        }
        //        VoxelShape fullBlock = VoxelShapes.fullCube();
        //        VoxelShape insideMiddle = Block.makeCuboidShape(7, 7, 7, 9, 9, 9);
        //        Evolution.LOGGER.debug("compare fullblock and insideMiddle ONLY_SECOND = {}", VoxelShapes.compare(fullBlock, insideMiddle, IBooleanFunction.ONLY_SECOND));
        //        Evolution.LOGGER.debug("compare fullblock and insideMiddle ONLY_FIRST = {}", VoxelShapes.compare(fullBlock, insideMiddle, IBooleanFunction.ONLY_FIRST));
        //        VoxelShape outside = Block.makeCuboidShape(18, 18, 18, 20, 20, 20);
        //        Evolution.LOGGER.debug("compare fullblock and outside ONLY_SECOND = {}", VoxelShapes.compare(fullBlock, outside, IBooleanFunction.ONLY_SECOND));
        //        Evolution.LOGGER.debug("compare fullblock and outside ONLY_FIRST = {}", VoxelShapes.compare(fullBlock, outside, IBooleanFunction.ONLY_FIRST));
        //        VoxelShape border = Block.makeCuboidShape(6, 14, 6, 10, 18, 10);
        //        Evolution.LOGGER.debug("compare fullblock and border ONLY_SECOND = {}", VoxelShapes.compare(fullBlock, border, IBooleanFunction.ONLY_SECOND));
        //        Evolution.LOGGER.debug("compare fullblock and border ONLY_FIRST = {}", VoxelShapes.compare(fullBlock, border, IBooleanFunction.ONLY_FIRST));
    }
}
