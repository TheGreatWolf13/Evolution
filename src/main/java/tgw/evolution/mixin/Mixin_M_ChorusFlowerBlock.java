package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;

@Mixin(ChorusFlowerBlock.class)
public abstract class Mixin_M_ChorusFlowerBlock extends Block {

    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final private ChorusPlantBlock plant;

    public Mixin_M_ChorusFlowerBlock(Properties properties) {
        super(properties);
    }

    @Contract(value = "_, _, _ -> _")
    @Shadow
    private static boolean allNeighborsEmpty(LevelReader levelReader,
                                             BlockPos blockPos,
                                             @Nullable Direction direction) {
        //noinspection Contract
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
        BlockState stateBelow = level.getBlockState_(x, y - 1, z);
        if (!stateBelow.is(this.plant) && !stateBelow.is(Blocks.END_STONE)) {
            if (!stateBelow.isAir()) {
                return false;
            }
            boolean bl = false;
            for (Direction direction : DirectionUtil.HORIZ_NESW) {
                BlockState stateAtSide = level.getBlockStateAtSide(x, y, z, direction);
                if (stateAtSide.is(this.plant)) {
                    if (bl) {
                        return false;
                    }
                    bl = true;
                }
                else if (!stateAtSide.isAir()) {
                    return false;
                }
            }
            return bl;
        }
        return true;
    }

    @Shadow
    protected abstract void placeDeadFlower(Level level, BlockPos blockPos);

    @Shadow
    protected abstract void placeGrownFlower(Level level, BlockPos blockPos, int i);

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
        if (y + 1 < level.getMaxBuildHeight() && level.isEmptyBlock_(x, y + 1, z)) {
            int age = state.getValue(AGE);
            if (age < 5) {
                boolean bottom = false;
                boolean veryBottom = false;
                BlockState stateBelow = level.getBlockState_(x, y - 1, z);
                int height;
                if (stateBelow.is(Blocks.END_STONE)) {
                    bottom = true;
                }
                else if (stateBelow.is(this.plant)) {
                    height = 1;
                    for (int k = 0; k < 4; ++k) {
                        BlockState stateVeryBelow = level.getBlockState_(x, y - height - 1, z);
                        if (!stateVeryBelow.is(this.plant)) {
                            if (stateVeryBelow.is(Blocks.END_STONE)) {
                                veryBottom = true;
                            }
                            break;
                        }
                        ++height;
                    }
                    if (height < 2 || height <= random.nextInt(veryBottom ? 5 : 4)) {
                        bottom = true;
                    }
                }
                else if (stateBelow.isAir()) {
                    bottom = true;
                }
                if (bottom && level.isEmptyBlock_(x, y + 2, z) && allNeighborsEmpty(level, new BlockPos(x, y + 1, z), null)) {
                    level.setBlock_(x, y, z, this.plant.getStateForPlacement(level, new BlockPos(x, y, z)), BlockFlags.BLOCK_UPDATE);
                    this.placeGrownFlower(level, new BlockPos(x, y + 1, z), age);
                }
                else if (age < 4) {
                    height = random.nextInt(4);
                    if (veryBottom) {
                        ++height;
                    }
                    boolean bl3 = false;
                    for (int l = 0; l < height; ++l) {
                        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                        int offX = x + dir.getStepX();
                        int offZ = z + dir.getStepZ();
                        if (level.isEmptyBlock_(offX, y, offZ) &&
                            level.isEmptyBlock_(offZ, y - 1, offZ)) {
                            BlockPos offPos = new BlockPos(offX, y, offZ);
                            if (allNeighborsEmpty(level, offPos, dir.getOpposite())) {
                                this.placeGrownFlower(level, offPos, age + 1);
                                bl3 = true;
                            }
                        }
                    }
                    if (bl3) {
                        level.setBlock_(x, y, z, this.plant.getStateForPlacement(level, new BlockPos(x, y, z)), BlockFlags.BLOCK_UPDATE);
                    }
                    else {
                        this.placeDeadFlower(level, new BlockPos(x, y, z));
                    }
                }
                else {
                    this.placeDeadFlower(level, new BlockPos(x, y, z));
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
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (!state.canSurvive_(level, x, y, z)) {
            level.destroyBlock_(x, y, z, true);
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
        if (from != Direction.UP && !state.canSurvive_(level, x, y, z)) {
            level.scheduleTick(new BlockPos(x, y, z), this, 1);
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
