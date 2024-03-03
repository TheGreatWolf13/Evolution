package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(BaseRailBlock.class)
public abstract class Mixin_M_BaseRailBlock extends Block implements SimpleWaterloggedBlock {

    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final protected static VoxelShape HALF_BLOCK_AABB;
    @Shadow @Final protected static VoxelShape FLAT_AABB;
    @Shadow @Final private boolean isStraight;

    public Mixin_M_BaseRailBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static boolean shouldBeRemoved(BlockPos blockPos, Level level, RailShape railShape) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return canSupportRigidBlock(level, new BlockPos(x, y - 1, z));
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Shadow
    public abstract Property<RailShape> getShapeProperty();

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        RailShape railShape = state.is(this) ? state.getValue(this.getShapeProperty()) : null;
        return railShape != null && railShape.isAscending() ? HALF_BLOCK_AABB : FLAT_AABB;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        throw new AbstractMethodError();
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
        if (!level.isClientSide && level.getBlockState_(x, y, z).is(this)) {
            RailShape railShape = state.getValue(this.getShapeProperty());
            BlockPos pos = new BlockPos(x, y, z);
            if (shouldBeRemoved(pos, level, railShape)) {
                dropResources(state, level, pos);
                level.removeBlock(pos, isMoving);
            }
            else {
                this.updateState(state, level, pos, oldBlock);
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            this.updateState(state, level, new BlockPos(x, y, z), isMoving);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!isMoving) {
            super.onRemove_(state, level, x, y, z, newState, false);
            if (state.getValue(this.getShapeProperty()).isAscending()) {
                level.updateNeighborsAt_(x, y + 1, z, this);
            }
            if (this.isStraight) {
                level.updateNeighborsAt_(x, y, z, this);
                level.updateNeighborsAt_(x, y - 1, z, this);
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        throw new AbstractMethodError();
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
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(new BlockPos(x, y, z), Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

    @Shadow
    protected abstract void updateState(BlockState blockState, Level level, BlockPos blockPos, Block block);

    @Shadow
    protected abstract BlockState updateState(BlockState blockState, Level level, BlockPos blockPos, boolean bl);
}
