package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

@Mixin(HopperBlock.class)
public abstract class Mixin_M_HopperBlock extends BaseEntityBlock {

    @Shadow @Final public static DirectionProperty FACING;
    @Shadow @Final private static VoxelShape DOWN_SHAPE;
    @Shadow @Final private static VoxelShape NORTH_SHAPE;
    @Shadow @Final private static VoxelShape SOUTH_SHAPE;
    @Shadow @Final private static VoxelShape WEST_SHAPE;
    @Shadow @Final private static VoxelShape EAST_SHAPE;
    @Shadow @Final private static VoxelShape BASE;
    @Shadow @Final private static VoxelShape DOWN_INTERACTION_SHAPE;
    @Shadow @Final private static VoxelShape NORTH_INTERACTION_SHAPE;
    @Shadow @Final private static VoxelShape SOUTH_INTERACTION_SHAPE;
    @Shadow @Final private static VoxelShape WEST_INTERACTION_SHAPE;
    @Shadow @Final private static VoxelShape EAST_INTERACTION_SHAPE;

    public Mixin_M_HopperBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract void checkPoweredState(Level level, BlockPos blockPos, BlockState blockState);

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getInteractionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return switch (state.getValue(FACING)) {
            case DOWN -> DOWN_INTERACTION_SHAPE;
            case NORTH -> NORTH_INTERACTION_SHAPE;
            case SOUTH -> SOUTH_INTERACTION_SHAPE;
            case WEST -> WEST_INTERACTION_SHAPE;
            case EAST -> EAST_INTERACTION_SHAPE;
            default -> Hopper.INSIDE;
        };
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return switch (state.getValue(FACING)) {
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> BASE;
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
        this.checkPoweredState(level, new BlockPos(x, y, z), state);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            this.checkPoweredState(level, new BlockPos(x, y, z), state);
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity_(x, y, z) instanceof HopperBlockEntity c) {
                Containers.dropContents(level, new BlockPos(x, y, z), c);
                level.updateNeighbourForOutputSignal_(x, y, z, this);
            }
            super.onRemove_(state, level, x, y, z, newState, isMoving);
        }
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
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity_(x, y, z) instanceof HopperBlockEntity tile) {
            player.openMenu(tile);
            player.awardStat(Stats.INSPECT_HOPPER);
        }
        return InteractionResult.CONSUME;
    }
}
