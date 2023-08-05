package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(GrowingPlantHeadBlock.class)
public abstract class Mixin_M_GrowingPlantHeadBlock extends GrowingPlantBlock implements BonemealableBlock {

    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final private double growPerTickProbability;

    public Mixin_M_GrowingPlantHeadBlock(Properties properties,
                                         Direction direction,
                                         VoxelShape voxelShape, boolean bl) {
        super(properties, direction, voxelShape, bl);
    }

    @Shadow
    protected abstract boolean canGrowInto(BlockState blockState);

    @Shadow
    protected abstract BlockState getGrowIntoState(BlockState blockState, Random random);

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (state.getValue(AGE) < 25 && random.nextDouble() < this.growPerTickProbability) {
            int offX = x + this.growthDirection.getStepX();
            int offY = y + this.growthDirection.getStepY();
            int offZ = z + this.growthDirection.getStepZ();
            if (this.canGrowInto(level.getBlockState_(offX, offY, offZ))) {
                level.setBlockAndUpdate_(offX, offY, offZ, this.getGrowIntoState(state, level.random));
            }
        }
    }

    @Shadow
    protected abstract BlockState updateBodyAfterConvertedFromHead(BlockState blockState, BlockState blockState2);

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
        if (from == this.growthDirection.getOpposite() && !state.canSurvive_(level, x, y, z)) {
            level.scheduleTick(new BlockPos(x, y, z), this, 1);
        }
        if (from != this.growthDirection || !fromState.is(this) && !fromState.is(this.getBodyBlock())) {
            if (this.scheduleFluidTicks) {
                level.scheduleTick(new BlockPos(x, y, z), Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
            return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
        }
        return this.updateBodyAfterConvertedFromHead(state, this.getBodyBlock().defaultBlockState());
    }
}
