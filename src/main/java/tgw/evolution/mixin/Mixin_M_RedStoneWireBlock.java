package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.MathHelper;

import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(RedStoneWireBlock.class)
public abstract class Mixin_M_RedStoneWireBlock extends Block {

    @Shadow @Final public static IntegerProperty POWER;
    @Shadow @Final public static Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION;
    @Shadow @Final private static Map<BlockState, VoxelShape> SHAPES_CACHE;
    @Shadow @Final private static Vec3[] COLORS;
    @Shadow @Final private BlockState crossState;
    @Shadow private boolean shouldSignal;

    public Mixin_M_RedStoneWireBlock(Properties properties) {
        super(properties);
    }

    @Contract(value = "_ -> _")
    @Shadow
    private static boolean isCross(BlockState blockState) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Contract(value = "_ -> _")
    @Shadow
    private static boolean isDot(BlockState blockState) {
        //noinspection Contract
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
        int power = state.getValue(POWER);
        if (power != 0) {
            BlockPos pos = new BlockPos(x, y, z);
            for (Direction dir : DirectionUtil.HORIZ_NESW) {
                RedstoneSide side = state.getValue(PROPERTY_BY_DIRECTION.get(dir));
                switch (side) {
                    case UP: {
                        this.spawnParticlesAlongLine(level, MathHelper.RANDOM, pos, COLORS[power], dir, Direction.UP, -0.5F, 0.5F);
                        //Falls through
                    }
                    case SIDE: {
                        this.spawnParticlesAlongLine(level, MathHelper.RANDOM, pos, COLORS[power], Direction.DOWN, dir, 0.0F, 0.5F);
                        break;
                    }
                    case NONE: {
                        this.spawnParticlesAlongLine(level, MathHelper.RANDOM, pos, COLORS[power], Direction.DOWN, dir, 0.0F, 0.3F);
                        break;
                    }
                }
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
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Shadow
    protected abstract boolean canSurviveOn(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState);

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        BlockState stateBelow = level.getBlockState_(x, y - 1, z);
        return this.canSurviveOn(level, new BlockPos(x, y - 1, z), stateBelow);
    }

    @Shadow
    protected abstract RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction);

    @Shadow
    protected abstract BlockState getConnectionState(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos);

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
        return SHAPES_CACHE.get(state.setValue(POWER, 0));
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
        if (this.shouldSignal && dir != Direction.DOWN) {
            int power = state.getValue(POWER);
            if (power == 0) {
                return 0;
            }
            return dir != Direction.UP && !this.getConnectionState(level, state, new BlockPos(x, y, z)).getValue(PROPERTY_BY_DIRECTION.get(dir.getOpposite())).isConnected() ? 0 : power;
        }
        return 0;
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
    public void neighborChanged_(BlockState state, Level level, int x, int y, int z, Block oldBlock, int fromX, int fromY, int fromZ, boolean isMoving) {
        if (!level.isClientSide) {
            if (state.canSurvive_(level, x, y, z)) {
                this.updatePowerStrength(level, new BlockPos(x, y, z), state);
            }
            else {
                dropResources(state, level, new BlockPos(x, y, z));
                level.removeBlock_(x, y, z, false);
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
        if (!oldState.is(state.getBlock()) && !level.isClientSide) {
            BlockPos pos = new BlockPos(x, y, z);
            this.updatePowerStrength(level, pos, state);
            for (Direction direction : Direction.Plane.VERTICAL) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }
            this.updateNeighborsOfNeighboringWires(level, pos);
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
        if (!isMoving && !state.is(newState.getBlock())) {
            super.onRemove_(state, level, x, y, z, newState, false);
            if (!level.isClientSide) {
                for (Direction dir : DirectionUtil.ALL) {
                    int offX = x + dir.getStepX();
                    int offY = y + dir.getStepY();
                    int offZ = z + dir.getStepZ();
                    level.updateNeighborsAt_(offX, offY, offZ, this);
                }
                BlockPos pos = new BlockPos(x, y, z);
                this.updatePowerStrength(level, pos, state);
                this.updateNeighborsOfNeighboringWires(level, pos);
            }
        }
    }

    @Shadow
    protected abstract void spawnParticlesAlongLine(Level level, Random random, BlockPos blockPos, Vec3 vec3, Direction direction, Direction direction2, float f, float g);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
        throw new AbstractMethodError();
    }

    @Override
    public void updateIndirectNeighbourShapes_(BlockState state, LevelAccessor level, int x, int y, int z, @BlockFlags int flags, int limit) {
        for (Direction dir : DirectionUtil.HORIZ_NESW) {
            if (state.getValue(PROPERTY_BY_DIRECTION.get(dir)) != RedstoneSide.NONE) {
                int offX = x + dir.getStepX();
                int offZ = z + dir.getStepZ();
                if (!level.getBlockState_(offX, y, offZ).is(this)) {
                    BlockState stateAtOffAtDown = level.getBlockState_(offX, y - 1, offZ);
                    if (!stateAtOffAtDown.is(Blocks.OBSERVER)) {
                        BlockState updatedState = stateAtOffAtDown.updateShape_(dir.getOpposite(), level.getBlockState_(x, y - 1, z), level,
                                                                                offX, y - 1, offZ,
                                                                                x, y - 1, z);
                        BlockUtils.updateOrDestroy(stateAtOffAtDown, updatedState, level, offX, y - 1, offZ, flags, limit);
                    }
                    BlockState stateAtOffAtUp = level.getBlockState_(offX, y + 1, offZ);
                    if (!stateAtOffAtUp.is(Blocks.OBSERVER)) {
                        BlockState updatedState = stateAtOffAtUp.updateShape_(dir.getOpposite(), level.getBlockState_(x, y + 1, z), level,
                                                                              offX, y + 1, offZ,
                                                                              x, y + 1, z);
                        BlockUtils.updateOrDestroy(stateAtOffAtUp, updatedState, level, offX, y + 1, offZ, flags, limit);
                    }
                }
            }
        }
    }

    @Shadow
    protected abstract void updateNeighborsOfNeighboringWires(Level level, BlockPos blockPos);

    @Shadow
    protected abstract void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState);

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
        if (from == Direction.DOWN) {
            return state;
        }
        if (from == Direction.UP) {
            return this.getConnectionState(level, state, new BlockPos(x, y, z));
        }
        BlockPos pos = new BlockPos(x, y, z);
        RedstoneSide redstoneSide = this.getConnectingSide(level, pos, from);
        return redstoneSide.isConnected() == state.getValue(PROPERTY_BY_DIRECTION.get(from)).isConnected() && !isCross(state) ?
               state.setValue(PROPERTY_BY_DIRECTION.get(from), redstoneSide) :
               this.getConnectionState(level,
                                       this.crossState.setValue(POWER, state.getValue(POWER)).setValue(PROPERTY_BY_DIRECTION.get(from), redstoneSide),
                                       pos);
    }

    @Shadow
    protected abstract void updatesOnShapeChange(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState,
                                 Level level,
                                 BlockPos blockPos,
                                 Player player,
                                 InteractionHand interactionHand,
                                 BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        if (isCross(state) || isDot(state)) {
            BlockState newState = isCross(state) ? this.defaultBlockState() : this.crossState;
            newState = newState.setValue(POWER, state.getValue(POWER));
            BlockPos pos = new BlockPos(x, y, z);
            newState = this.getConnectionState(level, newState, pos);
            if (newState != state) {
                level.setBlock(pos, newState, 3);
                this.updatesOnShapeChange(level, pos, state, newState);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
