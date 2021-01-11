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
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionHitBoxes;

import javax.annotation.Nullable;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;
import static tgw.evolution.init.EvolutionBStates.LIT;

public class BlockWallTorch extends BlockTorch {

    public BlockWallTorch() {
        this.setDefaultState(this.getDefaultState().with(DIRECTION_HORIZONTAL, Direction.NORTH).with(LIT, true));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (!state.get(LIT)) {
            return;
        }
        double dx = pos.getX() + 0.5;
        double dy = pos.getY() + 0.7;
        double dz = pos.getZ() + 0.5;
        Direction direction = state.get(DIRECTION_HORIZONTAL).getOpposite();
        world.addParticle(ParticleTypes.SMOKE, dx + 0.27 * direction.getXOffset(), dy + 0.22, dz + 0.27 * direction.getZOffset(), 0, 0, 0);
        world.addParticle(ParticleTypes.FLAME, dx + 0.27 * direction.getXOffset(), dy + 0.22, dz + 0.27 * direction.getZOffset(), 0, 0, 0);
    }

    @Override
    public void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION_HORIZONTAL);
        super.fillStateContainer(builder);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(DIRECTION_HORIZONTAL)) {
            case WEST:
                return EvolutionHitBoxes.TORCH_WEST;
            case SOUTH:
                return EvolutionHitBoxes.TORCH_SOUTH;
            case NORTH:
                return EvolutionHitBoxes.TORCH_NORTH;
        }
        return EvolutionHitBoxes.TORCH_EAST;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        boolean lit = true;
        if (!world.getFluidState(pos).isEmpty()) {
            lit = false;
        }
        BlockState state = this.getDefaultState();
        Direction[] directions = context.getNearestLookingDirections();
        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                state = state.with(DIRECTION_HORIZONTAL, direction.getOpposite());
                if (state.isValidPosition(world, pos)) {
                    if (!lit) {
                        return state.with(LIT, false);
                    }
                    boolean oxygen = ChunkStorageCapability.contains(context.getWorld().getChunkAt(context.getPos()), EnumStorage.OXYGEN, 1);
                    return state.with(LIT, oxygen);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        Direction direction = state.get(DIRECTION_HORIZONTAL);
        BlockPos blockpos = pos.offset(direction.getOpposite());
        BlockState blockstate = world.getBlockState(blockpos);
        return blockstate.func_224755_d(world, blockpos, direction);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.with(DIRECTION_HORIZONTAL, mirror.mirror(state.get(DIRECTION_HORIZONTAL)));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(DIRECTION_HORIZONTAL, rot.rotate(state.get(DIRECTION_HORIZONTAL)));
    }

    @Override
    public BlockState updatePostPlacement(BlockState state,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld world,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        return facing.getOpposite() == state.get(DIRECTION_HORIZONTAL) && !state.isValidPosition(world, currentPos) ?
               Blocks.AIR.getDefaultState() :
               state;
    }
}
