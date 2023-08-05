package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeverBlock;
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

@Mixin(LeverBlock.class)
public abstract class Mixin_M_LeverBlock extends FaceAttachedHorizontalDirectionalBlock {

    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final protected static VoxelShape UP_AABB_X;
    @Shadow @Final protected static VoxelShape UP_AABB_Z;
    @Shadow @Final protected static VoxelShape EAST_AABB;
    @Shadow @Final protected static VoxelShape WEST_AABB;
    @Shadow @Final protected static VoxelShape SOUTH_AABB;
    @Shadow @Final protected static VoxelShape NORTH_AABB;
    @Shadow @Final protected static VoxelShape DOWN_AABB_X;
    @Shadow @Final protected static VoxelShape DOWN_AABB_Z;

    public Mixin_M_LeverBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static void makeParticle(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, float f) {
        throw new AbstractMethodError();
    }

    @Overwrite
    @Override
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> {
                if (state.getValue(FACING).getAxis() == Direction.Axis.X) {
                    yield UP_AABB_X;
                }
                yield UP_AABB_Z;
            }
            case WALL -> switch (state.getValue(FACING)) {
                case EAST -> EAST_AABB;
                case WEST -> WEST_AABB;
                case SOUTH -> SOUTH_AABB;
                default -> NORTH_AABB;
            };
            case CEILING -> {
                if (state.getValue(FACING).getAxis() == Direction.Axis.X) {
                    yield DOWN_AABB_X;
                }
                yield DOWN_AABB_Z;
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
    public abstract BlockState pull(BlockState blockState, Level level, BlockPos blockPos);

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
        BlockState cycledState;
        if (level.isClientSide) {
            cycledState = state.cycle(POWERED);
            if (cycledState.getValue(POWERED)) {
                makeParticle(cycledState, level, new BlockPos(x, y, z), 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = new BlockPos(x, y, z);
        cycledState = this.pull(state, level, pos);
        float pitch = cycledState.getValue(POWERED) ? 0.6F : 0.5F;
        level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, pitch);
        level.gameEvent(player, cycledState.getValue(POWERED) ? GameEvent.BLOCK_SWITCH : GameEvent.BLOCK_UNSWITCH, pos);
        return InteractionResult.CONSUME;
    }
}
