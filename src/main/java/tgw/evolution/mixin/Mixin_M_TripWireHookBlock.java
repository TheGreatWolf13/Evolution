package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(TripWireHookBlock.class)
public abstract class Mixin_M_TripWireHookBlock extends Block {

    @Shadow @Final public static DirectionProperty FACING;
    @Shadow @Final public static BooleanProperty ATTACHED;
    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final protected static VoxelShape EAST_AABB;
    @Shadow @Final protected static VoxelShape WEST_AABB;
    @Shadow @Final protected static VoxelShape SOUTH_AABB;
    @Shadow @Final protected static VoxelShape NORTH_AABB;

    public Mixin_M_TripWireHookBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public abstract void calculateState(Level level,
                                        BlockPos blockPos,
                                        BlockState blockState,
                                        boolean bl,
                                        boolean bl2,
                                        int i,
                                        @Nullable BlockState blockState2);

    @Override
    @Overwrite
    @DeleteMethod
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return BlockUtils.hasSolidFaceAtSide(level, x, y, z, state.getValue(FACING).getOpposite());
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
            case WEST -> WEST_AABB;
            case SOUTH -> SOUTH_AABB;
            case NORTH -> NORTH_AABB;
            default -> EAST_AABB;
        };
    }

    @Override
    @Shadow
    public abstract BlockState mirror(BlockState blockState, Mirror mirror);

    @Override
    @Overwrite
    @DeleteMethod
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            boolean attached = state.getValue(ATTACHED);
            boolean powered = state.getValue(POWERED);
            if (attached || powered) {
                this.calculateState(level, new BlockPos(x, y, z), state, true, false, -1, null);
            }
            if (powered) {
                level.updateNeighborsAt_(x, y, z, this);
                Direction opp = state.getValue(FACING).getOpposite();
                int oppX = x + opp.getStepX();
                int oppZ = z + opp.getStepZ();
                level.updateNeighborsAt_(oppX, y, oppZ, this);
            }
            super.onRemove_(state, level, x, y, z, newState, false);
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
        this.calculateState(level, new BlockPos(x, y, z), state, false, true, -1, null);
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
        return from.getOpposite() == state.getValue(FACING) && !state.canSurvive_(level, x, y, z) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
