package tgw.evolution.blocks;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionShapes;

import java.util.random.RandomGenerator;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;
import static tgw.evolution.init.EvolutionBStates.LIT;

public class BlockTorchWall extends BlockTorch {

    public BlockTorchWall() {
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, Direction.NORTH).setValue(LIT, true));
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double dx = x + 0.5;
        double dy = y + 0.7;
        double dz = z + 0.5;
        Direction direction = state.getValue(DIRECTION_HORIZONTAL).getOpposite();
        level.addParticle(ParticleTypes.SMOKE, dx + 0.27 * direction.getStepX(), dy + 0.22, dz + 0.27 * direction.getStepZ(), 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, dx + 0.27 * direction.getStepX(), dy + 0.22, dz + 0.27 * direction.getStepZ(), 0, 0, 0);
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader world, int x, int y, int z) {
        Direction direction = state.getValue(DIRECTION_HORIZONTAL);
        Direction opposite = direction.getOpposite();
        return BlockUtils.hasSolidFace(world, x + opposite.getStepX(), y, z + opposite.getStepZ(), direction);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case WEST -> EvolutionShapes.TORCH_WEST;
            case SOUTH -> EvolutionShapes.TORCH_SOUTH;
            case NORTH -> EvolutionShapes.TORCH_NORTH;
            default -> EvolutionShapes.TORCH_EAST;
        };
    }

    @Override
    public @Nullable BlockState getStateForPlacement_(Level level,
                                                      int x,
                                                      int y,
                                                      int z,
                                                      Player player,
                                                      InteractionHand hand,
                                                      BlockHitResult hitResult) {
        boolean lit = level.getFluidState_(x, y, z).isEmpty();
        BlockState state = this.defaultBlockState();
        state = state.setValue(DIRECTION_HORIZONTAL, hitResult.getDirection());
        if (state.canSurvive_(level, x, y, z)) {
            return state.setValue(LIT, lit);
        }
        return null;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(DIRECTION_HORIZONTAL, mirror.mirror(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(DIRECTION_HORIZONTAL, rot.rotate(state.getValue(DIRECTION_HORIZONTAL)));
    }

    @Override
    public BlockState updateShape_(BlockState state,
                                   Direction from,
                                   BlockState fromState,
                                   LevelAccessor level,
                                   int x,
                                   int y,
                                   int z,
                                   int fromX,
                                   int fromY,
                                   int fromZ) {
        return from.getOpposite() == state.getValue(DIRECTION_HORIZONTAL) && !state.canSurvive_(level, x, y, z) ?
               Blocks.AIR.defaultBlockState() :
               state;
    }
}
