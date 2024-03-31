package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
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

@Mixin(PistonHeadBlock.class)
public abstract class Mixin_M_PistonHeadBlock extends DirectionalBlock {

    @Shadow @Final public static BooleanProperty SHORT;
    @Shadow @Final private static VoxelShape[] SHAPES_SHORT;
    @Shadow @Final private static VoxelShape[] SHAPES_LONG;

    public Mixin_M_PistonHeadBlock(Properties properties) {
        super(properties);
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
        Direction facing = state.getValue(FACING);
        BlockState stateAtSide = level.getBlockStateAtSide(x, y, z, facing.getOpposite());
        return this.isFittingBase(state, stateAtSide) || stateAtSide.is(Blocks.MOVING_PISTON) && stateAtSide.getValue(FACING) == facing;
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
        return (state.getValue(SHORT) ? SHAPES_SHORT : SHAPES_LONG)[state.getValue(FACING).ordinal()];
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
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
        if (state.canSurvive_(level, x, y, z)) {
            Direction opp = state.getValue(FACING).getOpposite();
            int oppX = x + opp.getStepX();
            int oppY = y + opp.getStepY();
            int oppZ = z + opp.getStepZ();
            level.getBlockState_(oppX, oppY, oppZ).neighborChanged_(level, oppX, oppY, oppZ, oldBlock, fromX, fromY, fromZ, false);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            super.onRemove_(state, level, x, y, z, newState, isMoving);
            Direction offset = state.getValue(FACING).getOpposite();
            int offX = x + offset.getStepX();
            int offY = y + offset.getStepY();
            int offZ = z + offset.getStepZ();
            if (this.isFittingBase(state, level.getBlockState_(offX, offY, offZ))) {
                level.destroyBlock_(offX, offY, offZ, true);
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
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player, Direction face, double hitX, double hitY, double hitZ) {
        if (!level.isClientSide && player.getAbilities().instabuild) {
            Direction dir = state.getValue(FACING).getOpposite();
            int otherX = x + dir.getStepX();
            int otherY = y + dir.getStepY();
            int otherZ = z + dir.getStepZ();
            if (this.isFittingBase(state, level.getBlockState_(otherX, otherY, otherZ))) {
                level.destroyBlock_(otherX, otherY, otherZ, false);
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
        return from.getOpposite() == state.getValue(FACING) && !state.canSurvive_(level, x, y, z) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

    @Shadow
    protected abstract boolean isFittingBase(BlockState blockState, BlockState blockState2);
}
