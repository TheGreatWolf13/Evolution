package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stat;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.function.Supplier;

@Mixin(ChestBlock.class)
public abstract class Mixin_M_ChestBlock extends AbstractChestBlock<ChestBlockEntity> implements SimpleWaterloggedBlock {

    @Shadow @Final public static EnumProperty<ChestType> TYPE;
    @Shadow @Final public static DirectionProperty FACING;
    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final protected static VoxelShape AABB;
    @Shadow @Final protected static VoxelShape NORTH_AABB;
    @Shadow @Final protected static VoxelShape SOUTH_AABB;
    @Shadow @Final protected static VoxelShape WEST_AABB;
    @Shadow @Final protected static VoxelShape EAST_AABB;

    public Mixin_M_ChestBlock(Properties properties,
                              Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier) {
        super(properties, supplier);
    }

    @Contract(value = "_ -> _")
    @Shadow
    public static Direction getConnectedDirection(BlockState blockState) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Shadow
    protected abstract Stat<ResourceLocation> getOpenChestStat();

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        if (state.getValue(TYPE) == ChestType.SINGLE) {
            return AABB;
        }
        return switch (getConnectedDirection(state)) {
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            default -> EAST_AABB;
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
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity_(x, y, z) instanceof Container c) {
                Containers.dropContents(level, new BlockPos(x, y, z), c);
                level.updateNeighbourForOutputSignal_(x, y, z, this);
            }
            super.onRemove_(state, level, x, y, z, newState, isMoving);
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.getBlockEntity_(x, y, z) instanceof ChestBlockEntity tile) {
            tile.recheckOpen();
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
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(new BlockPos(x, y, z), Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (fromState.is(this) && from.getAxis().isHorizontal()) {
            ChestType chestType = fromState.getValue(TYPE);
            if (state.getValue(TYPE) == ChestType.SINGLE &&
                chestType != ChestType.SINGLE &&
                state.getValue(FACING) == fromState.getValue(FACING) &&
                getConnectedDirection(fromState) == from.getOpposite()) {
                return state.setValue(TYPE, chestType.getOpposite());
            }
        }
        else if (getConnectedDirection(state) == from) {
            return state.setValue(TYPE, ChestType.SINGLE);
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
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider menuProvider = this.getMenuProvider(state, level, new BlockPos(x, y, z));
        if (menuProvider != null) {
            player.openMenu(menuProvider);
            player.awardStat(this.getOpenChestStat());
            PiglinAi.angerNearbyPiglins(player, true);
        }
        return InteractionResult.CONSUME;
    }
}
