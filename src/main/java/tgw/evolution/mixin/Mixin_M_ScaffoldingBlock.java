package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.entities.EntityUtils;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(ScaffoldingBlock.class)
public abstract class Mixin_M_ScaffoldingBlock extends Block implements SimpleWaterloggedBlock {

    @Shadow @Final public static IntegerProperty DISTANCE;
    @Shadow @Final public static BooleanProperty BOTTOM;
    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final private static VoxelShape STABLE_SHAPE;
    @Shadow @Final private static VoxelShape BELOW_BLOCK;
    @Shadow @Final private static VoxelShape UNSTABLE_SHAPE_BOTTOM;
    @Shadow @Final private static VoxelShape UNSTABLE_SHAPE;

    public Mixin_M_ScaffoldingBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public static int getDistance(BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
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
        return getDistance(level, new BlockPos(x, y, z)) < 7;
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
        if (entity == null || EntityUtils.isAbove(entity, Shapes.block(), y) && !entity.isDescending()) {
            return STABLE_SHAPE;
        }
        return state.getValue(DISTANCE) != 0 &&
               state.getValue(BOTTOM) &&
               EntityUtils.isAbove(entity, BELOW_BLOCK, y) ? UNSTABLE_SHAPE_BOTTOM : Shapes.empty();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getInteractionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return Shapes.block();
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
        if (entity instanceof LivingEntity living && living.getMainHandItem().getItem() != state.getBlock().asItem()) {
            return state.getValue(BOTTOM) ? UNSTABLE_SHAPE : STABLE_SHAPE;
        }
        return Shapes.block();
    }

    @Shadow
    protected abstract boolean isBottom(BlockGetter blockGetter, BlockPos blockPos, int i);

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
        if (!level.isClientSide) {
            level.scheduleTick(new BlockPos(x, y, z), this, 1);
        }
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
        BlockPos pos = new BlockPos(x, y, z);
        int i = getDistance(level, pos);
        BlockState blockState2 = state.setValue(DISTANCE, i).setValue(BOTTOM, this.isBottom(level, pos, i));
        if (blockState2.getValue(DISTANCE) == 7) {
            if (state.getValue(DISTANCE) == 7) {
                FallingBlockEntity.fall(level, pos, blockState2);
            }
            else {
                level.destroyBlock(pos, true);
            }
        }
        else if (state != blockState2) {
            level.setBlock(pos, blockState2, 3);
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
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(new BlockPos(x, y, z), Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (!level.isClientSide()) {
            level.scheduleTick(new BlockPos(x, y, z), this, 1);
        }
        return state;
    }
}
