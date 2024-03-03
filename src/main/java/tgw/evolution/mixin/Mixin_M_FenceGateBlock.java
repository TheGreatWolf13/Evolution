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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(FenceGateBlock.class)
public abstract class Mixin_M_FenceGateBlock extends HorizontalDirectionalBlock {

    @Shadow @Final public static BooleanProperty OPEN;
    @Shadow @Final public static BooleanProperty IN_WALL;
    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final protected static VoxelShape Z_COLLISION_SHAPE;
    @Shadow @Final protected static VoxelShape X_COLLISION_SHAPE;
    @Shadow @Final protected static VoxelShape X_OCCLUSION_SHAPE_LOW;
    @Shadow @Final protected static VoxelShape Z_OCCLUSION_SHAPE_LOW;
    @Shadow @Final protected static VoxelShape X_OCCLUSION_SHAPE;
    @Shadow @Final protected static VoxelShape Z_OCCLUSION_SHAPE;
    @Shadow @Final protected static VoxelShape X_SHAPE_LOW;
    @Shadow @Final protected static VoxelShape Z_SHAPE_LOW;
    @Shadow @Final protected static VoxelShape X_SHAPE;
    @Shadow @Final protected static VoxelShape Z_SHAPE;

    public Mixin_M_FenceGateBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        if (state.getValue(OPEN)) {
            return Shapes.empty();
        }
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getOcclusionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        if (state.getValue(IN_WALL)) {
            return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE_LOW : Z_OCCLUSION_SHAPE_LOW;
        }
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE : Z_OCCLUSION_SHAPE;
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
        if (state.getValue(IN_WALL)) {
            return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE_LOW : Z_SHAPE_LOW;
        }
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Shadow
    protected abstract boolean isWall(BlockState blockState);

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
        if (!level.isClientSide) {
            BlockPos pos = new BlockPos(x, y, z);
            boolean hasSignal = level.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != hasSignal) {
                level.setBlock(pos, state.setValue(POWERED, hasSignal).setValue(OPEN, hasSignal), 2);
                if (state.getValue(OPEN) != hasSignal) {
                    level.levelEvent_(null, hasSignal ? LevelEvent.SOUND_OPEN_FENCE_GATE : LevelEvent.SOUND_CLOSE_FENCE_GATE, x, y, z, 0);
                    level.gameEvent(hasSignal ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
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
        Direction.Axis axis = from.getAxis();
        if (state.getValue(FACING).getClockWise().getAxis() != axis) {
            return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
        }
        boolean bl = this.isWall(fromState) || this.isWall(level.getBlockStateAtSide(x, y, z, from.getOpposite()));
        return state.setValue(IN_WALL, bl);
    }

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
        boolean wasOpen = state.getValue(OPEN);
        BlockPos pos = new BlockPos(x, y, z);
        if (wasOpen) {
            state = state.setValue(OPEN, false);
            level.setBlock(pos, state, 10);
        }
        else {
            Direction direction = player.getDirection();
            if (state.getValue(FACING) == direction.getOpposite()) {
                state = state.setValue(FACING, direction);
            }
            state = state.setValue(OPEN, true);
            level.setBlock(pos, state, 10);
        }
        level.levelEvent(player, !wasOpen ? LevelEvent.SOUND_OPEN_FENCE_GATE : LevelEvent.SOUND_CLOSE_FENCE_GATE, pos, 0);
        level.gameEvent(player, !wasOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
