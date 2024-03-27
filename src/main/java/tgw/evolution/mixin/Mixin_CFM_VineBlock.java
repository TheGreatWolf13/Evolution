package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Map;
import java.util.Random;

@Mixin(VineBlock.class)
public abstract class Mixin_CFM_VineBlock extends Block {

    @Shadow @Final public static BooleanProperty WEST;
    @Shadow @Final public static BooleanProperty SOUTH;
    @Shadow @Final public static BooleanProperty EAST;
    @Shadow @Final public static BooleanProperty NORTH;
    @Shadow @Final public static BooleanProperty UP;
    @Unique private final O2OMap<BlockState, VoxelShape> cache;
    @Shadow @Final @DeleteField private Map<BlockState, VoxelShape> shapesCache;

    @ModifyConstructor
    public Mixin_CFM_VineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(UP, false).setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
        O2OMap<BlockState, VoxelShape> cache = new O2OHashMap<>();
        OList<BlockState> possibleStates = this.stateDefinition.getPossibleStates_();
        for (int i = 0, len = possibleStates.size(); i < len; ++i) {
            BlockState state = possibleStates.get(i);
            cache.put(state, VineBlock.calculateShape(state));
        }
        cache.trim();
        this.cache = cache;
    }

    @Contract(value = "_ -> _")
    @Shadow
    public static BooleanProperty getPropertyForFace(Direction direction) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Contract(value = "_, _, _ -> _")
    @Shadow
    public static boolean isAcceptableNeighbour(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
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
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return this.hasFaces(this.getUpdatedState(state, level, new BlockPos(x, y, z)));
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
        return this.cache.get(state);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean propagatesSkylightDown_(BlockState state, BlockGetter level, int x, int y, int z) {
        return true;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (random.nextInt(4) == 0) {
            Direction dir = Direction.getRandom(random);
            final BlockPos pos = new BlockPos(x, y, z);
            if (dir.getAxis().isHorizontal() && !state.getValue(getPropertyForFace(dir))) {
                if (this.canSpread(level, pos)) {
                    int posRelDirX = x + dir.getStepX();
                    int posRelDirZ = z + dir.getStepZ();
                    BlockState stateBelow = level.getBlockState_(posRelDirX, y, posRelDirZ);
                    if (stateBelow.isAir()) {
                        Direction dirClock = dir.getClockWise();
                        Direction dirCClock = dir.getCounterClockWise();
                        boolean bl = state.getValue(getPropertyForFace(dirClock));
                        boolean bl2 = state.getValue(getPropertyForFace(dirCClock));
                        int posClockX = posRelDirX + dirClock.getStepX();
                        int posClockZ = posRelDirZ + dirClock.getStepZ();
                        int posCClockX = posRelDirX + dirCClock.getStepX();
                        int posCClockZ = posRelDirZ + dirCClock.getStepZ();
                        if (bl && isAcceptableNeighbour(level, new BlockPos(posClockX, y, posClockZ), dirClock)) {
                            level.setBlock_(posRelDirX, y, posRelDirZ, this.defaultBlockState().setValue(getPropertyForFace(dirClock), true), BlockFlags.BLOCK_UPDATE);
                        }
                        else if (bl2 && isAcceptableNeighbour(level, new BlockPos(posCClockX, y, posCClockZ), dirCClock)) {
                            level.setBlock_(posRelDirX, y, posRelDirZ, this.defaultBlockState().setValue(getPropertyForFace(dirCClock), true), BlockFlags.BLOCK_UPDATE);
                        }
                        else {
                            Direction direction4 = dir.getOpposite();
                            if (bl && level.isEmptyBlock_(posClockX, y, posClockZ) && isAcceptableNeighbour(level, pos.relative(dirClock), direction4)) {
                                level.setBlock_(posClockX, y, posClockZ, this.defaultBlockState().setValue(getPropertyForFace(direction4), true), BlockFlags.BLOCK_UPDATE);
                            }
                            else if (bl2 && level.isEmptyBlock_(posCClockX, y, posCClockZ) && isAcceptableNeighbour(level, pos.relative(dirCClock), direction4)) {
                                level.setBlock_(posCClockX, y, posCClockZ, this.defaultBlockState().setValue(getPropertyForFace(direction4), true), BlockFlags.BLOCK_UPDATE);
                            }
                            else if (random.nextFloat() < 0.05 && isAcceptableNeighbour(level, new BlockPos(posRelDirX, y + 1, posRelDirZ), Direction.UP)) {
                                level.setBlock_(posRelDirX, y, posRelDirZ, this.defaultBlockState().setValue(UP, true), BlockFlags.BLOCK_UPDATE);
                            }
                        }
                    }
                    else if (isAcceptableNeighbour(level, new BlockPos(posRelDirX, y, posRelDirZ), dir)) {
                        level.setBlock_(x, y, z, state.setValue(getPropertyForFace(dir), true), BlockFlags.BLOCK_UPDATE);
                    }
                }
            }
            else {
                if (dir == Direction.UP && y < level.getMaxBuildHeight() - 1) {
                    if (this.canSupportAtFace(level, pos, dir)) {
                        level.setBlock_(x, y, z, state.setValue(UP, true), BlockFlags.BLOCK_UPDATE);
                        return;
                    }
                    if (level.isEmptyBlock_(x, y + 1, z)) {
                        if (!this.canSpread(level, pos)) {
                            return;
                        }
                        BlockState stateForPlacing = state;
                        final BlockPos posUp = pos.above();
                        for (Direction direction1 : DirectionUtil.HORIZ_NESW) {
                            if (!random.nextBoolean() && isAcceptableNeighbour(level, posUp.relative(direction1), direction1)) {
                                continue;
                            }
                            stateForPlacing = stateForPlacing.setValue(getPropertyForFace(direction1), false);
                        }
                        if (this.hasHorizontalConnection(stateForPlacing)) {
                            level.setBlock_(x, y + 1, z, stateForPlacing, BlockFlags.BLOCK_UPDATE);
                        }
                        return;
                    }
                }
                if (y > level.getMinBuildHeight()) {
                    BlockState stateBelow = level.getBlockState_(x, y - 1, z);
                    if (stateBelow.isAir() || stateBelow.is(this)) {
                        BlockState stateForChecking = stateBelow.isAir() ? this.defaultBlockState() : stateBelow;
                        BlockState stateForPlacing = this.copyRandomFaces(state, stateForChecking, random);
                        if (stateForChecking != stateForPlacing && this.hasHorizontalConnection(stateForPlacing)) {
                            level.setBlock_(x, y - 1, z, stateForPlacing, BlockFlags.BLOCK_UPDATE);
                        }
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
            return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
        }
        BlockState blockState3 = this.getUpdatedState(state, level, new BlockPos(x, y, z));
        return !this.hasFaces(blockState3) ? Blocks.AIR.defaultBlockState() : blockState3;
    }

    @Shadow
    protected abstract boolean canSpread(BlockGetter blockGetter, BlockPos blockPos);

    @Shadow
    protected abstract boolean canSupportAtFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction);

    @Shadow
    protected abstract BlockState copyRandomFaces(BlockState blockState, BlockState blockState2, Random random);

    @Shadow
    protected abstract BlockState getUpdatedState(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos);

    @Shadow
    protected abstract boolean hasFaces(BlockState blockState);

    @Shadow
    protected abstract boolean hasHorizontalConnection(BlockState blockState);
}
