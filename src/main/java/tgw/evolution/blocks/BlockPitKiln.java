package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.ItemClayMolded;
import tgw.evolution.items.ItemLog;
import tgw.evolution.util.DirectionDiagonal;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.Time;

import javax.annotation.Nullable;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.LAYERS_0_16;

public class BlockPitKiln extends Block implements IReplaceable {

    public BlockPitKiln() {
        super(Block.Properties.create(Material.ORGANIC).harvestLevel(HarvestLevel.UNBREAKABLE).tickRandomly());
        this.setDefaultState(this.getDefaultState().with(LAYERS_0_16, 0));
    }

    public static boolean canBurn(World world, BlockPos pos) {
        for (Direction dir : MathHelper.DIRECTIONS_HORIZONTAL) {
            if (!checkDirection(world, pos, dir)) {
                return false;
            }
        }
        return world.getBlockState(pos.up()).getBlock() == EvolutionBlocks.FIRE.get();
    }

    private static boolean checkDirection(World world, BlockPos pos, Direction direction) {
        return Block.hasSolidSide(world.getBlockState(pos.offset(direction)), world, pos.offset(direction), direction.getOpposite());
    }

    private static boolean manageStack(TEPitKiln tile, ItemStack handStack, PlayerEntity player, DirectionDiagonal direction) {
        if (tile.getStack(direction).isEmpty() &&
            !tile.isSingle() &&
            handStack.getItem() instanceof ItemClayMolded &&
            !((ItemClayMolded) handStack.getItem()).single) {
            tile.setStack(handStack, direction);
            tile.markDirty();
            return true;
        }
        if (!tile.getStack(direction).isEmpty()) {
            if (!tile.getWorld().isRemote && !player.inventory.addItemStackToInventory(tile.getStack(direction))) {
                BlockUtils.dropItemStack(tile.getWorld(), tile.getPos(), tile.getStack(direction));
            }
            tile.setStack(ItemStack.EMPTY, direction);
            tile.markDirty();
            tile.checkEmpty();
            return true;
        }
        return false;
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEPitKiln();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LAYERS_0_16);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(EvolutionItems.straw.get(), MathHelper.clamp(state.get(LAYERS_0_16), 0, 8));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(EvolutionItems.straw.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return EvolutionHitBoxes.PIT_KILN[state.get(LAYERS_0_16)];
    }

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        if (state.get(LAYERS_0_16) == 0) {
            return SoundType.STONE;
        }
        if (state.get(LAYERS_0_16) <= 8) {
            return SoundType.PLANT;
        }
        return SoundType.WOOD;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean isFireSource(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return side == Direction.UP && state.get(LAYERS_0_16) == 16;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return state.get(LAYERS_0_16) < 13;
    }

    @Override
    public boolean isSolid(BlockState state) {
        if (state.get(LAYERS_0_16) == 0 || state.get(LAYERS_0_16) == 16) {
            return false;
        }
        return super.isSolid(state);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos posDown = pos.down();
        BlockState down = worldIn.getBlockState(posDown);
        return Block.hasSolidSide(down, worldIn, posDown, Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isRemote) {
            if (!state.isValidPosition(world, pos)) {
                BlockUtils.dropItemStack(world, pos, this.getDrops(world, pos, state));
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        int layers = state.get(LAYERS_0_16);
        TEPitKiln tile = (TEPitKiln) world.getTileEntity(pos);
        if (tile == null) {
            return false;
        }
        ItemStack stack = player.getHeldItem(handIn);
        if (layers == 0) {
            if (stack.getItem() == EvolutionItems.straw.get() && !tile.hasFinished()) {
                world.setBlockState(pos, state.with(LAYERS_0_16, 1));
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                world.playSound(player, pos, SoundEvents.BLOCK_COMPOSTER_FILL_SUCCESS, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }
            if (tile.isSingle()) {
                return manageStack(tile, stack, player, DirectionDiagonal.NORTH_WEST);
            }
            int x = MathHelper.getIndex(2, 0, 16, (hit.getHitVec().x - pos.getX()) * 16);
            int z = MathHelper.getIndex(2, 0, 16, (hit.getHitVec().z - pos.getZ()) * 16);
            return manageStack(tile, stack, player, MathHelper.DIAGONALS[z][x]);
        }
        if (layers < 8) {
            if (stack.getItem() == EvolutionItems.straw.get()) {
                world.setBlockState(pos, state.with(LAYERS_0_16, layers + 1));
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                world.playSound(player, pos, SoundEvents.BLOCK_COMPOSTER_FILL_SUCCESS, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }
            return false;
        }
        if (layers != 16 && stack.getItem() instanceof ItemLog) {
            world.setBlockState(pos, state.with(LAYERS_0_16, layers + 1));
            tile.setLog(layers - 8, ((ItemLog) stack.getItem()).variant.getId());
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
            return true;
        }
        return false;
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if (worldIn.isRemote) {
            return;
        }
        int layers = state.get(LAYERS_0_16);
        if (layers == 0) {
            return;
        }
        if (layers <= 8) {
            worldIn.setBlockState(pos, state.with(LAYERS_0_16, layers - 1));
            ItemStack stack = new ItemStack(EvolutionItems.straw.get());
            if (!player.inventory.addItemStackToInventory(stack)) {
                BlockUtils.dropItemStack(worldIn, pos, stack);
            }
            return;
        }
        TEPitKiln tile = (TEPitKiln) worldIn.getTileEntity(pos);
        worldIn.setBlockState(pos, state.with(LAYERS_0_16, layers - 1));
        ItemStack stack = tile.getLogStack(layers - 9);
        tile.setLog(layers - 9, (byte) -1);
        tile.markDirty();
        if (!player.inventory.addItemStackToInventory(stack)) {
            BlockUtils.dropItemStack(worldIn, pos, stack);
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            ((TEPitKiln) worldIn.getTileEntity(pos)).onRemoved();
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (worldIn.isRemote) {
            return;
        }
        TEPitKiln tile = (TEPitKiln) worldIn.getTileEntity(pos);
        if (canBurn(worldIn, pos)) {
            if (worldIn.getDayTime() > tile.getTimeStart() + 8 * Time.HOUR_IN_TICKS) {
                worldIn.setBlockState(pos, state.with(LAYERS_0_16, 0));
                tile.finish();
            }
        }
        else {
            tile.reset();
        }
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return state.get(LAYERS_0_16) == 16;
    }
}
