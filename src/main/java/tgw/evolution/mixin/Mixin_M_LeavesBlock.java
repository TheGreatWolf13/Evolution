package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(LeavesBlock.class)
public abstract class Mixin_M_LeavesBlock extends Block {

    @Shadow @Final public static IntegerProperty DISTANCE;
    @Shadow @Final public static BooleanProperty PERSISTENT;

    public Mixin_M_LeavesBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static int getDistanceAt(BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static BlockState updateDistance(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (level.isRainingAt(new BlockPos(x, y + 1, z))) {
            if (random.nextInt(15) == 1) {
                BlockState stateBelow = level.getBlockState_(x, y - 1, z);
                if (!stateBelow.canOcclude() || !stateBelow.isFaceSturdy_(level, x, y - 1, z, Direction.UP)) {
                    level.addParticle(ParticleTypes.DRIPPING_WATER, x + random.nextDouble(), y - 0.05, z + random.nextDouble(), 0, 0, 0);
                }
            }
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getBlockSupportShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return Shapes.empty();
    }

    @Override
    @Overwrite
    @DeleteMethod
    public int getLightBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getLightBlock_(BlockState state, BlockGetter level, int x, int y, int z) {
        return 1;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (!state.getValue(PERSISTENT) && state.getValue(DISTANCE) == 7) {
            dropResources(state, level, new BlockPos(x, y, z));
            level.removeBlock_(x, y, z, false);
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
        level.setBlockAndUpdate_(x, y, z, updateDistance(state, level, new BlockPos(x, y, z)));
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
        int i = getDistanceAt(fromState) + 1;
        if (i != 1 || state.getValue(DISTANCE) != i) {
            level.scheduleTick(new BlockPos(x, y, z), this, 1);
        }
        return state;
    }
}
