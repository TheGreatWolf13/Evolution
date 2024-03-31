package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(DoublePlantBlock.class)
public abstract class Mixin_M_DoublePlantBlock extends BushBlock {

    @Shadow @Final public static EnumProperty<DoubleBlockHalf> HALF;

    public Mixin_M_DoublePlantBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void preventCreativeDropFromBottomPart(Level level, BlockPos pos, BlockState state, Player player) {
        Evolution.deprecatedMethod();
        BlockUtils.preventCreativeDropFromBottomPart(level, pos.getX(), pos.getY(), pos.getZ(), state, player);
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
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return super.canSurvive_(state, level, x, y, z);
        }
        BlockState stateBelow = level.getBlockState_(x, y - 1, z);
        return stateBelow.is(this) && stateBelow.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public long getSeed(BlockState blockState, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public long getSeed_(BlockState state, int x, int y, int z) {
        return Mth.getSeed(x, state.getValue(HALF) == DoubleBlockHalf.LOWER ? y : y - 1, z);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        throw new AbstractMethodError();
    }

    @Override
    public void playerDestroy_(Level level, Player player, int x, int y, int z, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy_(level, player, x, y, z, Blocks.AIR.defaultBlockState(), te, stack);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player, Direction face, double hitX, double hitY, double hitZ) {
        if (!level.isClientSide) {
            if (player.isCreative()) {
                BlockUtils.preventCreativeDropFromBottomPart(level, x, y, z, state, player);
            }
            else {
                BlockUtils.dropResources(state, level, x, y, z, null, player, player.getMainHandItem());
            }
        }
        return super.playerWillDestroy_(level, x, y, z, state, player, face, hitX, hitY, hitZ);
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
        DoubleBlockHalf doubleBlockHalf = state.getValue(HALF);
        if (from.getAxis() == Direction.Axis.Y &&
            doubleBlockHalf == DoubleBlockHalf.LOWER == (from == Direction.UP) &&
            (!fromState.is(this) || fromState.getValue(HALF) == doubleBlockHalf)) {
            return Blocks.AIR.defaultBlockState();
        }
        return doubleBlockHalf == DoubleBlockHalf.LOWER && from == Direction.DOWN && !state.canSurvive_(level, x, y, z) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
