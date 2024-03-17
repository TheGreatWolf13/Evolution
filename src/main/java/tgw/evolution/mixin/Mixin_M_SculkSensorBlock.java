package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(SculkSensorBlock.class)
public abstract class Mixin_M_SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    @Shadow @Final public static IntegerProperty POWER;
    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final public static EnumProperty<SculkSensorPhase> PHASE;
    @Shadow @Final protected static VoxelShape SHAPE;

    public Mixin_M_SculkSensorBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public static void deactivate(Level level, BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static SculkSensorPhase getPhase(BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static void updateNeighbours(Level level, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (getPhase(state) == SculkSensorPhase.ACTIVE) {
            Direction dir = DirectionUtil.getRandom(random);
            if (dir != Direction.UP && dir != Direction.DOWN) {
                int stepX = dir.getStepX();
                int stepZ = dir.getStepZ();
                level.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE,
                                  x + 0.5 + (stepX == 0 ? 0.5 - random.nextDouble() : stepX * 0.6), y + 0.25, z + 0.5 + (stepZ == 0 ? 0.5 - random.nextDouble() : stepZ * 0.6),
                                  0, random.nextFloat() * 0.04, 0
                );
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
        return state.getValue(POWER);
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
        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            BlockPos pos = new BlockPos(x, y, z);
            if (state.getValue(POWER) > 0 && !level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.setBlock(pos, state.setValue(POWER, 0), BlockFlags.UPDATE_NEIGHBORS | BlockFlags.BLOCK_UPDATE);
            }
            level.scheduleTick(new BlockPos(pos), state.getBlock(), 1);
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
            if (getPhase(state) == SculkSensorPhase.ACTIVE) {
                updateNeighbours(level, new BlockPos(x, y, z));
            }
            super.onRemove_(state, level, x, y, z, newState, isMoving);
        }
    }

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
        if (getPhase(state) != SculkSensorPhase.ACTIVE) {
            if (getPhase(state) == SculkSensorPhase.COOLDOWN) {
                level.setBlockAndUpdate_(x, y, z, state.setValue(PHASE, SculkSensorPhase.INACTIVE));
            }
        }
        else {
            deactivate(level, new BlockPos(x, y, z), state);
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
}
