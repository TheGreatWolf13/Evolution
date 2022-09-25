package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionShapes;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;
import static tgw.evolution.init.EvolutionBStates.LIT;

public class BlockWallTorch extends BlockTorch {

    public BlockWallTorch() {
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, Direction.NORTH).setValue(LIT, true));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        if (!state.getValue(LIT)) {
            return;
        }
        double dx = pos.getX() + 0.5;
        double dy = pos.getY() + 0.7;
        double dz = pos.getZ() + 0.5;
        Direction direction = state.getValue(DIRECTION_HORIZONTAL).getOpposite();
        level.addParticle(ParticleTypes.SMOKE, dx + 0.27 * direction.getStepX(), dy + 0.22, dz + 0.27 * direction.getStepZ(), 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, dx + 0.27 * direction.getStepX(), dy + 0.22, dz + 0.27 * direction.getStepZ(), 0, 0, 0);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = state.getValue(DIRECTION_HORIZONTAL);
        return BlockUtils.hasSolidSide(world, pos.relative(direction.getOpposite()), direction);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case WEST -> EvolutionShapes.TORCH_WEST;
            case SOUTH -> EvolutionShapes.TORCH_SOUTH;
            case NORTH -> EvolutionShapes.TORCH_NORTH;
            default -> EvolutionShapes.TORCH_EAST;
        };
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean lit = level.getFluidState(pos).isEmpty();
        BlockState state = this.defaultBlockState();
        Direction[] directions = context.getNearestLookingDirections();
        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                state = state.setValue(DIRECTION_HORIZONTAL, direction.getOpposite());
                if (state.canSurvive(level, pos)) {
                    if (!lit) {
                        return state.setValue(LIT, false);
                    }
                    boolean oxygen = CapabilityChunkStorage.contains(context.getLevel().getChunkAt(context.getClickedPos()), EnumStorage.OXYGEN, 1);
                    return state.setValue(LIT, oxygen);
                }
            }
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
    public BlockState updateShape(BlockState state,
                                  Direction facing,
                                  BlockState facingState,
                                  LevelAccessor level,
                                  BlockPos currentPos,
                                  BlockPos facingPos) {
        return facing.getOpposite() == state.getValue(DIRECTION_HORIZONTAL) && !state.canSurvive(level, currentPos) ?
               Blocks.AIR.defaultBlockState() :
               state;
    }
}
