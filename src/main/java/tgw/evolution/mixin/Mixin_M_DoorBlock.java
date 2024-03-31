package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
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
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.LvlEvent;

import java.util.Random;
import java.util.function.Consumer;

@Mixin(DoorBlock.class)
public abstract class Mixin_M_DoorBlock extends Block {

    @Shadow @Final public static DirectionProperty FACING;
    @Shadow @Final public static BooleanProperty OPEN;
    @Shadow @Final public static EnumProperty<DoorHingeSide> HINGE;
    @Shadow @Final public static EnumProperty<DoubleBlockHalf> HALF;
    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final protected static VoxelShape EAST_AABB;
    @Shadow @Final protected static VoxelShape SOUTH_AABB;
    @Shadow @Final protected static VoxelShape WEST_AABB;
    @Shadow @Final protected static VoxelShape NORTH_AABB;

    public Mixin_M_DoorBlock(Properties properties) {
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
        BlockState stateBelow = level.getBlockState_(x, y - 1, z);
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ?
               stateBelow.isFaceSturdy_(level, x, y - 1, z, Direction.UP) :
               stateBelow.is(this);
    }

    @Override
    public void dropLoot(BlockState state, ServerLevel level, int x, int y, int z, ItemStack tool, @Nullable BlockEntity tile, @Nullable Entity entity, Random random, Consumer<ItemStack> consumer) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            super.dropLoot(state, level, x, y, z, tool, tile, entity, random, consumer);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public long getSeed(BlockState blockState, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public long getSeed_(BlockState state, int x, int y, int z) {
        return Mth.getSeed(x, state.getValue(HALF) == DoubleBlockHalf.LOWER ? y : y - 1, z);
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
        Direction direction = state.getValue(FACING);
        boolean open = !state.getValue(OPEN);
        boolean rightHinge = state.getValue(HINGE) == DoorHingeSide.RIGHT;
        return switch (direction) {
            case SOUTH -> open ? SOUTH_AABB : rightHinge ? EAST_AABB : WEST_AABB;
            case WEST -> open ? WEST_AABB : rightHinge ? SOUTH_AABB : NORTH_AABB;
            case NORTH -> open ? NORTH_AABB : rightHinge ? WEST_AABB : EAST_AABB;
            default -> open ? EAST_AABB : rightHinge ? NORTH_AABB : SOUTH_AABB;
        };
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (y < level.getMaxBuildHeight() - 1 && level.getBlockState_(x, y + 1, z).canBeReplaced_(level, x, y, z, context.getPlayer(), context.getHand(), context.getHitResult())) {
            boolean bl = level.hasNeighborSignal_(x, y, z) || level.hasNeighborSignal_(x, y + 1, z);
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(HINGE, this.getHinge(context)).setValue(POWERED, bl).setValue(OPEN, bl).setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Shadow
    public abstract boolean isOpen(BlockState blockState);

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
        boolean hasSignal = level.hasNeighborSignal_(x, y, z) || level.hasNeighborSignal_(x, y + (state.getValue(HALF) == DoubleBlockHalf.LOWER ? 1 : -1), z);
        if (!this.defaultBlockState().is(oldBlock) && hasSignal != state.getValue(POWERED)) {
            if (hasSignal != state.getValue(OPEN)) {
                BlockPos pos = new BlockPos(x, y, z);
                this.playSound(level, pos, hasSignal);
                level.gameEvent(hasSignal ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
            level.setBlock_(x, y, z, state.setValue(POWERED, hasSignal).setValue(OPEN, hasSignal), BlockFlags.BLOCK_UPDATE);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player, Direction face, double hitX, double hitY, double hitZ) {
        if (!level.isClientSide && player.isCreative()) {
            BlockUtils.preventCreativeDropFromBottomPart(level, x, y, z, state, player);
        }
        return super.playerWillDestroy_(level, x, y, z, state, player, face, hitX, hitY, hitZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("removal")
    @Override
    @Overwrite
    @DeleteMethod
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        throw new AbstractMethodError();
    }

    @Override
    public void setPlacedBy_(Level level, int x, int y, int z, BlockState stateAtPos, Player player, ItemStack stack) {
        level.setBlock_(x, y + 1, z, stateAtPos.setValue(HALF, DoubleBlockHalf.UPPER), BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState updateShape_(BlockState state, Direction from, BlockState fromState, LevelAccessor level, int x, int y, int z, int fromX, int fromY, int fromZ) {
        DoubleBlockHalf doubleBlockHalf = state.getValue(HALF);
        if (from.getAxis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (from == Direction.UP)) {
            return fromState.is(this) && fromState.getValue(HALF) != doubleBlockHalf ?
                   state.setValue(FACING, fromState.getValue(FACING))
                        .setValue(OPEN, fromState.getValue(OPEN))
                        .setValue(HINGE, fromState.getValue(HINGE))
                        .setValue(POWERED, fromState.getValue(POWERED)) :
                   Blocks.AIR.defaultBlockState();
        }
        return doubleBlockHalf == DoubleBlockHalf.LOWER && from == Direction.DOWN && !state.canSurvive_(level, x, y, z) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (this.material == Material.METAL) {
            return InteractionResult.PASS;
        }
        state = state.cycle(OPEN);
        level.setBlock_(x, y, z, state, BlockFlags.BLOCK_UPDATE | BlockFlags.RENDER_MAINTHREAD);
        boolean isOpen = this.isOpen(state);
        level.levelEvent_(player, isOpen ? this.getOpenSound() : this.getCloseSound(), x, y, z, 0);
        level.gameEvent(player, isOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, new BlockPos(x, y, z));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Shadow
    protected abstract @LvlEvent int getCloseSound();

    @Shadow
    protected abstract @LvlEvent int getOpenSound();

    @Shadow
    protected abstract void playSound(Level level, BlockPos blockPos, boolean bl);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private DoorHingeSide getHinge(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        Direction horizDir = context.getHorizontalDirection();
        Direction nextHorizDir = horizDir.getCounterClockWise();
        int nextX = x + nextHorizDir.getStepX();
        int nextZ = z + nextHorizDir.getStepZ();
        BlockState stateAtNext = level.getBlockState_(nextX, y, nextZ);
        BlockState stateAtNextUp = level.getBlockState_(nextX, y + 1, nextZ);
        Direction prevHorizDir = horizDir.getClockWise();
        int prevX = x + prevHorizDir.getStepX();
        int prevZ = z + prevHorizDir.getStepZ();
        BlockState stateAtPrev = level.getBlockState_(prevX, y, prevZ);
        BlockState stateAtPrevUp = level.getBlockState_(prevX, y + 1, prevZ);
        int i = (stateAtNext.isCollisionShapeFullBlock_(level, nextX, y, nextZ) ? -1 : 0) + (stateAtNextUp.isCollisionShapeFullBlock_(level, nextX, y + 1, nextZ) ? -1 : 0) + (stateAtPrev.isCollisionShapeFullBlock_(level, prevX, y, prevZ) ? 1 : 0) + (stateAtPrevUp.isCollisionShapeFullBlock_(level, prevX, y + 1, prevZ) ? 1 : 0);
        boolean bl = stateAtNext.is(this) && stateAtNext.getValue(HALF) == DoubleBlockHalf.LOWER;
        boolean bl2 = stateAtPrev.is(this) && stateAtPrev.getValue(HALF) == DoubleBlockHalf.LOWER;
        if ((!bl || bl2) && i <= 0) {
            if ((!bl2 || bl) && i == 0) {
                int j = horizDir.getStepX();
                int k = horizDir.getStepZ();
                BlockHitResult hitResult = context.getHitResult();
                double d = hitResult.x() - pos.getX();
                double e = hitResult.z() - pos.getZ();
                return (j >= 0 || !(e < 0.5)) && (j <= 0 || !(e > 0.5)) && (k >= 0 || !(d > 0.5)) && (k <= 0 || !(d < 0.5)) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            }
            return DoorHingeSide.LEFT;
        }
        return DoorHingeSide.RIGHT;
    }
}
