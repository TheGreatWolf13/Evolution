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
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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

public class BlockPitKiln extends BlockGeneric implements IReplaceable {

    public BlockPitKiln() {
        super(Properties.of(Material.GRASS).harvestLevel(HarvestLevel.UNBREAKABLE).randomTicks().noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_0_16, 0));
    }

    public static boolean canBurn(World world, BlockPos pos) {
        for (Direction dir : MathHelper.DIRECTIONS_HORIZONTAL) {
            if (!checkDirection(world, pos, dir)) {
                return false;
            }
        }
        return world.getBlockState(pos.above()).getBlock() == EvolutionBlocks.FIRE.get();
    }

    private static boolean checkDirection(World world, BlockPos pos, Direction direction) {
        return BlockUtils.hasSolidSide(world, pos.relative(direction), direction.getOpposite());
    }

    private static ActionResultType manageStack(TEPitKiln tile, ItemStack handStack, PlayerEntity player, DirectionDiagonal direction) {
        if (tile.getStack(direction).isEmpty() &&
            !tile.isSingle() &&
            handStack.getItem() instanceof ItemClayMolded &&
            !((ItemClayMolded) handStack.getItem()).single) {
            tile.setStack(handStack, direction);
            tile.setChanged();
            return ActionResultType.SUCCESS;
        }
        if (!tile.getStack(direction).isEmpty()) {
            if (!tile.getLevel().isClientSide && !player.inventory.add(tile.getStack(direction))) {
                BlockUtils.dropItemStack(tile.getLevel(), tile.getBlockPos(), tile.getStack(direction));
            }
            tile.setStack(ItemStack.EMPTY, direction);
            tile.setChanged();
            tile.checkEmpty();
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void attack(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if (worldIn.isClientSide) {
            return;
        }
        int layers = state.getValue(LAYERS_0_16);
        if (layers == 0) {
            return;
        }
        if (layers <= 8) {
            worldIn.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, layers - 1));
            ItemStack stack = new ItemStack(EvolutionItems.straw.get());
            if (!player.inventory.add(stack)) {
                BlockUtils.dropItemStack(worldIn, pos, stack);
            }
            return;
        }
        TEPitKiln tile = (TEPitKiln) worldIn.getBlockEntity(pos);
        worldIn.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, layers - 1));
        ItemStack stack = tile.getLogStack(layers - 9);
        tile.setLog(layers - 9, (byte) -1);
        tile.setChanged();
        if (!player.inventory.add(stack)) {
            BlockUtils.dropItemStack(worldIn, pos, stack);
        }
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return BlockUtils.hasSolidSide(world, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LAYERS_0_16);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEPitKiln();
    }

    @Override
    public NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.of(new ItemStack(EvolutionItems.straw.get(), MathHelper.clamp(state.getValue(LAYERS_0_16), 0, 8)));
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.45f;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(EvolutionItems.straw.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return EvolutionHitBoxes.PIT_KILN[state.getValue(LAYERS_0_16)];
    }

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        if (state.getValue(LAYERS_0_16) == 0) {
            return SoundType.STONE;
        }
        if (state.getValue(LAYERS_0_16) <= 8) {
            return SoundType.GRASS;
        }
        return SoundType.WOOD;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean isFireSource(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
        return side == Direction.UP && state.getValue(LAYERS_0_16) == 16;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(LAYERS_0_16) == 16;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return state.getValue(LAYERS_0_16) < 13;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (!state.canSurvive(world, pos)) {
                for (ItemStack stack : this.getDrops(world, pos, state)) {
                    BlockUtils.dropItemStack(world, pos, stack);
                }
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            ((TEPitKiln) worldIn.getBlockEntity(pos)).onRemoved();
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        TEPitKiln tile = (TEPitKiln) world.getBlockEntity(pos);
        if (canBurn(world, pos)) {
            if (world.getDayTime() > tile.getTimeStart() + 8 * Time.HOUR_IN_TICKS) {
                world.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, 0));
                tile.finish();
            }
        }
        else {
            tile.reset();
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        int layers = state.getValue(LAYERS_0_16);
        TEPitKiln tile = (TEPitKiln) world.getBlockEntity(pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        ItemStack stack = player.getItemInHand(handIn);
        if (layers == 0) {
            if (stack.getItem() == EvolutionItems.straw.get() && !tile.hasFinished()) {
                world.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, 1));
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                world.playSound(player, pos, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResultType.SUCCESS;
            }
            if (tile.isSingle()) {
                return manageStack(tile, stack, player, DirectionDiagonal.NORTH_WEST);
            }
            int x = MathHelper.getIndex(2, 0, 16, (hit.getLocation().x - pos.getX()) * 16);
            int z = MathHelper.getIndex(2, 0, 16, (hit.getLocation().z - pos.getZ()) * 16);
            return manageStack(tile, stack, player, MathHelper.DIAGONALS[z][x]);
        }
        if (layers < 8) {
            if (stack.getItem() == EvolutionItems.straw.get()) {
                world.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, layers + 1));
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                world.playSound(player, pos, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        }
        if (layers != 16 && stack.getItem() instanceof ItemLog) {
            world.setBlockAndUpdate(pos, state.setValue(LAYERS_0_16, layers + 1));
            tile.setLog(layers - 8, ((ItemLog) stack.getItem()).variant.getId());
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            world.playSound(player, pos, SoundEvents.WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
