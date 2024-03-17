package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Random;

@Mixin(ObserverBlock.class)
public abstract class Mixin_M_ObserverBlock extends DirectionalBlock {

    @Shadow @Final public static BooleanProperty POWERED;

    public Mixin_M_ObserverBlock(Properties properties) {
        super(properties);
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
        return state.getValue(POWERED) && state.getValue(FACING) == dir ? 15 : 0;
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
        if (!state.is(oldState.getBlock())) {
            if (!level.isClientSide() && state.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(new BlockPos(x, y, z), this)) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockState unpoweredState = state.setValue(POWERED, false);
                level.setBlock(pos, unpoweredState, BlockFlags.UPDATE_NEIGHBORS | BlockFlags.BLOCK_UPDATE);
                this.updateNeighborsInFront(level, pos, unpoweredState);
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
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && state.getValue(POWERED)) {
                BlockPos pos = new BlockPos(x, y, z);
                if (level.getBlockTicks().hasScheduledTick(pos, this)) {
                    this.updateNeighborsInFront(level, pos, state.setValue(POWERED, false));
                }
            }
        }
    }

    @Shadow
    protected abstract void startSignal(LevelAccessor levelAccessor, BlockPos blockPos);

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
        if (state.getValue(POWERED)) {
            level.setBlock_(x, y, z, state.setValue(POWERED, false), BlockFlags.BLOCK_UPDATE);
        }
        else {
            level.setBlock_(x, y, z, state.setValue(POWERED, true), BlockFlags.BLOCK_UPDATE);
            level.scheduleTick(pos, this, 2);
        }
        this.updateNeighborsInFront(level, pos, state);
    }

    @Shadow
    protected abstract void updateNeighborsInFront(Level level, BlockPos blockPos, BlockState blockState);

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
        if (state.getValue(FACING) == from && !state.getValue(POWERED)) {
            this.startSignal(level, new BlockPos(x, y, z));
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
