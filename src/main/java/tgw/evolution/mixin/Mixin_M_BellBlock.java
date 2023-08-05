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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(BellBlock.class)
public abstract class Mixin_M_BellBlock extends BaseEntityBlock {

    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final public static EnumProperty<BellAttachType> ATTACHMENT;
    @Shadow @Final public static DirectionProperty FACING;

    public Mixin_M_BellBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static Direction getConnectedDirection(BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    public abstract boolean attemptToRing(Level level, BlockPos blockPos, @Nullable Direction direction);

    @Override
    @Overwrite
    @DeleteMethod
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        Direction direction = getConnectedDirection(state).getOpposite();
        return direction == Direction.UP ?
               BlockUtils.canSupportCenter(level, x, y + 1, z, Direction.DOWN) :
               FaceAttachedHorizontalDirectionalBlock.canAttach(level, new BlockPos(x, y, z), direction);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.getVoxelShape(state);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.getVoxelShape(state);
    }

    @Shadow
    protected abstract VoxelShape getVoxelShape(BlockState blockState);

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
        BlockPos pos = new BlockPos(x, y, z);
        boolean shouldBePowered = level.hasNeighborSignal(pos);
        if (shouldBePowered != state.getValue(POWERED)) {
            if (shouldBePowered) {
                this.attemptToRing(level, pos, null);
            }
            level.setBlock(pos, state.setValue(POWERED, shouldBePowered), 3);
        }
    }

    @Shadow
    public abstract boolean onHit(Level level,
                                  BlockState blockState,
                                  BlockHitResult blockHitResult,
                                  @Nullable Player player, boolean bl);

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
        BellAttachType bellAttachType = state.getValue(ATTACHMENT);
        Direction dir = getConnectedDirection(state).getOpposite();
        if (dir == from && !state.canSurvive_(level, x, y, z) && bellAttachType != BellAttachType.DOUBLE_WALL) {
            return Blocks.AIR.defaultBlockState();
        }
        if (from.getAxis() == state.getValue(FACING).getAxis()) {
            if (bellAttachType == BellAttachType.DOUBLE_WALL && !fromState.isFaceSturdy_(level, fromX, fromY, fromZ, from)) {
                return state.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL).setValue(FACING, from.getOpposite());
            }
            if (bellAttachType == BellAttachType.SINGLE_WALL &&
                dir.getOpposite() == from &&
                fromState.isFaceSturdy_(level, fromX, fromY, fromZ, state.getValue(FACING))) {
                return state.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
            }
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

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
        return this.onHit(level, state, hitResult, player, true) ?
               InteractionResult.sidedSuccess(level.isClientSide) :
               InteractionResult.PASS;
    }
}
