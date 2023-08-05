package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

@Mixin(CactusBlock.class)
public abstract class Mixin_M_CactusBlock extends Block {

    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final protected static VoxelShape COLLISION_SHAPE;
    @Shadow @Final protected static VoxelShape OUTLINE_SHAPE;

    public Mixin_M_CactusBlock(Properties properties) {
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
        for (Direction direction : DirectionUtil.HORIZ_NESW) {
            if (level.getBlockStateAtSide(x, y, z, direction).getMaterial().isSolid() ||
                level.getFluidStateAtSide(x, y, z, direction).is(FluidTags.LAVA)) {
                return false;
            }
        }
        BlockState stateBelow = level.getBlockState_(x, y - 1, z);
        return (stateBelow.is(Blocks.CACTUS) || stateBelow.is(Blocks.SAND) || stateBelow.is(Blocks.RED_SAND)) &&
               !level.getBlockState_(x, y + 1, z).getMaterial().isLiquid();
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return COLLISION_SHAPE;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return OUTLINE_SHAPE;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.isEmptyBlock_(x, y + 1, z)) {
            int height = 1;
            while (level.getBlockState_(x, y - height, z).is(this)) {
                if (++height == 3) {
                    break;
                }
            }
            if (height < 3) {
                int age = state.getValue(AGE);
                if (age == 15) {
                    level.setBlockAndUpdate_(x, y + 1, z, this.defaultBlockState());
                    BlockState stateAge0 = state.setValue(AGE, 0);
                    level.setBlock_(x, y, z, stateAge0, BlockFlags.NO_RERENDER);
                    stateAge0.neighborChanged_(level, x, y + 1, z, this, x, y, z, false);
                }
                else {
                    level.setBlock_(x, y, z, state.setValue(AGE, age + 1), BlockFlags.NO_RERENDER);
                }
            }
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
        if (!state.canSurvive_(level, x, y, z)) {
            level.destroyBlock_(x, y, z, true);
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
        if (!state.canSurvive_(level, x, y, z)) {
            level.scheduleTick(new BlockPos(x, y, z), this, 1);
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
