package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.NutrientHelper;

import javax.annotation.Nullable;

import static tgw.evolution.init.EvolutionBStates.HALF;

public class BlockDoublePlant extends BlockBush {

    public BlockDoublePlant() {
        super(Properties.of(Material.PLANT).noCollission().strength(0.0F).sound(SoundType.GRASS));
        this.registerDefaultState(this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        if (state.getBlock() == EvolutionBlocks.TALLGRASS.get()) {
            return ItemStack.EMPTY;
        }
        return super.getDrops(world, pos, state);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualFlammability(state);
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XZ;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public long getSeed(BlockState state, BlockPos pos) {
        return MathHelper.getSeed(pos.getX(), pos.below(state.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        World world = context.getLevel();
        return pos.getY() < world.dimensionType().logicalHeight() - 1 && world.getBlockState(pos.above()).canBeReplaced(context) ?
               super.getStateForPlacement(context) :
               null;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        CapabilityChunkStorage.addElements(world.getChunkAt(pos), NutrientHelper.DECAY_TALL_GRASS);
    }

    @Override
    public void playerDestroy(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity tile, ItemStack stack) {
        super.playerDestroy(world, player, pos, Blocks.AIR.defaultBlockState(), tile, stack);
    }

    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        DoubleBlockHalf doubleblockhalf = state.getValue(HALF);
        BlockPos blockpos = doubleblockhalf == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.getBlock() == this && blockstate.getValue(HALF) != doubleblockhalf) {
            world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), BlockFlags.NO_NEIGHBOR_DROPS + BlockFlags.NOTIFY_AND_UPDATE);
            world.levelEvent(player, Constants.WorldEvents.BREAK_BLOCK_EFFECTS, blockpos, Block.getId(blockstate));
            if (!world.isClientSide && !player.isCreative()) {
                dropResources(state, world, pos, null, player, player.getMainHandItem());
                dropResources(blockstate, world, blockpos, null, player, player.getMainHandItem());
            }
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        world.setBlock(pos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER), BlockFlags.NOTIFY_AND_UPDATE);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        DoubleBlockHalf doubleblockhalf = state.getValue(HALF);
        if (facing.getAxis() != Direction.Axis.Y ||
            doubleblockhalf == DoubleBlockHalf.LOWER != (facing == Direction.UP) ||
            facingState.getBlock() == this && facingState.getValue(HALF) != doubleblockhalf) {
            return doubleblockhalf == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(world, currentPos) ?
                   Blocks.AIR.defaultBlockState() :
                   super.updateShape(state, facing, facingState, world, currentPos, facingPos);
        }
        return Blocks.AIR.defaultBlockState();
    }
}
