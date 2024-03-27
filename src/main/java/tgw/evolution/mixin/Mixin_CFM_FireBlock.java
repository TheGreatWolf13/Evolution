package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2IHashMap;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Map;
import java.util.Random;

@Mixin(FireBlock.class)
public abstract class Mixin_CFM_FireBlock extends BaseFireBlock {

    @Shadow @Final public static BooleanProperty UP;
    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final public static BooleanProperty NORTH;
    @Shadow @Final public static BooleanProperty EAST;
    @Shadow @Final public static BooleanProperty SOUTH;
    @Shadow @Final public static BooleanProperty WEST;
    @Mutable @Shadow @Final @RestoreFinal private Object2IntMap<Block> burnOdds;
    @Unique private final O2OMap<BlockState, VoxelShape> cache;
    @Mutable @Shadow @Final @RestoreFinal private Object2IntMap<Block> flameOdds;
    @Shadow @Final @DeleteField private Map<BlockState, VoxelShape> shapesCache;

    @ModifyConstructor
    public Mixin_CFM_FireBlock(BlockBehaviour.Properties properties) {
        super(properties, 1.0F);
        this.flameOdds = new O2IHashMap<>();
        this.burnOdds = new O2IHashMap();
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(UP, false));
        O2OMap<BlockState, VoxelShape> cache = new O2OHashMap<>();
        OList<BlockState> states = this.stateDefinition.getPossibleStates_();
        for (int i = 0, len = states.size(); i < len; ++i) {
            BlockState state = states.get(i);
            if (state.getValue(AGE) == 0) {
                cache.put(state, FireBlock.calculateShape(state));
            }
        }
        cache.trim();
        this.cache = cache;
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
        return this.cache.get(state.setValue(AGE, 0));
    }

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
            if (!state.canSurvive_(level, x, y, z)) {
                level.removeBlock_(x, y, z, false);
            }
            BlockState blockState2 = level.getBlockState_(x, y - 1, z);
            boolean bl = blockState2.is(level.dimensionType().infiniburn());
            int i = state.getValue(AGE);
            if (!bl && level.isRaining() && this.isNearRain(level, pos) && random.nextFloat() < 0.2F + i * 0.03F) {
                level.removeBlock_(x, y, z, false);
            }
            else {
                int j = Math.min(15, i + random.nextInt(3) / 2);
                if (i != j) {
                    state = state.setValue(AGE, j);
                    level.setBlock_(x, y, z, state, BlockFlags.NO_RERENDER);
                }
                if (!bl) {
                    if (!this.isValidFireLocation(level, pos)) {
                        if (!level.getBlockState_(x, y - 1, z).isFaceSturdy_(level, x, y - 1, z, Direction.UP) || i > 3) {
                            level.removeBlock_(x, y, z, false);
                        }
                        return;
                    }
                    if (i == 15 && random.nextInt(4) == 0 && !this.canBurn(level.getBlockState_(x, y - 1, z))) {
                        level.removeBlock_(x, y, z, false);
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
                                        level.setBlock_(mutableBlockPos.getX(), mutableBlockPos.getY(), mutableBlockPos.getZ(), this.getStateWithAge(level, mutableBlockPos, r), BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
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

    @Shadow
    protected abstract void checkBurnOut(Level level, BlockPos blockPos, int i, Random random, int j);

    @Shadow
    protected abstract int getFireOdds(LevelReader levelReader, BlockPos blockPos);

    @Shadow
    protected abstract BlockState getStateWithAge(LevelAccessor levelAccessor, BlockPos blockPos, int i);

    @Shadow
    protected abstract boolean isNearRain(Level level, BlockPos blockPos);

    @Shadow
    protected abstract boolean isValidFireLocation(BlockGetter blockGetter, BlockPos blockPos);
}
