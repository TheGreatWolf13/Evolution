package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import tgw.evolution.blocks.tileentities.TEPuzzle;
import tgw.evolution.client.gui.ScreenPuzzle;

import javax.annotation.Nullable;

public class BlockPuzzle extends DirectionalBlock implements EntityBlock, GameMasterBlock {

    public BlockPuzzle() {
        super(Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3_600_000.0F).sound(SoundType.METAL).noDrops());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    public static boolean canAttach(StructureTemplate.StructureBlockInfo puzzle, StructureTemplate.StructureBlockInfo other) {
        return puzzle.state.getValue(FACING) == other.state.getValue(FACING).getOpposite() &&
               puzzle.nbt.getString("AttachementType").equals(other.nbt.getString("AttachementType"));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    @Deprecated
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEPuzzle(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TEPuzzle tePuzzle && player.canUseGameMasterBlocks()) {
            if (level.isClientSide) {
                ScreenPuzzle.open(tePuzzle);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
