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
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
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

import java.util.Optional;
import java.util.Random;

@Mixin(PointedDripstoneBlock.class)
public abstract class Mixin_M_PointedDripstoneBlock extends Block implements Fallable, SimpleWaterloggedBlock {

    @Shadow @Final public static EnumProperty<DripstoneThickness> THICKNESS;
    @Shadow @Final public static DirectionProperty TIP_DIRECTION;
    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final private static VoxelShape BASE_SHAPE;
    @Shadow @Final private static VoxelShape MIDDLE_SHAPE;
    @Shadow @Final private static VoxelShape FRUSTUM_SHAPE;
    @Shadow @Final private static VoxelShape TIP_SHAPE_UP;
    @Shadow @Final private static VoxelShape TIP_SHAPE_DOWN;
    @Shadow @Final private static VoxelShape TIP_MERGE_SHAPE;

    public Mixin_M_PointedDripstoneBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static DripstoneThickness calculateDripstoneThickness(LevelReader levelReader,
                                                                  BlockPos blockPos,
                                                                  Direction direction,
                                                                  boolean bl) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static boolean canDrip(BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static boolean canFillCauldron(Fluid fluid) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static Optional<Fluid> getFluidAboveStalactite(Level level, BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static void growStalactiteOrStalagmiteIfPossible(BlockState blockState,
                                                            ServerLevel serverLevel,
                                                            BlockPos blockPos,
                                                            Random random) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static boolean isStalactiteStartPos(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static boolean isStalagmite(BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static boolean isValidPointedDripstonePlacement(LevelReader levelReader, BlockPos blockPos, Direction direction) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static void maybeFillCauldron(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, float f) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static void spawnDripParticle(Level level, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static void spawnFallingStalactite(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (canDrip(blockState)) {
            float f = random.nextFloat();
            if (!(f > 0.12F)) {
                Optional<Fluid> fluidAboveStalactite = getFluidAboveStalactite(level, blockPos, blockState);
                if (fluidAboveStalactite.isPresent()) {
                    Fluid fluid = fluidAboveStalactite.get();
                    if (f < 0.02F || canFillCauldron(fluid)) {
                        spawnDripParticle(level, blockPos, blockState, fluid);
                    }
                }
            }
        }
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
        return isValidPointedDripstonePlacement(level, new BlockPos(x, y, z), state.getValue(TIP_DIRECTION));
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getOcclusionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return Shapes.empty();
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
        DripstoneThickness thickness = state.getValue(THICKNESS);
        VoxelShape shape = switch (thickness) {
            case TIP_MERGE -> TIP_MERGE_SHAPE;
            case TIP -> {
                if (state.getValue(TIP_DIRECTION) == Direction.DOWN) {
                    yield TIP_SHAPE_DOWN;
                }
                yield TIP_SHAPE_UP;
            }
            case FRUSTUM -> FRUSTUM_SHAPE;
            case MIDDLE -> MIDDLE_SHAPE;
            default -> BASE_SHAPE;
        };
        return this.moveShapeByOffset(shape, x, z);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean isCollisionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean isCollisionShapeFullBlock_(BlockState state, BlockGetter level, int x, int y, int z) {
        return false;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        BlockPos pos = new BlockPos(x, y, z);
        maybeFillCauldron(state, level, pos, random.nextFloat());
        if (random.nextFloat() < 0.011_377_778F && isStalactiteStartPos(state, level, pos)) {
            growStalactiteOrStalagmiteIfPossible(state, level, pos, random);
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
        if (isStalagmite(state) && !this.canSurvive_(state, level, x, y, z)) {
            level.destroyBlock_(x, y, z, true);
        }
        else {
            spawnFallingStalactite(state, level, new BlockPos(x, y, z));
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
        if (from != Direction.UP && from != Direction.DOWN) {
            return state;
        }
        Direction direction2 = state.getValue(TIP_DIRECTION);
        if (direction2 == Direction.DOWN && level.getBlockTicks().hasScheduledTick(new BlockPos(x, y, z), this)) {
            return state;
        }
        if (from == direction2.getOpposite() && !this.canSurvive_(state, level, x, y, z)) {
            if (direction2 == Direction.DOWN) {
                level.scheduleTick(new BlockPos(x, y, z), this, 2);
            }
            else {
                level.scheduleTick(new BlockPos(x, y, z), this, 1);
            }
            return state;
        }
        boolean bl = state.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
        DripstoneThickness dripstoneThickness = calculateDripstoneThickness(level, new BlockPos(x, y, z), direction2, bl);
        return state.setValue(THICKNESS, dripstoneThickness);
    }
}
