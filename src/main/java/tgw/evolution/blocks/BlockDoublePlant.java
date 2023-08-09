package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.constants.BlockFlags;

import static tgw.evolution.init.EvolutionBStates.HALF;

public class BlockDoublePlant extends BlockBush {

    protected BlockDoublePlant(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public static BlockDoublePlant make(boolean drops) {
        if (drops) {
            return new BlockDoublePlant(Properties.of(Material.PLANT).noCollission().strength(0.0F).sound(SoundType.GRASS));
        }
        return new BlockDoublePlant(Properties.of(Material.PLANT).noCollission().strength(0.0F).sound(SoundType.GRASS).noDrops());
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader world, int x, int y, int z) {
        if (state.getBlock() != this) {
            return super.canSurvive_(state, world, x, y, z);
        }
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return super.canSurvive_(state, world, x, y, z);
        }
        BlockState stateBelow = world.getBlockState_(x, y - 1, z);
        return stateBelow.getBlock() == this && stateBelow.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XZ;
    }

    @Override
    public long getSeed_(BlockState state, int x, int y, int z) {
        return Mth.getSeed(x, state.getValue(HALF) == DoubleBlockHalf.LOWER ? y : y - 1, z);
    }

    @Override
    public @Nullable BlockState getStateForPlacement_(Level level,
                                                      int x,
                                                      int y,
                                                      int z,
                                                      Player player,
                                                      InteractionHand hand,
                                                      BlockHitResult hitResult) {
        return y < level.dimensionType().logicalHeight() - 1 &&
               level.getBlockState_(x, y + 1, z).canBeReplaced_(level, x, y, z, player, hand, hitResult) ?
               super.getStateForPlacement_(level, x, y, z, player, hand, hitResult) :
               null;
    }

    @Override
    public void playerDestroy_(Level level, Player player, int x, int y, int z, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy_(level, player, x, y, z, Blocks.AIR.defaultBlockState(), te, stack);
    }

    @Override
    public void playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player) {
//        DoubleBlockHalf half = state.getValue(HALF);
//        int otherY = half == DoubleBlockHalf.LOWER ? y + 1 : y - 1;
//        BlockState stateOfHalf = level.getBlockState_(x, otherY, z);
//        if (stateOfHalf.getBlock() == this && stateOfHalf.getValue(HALF) != half) {
//            level.setBlock(x, otherY, z, Blocks.AIR.defaultBlockState(), BlockFlags.NO_NEIGHBOR_DROPS | BlockFlags.NOTIFY | BlockFlags
//            .BLOCK_UPDATE);
//            level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, x, otherY, z, Block.getId(stateOfHalf));
//            if (!level.isClientSide && !player.isCreative()) {
//                dropResources(state, level, x, y, z, null, player, player.getMainHandItem());
//                dropResources(stateOfHalf, level, x, otherY, z, null, player, player.getMainHandItem());
//            }
//        }
        super.playerWillDestroy_(level, x, y, z, state, player);
    }

    @Override
    public void setPlacedBy_(Level level, int x, int y, int z, BlockState stateAtPos, Player player, ItemStack stack) {
        level.setBlock(new BlockPos(x, y + 1, z), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER),
                       BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
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
        DoubleBlockHalf half = state.getValue(HALF);
        if (from.getAxis() != Direction.Axis.Y ||
            half == DoubleBlockHalf.LOWER != (from == Direction.UP) ||
            fromState.getBlock() == this && fromState.getValue(HALF) != half) {
            return half == DoubleBlockHalf.LOWER && from == Direction.DOWN && !state.canSurvive_(level, x, y, z) ?
                   Blocks.AIR.defaultBlockState() :
                   super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
        }
        return Blocks.AIR.defaultBlockState();
    }
}
