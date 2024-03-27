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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.patches.PatchBooleanProperty;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.collection.sets.RSet;

import java.util.Map;

@Mixin(WallBlock.class)
public abstract class Mixin_M_WallBlock extends Block implements SimpleWaterloggedBlock {

    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final public static BooleanProperty UP;
    @Shadow @Final public static EnumProperty<WallSide> EAST_WALL;
    @Shadow @Final public static EnumProperty<WallSide> NORTH_WALL;
    @Shadow @Final public static EnumProperty<WallSide> WEST_WALL;
    @Shadow @Final public static EnumProperty<WallSide> SOUTH_WALL;
    @Shadow @Final private Map<BlockState, VoxelShape> collisionShapeByIndex;
    @Shadow @Final private Map<BlockState, VoxelShape> shapeByIndex;

    public Mixin_M_WallBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static VoxelShape applyWallShape(VoxelShape voxelShape, WallSide wallSide, VoxelShape voxelShape2, VoxelShape voxelShape3) {
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
        return this.collisionShapeByIndex.get(state);
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
        return this.shapeByIndex.get(state);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState updateShape_(BlockState state, Direction from, BlockState fromState, LevelAccessor level, int x, int y, int z, int fromX, int fromY, int fromZ) {
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

    @Shadow
    protected abstract BlockState sideUpdate(LevelReader levelReader, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2, Direction direction);

    @Shadow
    protected abstract BlockState topUpdate(LevelReader levelReader, BlockState blockState, BlockPos blockPos, BlockState blockState2);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private Map<BlockState, VoxelShape> makeShapes(float upSize, float g, float upHeight, float i, float j, float k) {
        float up0 = 8.0F - upSize;
        float up1 = 8.0F + upSize;
        float n = 8.0F - g;
        float o = 8.0F + g;
        VoxelShape upShape = Block.box(up0, 0, up0, up1, upHeight, up1);
        VoxelShape voxelShape2 = Block.box(n, i, 0, o, j, o);
        VoxelShape voxelShape3 = Block.box(n, i, n, o, j, 16);
        VoxelShape voxelShape4 = Block.box(0, i, n, o, j, o);
        VoxelShape voxelShape5 = Block.box(n, i, n, 16, j, o);
        VoxelShape voxelShape6 = Block.box(n, i, 0, o, k, o);
        VoxelShape voxelShape7 = Block.box(n, i, n, o, k, 16);
        VoxelShape voxelShape8 = Block.box(0, i, n, o, k, o);
        VoxelShape voxelShape9 = Block.box(n, i, n, 16, k, o);
        O2OMap<BlockState, VoxelShape> map = new O2OHashMap<>();
        for (Boolean up : PatchBooleanProperty.VALUES_ARR) {
            RSet<WallSide> eastWall = EAST_WALL.values();
            for (long it = eastWall.beginIteration(); eastWall.hasNextIteration(it); it = eastWall.nextEntry(it)) {
                WallSide east = eastWall.getIteration(it);
                RSet<WallSide> northWall = NORTH_WALL.values();
                for (long it2 = northWall.beginIteration(); northWall.hasNextIteration(it2); it2 = northWall.nextEntry(it2)) {
                    WallSide north = northWall.getIteration(it2);
                    RSet<WallSide> westWall = WEST_WALL.values();
                    for (long it3 = westWall.beginIteration(); westWall.hasNextIteration(it3); it3 = westWall.nextEntry(it3)) {
                        WallSide west = westWall.getIteration(it3);
                        RSet<WallSide> southWall = SOUTH_WALL.values();
                        for (long it4 = southWall.beginIteration(); southWall.hasNextIteration(it4); it4 = southWall.nextEntry(it4)) {
                            WallSide south = southWall.getIteration(it4);
                            VoxelShape shape = Shapes.empty();
                            shape = applyWallShape(shape, east, voxelShape5, voxelShape9);
                            shape = applyWallShape(shape, west, voxelShape4, voxelShape8);
                            shape = applyWallShape(shape, north, voxelShape2, voxelShape6);
                            shape = applyWallShape(shape, south, voxelShape3, voxelShape7);
                            if (up) {
                                shape = Shapes.or(shape, upShape);
                            }
                            BlockState blockState = this.defaultBlockState().setValue(UP, up).setValue(EAST_WALL, east).setValue(WEST_WALL, west).setValue(NORTH_WALL, north).setValue(SOUTH_WALL, south);
                            map.put(blockState.setValue(WATERLOGGED, false), shape);
                            map.put(blockState.setValue(WATERLOGGED, true), shape);
                        }
                    }
                }
            }
        }
        map.trim();
        return map.view();
    }
}
