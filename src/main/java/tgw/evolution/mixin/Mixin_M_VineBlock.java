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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

@Mixin(VineBlock.class)
public abstract class Mixin_M_VineBlock extends Block {

    @Shadow @Final public static BooleanProperty UP;
    @Shadow @Final private Map<BlockState, VoxelShape> shapesCache;

    public Mixin_M_VineBlock(Properties properties) {
        super(properties);
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

    @Shadow
    protected abstract boolean canSpread(BlockGetter blockGetter, BlockPos blockPos);

    @Shadow
    protected abstract boolean canSupportAtFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction);

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

    @Shadow
    protected abstract BlockState copyRandomFaces(BlockState blockState, BlockState blockState2, Random random);

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.shapesCache.get(state);
    }

    @Shadow
    protected abstract BlockState getUpdatedState(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos);

    @Shadow
    protected abstract boolean hasFaces(BlockState blockState);

    @Shadow
    protected abstract boolean hasHorizontalConnection(BlockState blockState);

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

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (random.nextInt(4) == 0) {
            Direction direction = Direction.getRandom(random);
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos blockPos2 = pos.above();
            Direction direction2;
            if (direction.getAxis().isHorizontal() && !state.getValue(getPropertyForFace(direction))) {
                if (this.canSpread(level, pos)) {
                    BlockPos blockPos3 = pos.relative(direction);
                    BlockState stateBelow = level.getBlockState(blockPos3);
                    if (stateBelow.isAir()) {
                        direction2 = direction.getClockWise();
                        Direction direction3 = direction.getCounterClockWise();
                        boolean bl = state.getValue(getPropertyForFace(direction2));
                        boolean bl2 = state.getValue(getPropertyForFace(direction3));
                        BlockPos blockPos4 = blockPos3.relative(direction2);
                        BlockPos blockPos5 = blockPos3.relative(direction3);
                        if (bl && isAcceptableNeighbour(level, blockPos4, direction2)) {
                            level.setBlock(blockPos3, this.defaultBlockState().setValue(getPropertyForFace(direction2), true), 2);
                        }
                        else if (bl2 && isAcceptableNeighbour(level, blockPos5, direction3)) {
                            level.setBlock(blockPos3, this.defaultBlockState().setValue(getPropertyForFace(direction3), true), 2);
                        }
                        else {
                            Direction direction4 = direction.getOpposite();
                            if (bl &&
                                level.isEmptyBlock(blockPos4) &&
                                isAcceptableNeighbour(level, pos.relative(direction2), direction4)) {
                                level.setBlock(blockPos4, this.defaultBlockState().setValue(getPropertyForFace(direction4), true),
                                               2);
                            }
                            else if (bl2 &&
                                     level.isEmptyBlock(blockPos5) &&
                                     isAcceptableNeighbour(level, pos.relative(direction3), direction4)) {
                                level.setBlock(blockPos5, this.defaultBlockState().setValue(getPropertyForFace(direction4), true),
                                               2);
                            }
                            else if (random.nextFloat() < 0.05 && isAcceptableNeighbour(level, blockPos3.above(), Direction.UP)) {
                                level.setBlock(blockPos3, this.defaultBlockState().setValue(UP, true), 2);
                            }
                        }
                    }
                    else if (isAcceptableNeighbour(level, blockPos3, direction)) {
                        level.setBlock_(x, y, z, state.setValue(getPropertyForFace(direction), true), BlockFlags.BLOCK_UPDATE);
                    }

                }
            }
            else {
                if (direction == Direction.UP && y < level.getMaxBuildHeight() - 1) {
                    if (this.canSupportAtFace(level, pos, direction)) {
                        level.setBlock_(x, y, z, state.setValue(UP, true), BlockFlags.BLOCK_UPDATE);
                        return;
                    }
                    if (level.isEmptyBlock(blockPos2)) {
                        if (!this.canSpread(level, pos)) {
                            return;
                        }
                        BlockState blockState3 = state;
                        Iterator<Direction> var17 = Direction.Plane.HORIZONTAL.iterator();
                        while (true) {
                            do {
                                if (!var17.hasNext()) {
                                    if (this.hasHorizontalConnection(blockState3)) {
                                        level.setBlock(blockPos2, blockState3, 2);
                                    }

                                    return;
                                }

                                direction2 = var17.next();
                            } while (!random.nextBoolean() && isAcceptableNeighbour(level, blockPos2.relative(direction2), direction2));
                            blockState3 = blockState3.setValue(getPropertyForFace(direction2), false);
                        }
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
}
