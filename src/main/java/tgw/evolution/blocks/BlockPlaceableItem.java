package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.blocks.util.BlockUtils;
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
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP);
    }

    @Override
    public double getMass(Level level, int x, int y, int z, BlockState state) {
        return 0;
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XZ;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.moveShapeByOffset(EvolutionShapes.GROUND_ITEM, x, z);
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
    public void neighborChanged_(BlockState state,
                                 Level level,
                                 int x,
                                 int y,
                                 int z,
                                 Block oldBlock,
                                 int fromX,
                                 int fromY,
                                 int fromZ,
                                 boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive_(level, x, y, z)) {
                BlockPos pos = new BlockPos(x, y, z);
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
        }
    }
}
