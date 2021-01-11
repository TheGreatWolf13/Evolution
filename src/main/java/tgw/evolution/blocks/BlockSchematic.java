package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.SchematicMode;
import tgw.evolution.blocks.tileentities.TESchematic;

import javax.annotation.Nullable;

import static tgw.evolution.init.EvolutionBStates.SCHEMATIC_MODE;

public class BlockSchematic extends Block {

    public BlockSchematic() {
        super(Properties.create(Material.IRON, MaterialColor.LIGHT_GRAY).hardnessAndResistance(-1.0F, 3_600_000.0F).sound(SoundType.METAL).noDrops());
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TESchematic();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(SCHEMATIC_MODE);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(SCHEMATIC_MODE, SchematicMode.SAVE);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof TESchematic && ((TESchematic) tileentity).usedBy(player);
    }
}
