//package tgw.evolution.blocks.fluids;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.tags.FluidTags;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.BlockGetter;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.LevelAccessor;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.LevelEvent;
//import net.minecraft.world.level.block.RenderShape;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.block.state.StateDefinition;
//import net.minecraft.world.level.block.state.properties.BooleanProperty;
//import net.minecraft.world.level.material.Fluid;
//import net.minecraft.world.level.material.FluidState;
//import net.minecraft.world.level.pathfinder.PathComputationType;
//import net.minecraft.world.level.storage.loot.LootContext;
//import net.minecraft.world.phys.shapes.Shapes;
//import net.minecraft.world.phys.shapes.VoxelShape;
//import org.jetbrains.annotations.Nullable;
//import tgw.evolution.Evolution;
//import tgw.evolution.blocks.BlockPhysics;
//import tgw.evolution.blocks.IBlockFluidContainer;
//import tgw.evolution.blocks.IReplaceable;
//import tgw.evolution.blocks.util.BlockUtils;
//import tgw.evolution.blocks.util.IntProperty;
//import tgw.evolution.init.EvolutionBStates;
//import tgw.evolution.util.collection.lists.OArrayList;
//import tgw.evolution.util.collection.lists.OList;
//import tgw.evolution.util.math.DirectionUtil;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Random;
//
//import static tgw.evolution.init.EvolutionBStates.LEVEL_1_8;
//
//public abstract class BlockGenericFluid extends BlockPhysics implements IBlockFluidContainer, IReplaceable {
//    public static final IntProperty LEVEL = LEVEL_1_8;
//    public static final BooleanProperty FULL = EvolutionBStates.FULL;
//    private final FluidGeneric fluid;
//    private final OList<FluidState> fluidStateCache;
//    private boolean fluidStateCacheInitialized;
//
//    public BlockGenericFluid(FluidGeneric fluid, Properties properties) {
//        super(properties);
//        this.fluidStateCache = new OArrayList<>();
//        this.registerDefaultState(this.defaultBlockState().setValue(LEVEL, 8).setValue(FULL, true));
//        this.fluid = fluid;
//    }
//
//    public static int getFluidAmount(BlockGetter level, BlockPos pos, BlockState state, FluidState fluid) {
//        if (fluid.isEmpty()) {
//            return 0;
//        }
//        Block block = state.getBlock();
//        if (block instanceof BlockGenericFluid) {
//            int layers = fluid.getValue(LEVEL_1_8);
//            return 12_500 * layers;
//        }
//        return 0;
//    }
//
//    public static void place(Level level, BlockPos pos, FluidGeneric fluid, int amount) {
//        fluid.setBlockState(level, pos, amount);
//    }
//
//    private static void triggerMixEffects(LevelAccessor level, BlockPos pos) {
//        level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
//    }
//
//    @Override
//    public boolean canBeReplacedByFluid(BlockState state) {
//        if (!state.getValue(FULL)) {
//            return true;
//        }
//        return state.getValue(LEVEL) < 8;
//    }
//
//    @Override
//    public boolean canBeReplacedByRope(BlockState state) {
//        return false;
//    }
//
//    @Override
//    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
//        builder.add(LEVEL, FULL);
//    }
//
//    @Override
//    public int getAmountRemoved(Level level, BlockPos pos, int maxAmount) {
//        FluidState stateAtPos = level.getFluidState(pos);
//        int amountAtPos = FluidGeneric.getFluidAmount(level, pos, stateAtPos);
//        int amountRemoved = Math.min(amountAtPos, maxAmount);
//        amountAtPos -= amountRemoved;
//        this.getFluid().setBlockState(level, pos, amountAtPos);
//        return amountRemoved;
//    }
//
//    @Override
//    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
//        return Collections.emptyList();
//    }
//
//    @Override
//    public FluidGeneric getFluid(BlockGetter level, BlockPos pos) {
//        return this.fluid;
//    }
//
//    public FluidGeneric getFluid() {
//        return this.fluid;
//    }
//
//    @Override
//    public FluidState getFluidState(BlockState state) {
//        if (!this.fluidStateCacheInitialized) {
//            this.initFluidStateCache();
//        }
//        int level = state.getValue(LEVEL) - 1;
//        if (state.getValue(FULL)) {
//            level += 8;
//        }
//        return this.fluidStateCache.get(level);
//    }
//
//    @Override
//    public RenderShape getRenderShape(BlockState state) {
//        return RenderShape.INVISIBLE;
//    }
//
//    @Override
//    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
//        return Shapes.empty();
//    }
//
//    protected synchronized void initFluidStateCache() {
//        if (!this.fluidStateCacheInitialized) {
//            for (int i = 1; i <= 8; i++) {
//                this.fluidStateCache.add(this.getFluid().getFlowing(i, false));
//            }
//            for (int i = 1; i <= 8; i++) {
//                this.fluidStateCache.add(this.getFluid().getFlowing(i, true));
//            }
//            this.fluidStateCacheInitialized = true;
//        }
//    }
//
//    @Override
//    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
//        return !this.getFluid().is(FluidTags.LAVA);
//    }
//
//    @Override
//    public boolean isReplaceable(BlockState state) {
//        return true;
//    }
//
//    @Override
//    public void neighborChanged_(BlockState state,
//                                 Level level,
//                                 int x,
//                                 int y,
//                                 int z,
//                                 Block oldBlock,
//                                 int fromX,
//                                 int fromY,
//                                 int fromZ,
//                                 boolean isMoving) {
//        if (this.reactWithNeighbors(level, x, y, z)) {
//            BlockUtils.scheduleFluidTick(level, new BlockPos(x, y, z));
//        }
//    }
//
//    @Override
//    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
//        if (this.reactWithNeighbors(level, x, y, z)) {
//            BlockUtils.scheduleFluidTick(level, new BlockPos(x, y, z));
//        }
//    }
//
//    @Override
//    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
//        //todo
////        if (isMoving) {
////            return;
////        }
////        Block newBlock = newState.getBlock();
////        if (newBlock instanceof BlockGenericFluid || newBlock == Blocks.AIR) {
////            return;
////        }
////        FluidState fluidState;
////        if (!(state.getBlock() instanceof BlockGenericFluid)) {
////            fluidState = level.getFluidState_(x, y, z);
////        }
////        else {
////            fluidState = state.getFluidState();
////        }
////        int currentAmount = getFluidAmount(level, pos, state, fluidState);
////        if (currentAmount == 0) {
////            return;
////        }
////        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
////        int list = DirectionList.fillHorizontal();
////        //Try placing liquid to the sides
////        while (!DirectionList.isEmpty(list)) {
////            int index = DirectionList.getRandom(list, level.random);
////            Direction dir = DirectionList.get(list, index);
////            list = DirectionList.remove(list, index);
////            currentAmount = this.tryDisplaceToDirection(level, pos, dir, currentAmount, 16);
////            if (currentAmount == 0) {
////                return;
////            }
////        }
////        mutablePos.set(pos);
////        //try placing liquid up
////        currentAmount = this.tryDisplaceToDirection(level, pos, Direction.UP, currentAmount, 255 - pos.getY());
////        if (currentAmount == 0) {
////            return;
////        }
////        CapabilityChunkStorage.add(level.getChunkAt(pos), EnumStorage.WATER, currentAmount);
//    }
//
//    @Override
//    public boolean propagatesSkylightDown_(BlockState state, BlockGetter level, int x, int y, int z) {
//        return false;
//    }
//
//    @Override
//    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
//        level.getFluidState_(x, y, z).randomTick(level, new BlockPos(x, y, z), random);
//    }
//
//    public boolean reactWithNeighbors(Level level, int x, int y, int z) {
//        //todo
//        BlockPos pos = new BlockPos(x, y, z);
//        if (this.getFluid().is(FluidTags.LAVA)) {
//            boolean flag = false;
//            for (Direction direction : DirectionUtil.ALL) {
//                if (direction != Direction.DOWN && level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
//                    flag = true;
//                    break;
//                }
//            }
//            if (flag) {
//                FluidState ifluidstate = level.getFluidState(pos);
//                if (ifluidstate.isSource()) {
//                    level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
//                    triggerMixEffects(level, pos);
//                    return false;
//                }
//                if (ifluidstate.getHeight(level, pos) >= 0.444_444_45F) {
//                    level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
//                    triggerMixEffects(level, pos);
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public int receiveFluid(Level level, BlockPos pos, BlockState originalState, FluidGeneric fluid, int amount) {
//        FluidState stateAtPos = originalState.getFluidState();
//        FluidGeneric fluidAtPos = this.getFluid();
//        if (fluidAtPos == fluid) {
//            int amountAtPos = FluidGeneric.getFluidAmount(level, pos, stateAtPos);
//            int amountToReplace = Math.min(amountAtPos + amount, 100_000);
//            fluid.setBlockState(level, pos, amountToReplace);
//            return amountToReplace - amountAtPos;
//        }
//        Evolution.warn("Fluids are different, handle them!");
//        return 0;
//    }
//
//    @Override
//    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
//        return adjacentBlockState.getFluidState().getType().isSame(this.getFluid()) || state.canOcclude();
//    }
//
//    public abstract int tryDisplaceIn(Level level, BlockPos pos, BlockState stateAtPos, FluidGeneric otherFluid, int amount);
//
//    public int tryDisplaceToDirection(Level level, BlockPos origin, Direction direction, int amount, int maxDistance) {
//        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
//        mutablePos.set(origin);
//        for (int offset = 1; offset <= maxDistance; offset++) {
//            BlockState stateAtPos = level.getBlockState(mutablePos);
//            if (!FluidGeneric.canSendToOrReceiveFrom(stateAtPos, direction)) {
//                return amount;
//            }
//            mutablePos.move(direction);
//            stateAtPos = level.getBlockState(mutablePos);
//            if (!FluidGeneric.canSendToOrReceiveFrom(stateAtPos, direction.getOpposite())) {
//                return amount;
//            }
//            if (FluidGeneric.isFull(level, mutablePos)) {
//                continue;
//            }
//            if (!BlockUtils.canBeReplacedByFluid(stateAtPos)) {
//                return amount;
//            }
//            if (this.getFluid().isEquivalentOrEmpty(level, mutablePos)) {
//                int amountAlreadyAtPos = FluidGeneric.getFluidAmount(level, mutablePos, level.getFluidState(mutablePos));
//                int capacity = FluidGeneric.getCapacity(stateAtPos);
//                int placed = Math.min(amountAlreadyAtPos + amount, capacity);
//                this.getFluid().setBlockState(level, mutablePos, placed);
//                amount = amount - placed + amountAlreadyAtPos;
//                if (amount == 0) {
//                    return 0;
//                }
//                continue;
//            }
//            Fluid otherFluid = level.getFluidState(mutablePos).getType();
//            if (otherFluid instanceof FluidGeneric fluidGeneric) {
//                amount = this.tryDisplaceIn(level, mutablePos, stateAtPos, fluidGeneric, amount);
//                if (amount == 0) {
//                    return 0;
//                }
//                continue;
//            }
//            Evolution.warn("Invalid fluid at {}: {}", mutablePos, otherFluid);
//        }
//        return amount;
//    }
//
//    @Override
//    public BlockState updateShape_(BlockState state,
//                                   Direction from,
//                                   BlockState fromState,
//                                   LevelAccessor level,
//                                   int x,
//                                   int y,
//                                   int z,
//                                   int fromX,
//                                   int fromY,
//                                   int fromZ) {
//        BlockUtils.scheduleFluidTick(level, new BlockPos(x, y, z));
//        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
//    }
//}
