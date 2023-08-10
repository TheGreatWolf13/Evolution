package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.items.ItemUtils;

import java.util.Random;

@Mixin(SnowLayerBlock.class)
public abstract class Mixin_M_SnowLayerBlock extends Block {

    @Shadow @Final public static IntegerProperty LAYERS;
    @Shadow @Final protected static VoxelShape[] SHAPE_BY_LAYER;

    public Mixin_M_SnowLayerBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        BlockState stateBelow = level.getBlockState_(x, y - 1, z);
        if (!stateBelow.is(Blocks.ICE) && !stateBelow.is(Blocks.PACKED_ICE) && !stateBelow.is(Blocks.BARRIER)) {
            if (!stateBelow.is(Blocks.HONEY_BLOCK) && !stateBelow.is(Blocks.SOUL_SAND)) {
                return Block.isFaceFull(stateBelow.getCollisionShape_(level, x, y - 1, z), Direction.UP) ||
                       stateBelow.is(this) && stateBelow.getValue(LAYERS) == 8;
            }
            return true;
        }
        return false;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getBlockSupportShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS) - 1];
    }

    @Override
    public ItemUtils.RepeatedUse getRepeatedUse() {
        return ItemUtils.RepeatedUse.NOT_ON_FIRST_TICK;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    @Overwrite
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState stateAtPos = context.getLevel().getBlockState_(context.getClickedPos());
        if (stateAtPos.is(this)) {
            return stateAtPos.setValue(LAYERS, Math.min(8, stateAtPos.getValue(LAYERS) + 1));
        }
        return super.getStateForPlacement(context);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getVisualShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.getBrightness_(LightLayer.BLOCK, BlockPos.asLong(x, y, z)) > 11) {
            BlockUtils.dropResources(state, level, x, y, z);
            level.removeBlock_(x, y, z, false);
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
        return !state.canSurvive_(level, x, y, z) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
