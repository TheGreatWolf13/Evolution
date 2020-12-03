package tgw.evolution.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.DirectionProperty;
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

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public class BlockWallTorch extends BlockTorch {

    public static final DirectionProperty HORIZONTAL_FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH,
                                                                                            EvolutionHitBoxes.TORCH_NORTH,
                                                                                            Direction.SOUTH,
                                                                                            EvolutionHitBoxes.TORCH_SOUTH,
                                                                                            Direction.WEST,
                                                                                            EvolutionHitBoxes.TORCH_WEST,
                                                                                            Direction.EAST,
                                                                                            EvolutionHitBoxes.TORCH_EAST));

    public BlockWallTorch() {
        this.setDefaultState(this.getDefaultState().with(HORIZONTAL_FACING, Direction.NORTH).with(LIT, true));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (!stateIn.get(LIT)) {
            return;
        }
        double dx = pos.getX() + 0.5;
        double dy = pos.getY() + 0.7;
        double dz = pos.getZ() + 0.5;
        Direction direction = stateIn.get(HORIZONTAL_FACING).getOpposite();
        worldIn.addParticle(ParticleTypes.SMOKE, dx + 0.27 * direction.getXOffset(), dy + 0.22, dz + 0.27 * direction.getZOffset(), 0, 0, 0);
        worldIn.addParticle(ParticleTypes.FLAME, dx + 0.27 * direction.getXOffset(), dy + 0.22, dz + 0.27 * direction.getZOffset(), 0, 0, 0);
    }

    @Override
    public void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
        super.fillStateContainer(builder);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.get(HORIZONTAL_FACING));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = this.getDefaultState();
        IWorldReader worldIn = context.getWorld();
        BlockPos pos = context.getPos();
        Direction[] directions = context.getNearestLookingDirections();
        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                state = state.with(HORIZONTAL_FACING, direction.getOpposite());
                if (state.isValidPosition(worldIn, pos)) {
                    return state;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        Direction direction = state.get(HORIZONTAL_FACING);
        BlockPos blockpos = pos.offset(direction.getOpposite());
        BlockState blockstate = world.getBlockState(blockpos);
        return blockstate.func_224755_d(world, blockpos, direction);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(HORIZONTAL_FACING)));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(HORIZONTAL_FACING, rot.rotate(state.get(HORIZONTAL_FACING)));
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld worldIn,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        return facing.getOpposite() == stateIn.get(HORIZONTAL_FACING) && !stateIn.isValidPosition(worldIn, currentPos) ?
               Blocks.AIR.getDefaultState() :
               stateIn;
    }
}
