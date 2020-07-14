package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import tgw.evolution.blocks.tileentities.TEFeces;

public class BlockFeces extends Block implements IReplaceable {

    public BlockFeces() {
        super(Block.Properties.create(Material.EARTH).hardnessAndResistance(0.4F, 0.1F).sound(SoundType.SLIME));
    }

    @Override
    public boolean canBeReplacedByLiquid(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEFeces();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    @Override
    public ItemStack getDrops(BlockState state) {
        return new ItemStack(this);
    }
}
