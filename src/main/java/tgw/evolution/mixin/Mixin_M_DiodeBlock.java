package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

@Mixin(DiodeBlock.class)
public abstract class Mixin_M_DiodeBlock extends HorizontalDirectionalBlock {

    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final protected static VoxelShape SHAPE;
    public Mixin_M_DiodeBlock(Properties properties) {
        super(properties);
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
        return BlockUtils.canSupportRigidBlock(level, x, y - 1, z);
    }

    @Shadow
    protected abstract void checkTickOnNeighbor(Level level, BlockPos blockPos, BlockState blockState);

    @Shadow
    protected abstract int getDelay(BlockState blockState);

    @Shadow
    protected abstract int getOutputSignal(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState);

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

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        throw new AbstractMethodError();
    }

    @Override
    public int getSignal_(BlockState state, BlockGetter level, int x, int y, int z, Direction dir) {
        if (!state.getValue(POWERED)) {
            return 0;
        }
        return state.getValue(FACING) == dir ? this.getOutputSignal(level, new BlockPos(x, y, z), state) : 0;
    }

    @Shadow
    public abstract boolean isLocked(LevelReader levelReader, BlockPos blockPos, BlockState blockState);

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
        if (state.canSurvive_(level, x, y, z)) {
            this.checkTickOnNeighbor(level, new BlockPos(x, y, z), state);
        }
        else {
            BlockEntity tile = state.hasBlockEntity() ? level.getBlockEntity_(x, y, z) : null;
            BlockPos pos = new BlockPos(x, y, z);
            dropResources(state, level, pos, tile);
            level.removeBlock(pos, false);
            for (Direction direction : DirectionUtil.ALL) {
                level.updateNeighborsAt(pos.relative(direction), this);
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
        this.updateNeighborsInFront(level, new BlockPos(x, y, z), state);
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
        if (!isMoving && !state.is(newState.getBlock())) {
            super.onRemove_(state, level, x, y, z, newState, false);
            this.updateNeighborsInFront(level, new BlockPos(x, y, z), state);
        }
    }

    @Shadow
    protected abstract boolean shouldTurnOn(Level level, BlockPos blockPos, BlockState blockState);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!this.isLocked(level, pos, state)) {
            boolean powered = state.getValue(POWERED);
            boolean shouldBePowered = this.shouldTurnOn(level, pos, state);
            if (powered && !shouldBePowered) {
                level.setBlock_(x, y, z, state.setValue(POWERED, false), BlockFlags.BLOCK_UPDATE);
            }
            else if (!powered) {
                level.setBlock_(x, y, z, state.setValue(POWERED, true), BlockFlags.BLOCK_UPDATE);
                if (!shouldBePowered) {
                    level.scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
                }
            }
        }
    }

    @Shadow
    protected abstract void updateNeighborsInFront(Level level, BlockPos blockPos, BlockState blockState);
}
