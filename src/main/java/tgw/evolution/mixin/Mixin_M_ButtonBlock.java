package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(ButtonBlock.class)
public abstract class Mixin_M_ButtonBlock extends FaceAttachedHorizontalDirectionalBlock {

    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final protected static VoxelShape PRESSED_FLOOR_AABB_X;
    @Shadow @Final protected static VoxelShape FLOOR_AABB_X;
    @Shadow @Final protected static VoxelShape PRESSED_FLOOR_AABB_Z;
    @Shadow @Final protected static VoxelShape FLOOR_AABB_Z;
    @Shadow @Final protected static VoxelShape PRESSED_CEILING_AABB_X;
    @Shadow @Final protected static VoxelShape CEILING_AABB_X;
    @Shadow @Final protected static VoxelShape PRESSED_CEILING_AABB_Z;
    @Shadow @Final protected static VoxelShape CEILING_AABB_Z;
    @Shadow @Final protected static VoxelShape PRESSED_NORTH_AABB;
    @Shadow @Final protected static VoxelShape NORTH_AABB;
    @Shadow @Final protected static VoxelShape PRESSED_SOUTH_AABB;
    @Shadow @Final protected static VoxelShape SOUTH_AABB;
    @Shadow @Final protected static VoxelShape PRESSED_WEST_AABB;
    @Shadow @Final protected static VoxelShape WEST_AABB;
    @Shadow @Final protected static VoxelShape PRESSED_EAST_AABB;
    @Shadow @Final protected static VoxelShape EAST_AABB;
    @Shadow @Final private boolean sensitive;

    public Mixin_M_ButtonBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract void checkPressed(BlockState blockState, Level level, BlockPos blockPos);

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        Direction direction = state.getValue(FACING);
        boolean pressed = state.getValue(POWERED);
        return switch (state.getValue(FACE)) {
            case FLOOR -> {
                if (direction.getAxis() == Direction.Axis.X) {
                    yield pressed ? PRESSED_FLOOR_AABB_X : FLOOR_AABB_X;
                }
                yield pressed ? PRESSED_FLOOR_AABB_Z : FLOOR_AABB_Z;
            }
            case WALL -> switch (direction) {
                case EAST -> pressed ? PRESSED_EAST_AABB : EAST_AABB;
                case WEST -> pressed ? PRESSED_WEST_AABB : WEST_AABB;
                case SOUTH -> pressed ? PRESSED_SOUTH_AABB : SOUTH_AABB;
                default -> pressed ? PRESSED_NORTH_AABB : NORTH_AABB;
            };
            case CEILING -> {
                if (direction.getAxis() == Direction.Axis.X) {
                    yield pressed ? PRESSED_CEILING_AABB_X : CEILING_AABB_X;
                }
                yield pressed ? PRESSED_CEILING_AABB_Z : CEILING_AABB_Z;
            }
        };
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            if (state.getValue(POWERED)) {
                this.updateNeighbours(state, level, new BlockPos(x, y, z));
            }
            super.onRemove_(state, level, x, y, z, newState, false);
        }
    }

    @Shadow
    protected abstract void playSound(@Nullable Player player,
                                      LevelAccessor levelAccessor,
                                      BlockPos blockPos, boolean bl);

    @Shadow
    public abstract void press(BlockState blockState, Level level, BlockPos blockPos);

    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (state.getValue(POWERED)) {
            if (this.sensitive) {
                this.checkPressed(state, level, new BlockPos(x, y, z));
            }
            else {
                level.setBlockAndUpdate_(x, y, z, state.setValue(POWERED, false));
                BlockPos pos = new BlockPos(x, y, z);
                this.updateNeighbours(state, level, pos);
                this.playSound(null, level, pos, false);
                level.gameEvent(GameEvent.BLOCK_UNPRESS, pos);
            }
        }
    }

    @Shadow
    protected abstract void updateNeighbours(BlockState blockState, Level level, BlockPos blockPos);

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
        if (state.getValue(POWERED)) {
            return InteractionResult.CONSUME;
        }
        BlockPos pos = new BlockPos(x, y, z);
        this.press(state, level, pos);
        this.playSound(player, level, pos, true);
        level.gameEvent(player, GameEvent.BLOCK_PRESS, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
