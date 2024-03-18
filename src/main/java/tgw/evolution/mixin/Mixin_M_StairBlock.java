package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
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
import java.util.random.RandomGenerator;

@Mixin(StairBlock.class)
public abstract class Mixin_M_StairBlock extends Block implements SimpleWaterloggedBlock {

    @Shadow @Final public static EnumProperty<Half> HALF;
    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final public static EnumProperty<StairsShape> SHAPE;
    @Shadow @Final public static DirectionProperty FACING;
    @Shadow @Final protected static VoxelShape[] TOP_SHAPES;
    @Shadow @Final protected static VoxelShape[] BOTTOM_SHAPES;
    @Shadow @Final private static int[] SHAPE_BY_STATE;
    @Shadow @Final private Block base;
    @Shadow @Final private BlockState baseState;

    public Mixin_M_StairBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static StairsShape getStairsShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
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
        this.base.animateTick_(state, level, x, y, z, random);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult attack_(BlockState state, Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
        return this.baseState.attack_(level, x, y, z, face, hitX, hitY, hitZ, player);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Override
    public void destroy_(LevelAccessor level, int x, int y, int z, BlockState state) {
        this.base.destroy_(level, x, y, z, state);
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

    @Shadow
    protected abstract int getShapeIndex(BlockState blockState);

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return (state.getValue(HALF) == Half.TOP ? TOP_SHAPES : BOTTOM_SHAPES)[SHAPE_BY_STATE[this.getShapeIndex(state)]];
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);
        BlockState state = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(HALF, direction != Direction.DOWN && (direction == Direction.UP || !(context.getHitResult().y() - pos.getY() > 0.5)) ? Half.BOTTOM : Half.TOP).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        return state.setValue(SHAPE, getStairsShape(state, context.getLevel(), pos));
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
        if (!state.is(oldState.getBlock())) {
            BlockPos pos = new BlockPos(x, y, z);
            this.baseState.neighborChanged(level, pos, Blocks.AIR, pos, false);
            this.base.onPlace_(this.baseState, level, x, y, z, oldState, false);
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
            this.baseState.onRemove_(level, x, y, z, newState, isMoving);
        }
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
        this.base.randomTick_(state, level, x, y, z, random);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        throw new AbstractMethodError();
    }

    @Override
    public void stepOn_(Level level, int x, int y, int z, BlockState state, Entity entity) {
        this.base.stepOn_(level, x, y, z, state, entity);
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
        this.base.tick_(state, level, x, y, z, random);
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
        return from.getAxis().isHorizontal() ?
               state.setValue(SHAPE, getStairsShape(state, level, new BlockPos(x, y, z))) :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
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
        return this.baseState.use(level, player, hand, hitResult);
    }
}
