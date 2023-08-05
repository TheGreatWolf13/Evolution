package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Map;
import java.util.Random;

@Mixin(TripWireBlock.class)
public abstract class Mixin_M_TripWireBlock extends Block {

    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final public static BooleanProperty DISARMED;
    @Shadow @Final public static BooleanProperty ATTACHED;
    @Shadow @Final protected static VoxelShape AABB;
    @Shadow @Final protected static VoxelShape NOT_ATTACHED_AABB;
    @Shadow @Final private static Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION;
    public Mixin_M_TripWireBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract void checkPressed(Level level, BlockPos blockPos);

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return state.getValue(ATTACHED) ? AABB : NOT_ATTACHED_AABB;
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
            this.updateSource(level, new BlockPos(x, y, z), state);
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
        if (!isMoving && !state.is(newState.getBlock())) {
            this.updateSource(level, new BlockPos(x, y, z), state.setValue(POWERED, true));
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public void playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player) {
        if (!level.isClientSide && !player.getMainHandItem().isEmpty() && player.getMainHandItem().is(Items.SHEARS)) {
            BlockPos pos = new BlockPos(x, y, z);
            level.setBlock(pos, state.setValue(DISARMED, true), 4);
            level.gameEvent(player, GameEvent.SHEAR, pos);
        }
        super.playerWillDestroy_(level, x, y, z, state, player);
    }

    @Shadow
    public abstract boolean shouldConnectTo(BlockState blockState, Direction direction);

    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.getBlockState_(x, y, z).getValue(POWERED)) {
            this.checkPressed(level, new BlockPos(x, y, z));
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
        return from.getAxis().isHorizontal() ?
               state.setValue(PROPERTY_BY_DIRECTION.get(from), this.shouldConnectTo(fromState, from)) :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

    @Shadow
    protected abstract void updateSource(Level level, BlockPos blockPos, BlockState blockState);
}
