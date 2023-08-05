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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(TrapDoorBlock.class)
public abstract class Mixin_M_TrapDoorBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {

    @Shadow @Final public static BooleanProperty OPEN;
    @Shadow @Final public static EnumProperty<Half> HALF;
    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final protected static VoxelShape TOP_AABB;
    @Shadow @Final protected static VoxelShape BOTTOM_AABB;
    @Shadow @Final protected static VoxelShape NORTH_OPEN_AABB;
    @Shadow @Final protected static VoxelShape SOUTH_OPEN_AABB;
    @Shadow @Final protected static VoxelShape WEST_OPEN_AABB;
    @Shadow @Final protected static VoxelShape EAST_OPEN_AABB;

    public Mixin_M_TrapDoorBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        if (!state.getValue(OPEN)) {
            return state.getValue(HALF) == Half.TOP ? TOP_AABB : BOTTOM_AABB;
        }
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_OPEN_AABB;
            case WEST -> WEST_OPEN_AABB;
            case EAST -> EAST_OPEN_AABB;
            default -> NORTH_OPEN_AABB;
        };
    }

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
            if (hasSignal != state.getValue(POWERED)) {
                if (state.getValue(OPEN) != hasSignal) {
                    state = state.setValue(OPEN, hasSignal);
                    this.playSound(null, level, pos, hasSignal);
                }
                level.setBlock(pos, state.setValue(POWERED, hasSignal), 2);
                if (state.getValue(WATERLOGGED)) {
                    level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
                }
            }
        }
    }

    @Shadow
    protected abstract void playSound(@Nullable Player player, Level level, BlockPos blockPos, boolean bl);

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
        if (this.material == Material.METAL) {
            return InteractionResult.PASS;
        }
        state = state.cycle(OPEN);
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, state, 2);
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        this.playSound(player, level, pos, state.getValue(OPEN));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
