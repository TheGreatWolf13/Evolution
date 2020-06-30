package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import tgw.evolution.blocks.tileentities.TEPuzzle;
import tgw.evolution.client.gui.ScreenPuzzle;

import javax.annotation.Nullable;

public class BlockPuzzle extends DirectionalBlock {

    public BlockPuzzle() {
        super(Properties.create(Material.IRON, MaterialColor.LIGHT_GRAY).hardnessAndResistance(-1.0F, 3600000.0F).sound(SoundType.METAL).noDrops());
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.UP));
    }

    public static boolean puzzlesMatches(Template.BlockInfo puzzle, Template.BlockInfo other) {
        return puzzle.state.get(FACING) == other.state.get(FACING).getOpposite() && puzzle.nbt.getString("AttachementType").equals(other.nbt.getString("AttachementType"));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    @Deprecated
    public BlockState mirror(BlockState state, net.minecraft.util.Mirror mirrorIn) {
        return state.with(FACING, mirrorIn.mirror(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getFace());
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEPuzzle();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TEPuzzle && player.canUseCommandBlock()) {
            if (worldIn.isRemote) {
                ScreenPuzzle.open((TEPuzzle) tile);
            }
            return true;
        }
        return false;
    }
}
