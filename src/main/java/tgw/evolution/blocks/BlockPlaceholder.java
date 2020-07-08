package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import tgw.evolution.Evolution;

import javax.annotation.Nullable;

public class BlockPlaceholder extends BlockMass {

    public BlockPlaceholder() {
        super(Properties.create(Material.IRON).hardnessAndResistance(2.0f, 3.0f).sound(SoundType.METAL), 1000000);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (worldIn.isRemote) {
            return;
        }
        VoxelShape fullBlock = VoxelShapes.fullCube();
        VoxelShape outside = Block.makeCuboidShape(18, 18, 18, 20, 20, 20);
        Evolution.LOGGER.debug("compare fullblock and outside ONLY_SECOND = {}", VoxelShapes.compare(fullBlock, outside, IBooleanFunction.ONLY_SECOND));
        Evolution.LOGGER.debug("compare fullblock and outside ONLY_FIRST = {}", VoxelShapes.compare(fullBlock, outside, IBooleanFunction.ONLY_FIRST));
        Evolution.LOGGER.debug("compare fullblock and outside OR = {}", VoxelShapes.compare(fullBlock, outside, IBooleanFunction.OR));
        Evolution.LOGGER.debug("compare fullblock and outside AND = {}", VoxelShapes.compare(fullBlock, outside, IBooleanFunction.AND));
        Evolution.LOGGER.debug("compare outside and fullblock AND = {}", VoxelShapes.compare(outside, fullBlock, IBooleanFunction.AND));
        Evolution.LOGGER.debug("compare fullblock and outside NOT_SAME = {}", VoxelShapes.compare(fullBlock, outside, IBooleanFunction.NOT_SAME));
        VoxelShape border = Block.makeCuboidShape(6, 14, 6, 10, 18, 10);
        Evolution.LOGGER.debug("compare fullblock and border ONLY_SECOND = {}", VoxelShapes.compare(fullBlock, border, IBooleanFunction.ONLY_SECOND));
        Evolution.LOGGER.debug("compare fullblock and border ONLY_FIRST = {}", VoxelShapes.compare(fullBlock, border, IBooleanFunction.ONLY_FIRST));
        Evolution.LOGGER.debug("compare fullblock and border OR = {}", VoxelShapes.compare(fullBlock, border, IBooleanFunction.OR));
        Evolution.LOGGER.debug("compare fullblock and border AND = {}", VoxelShapes.compare(fullBlock, border, IBooleanFunction.AND));
        Evolution.LOGGER.debug("compare fullblock and border NOT_SAME = {}", VoxelShapes.compare(fullBlock, border, IBooleanFunction.NOT_SAME));
        VoxelShape inside = Block.makeCuboidShape(6, 6, 6, 10, 10, 10);
        Evolution.LOGGER.debug("compare fullblock and inside ONLY_SECOND = {}", VoxelShapes.compare(fullBlock, inside, IBooleanFunction.ONLY_SECOND));
        Evolution.LOGGER.debug("compare fullblock and inside ONLY_FIRST = {}", VoxelShapes.compare(fullBlock, inside, IBooleanFunction.ONLY_FIRST));
        Evolution.LOGGER.debug("compare fullblock and inside OR = {}", VoxelShapes.compare(fullBlock, inside, IBooleanFunction.OR));
        Evolution.LOGGER.debug("compare fullblock and inside AND = {}", VoxelShapes.compare(fullBlock, inside, IBooleanFunction.AND));
        Evolution.LOGGER.debug("compare fullblock and inside NOT_SAME = {}", VoxelShapes.compare(fullBlock, inside, IBooleanFunction.NOT_SAME));
    }
}
