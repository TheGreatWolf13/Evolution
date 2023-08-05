package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Map;

@Mixin(WallBlock.class)
public abstract class Mixin_M_WallBlock extends Block implements SimpleWaterloggedBlock {

    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final private Map<BlockState, VoxelShape> collisionShapeByIndex;
    @Shadow @Final private Map<BlockState, VoxelShape> shapeByIndex;

    public Mixin_M_WallBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.collisionShapeByIndex.get(state);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.shapeByIndex.get(state);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean propagatesSkylightDown_(BlockState state, BlockGetter level, int x, int y, int z) {
        return !state.getValue(WATERLOGGED);
    }

    @Shadow
    protected abstract BlockState sideUpdate(LevelReader levelReader,
                                             BlockPos blockPos,
                                             BlockState blockState,
                                             BlockPos blockPos2,
                                             BlockState blockState2, Direction direction);

    @Shadow
    protected abstract BlockState topUpdate(LevelReader levelReader,
                                            BlockState blockState,
                                            BlockPos blockPos,
                                            BlockState blockState2);

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
        if (from == Direction.DOWN) {
            return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
        }
        return from == Direction.UP ?
               this.topUpdate(level, state, new BlockPos(fromX, fromY, fromZ), fromState) :
               this.sideUpdate(level, new BlockPos(x, y, z), state, new BlockPos(fromX, fromY, fromZ), fromState, from);
    }
}
