package tgw.evolution.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.DirectionUtil;

@Mixin(MultifaceBlock.class)
public abstract class Mixin_M_MultifaceBlock extends Block {

    @Shadow @Final private ImmutableMap<BlockState, VoxelShape> shapesCache;

    public Mixin_M_MultifaceBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static boolean canAttachTo(BlockGetter blockGetter,
                                       Direction direction,
                                       BlockPos blockPos,
                                       BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static BooleanProperty getFaceProperty(Direction direction) {
        throw new AbstractMethodError();
    }

    @Shadow
    protected static boolean hasAnyFace(BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static boolean hasFace(BlockState blockState, Direction direction) {
        return false;
    }

    @Shadow
    private static BlockState removeFace(BlockState blockState, BooleanProperty booleanProperty) {
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
        for (Direction direction : DirectionUtil.ALL) {
            if (hasFace(state, direction)) {
                int sx = x;
                int sy = y;
                int sz = z;
                switch (direction) {
                    case WEST -> --sx;
                    case EAST -> ++sx;
                    case DOWN -> --sy;
                    case UP -> ++sy;
                    case NORTH -> --sz;
                    case SOUTH -> ++sz;
                }
                if (!canAttachTo(level, direction, new BlockPos(sx, sy, sz), level.getBlockState_(sx, sy, sz))) {
                    return false;
                }
            }
        }
        return false;
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
        //noinspection ConstantConditions
        return this.shapesCache.get(state);
    }

    @Shadow
    protected abstract boolean isWaterloggable();

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
        if (!hasAnyFace(state)) {
            return Blocks.AIR.defaultBlockState();
        }
        return hasFace(state, from) && !canAttachTo(level, from, new BlockPos(fromX, fromY, fromZ), fromState) ?
               removeFace(state, getFaceProperty(from)) :
               state;
    }
}
