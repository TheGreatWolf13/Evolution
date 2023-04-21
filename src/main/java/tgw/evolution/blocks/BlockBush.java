package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraftforge.common.IPlantable;

public class BlockBush extends BlockPhysics implements IPlantable, IReplaceable, IPoppable {

    protected BlockBush(Properties builder) {
        super(builder);
    }

    /**
     * Returns whether the blockstate can sustain the bush.
     */
    public static boolean isValidGround(BlockState state) {
        Block block = state.getBlock(); //TODO proper farmlad
        return block instanceof BlockGrass || block instanceof BlockDirt || block instanceof BlockDryGrass;
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockpos = pos.below();
        if (state.getBlock() == this) {
            return BlockUtils.canSustainSapling(level.getBlockState(blockpos), this);
        }
        return isValidGround(level.getBlockState(blockpos));
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        return 0;
    }

    @Override
    public BlockState getPlant(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() != this) {
            return this.defaultBlockState();
        }
        return state;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return type == PathComputationType.AIR && !this.hasCollision || super.isPathfindable(state, level, pos, type);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction direction,
                                  BlockState fromState,
                                  LevelAccessor level,
                                  BlockPos pos,
                                  BlockPos fromPos) {
        return !state.canSurvive(level, pos) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape(state, direction, fromState, level, pos, fromPos);
    }
}