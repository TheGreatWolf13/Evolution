package tgw.evolution.blocks.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IFluidLoggable;
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.blocks.tileentities.TELiquid;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.util.*;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class FluidGeneric extends FlowingFluid {
    public static final byte FRESH_WATER = 1;
    public static final byte SALT_WATER = 2;
    private final DirectionList auxList = new DirectionList();
    private final BlockPos.Mutable auxPos = new BlockPos.Mutable();
    private final Supplier<? extends BlockGenericFluid> block;
    private final FluidAttributes.Builder builder;
    private final DirectionDiagonalList diagList = new DirectionDiagonalList();
    private final float explosionResistance;
    private final Supplier<? extends Fluid> fluid;
    private final int levelDecreasePerBlock;
    private final int mass;
    private final int slopeFindDistance;
    private final int tickRate;

    protected FluidGeneric(Properties properties, int mass) {
        this.fluid = properties.still;
        this.builder = properties.attributes;
        this.block = properties.block;
        this.slopeFindDistance = Properties.SLOPE_FIND_DISTANCE;
        this.levelDecreasePerBlock = Properties.LEVEL_DECREASE_PER_BLOCK;
        this.explosionResistance = Properties.EXPLOSION_RESISTANCE;
        this.tickRate = Properties.TICK_RATE;
        this.mass = mass;
    }

    @Nullable
    public static FluidGeneric byId(int id) {
        switch (id) {
            case FRESH_WATER:
                return EvolutionFluids.FRESH_WATER.get();
            case SALT_WATER:
                return EvolutionFluids.SALT_WATER.get();
        }
        return null;
    }

    public static boolean canSendToOrReceiveFrom(BlockState state, Direction direction) {
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable) {
            return ((IFluidLoggable) block).canFlowThrough(state, direction);
        }
        return true;
    }

    public static int getApparentAmount(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable) {
            return ((IFluidLoggable) block).getApparentAmount(world, pos, state);
        }
        FluidState fluidState = world.getFluidState(pos);
        if (block instanceof BlockGenericFluid) {
            int layers = fluidState.getValue(LEVEL);
            int amount = 12_500 * layers;
            if (!fluidState.getValue(FALLING)) {
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
        return 0;
    }

    public static int getCapacity(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable) {
            return ((IFluidLoggable) block).getFluidCapacity(state);
        }
        return 100_000;
    }

    public static int getFluidAmount(World world, BlockPos pos, FluidState state) {
        if (state.isEmpty()) {
            return 0;
        }
        BlockState stateAtPos = world.getBlockState(pos);
        Block block = stateAtPos.getBlock();
        if (block instanceof BlockGenericFluid) {
            int layers = state.getValue(LEVEL);
            int amount = 12_500 * layers;
            if (!state.getValue(FALLING)) {
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
            return ((IFluidLoggable) block).getCurrentAmount(world, pos, stateAtPos);
        }
        return 0;
    }

    public static int getTolerance(World world, BlockPos pos) {
        if (!canSendToOrReceiveFrom(world.getBlockState(pos), Direction.UP)) {
            return 250;
        }
        BlockPos posUp = pos.above();
        if (!canSendToOrReceiveFrom(world.getBlockState(posUp), Direction.DOWN)) {
            return 250;
        }
        return world.getFluidState(posUp).isEmpty() ? 250 : 0;
    }

    public static boolean isFull(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable) {
            return ((IFluidLoggable) block).isFull(world, pos, state);
        }
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.isEmpty()) {
            return false;
        }
        if (!fluidState.getValue(FALLING)) {
            return false;
        }
        return fluidState.getValue(LEVEL) == 8;
    }

    public static void onReplace(World world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable) {
            return;
        }
        if (block instanceof IReplaceable) {
            ((IReplaceable) block).onReplaced(state, world, pos);
        }
    }

    @Override
    protected void beforeDestroyingBlock(IWorld world, BlockPos pos, BlockState state) {
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, IBlockReader world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected boolean canConvertToSource() {
        return false;
    }

    @Override
    protected FluidAttributes createAttributes() {
        return this.builder.build(this);
    }

    @Override
    protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return this.block.get()
                         .defaultBlockState()
                         .setValue(BlockGenericFluid.LEVEL, state.getValue(LEVEL))
                         .setValue(BlockGenericFluid.FULL, state.getValue(FALLING));
    }

    public BlockState getBlockstate(int level, boolean full) {
        if (level == 0) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.getFluidState(level, full).createLegacyBlock();
    }

    @Override
    public Item getBucket() {
        return Items.AIR;
    }

    @Override
    protected int getDropOff(IWorldReader worldIn) {
        return this.levelDecreasePerBlock;
    }

    @Override
    protected float getExplosionResistance() {
        return this.explosionResistance;
    }

    @Override
    public Vector3d getFlow(IBlockReader world, BlockPos pos, FluidState state) {
        return Vector3d.ZERO;
    }

    @Override
    public Fluid getFlowing() {
        return this.fluid.get();
    }

    public FluidState getFluidState(int level, boolean full) {
        return this.getSource().defaultFluidState().setValue(LEVEL, level).setValue(FALLING, full);
    }

    public abstract byte getId();

    public int getMass() {
        return this.mass;
    }

    @Override
    protected int getSlopeFindDistance(IWorldReader worldIn) {
        return this.slopeFindDistance;
    }

    @Override
    public Fluid getSource() {
        return this.fluid.get();
    }

    public abstract ITextComponent getTextComp();

    @Override
    public int getTickDelay(IWorldReader world) {
        return this.tickRate;
    }

    public boolean isEquivalentOrEmpty(World world, BlockPos pos) {
        FluidState state = world.getFluidState(pos);
        if (state.isEmpty()) {
            return true;
        }
        return state.getType() == this;
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == this.fluid.get();
    }

    /**
     * Return true if the operation was successful and the loop should break.
     * Return false if the operation was not successful and the loop should continue.
     */
    public abstract boolean level(World world, BlockPos pos, FluidState fluidState, Direction direction, FluidGeneric otherFluid, int tolerance);

    public void replacedAt(World world, BlockPos pos, int amount) {
        Evolution.LOGGER.debug("{} replaced at {} with {}", this, pos, amount);
    }

    public void setBlockState(World world, BlockPos pos, int fluidAmount) {
        pos = pos.immutable();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable) {
            ((IFluidLoggable) block).setBlockState(world, pos, state, this, fluidAmount);
            return;
        }
        this.setBlockStateInternal(world, pos, fluidAmount);
    }

    public void setBlockStateInternal(World world, BlockPos pos, int amount) {
        int layers = MathHelper.ceil(amount / 12_500.0);
        int missing = layers * 12_500 - amount;
        boolean isFull = missing == 0;
        BlockState stateForPlacement = this.getBlockstate(layers, isFull);
        world.setBlockAndUpdate(pos, stateForPlacement);
        if (!isFull) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TELiquid) {
                ((TELiquid) tile).setMissingLiquid(missing);
            }
            else {
                Evolution.LOGGER.warn("Invalid tile entity for fluid at {}: {}", pos, tile);
            }
        }
        else {
            world.removeBlockEntity(pos);
        }
        for (Direction dir : DirectionUtil.ALL) {
            BlockPos offsetPos = pos.relative(dir);
            BlockState stateAtOffset = world.getBlockState(offsetPos);
            if (stateAtOffset.getBlock() instanceof IFluidLoggable) {
                BlockUtils.scheduleBlockTick(world, offsetPos, this.tickRate);
            }
        }
    }

    @Override
    public void tick(World world, BlockPos pos, FluidState fluidState) {
        if (this.tryFall(world, pos, fluidState)) {
            return;
        }
        this.auxList.clear();
        for (Direction direction : DirectionUtil.HORIZ_NESW) {
            if (BlockUtils.canBeReplacedByFluid(world.getBlockState(this.auxPos.set(pos).move(direction)))) {
                this.auxList.add(direction);
            }
        }
        if (this.auxList.isEmpty()) {
            this.tryToLevel(world, pos, fluidState);
            return;
        }
        BlockState state = world.getBlockState(pos);
        int tolerance = getTolerance(world, pos);
        while (!this.auxList.isEmpty()) {
            Direction direction = this.auxList.getRandomAndRemove(MathHelper.RANDOM);
            if (!canSendToOrReceiveFrom(state, direction)) {
                continue;
            }
            this.auxPos.set(pos).move(direction);
            BlockState stateAtOffset = world.getBlockState(this.auxPos);
            if (!canSendToOrReceiveFrom(stateAtOffset, direction.getOpposite())) {
                continue;
            }
            if (this.isEquivalentOrEmpty(world, this.auxPos)) {
                int apAtPos = getApparentAmount(world, this.auxPos);
                int apThis = getApparentAmount(world, pos);
                if (apAtPos >= apThis - tolerance) {
                    continue;
                }
                int apMean = MathHelper.ceil((apAtPos + apThis) / 2.0);
                int rlThis = getFluidAmount(world, pos, fluidState);
                int rlAtPos = getFluidAmount(world, this.auxPos, world.getFluidState(this.auxPos));
                int amountToSwap = MathHelper.clampMax(apMean - apAtPos, rlThis);
                if (amountToSwap == 0) {
                    break;
                }
                int stay = rlThis - amountToSwap;
                this.setBlockState(world, pos, stay);
                onReplace(world, this.auxPos, stateAtOffset);
                int receive = amountToSwap + rlAtPos;
                this.setBlockState(world, this.auxPos, receive);
                break;
            }
            Fluid fluid = world.getFluidState(this.auxPos).getType();
            if (fluid instanceof FluidGeneric) {
                if (this.level(world, pos, fluidState, direction, (FluidGeneric) fluid, tolerance)) {
                    break;
                }
            }
            else {
                Evolution.LOGGER.warn("Invalid fluid to calculate physics: {}", fluid.getRegistryName());
            }
        }
    }

    @Override
    public String toString() {
        return this.getRegistryName().toString();
    }

    public boolean tryFall(World world, BlockPos pos, FluidState fluidState) {
        BlockState state = world.getBlockState(pos);
        if (!canSendToOrReceiveFrom(state, Direction.DOWN)) {
            return false;
        }
        BlockPos posDown = pos.below();
        if (BlockUtils.canBeReplacedByFluid(world.getBlockState(posDown))) {
            if (isFull(world, posDown)) {
                return false;
            }
            BlockState stateAtFall = world.getBlockState(posDown);
            if (!canSendToOrReceiveFrom(stateAtFall, Direction.UP)) {
                return false;
            }
            FluidState fluidAtFall = world.getFluidState(posDown);
            if (this.isEquivalentOrEmpty(world, posDown)) {
                int rlThis = getFluidAmount(world, pos, fluidState);
                int rlAtPos = getFluidAmount(world, posDown, fluidAtFall);
                int amountForFall = MathHelper.clampMax(rlThis + rlAtPos, getCapacity(stateAtFall));
                this.setBlockState(world, posDown, amountForFall);
                onReplace(world, posDown, stateAtFall);
                int amountRemaining = rlThis + rlAtPos - amountForFall;
                this.setBlockState(world, pos, amountRemaining);
                return true;
            }
            return this.tryFall(world, pos, fluidAtFall.getType());
        }
        return false;
    }

    public abstract boolean tryFall(World world, BlockPos pos, Fluid other);

    private void tryToLevel(World world, BlockPos pos, FluidState state) {
        int rlThis = getFluidAmount(world, pos, state);
        if (rlThis == 0) {
            return;
        }
        if (!canSendToOrReceiveFrom(world.getBlockState(pos), Direction.DOWN)) {
            return;
        }
        this.auxPos.set(pos).move(Direction.DOWN);
        BlockState stateDown = world.getBlockState(this.auxPos);
        FluidState fluidDown = world.getFluidState(this.auxPos);
        if (this == fluidDown.getType()) {
            this.diagList.clear();
            this.auxList.fillHorizontal();
            while (!this.auxList.isEmpty()) {
                Direction direction = this.auxList.getRandomAndRemove(MathHelper.RANDOM);
                if (!canSendToOrReceiveFrom(stateDown, direction)) {
                    continue;
                }
                this.auxPos.set(pos).move(Direction.DOWN).move(direction);
                BlockState stateAtPos = world.getBlockState(this.auxPos);
                if (!canSendToOrReceiveFrom(stateAtPos, direction.getOpposite())) {
                    continue;
                }
                FluidState fluidAtPos = world.getFluidState(this.auxPos);
                if (this == fluidAtPos.getType()) {
                    if (!isFull(world, this.auxPos)) {
                        int rlAtPos = getFluidAmount(world, this.auxPos, fluidAtPos);
                        int amountForReplacement = MathHelper.clampMax(rlThis + rlAtPos, getCapacity(stateAtPos));
                        this.setBlockState(world, this.auxPos, amountForReplacement);
                        rlThis = rlThis - amountForReplacement + rlAtPos;
                        if (rlThis == 0) {
                            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            return;
                        }
                        this.setBlockState(world, pos, rlThis);
                    }
                    if (direction.getAxis() == Direction.Axis.X) {
                        for (Direction dir : DirectionUtil.Z) {
                            if (!canSendToOrReceiveFrom(stateAtPos, dir)) {
                                continue;
                            }
                            this.auxPos.set(pos).move(Direction.DOWN).move(direction).move(dir);
                            BlockState stateDiag = world.getBlockState(this.auxPos);
                            if (!canSendToOrReceiveFrom(stateDiag, dir.getOpposite())) {
                                continue;
                            }
                            this.diagList.add(DirectionDiagonal.sum(dir, direction));
                        }
                    }
                    else {
                        for (Direction dir : DirectionUtil.X) {
                            if (!canSendToOrReceiveFrom(stateAtPos, dir)) {
                                continue;
                            }
                            this.auxPos.set(pos).move(Direction.DOWN).move(direction).move(dir);
                            BlockState stateDiag = world.getBlockState(this.auxPos);
                            if (!canSendToOrReceiveFrom(stateDiag, dir.getOpposite())) {
                                continue;
                            }
                            this.diagList.add(DirectionDiagonal.sum(dir, direction));
                        }
                    }
                }
            }
            while (!this.diagList.isEmpty()) {
                DirectionDiagonal diagonal = this.diagList.getRandomAndRemove(MathHelper.RANDOM);
                diagonal.movePos(this.auxPos.set(pos).move(Direction.DOWN));
                FluidState fluidAtPos = world.getFluidState(this.auxPos);
                if (this == fluidAtPos.getType()) {
                    if (isFull(world, this.auxPos)) {
                        continue;
                    }
                    int rlAtPos = getFluidAmount(world, this.auxPos, fluidAtPos);
                    int amountForReplacement = MathHelper.clampMax(rlThis + rlAtPos, getCapacity(world.getBlockState(this.auxPos)));
                    this.setBlockState(world, this.auxPos, amountForReplacement);
                    rlThis = rlThis - amountForReplacement + rlAtPos;
                    if (rlThis == 0) {
                        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        return;
                    }
                    this.setBlockState(world, pos, rlThis);
                }
            }
        }
    }

    public static class Properties {
        private static final float EXPLOSION_RESISTANCE = 100;
        private static final int LEVEL_DECREASE_PER_BLOCK = 1;
        private static final int SLOPE_FIND_DISTANCE = 4;
        private static final int TICK_RATE = 5;
        private final FluidAttributes.Builder attributes;
        private final Supplier<? extends Fluid> still;
        private Supplier<? extends BlockGenericFluid> block;

        public Properties(Supplier<? extends Fluid> still, FluidAttributes.Builder attributes) {
            this.still = still;
            this.attributes = attributes;
        }

        public Properties block(Supplier<? extends BlockGenericFluid> block) {
            this.block = block;
            return this;
        }
    }
}
