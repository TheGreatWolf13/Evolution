package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

@Mixin(FrostedIceBlock.class)
public abstract class Mixin_M_FrostedIceBlock extends IceBlock {

    @Shadow @Final public static IntegerProperty AGE;

    public Mixin_M_FrostedIceBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract boolean fewerNeigboursThan(BlockGetter blockGetter, BlockPos blockPos, int i);

    @Override
    @Overwrite
    @DeleteMethod
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void neighborChanged_(BlockState state,
                                 Level level,
                                 int x,
                                 int y,
                                 int z,
                                 Block oldBlock,
                                 int fromX,
                                 int fromY,
                                 int fromZ,
                                 boolean isMoving) {
        if (oldBlock.defaultBlockState().is(this)) {
            BlockPos pos = new BlockPos(x, y, z);
            if (this.fewerNeigboursThan(level, pos, 2)) {
                this.melt(state, level, pos);
            }
        }
        super.neighborChanged_(state, level, x, y, z, oldBlock, fromX, fromY, fromZ, isMoving);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        this.tick(state, level, new BlockPos(x, y, z), random);
    }

    @Shadow
    protected abstract boolean slightlyMelt(BlockState blockState, Level level, BlockPos blockPos);

    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        BlockPos pos = new BlockPos(x, y, z);
        if ((random.nextInt(3) == 0 || this.fewerNeigboursThan(level, pos, 4)) &&
            level.getMaxLocalRawBrightness(pos) > 11 - state.getValue(AGE) - state.getLightBlock(level, pos) &&
            this.slightlyMelt(state, level, pos)) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (Direction direction : DirectionUtil.ALL) {
                mutableBlockPos.setWithOffset(pos, direction);
                BlockState blockState2 = level.getBlockState(mutableBlockPos);
                if (blockState2.is(this) && !this.slightlyMelt(blockState2, level, mutableBlockPos)) {
                    level.scheduleTick(mutableBlockPos, this, Mth.nextInt(random, 20, 40));
                }
            }
        }
        else {
            level.scheduleTick(pos, this, Mth.nextInt(random, 20, 40));
        }
    }
}
