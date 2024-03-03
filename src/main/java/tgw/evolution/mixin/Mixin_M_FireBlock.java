package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
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

import java.util.Map;
import java.util.Random;

@Mixin(FireBlock.class)
public abstract class Mixin_M_FireBlock extends BaseFireBlock {

    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final private Map<BlockState, VoxelShape> shapesCache;

    public Mixin_M_FireBlock(Properties properties, float f) {
        super(properties, f);
    }

    @Shadow
    private static int getFireTickDelay(Random random) {
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
        return level.getBlockState_(x, y - 1, z).isFaceSturdy_(level, x, y - 1, z, Direction.UP) ||
               this.isValidFireLocation(level, new BlockPos(x, y, z));
    }

    @Shadow
    protected abstract void checkBurnOut(Level level, BlockPos blockPos, int i, Random random, int j);

    @Shadow
    protected abstract int getFireOdds(LevelReader levelReader, BlockPos blockPos);

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
        return this.shapesCache.get(state.setValue(AGE, 0));
    }

    @Shadow
    protected abstract BlockState getStateWithAge(LevelAccessor levelAccessor, BlockPos blockPos, int i);

    @Shadow
    protected abstract boolean isNearRain(Level level, BlockPos blockPos);

    @Shadow
    protected abstract boolean isValidFireLocation(BlockGetter blockGetter, BlockPos blockPos);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        super.onPlace_(state, level, x, y, z, oldState, isMoving);
        level.scheduleTick(new BlockPos(x, y, z), this, getFireTickDelay(level.random));
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
        level.scheduleTick(pos, this, getFireTickDelay(level.random));
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (!state.canSurvive(level, pos)) {
                level.removeBlock(pos, false);
            }
            BlockState blockState2 = level.getBlockState(pos.below());
            boolean bl = blockState2.is(level.dimensionType().infiniburn());
            int i = state.getValue(AGE);
            if (!bl && level.isRaining() && this.isNearRain(level, pos) && random.nextFloat() < 0.2F + i * 0.03F) {
                level.removeBlock(pos, false);
            }
            else {
                int j = Math.min(15, i + random.nextInt(3) / 2);
                if (i != j) {
                    state = state.setValue(AGE, j);
                    level.setBlock(pos, state, 4);
                }
                if (!bl) {
                    if (!this.isValidFireLocation(level, pos)) {
                        BlockPos blockPos2 = pos.below();
                        if (!level.getBlockState(blockPos2).isFaceSturdy(level, blockPos2, Direction.UP) || i > 3) {
                            level.removeBlock(pos, false);
                        }
                        return;
                    }
                    if (i == 15 && random.nextInt(4) == 0 && !this.canBurn(level.getBlockState(pos.below()))) {
                        level.removeBlock(pos, false);
                        return;
                    }
                }
                boolean bl2 = level.isHumidAt(pos);
                int k = bl2 ? -50 : 0;
                this.checkBurnOut(level, pos.east(), 300 + k, random, i);
                this.checkBurnOut(level, pos.west(), 300 + k, random, i);
                this.checkBurnOut(level, pos.below(), 250 + k, random, i);
                this.checkBurnOut(level, pos.above(), 250 + k, random, i);
                this.checkBurnOut(level, pos.north(), 300 + k, random, i);
                this.checkBurnOut(level, pos.south(), 300 + k, random, i);
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                for (int l = -1; l <= 1; ++l) {
                    for (int m = -1; m <= 1; ++m) {
                        for (int n = -1; n <= 4; ++n) {
                            if (l != 0 || n != 0 || m != 0) {
                                int o = 100;
                                if (n > 1) {
                                    o += (n - 1) * 100;
                                }
                                mutableBlockPos.setWithOffset(pos, l, n, m);
                                int p = this.getFireOdds(level, mutableBlockPos);
                                if (p > 0) {
                                    int q = (p + 40 + level.getDifficulty().getId() * 7) / (i + 30);
                                    if (bl2) {
                                        q /= 2;
                                    }
                                    if (q > 0 &&
                                        random.nextInt(o) <= q &&
                                        (!level.isRaining() || !this.isNearRain(level, mutableBlockPos))) {
                                        int r = Math.min(15, i + random.nextInt(5) / 4);
                                        level.setBlock(mutableBlockPos, this.getStateWithAge(level, mutableBlockPos, r), 3);
                                    }
                                }
                            }
                        }
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
        return this.canSurvive_(state, level, x, y, z) ?
               this.getStateWithAge(level, new BlockPos(x, y, z), state.getValue(AGE)) :
               Blocks.AIR.defaultBlockState();
    }
}
