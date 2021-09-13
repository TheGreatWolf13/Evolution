package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionHitBoxes;

import javax.annotation.Nullable;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;
import static tgw.evolution.init.EvolutionBStates.LIT;

public class BlockWallTorch extends BlockTorch {

    public BlockWallTorch() {
        this.registerDefaultState(this.defaultBlockState().setValue(DIRECTION_HORIZONTAL, Direction.NORTH).setValue(LIT, true));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (!state.getValue(LIT)) {
            return;
        }
        double dx = pos.getX() + 0.5;
        double dy = pos.getY() + 0.7;
        double dz = pos.getZ() + 0.5;
        Direction direction = state.getValue(DIRECTION_HORIZONTAL).getOpposite();
        world.addParticle(ParticleTypes.SMOKE, dx + 0.27 * direction.getStepX(), dy + 0.22, dz + 0.27 * direction.getStepZ(), 0, 0, 0);
        world.addParticle(ParticleTypes.FLAME, dx + 0.27 * direction.getStepX(), dy + 0.22, dz + 0.27 * direction.getStepZ(), 0, 0, 0);
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        Direction direction = state.getValue(DIRECTION_HORIZONTAL);
        return BlockUtils.hasSolidSide(world, pos.relative(direction.getOpposite()), direction);
    }

    @Override
    public void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(DIRECTION_HORIZONTAL)) {
            case WEST: {
                return EvolutionHitBoxes.TORCH_WEST;
            }
            case SOUTH: {
                return EvolutionHitBoxes.TORCH_SOUTH;
            }
            case NORTH: {
                return EvolutionHitBoxes.TORCH_NORTH;
            }
        }
        return EvolutionHitBoxes.TORCH_EAST;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean lit = world.getFluidState(pos).isEmpty();
        BlockState state = this.defaultBlockState();
        Direction[] directions = context.getNearestLookingDirections();
        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                state = state.setValue(DIRECTION_HORIZONTAL, direction.getOpposite());
                if (state.canSurvive(world, pos)) {
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
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        return facing.getOpposite() == state.getValue(DIRECTION_HORIZONTAL) && !state.canSurvive(world, currentPos) ?
               Blocks.AIR.defaultBlockState() :
               state;
    }
}
