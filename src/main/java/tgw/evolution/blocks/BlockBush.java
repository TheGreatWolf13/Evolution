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

public class BlockBush extends BlockPhysics implements IReplaceable, IPoppable {

    protected BlockBush(Properties builder) {
        super(builder);
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
        Block blockBelow = level.getBlockState(pos.below()).getBlock();
        return blockBelow instanceof BlockGrass || blockBelow instanceof BlockDirt || blockBelow instanceof BlockDryGrass;
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