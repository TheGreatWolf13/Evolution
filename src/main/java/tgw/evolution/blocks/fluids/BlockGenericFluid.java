package tgw.evolution.blocks.fluids;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IBlockFluidContainer;
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.blocks.tileentities.TELiquid;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionBlockStateProperties;
import tgw.evolution.util.DirectionList;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public abstract class BlockGenericFluid extends Block implements IBlockFluidContainer, IReplaceable {
    public static final IntegerProperty LEVEL = EvolutionBlockStateProperties.LEVEL_1_8;
    public static final BooleanProperty FULL = EvolutionBlockStateProperties.FULL;
    private final List<IFluidState> fluidStateCache;
    private final Supplier<? extends FluidGeneric> supplier;
    private boolean fluidStateCacheInitialized;

    public BlockGenericFluid(Supplier<? extends FluidGeneric> fluid, Properties properties) {
        super(properties);
        this.fluidStateCache = Lists.newArrayList();
        this.setDefaultState(this.getDefaultState().with(LEVEL, 8).with(FULL, true));
        this.supplier = fluid;
    }

    public static void place(World world, BlockPos pos, FluidGeneric fluid, int amount) {
        fluid.setBlockState(world, pos, amount);
    }

    private static void triggerMixEffects(IWorld world, BlockPos pos) {
        world.playEvent(Constants.WorldEvents.LAVA_EXTINGUISH, pos, 0);
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader world, BlockPos pos, PathType type) {
        return !this.getFluid().isIn(FluidTags.LAVA);
    }

    @Override
    public boolean canBeReplacedByLiquid(BlockState state) {
        if (!state.get(FULL)) {
            return true;
        }
        return state.get(LEVEL) < 8;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TELiquid();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, FULL);
    }

    @Override
    public int getAmountRemoved(World world, BlockPos pos, int maxAmount) {
        IFluidState stateAtPos = world.getFluidState(pos);
        int amountAtPos = FluidGeneric.getFluidAmount(world, pos, stateAtPos);
        int amountRemoved = MathHelper.clampMax(amountAtPos, maxAmount);
        amountAtPos -= amountRemoved;
        if (amountAtPos == 0) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
        else {
            this.getFluid().setBlockState(world, pos, amountAtPos);
        }
        return amountRemoved;
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.emptyList();
    }

    @Override
    public FluidGeneric getFluid() {
        return this.supplier.get();
    }

    @Override
    public IFluidState getFluidState(BlockState state) {
        if (!this.fluidStateCacheInitialized) {
            this.initFluidStateCache();
        }
        int level = state.get(LEVEL) - 1;
        if (state.get(FULL)) {
            level += 8;
        }
        return this.fluidStateCache.get(level);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return !state.get(FULL);
    }

    protected synchronized void initFluidStateCache() {
        if (!this.fluidStateCacheInitialized) {
            for (int i = 1; i <= 8; i++) {
                this.fluidStateCache.add(this.getFluid().getFlowingFluidState(i, false));
            }
            for (int i = 1; i <= 8; i++) {
                this.fluidStateCache.add(this.getFluid().getFlowingFluidState(i, true));
            }
            this.fluidStateCacheInitialized = true;
        }
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.getFluidState().getFluid().isEquivalentTo(this.getFluid()) || super.isSolid(state);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (this.reactWithNeighbors(world, pos)) {
            world.getPendingFluidTicks().scheduleTick(pos, state.getFluidState().getFluid(), this.tickRate(world));
        }

    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (this.reactWithNeighbors(world, pos)) {
            world.getPendingFluidTicks().scheduleTick(pos, state.getFluidState().getFluid(), this.tickRate(world));
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (this.getFluid().isIn(FluidTags.LAVA)) {
            entity.setInLava();
        }
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        Block newBlock = newState.getBlock();
        if (newBlock == this || newBlock == Blocks.AIR) {
            return;
        }
        int currentAmount = FluidGeneric.getFluidAmount(world, pos, state.getFluidState());
        if (currentAmount == 0) {
            return;
        }
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        DirectionList list = new DirectionList();
        list.fillHorizontal();
        //Try placing liquid to the sides
        while (!list.isEmpty()) {
            mutablePos.setPos(pos).move(list.getRandomAndRemove(MathHelper.RANDOM));
            BlockState stateAtPos = world.getBlockState(mutablePos);
            if (BlockUtils.canBeReplacedByWater(stateAtPos)) {
                if (stateAtPos.getBlock() == this) {
                    int amountAlreadyAtPos = FluidGeneric.getFluidAmount(world, mutablePos, stateAtPos.getFluidState());
                    if (amountAlreadyAtPos == 100_000) {
                        continue;
                    }
                    int amountForReplacement = MathHelper.clampMax(currentAmount + amountAlreadyAtPos, 100_000);
                    this.getFluid().setBlockState(world, mutablePos, amountForReplacement);
                    currentAmount = currentAmount - amountForReplacement + amountAlreadyAtPos;
                    if (currentAmount == 0) {
                        return;
                    }
                }
                else {
                    if (stateAtPos.getBlock() instanceof IReplaceable) {
                        ((IReplaceable) stateAtPos.getBlock()).onReplaced(stateAtPos, world, mutablePos);
                    }
                    if (stateAtPos.getFluidState().isEmpty()) {
                        this.getFluid().setBlockState(world, mutablePos, currentAmount);
                    }
                    else {
                        Evolution.LOGGER.warn("Fluids are different, handle them!");
                    }
                    return;
                }
            }
        }
        mutablePos.setPos(pos);
        //try placing liquid up
        for (int y = pos.getY() + 1; y < 256; y++) {
            mutablePos.setY(y);
            BlockState stateAtPos = world.getBlockState(mutablePos);
            if (BlockUtils.canBeReplacedByWater(stateAtPos)) {
                Block blockAtPos = stateAtPos.getBlock();
                if (blockAtPos == this) {
                    int amountAtPos = FluidGeneric.getFluidAmount(world, mutablePos, stateAtPos.getFluidState());
                    if (amountAtPos == 100_000) {
                        continue;
                    }
                    int amountForReplacement = MathHelper.clampMax(currentAmount + amountAtPos, 100_000);
                    this.getFluid().setBlockState(world, mutablePos, amountForReplacement);
                    currentAmount = currentAmount - amountForReplacement + amountAtPos;
                    if (currentAmount == 0) {
                        return;
                    }
                    continue;
                }
                if (blockAtPos instanceof IReplaceable) {
                    ((IReplaceable) blockAtPos).onReplaced(stateAtPos, world, mutablePos);
                }
                if (stateAtPos.getFluidState().isEmpty()) {
                    this.getFluid().setBlockState(world, mutablePos, currentAmount);
                }
                else {
                    Evolution.LOGGER.warn("Fluids are different, handle them!");
                }
                return;
            }
            if (stateAtPos.getBlock() == this) {
                continue;
            }
            break;
        }
        list.fillHorizontal();
        while (!list.isEmpty()) {
            mutablePos.setPos(pos).move(list.getRandomAndRemove(MathHelper.RANDOM));
            for (; mutablePos.getY() < 256; mutablePos.move(Direction.UP)) {
                BlockState stateAtPos = world.getBlockState(mutablePos);
                if (BlockUtils.canBeReplacedByWater(stateAtPos)) {
                    if (stateAtPos.getBlock() == this) {
                        int amountAlreadyAtPos = FluidGeneric.getFluidAmount(world, mutablePos, stateAtPos.getFluidState());
                        if (amountAlreadyAtPos == 100_000) {
                            continue;
                        }
                        int amountForReplacement = MathHelper.clampMax(currentAmount + amountAlreadyAtPos, 100_000);
                        this.getFluid().setBlockState(world, mutablePos, amountForReplacement);
                        currentAmount = currentAmount - amountForReplacement + amountAlreadyAtPos;
                        if (currentAmount == 0) {
                            return;
                        }
                    }
                    else {
                        if (stateAtPos.getBlock() instanceof IReplaceable) {
                            ((IReplaceable) stateAtPos.getBlock()).onReplaced(stateAtPos, world, mutablePos);
                        }
                        if (stateAtPos.getFluidState().isEmpty()) {
                            this.getFluid().setBlockState(world, mutablePos, currentAmount);
                        }
                        else {
                            Evolution.LOGGER.warn("Fluids are different, handle them!");
                        }
                        return;
                    }
                }
                if (stateAtPos.getBlock() == this) {
                    continue;
                }
                break;
            }
        }
        ChunkStorageCapability.add(world.getChunkAt(pos), EnumStorage.WATER, currentAmount);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos) {
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return false;
    }

    @Override
    public void randomTick(BlockState state, World world, BlockPos pos, Random random) {
        world.getFluidState(pos).randomTick(world, pos, random);
    }

    public boolean reactWithNeighbors(World world, BlockPos pos) {
        if (this.getFluid().isIn(FluidTags.LAVA)) {
            boolean flag = false;
            for (Direction direction : Direction.values()) {
                if (direction != Direction.DOWN && world.getFluidState(pos.offset(direction)).isTagged(FluidTags.WATER)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                IFluidState ifluidstate = world.getFluidState(pos);
                if (ifluidstate.isSource()) {
                    world.setBlockState(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos, pos, Blocks.OBSIDIAN.getDefaultState()));
                    triggerMixEffects(world, pos);
                    return false;
                }
                if (ifluidstate.getActualHeight(world, pos) >= 0.444_444_45F) {
                    world.setBlockState(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos, pos, Blocks.COBBLESTONE.getDefaultState()));
                    triggerMixEffects(world, pos);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int receiveFluid(World world, BlockPos pos, BlockState originalState, Fluid fluid, int amount) {
        IFluidState stateAtPos = originalState.getFluidState();
        FluidGeneric fluidAtPos = this.getFluid();
        if (fluidAtPos == fluid) {
            int amountAtPos = FluidGeneric.getFluidAmount(world, pos, stateAtPos);
            int amountToReplace = MathHelper.clampMax(amountAtPos + amount, 100_000);
            ((FluidGeneric) fluid).setBlockState(world, pos, amountToReplace);
            return amountToReplace - amountAtPos;
        }
        Evolution.LOGGER.warn("Fluids are different, handle them!");
        return 0;
    }

    @Override
    public int tickRate(IWorldReader world) {
        return this.getFluid().getTickRate(world);
    }

    @Override
    public BlockState updatePostPlacement(BlockState state,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld world,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        if (state.getFluidState().isSource() || facingState.getFluidState().isSource()) {
            world.getPendingFluidTicks().scheduleTick(currentPos, state.getFluidState().getFluid(), this.tickRate(world));
        }
        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }
}
