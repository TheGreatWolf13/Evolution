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
import net.minecraft.util.*;
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
        super(Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3_600_000.0F).sound(SoundType.METAL).noDrops());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    public static boolean puzzlesMatches(Template.BlockInfo puzzle, Template.BlockInfo other) {
        return puzzle.state.getValue(FACING) == other.state.getValue(FACING).getOpposite() &&
               puzzle.nbt.getString("AttachementType").equals(other.nbt.getString("AttachementType"));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEPuzzle();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TEPuzzle && player.canUseGameMasterBlocks()) {
            if (world.isClientSide) {
                ScreenPuzzle.open((TEPuzzle) tile);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
