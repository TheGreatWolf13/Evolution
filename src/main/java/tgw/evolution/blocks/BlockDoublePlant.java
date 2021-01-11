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
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.NutrientHelper;

import javax.annotation.Nullable;

import static tgw.evolution.init.EvolutionBStates.HALF;

public class BlockDoublePlant extends BlockBush {

    public BlockDoublePlant() {
        super(Block.Properties.create(Material.TALL_PLANTS).doesNotBlockMovement().hardnessAndResistance(0.0F).sound(SoundType.PLANT));
        this.setDefaultState(this.stateContainer.getBaseState().with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
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
    public Block.OffsetType getOffsetType() {
        return Block.OffsetType.XZ;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public long getPositionRandom(BlockState state, BlockPos pos) {
        return MathHelper.getCoordinateRandom(pos.getX(), pos.down(state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getPos();
        return pos.getY() < context.getWorld().getDimension().getHeight() - 1 && context.getWorld().getBlockState(pos.up()).isReplaceable(context) ?
               super.getStateForPlacement(context) :
               null;
    }

    @Override
    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, Blocks.AIR.getDefaultState(), te, stack);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        if (state.get(HALF) != DoubleBlockHalf.UPPER) {
            return super.isValidPosition(state, world, pos);
        }
        BlockState blockstate = world.getBlockState(pos.down());
        if (state.getBlock() != this) {
            //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the
            // pre-check.
            return super.isValidPosition(state, world, pos);
        }
        return blockstate.getBlock() == this && blockstate.get(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        DoubleBlockHalf doubleblockhalf = state.get(HALF);
        BlockPos blockpos = doubleblockhalf == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
        BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.getBlock() == this && blockstate.get(HALF) != doubleblockhalf) {
            world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), BlockFlags.NO_NEIGHBOR_DROPS + BlockFlags.NOTIFY_AND_UPDATE);
            world.playEvent(player, Constants.WorldEvents.BREAK_BLOCK_EFFECTS, blockpos, Block.getStateId(blockstate));
            if (!world.isRemote && !player.isCreative()) {
                spawnDrops(state, world, pos, null, player, player.getHeldItemMainhand());
                spawnDrops(blockstate, world, blockpos, null, player, player.getHeldItemMainhand());
            }
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        world.setBlockState(pos.up(), this.getDefaultState().with(HALF, DoubleBlockHalf.UPPER), BlockFlags.NOTIFY_AND_UPDATE);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        ChunkStorageCapability.addElements(world.getChunkAt(pos), NutrientHelper.DECAY_TALL_GRASS);
    }

    @Override
    public BlockState updatePostPlacement(BlockState state,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld world,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        DoubleBlockHalf doubleblockhalf = state.get(HALF);
        if (facing.getAxis() != Direction.Axis.Y ||
            doubleblockhalf == DoubleBlockHalf.LOWER != (facing == Direction.UP) ||
            facingState.getBlock() == this && facingState.get(HALF) != doubleblockhalf) {
            return doubleblockhalf == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.isValidPosition(world, currentPos) ?
                   Blocks.AIR.getDefaultState() :
                   super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
        }
        return Blocks.AIR.getDefaultState();
    }
}
