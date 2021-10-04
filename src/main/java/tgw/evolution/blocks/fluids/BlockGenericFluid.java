package tgw.evolution.blocks.fluids;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.*;
import tgw.evolution.blocks.tileentities.TELiquid;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.util.DirectionList;
import tgw.evolution.util.DirectionUtil;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static net.minecraft.fluid.FlowingFluid.FALLING;
import static tgw.evolution.init.EvolutionBStates.LEVEL_1_8;

public abstract class BlockGenericFluid extends BlockMass implements IBlockFluidContainer, IReplaceable {
    public static final IntegerProperty LEVEL = LEVEL_1_8;
    public static final BooleanProperty FULL = EvolutionBStates.FULL;
    private final List<FluidState> fluidStateCache;
    private final Supplier<? extends FluidGeneric> supplier;
    private boolean fluidStateCacheInitialized;

    public BlockGenericFluid(Supplier<? extends FluidGeneric> fluid, Properties properties, int mass) {
        super(properties, mass);
        this.fluidStateCache = Lists.newArrayList();
        this.registerDefaultState(this.defaultBlockState().setValue(LEVEL, 8).setValue(FULL, true));
        this.supplier = fluid;
    }

    public static int getFluidAmount(World world, BlockPos pos, BlockState state, FluidState fluid) {
        if (fluid.isEmpty()) {
            return 0;
        }
        Block block = state.getBlock();
        if (block instanceof BlockGenericFluid) {
            int layers = fluid.getValue(LEVEL_1_8);
            int amount = 12_500 * layers;
            if (!fluid.getValue(FALLING)) {
                TileEntity tile = world.getBlockEntity(pos);
                if (tile instanceof TELiquid) {
                    amount -= ((TELiquid) tile).getMissingLiquid();
                }
                else {
                    Evolution.LOGGER.warn("Invalid tile entity for block at {}: {}", pos, tile);
                }
            }
            return amount;
        }
        if (block instanceof IFluidLoggable) {
            return ((IFluidLoggable) block).getCurrentAmount(world, pos, state);
        }
        return 0;
    }

    public static void place(World world, BlockPos pos, FluidGeneric fluid, int amount) {
        fluid.setBlockState(world, pos, amount);
    }

    private static void triggerMixEffects(IWorld world, BlockPos pos) {
        world.levelEvent(Constants.WorldEvents.LAVA_EXTINGUISH, pos, 0);
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        if (!state.getValue(FULL)) {
            return true;
        }
        return state.getValue(LEVEL) < 8;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, FULL);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TELiquid();
    }

    @Override
    public int getAmountRemoved(World world, BlockPos pos, int maxAmount) {
        FluidState stateAtPos = world.getFluidState(pos);
        int amountAtPos = FluidGeneric.getFluidAmount(world, pos, stateAtPos);
        int amountRemoved = MathHelper.clampMax(amountAtPos, maxAmount);
        amountAtPos -= amountRemoved;
        this.getFluid().setBlockState(world, pos, amountAtPos);
        for (Direction dir : DirectionUtil.ALL) {
            BlockPos offsetPos = pos.relative(dir);
            BlockState stateAtOffset = world.getBlockState(offsetPos);
            if (stateAtOffset.getBlock() instanceof IFluidLoggable) {
                BlockUtils.scheduleBlockTick(world, offsetPos, 2);
            }
        }
        return amountRemoved;
    }

    @Override
    public NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.emptyList();
    }

    @Override
    public FluidGeneric getFluid(IBlockReader world, BlockPos pos) {
        return this.supplier.get();
    }

    public FluidGeneric getFluid() {
        return this.supplier.get();
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (!this.fluidStateCacheInitialized) {
            this.initFluidStateCache();
        }
        int level = state.getValue(LEVEL) - 1;
        if (state.getValue(FULL)) {
            level += 8;
        }
        return this.fluidStateCache.get(level);
    }

    @Override
    public int getMass(World world, BlockPos pos, BlockState state) {
        return this.getMass(state);
    }

    @Override
    public int getMass(BlockState state) {
        return state.getValue(LEVEL) * this.getBaseMass() / 8;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return !state.getValue(FULL);
    }

    protected synchronized void initFluidStateCache() {
        if (!this.fluidStateCacheInitialized) {
            for (int i = 1; i <= 8; i++) {
                this.fluidStateCache.add(this.getFluid().getFlowing(i, false));
            }
            for (int i = 1; i <= 8; i++) {
                this.fluidStateCache.add(this.getFluid().getFlowing(i, true));
            }
            this.fluidStateCacheInitialized = true;
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader world, BlockPos pos, PathType type) {
        return !this.getFluid().is(FluidTags.LAVA);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (this.reactWithNeighbors(world, pos)) {
            BlockUtils.scheduleFluidTick(world, pos);
        }
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (this.reactWithNeighbors(world, pos)) {
            BlockUtils.scheduleFluidTick(world, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (isMoving) {
            return;
        }
        Block newBlock = newState.getBlock();
        if (newBlock instanceof BlockGenericFluid || newBlock == Blocks.AIR) {
            return;
        }
        FluidState fluidState;
        if (!(state.getBlock() instanceof BlockGenericFluid)) {
            fluidState = world.getFluidState(pos);
        }
        else {
            fluidState = state.getFluidState();
        }
        int currentAmount = getFluidAmount(world, pos, state, fluidState);
        if (currentAmount == 0) {
            return;
        }
        if (newBlock instanceof IFluidLoggable) {
            int maxAmount = ((IFluidLoggable) newBlock).getFluidCapacity(newState);
            int placed = MathHelper.clampMax(currentAmount, maxAmount);
            ((IFluidLoggable) newBlock).setBlockState(world, pos, newState, this.getFluid(), placed);
            currentAmount -= placed;
        }
        if (currentAmount == 0) {
            return;
        }
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        DirectionList list = new DirectionList();
        list.fillHorizontal();
        //Try placing liquid to the sides
        while (!list.isEmpty()) {
            currentAmount = this.tryDisplaceToDirection(world, pos, list.getRandomAndRemove(MathHelper.RANDOM), currentAmount, 16);
            if (currentAmount == 0) {
                return;
            }
        }
        mutablePos.set(pos);
        //try placing liquid up
        currentAmount = this.tryDisplaceToDirection(world, pos, Direction.UP, currentAmount, 255 - pos.getY());
        if (currentAmount == 0) {
            return;
        }
        CapabilityChunkStorage.add(world.getChunkAt(pos), EnumStorage.WATER, currentAmount);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos) {
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return false;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.getFluidState(pos).randomTick(world, pos, random);
    }

    public boolean reactWithNeighbors(World world, BlockPos pos) {
        if (this.getFluid().is(FluidTags.LAVA)) {
            boolean flag = false;
            for (Direction direction : DirectionUtil.ALL) {
                if (direction != Direction.DOWN && world.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                FluidState ifluidstate = world.getFluidState(pos);
                if (ifluidstate.isSource()) {
                    world.setBlockAndUpdate(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos, pos, Blocks.OBSIDIAN.defaultBlockState()));
                    triggerMixEffects(world, pos);
                    return false;
                }
                if (ifluidstate.getHeight(world, pos) >= 0.444_444_45F) {
                    world.setBlockAndUpdate(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos, pos, Blocks.COBBLESTONE.defaultBlockState()));
                    triggerMixEffects(world, pos);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int receiveFluid(World world, BlockPos pos, BlockState originalState, FluidGeneric fluid, int amount) {
        FluidState stateAtPos = originalState.getFluidState();
        FluidGeneric fluidAtPos = this.getFluid();
        if (fluidAtPos == fluid) {
            int amountAtPos = FluidGeneric.getFluidAmount(world, pos, stateAtPos);
            int amountToReplace = MathHelper.clampMax(amountAtPos + amount, 100_000);
            fluid.setBlockState(world, pos, amountToReplace);
            return amountToReplace - amountAtPos;
        }
        Evolution.LOGGER.warn("Fluids are different, handle them!");
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.getFluidState().getType().isSame(this.getFluid()) || state.canOcclude();
    }

    public abstract int tryDisplaceIn(World world, BlockPos pos, BlockState stateAtPos, FluidGeneric otherFluid, int amount);

    public int tryDisplaceToDirection(World world, BlockPos origin, Direction direction, int amount, int maxDistance) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        mutablePos.set(origin);
        for (int offset = 1; offset <= maxDistance; offset++) {
            BlockState stateAtPos = world.getBlockState(mutablePos);
            if (!FluidGeneric.canSendToOrReceiveFrom(stateAtPos, direction)) {
                return amount;
            }
            mutablePos.move(direction);
            stateAtPos = world.getBlockState(mutablePos);
            if (!FluidGeneric.canSendToOrReceiveFrom(stateAtPos, direction.getOpposite())) {
                return amount;
            }
            if (FluidGeneric.isFull(world, mutablePos)) {
                continue;
            }
            if (!BlockUtils.canBeReplacedByFluid(stateAtPos)) {
                return amount;
            }
            if (this.getFluid().isEquivalentOrEmpty(world, mutablePos)) {
                int amountAlreadyAtPos = FluidGeneric.getFluidAmount(world, mutablePos, world.getFluidState(mutablePos));
                int capacity = FluidGeneric.getCapacity(stateAtPos);
                int placed = MathHelper.clampMax(amountAlreadyAtPos + amount, capacity);
                this.getFluid().setBlockState(world, mutablePos, placed);
                FluidGeneric.onReplace(world, mutablePos, stateAtPos);
                amount = amount - placed + amountAlreadyAtPos;
                if (amount == 0) {
                    return 0;
                }
                continue;
            }
            Fluid otherFluid = world.getFluidState(mutablePos).getType();
            if (otherFluid instanceof FluidGeneric) {
                amount = this.tryDisplaceIn(world, mutablePos, stateAtPos, (FluidGeneric) otherFluid, amount);
                if (amount == 0) {
                    return 0;
                }
                continue;
            }
            Evolution.LOGGER.warn("Invalid fluid at {}: {}", mutablePos, otherFluid);
        }
        return amount;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        BlockUtils.scheduleFluidTick(world, currentPos);
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }
}
