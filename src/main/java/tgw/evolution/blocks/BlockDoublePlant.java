package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.NutrientHelper;
import tgw.evolution.util.constants.BlockFlags;

import javax.annotation.Nullable;

import static tgw.evolution.init.EvolutionBStates.HALF;

public class BlockDoublePlant extends BlockBush {

    public BlockDoublePlant() {
        super(Properties.of(Material.PLANT).noCollission().strength(0.0F).sound(SoundType.GRASS));
        this.registerDefaultState(this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return super.canSurvive(state, world, pos);
        }
        BlockState blockstate = world.getBlockState(pos.below());
        if (state.getBlock() != this) {
            //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the
            // pre-check.
            return super.canSurvive(state, world, pos);
        }
        return blockstate.getBlock() == this && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    public NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() == EvolutionBlocks.TALLGRASS.get()) {
            return NonNullList.of(ItemStack.EMPTY);
        }
        return super.getDrops(level, pos, state);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualFlammability(state);
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XZ;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public long getSeed(BlockState state, BlockPos pos) {
        return Mth.getSeed(pos.getX(), pos.below(state.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        return pos.getY() < level.dimensionType().logicalHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context) ?
               super.getStateForPlacement(context) :
               null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        CapabilityChunkStorage.addElements(level.getChunkAt(pos), NutrientHelper.DECAY_TALL_GRASS);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity tile, ItemStack stack) {
        super.playerDestroy(level, player, pos, Blocks.AIR.defaultBlockState(), tile, stack);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        DoubleBlockHalf doubleblockhalf = state.getValue(HALF);
        BlockPos blockpos = doubleblockhalf == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.getBlock() == this && blockstate.getValue(HALF) != doubleblockhalf) {
            level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), BlockFlags.NO_NEIGHBOR_DROPS + BlockFlags.NOTIFY_AND_UPDATE);
            level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, blockpos, Block.getId(blockstate));
            if (!level.isClientSide && !player.isCreative()) {
                dropResources(state, level, pos, null, player, player.getMainHandItem());
                dropResources(blockstate, level, blockpos, null, player, player.getMainHandItem());
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        level.setBlock(pos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER), BlockFlags.NOTIFY_AND_UPDATE);
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction facing,
                                  BlockState facingState,
                                  LevelAccessor level,
                                  BlockPos currentPos,
                                  BlockPos facingPos) {
        DoubleBlockHalf doubleblockhalf = state.getValue(HALF);
        if (facing.getAxis() != Direction.Axis.Y ||
            doubleblockhalf == DoubleBlockHalf.LOWER != (facing == Direction.UP) ||
            facingState.getBlock() == this && facingState.getValue(HALF) != doubleblockhalf) {
            return doubleblockhalf == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(level, currentPos) ?
                   Blocks.AIR.defaultBlockState() :
                   super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        }
        return Blocks.AIR.defaultBlockState();
    }
}
