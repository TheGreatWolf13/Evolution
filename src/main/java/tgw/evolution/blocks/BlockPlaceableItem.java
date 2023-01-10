package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Range;
import tgw.evolution.init.EvolutionShapes;

public class BlockPlaceableItem extends BlockPhysics implements IReplaceable, IPoppable, IAir {

    public BlockPlaceableItem(Properties properties) {
        super(properties.dynamicShape());
    }

    @Override
    public boolean allowsFrom(BlockState state, Direction from) {
        return true;
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
        return BlockUtils.hasSolidSide(level, pos.below(), Direction.UP);
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        return 0;
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XZ;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return EvolutionShapes.GROUND_ITEM.move(offset.x, offset.y, offset.z);
    }

    @Override
    public @Range(from = 1, to = 31) int increment(BlockState state, Direction from) {
        return 1;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive(level, pos)) {
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
        }
    }
}
