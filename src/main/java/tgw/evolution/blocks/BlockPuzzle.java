package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.tileentities.TEPuzzle;
import tgw.evolution.client.gui.ScreenPuzzle;
import tgw.evolution.init.EvolutionBStates;

public class BlockPuzzle extends DirectionalBlock implements EntityBlock, GameMasterBlock {

    public BlockPuzzle() {
        super(Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3_600_000.0F).sound(SoundType.METAL).noDrops());
        this.registerDefaultState(this.defaultBlockState().setValue(EvolutionBStates.DIRECTION, Direction.UP));
    }

    public static boolean canAttach(StructureTemplate.StructureBlockInfo puzzle, StructureTemplate.StructureBlockInfo other) {
        return puzzle.state.getValue(EvolutionBStates.DIRECTION) == other.state.getValue(EvolutionBStates.DIRECTION).getOpposite() &&
               puzzle.nbt.getString("AttachmentType").equals(other.nbt.getString("AttachmentType"));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EvolutionBStates.DIRECTION);
    }

    @Override
    public @Nullable BlockState getStateForPlacement_(Level level,
                                                      int x,
                                                      int y,
                                                      int z,
                                                      Player player,
                                                      InteractionHand hand,
                                                      BlockHitResult hitResult) {
        return this.defaultBlockState().setValue(EvolutionBStates.DIRECTION, hitResult.getDirection());
    }

    @Override
    @Deprecated
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(EvolutionBStates.DIRECTION, mirror.mirror(state.getValue(EvolutionBStates.DIRECTION)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEPuzzle(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(EvolutionBStates.DIRECTION, rot.rotate(state.getValue(EvolutionBStates.DIRECTION)));
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.canUseGameMasterBlocks() && level.getBlockEntity_(x, y, z) instanceof TEPuzzle tePuzzle) {
            if (level.isClientSide) {
                ScreenPuzzle.open(tePuzzle);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
