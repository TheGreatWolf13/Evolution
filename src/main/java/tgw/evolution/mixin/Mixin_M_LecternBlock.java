package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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

@Mixin(LecternBlock.class)
public abstract class Mixin_M_LecternBlock extends BaseEntityBlock {

    @Shadow @Final public static VoxelShape SHAPE_COLLISION;
    @Shadow @Final public static VoxelShape SHAPE_COMMON;
    @Shadow @Final public static DirectionProperty FACING;
    @Shadow @Final public static VoxelShape SHAPE_NORTH;
    @Shadow @Final public static VoxelShape SHAPE_SOUTH;
    @Shadow @Final public static VoxelShape SHAPE_EAST;
    @Shadow @Final public static VoxelShape SHAPE_WEST;
    @Shadow @Final public static BooleanProperty HAS_BOOK;
    @Shadow @Final public static BooleanProperty POWERED;

    public Mixin_M_LecternBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static void changePowered(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        throw new AbstractMethodError();
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
        return SHAPE_COLLISION;
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
        return SHAPE_COMMON;
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
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_COMMON;
        };
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
        return state.getValue(POWERED) ? 15 : 0;
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
            if (state.getValue(HAS_BOOK)) {
                this.popBook(state, level, new BlockPos(x, y, z));
            }
            if (state.getValue(POWERED)) {
                level.updateNeighborsAt_(x, y - 1, z, this);
            }
            super.onRemove_(state, level, x, y, z, newState, isMoving);
        }
    }

    @Shadow
    protected abstract void openScreen(Level level, BlockPos blockPos, Player player);

    @Shadow
    protected abstract void popBook(BlockState blockState, Level level, BlockPos blockPos);

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
        changePowered(level, new BlockPos(x, y, z), state, false);
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
        if (state.getValue(HAS_BOOK)) {
            if (!level.isClientSide) {
                this.openScreen(level, new BlockPos(x, y, z), player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        ItemStack stack = player.getItemInHand(hand);
        return !stack.isEmpty() && !stack.is(ItemTags.LECTERN_BOOKS) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }
}
