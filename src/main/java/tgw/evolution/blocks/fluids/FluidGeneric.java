package tgw.evolution.blocks.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidAttributes;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IFluidLoggable;
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.blocks.tileentities.TELiquid;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.util.math.*;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class FluidGeneric extends FlowingFluid {
    public static final byte FRESH_WATER = 1;
    public static final byte SALT_WATER = 2;
    private final DirectionList auxList = new DirectionList();
    private final BlockPos.MutableBlockPos auxPos = new BlockPos.MutableBlockPos();
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
        return switch (id) {
            case FRESH_WATER -> EvolutionFluids.FRESH_WATER.get();
            case SALT_WATER -> EvolutionFluids.SALT_WATER.get();
            default -> null;
        };
    }

    public static boolean canSendToOrReceiveFrom(BlockState state, Direction direction) {
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable fluidLoggable) {
            return fluidLoggable.canFlowThrough(state, direction);
        }
        return true;
    }

    public static int getApparentAmount(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable fluidLoggable) {
            return fluidLoggable.getApparentAmount(level, pos, state);
        }
        FluidState fluidState = level.getFluidState(pos);
        if (block instanceof BlockGenericFluid) {
            int layers = fluidState.getValue(LEVEL);
            int amount = 12_500 * layers;
            if (!fluidState.getValue(FALLING)) {
                BlockEntity tile = level.getBlockEntity(pos);
                if (tile instanceof TELiquid teLiquid) {
                    amount -= teLiquid.getMissingLiquid();
                }
                else {
                    Evolution.warn("Invalid tile entity for block at {}: {}", pos, tile);
                }
            }
            return amount;
        }
        return 0;
    }

    public static int getCapacity(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable fluidLoggable) {
            return fluidLoggable.getFluidCapacity(state);
        }
        return 100_000;
    }

    public static int getFluidAmount(Level level, BlockPos pos, FluidState state) {
        if (state.isEmpty()) {
            return 0;
        }
        BlockState stateAtPos = level.getBlockState(pos);
        Block block = stateAtPos.getBlock();
        if (block instanceof BlockGenericFluid) {
            int layers = state.getValue(LEVEL);
            int amount = 12_500 * layers;
            if (!state.getValue(FALLING)) {
                BlockEntity tile = level.getBlockEntity(pos);
                if (tile instanceof TELiquid teLiquid) {
                    amount -= teLiquid.getMissingLiquid();
                }
                else {
                    Evolution.warn("Invalid tile entity for block at {}: {}", pos, tile);
                }
            }
            return amount;
        }
        if (block instanceof IFluidLoggable fluidLoggable) {
            return fluidLoggable.getCurrentAmount(level, pos, stateAtPos);
        }
        return 0;
    }

    public static int getTolerance(BlockGetter level, BlockPos pos) {
        if (!canSendToOrReceiveFrom(level.getBlockState(pos), Direction.UP)) {
            return 250;
        }
        BlockPos posUp = pos.above();
        if (!canSendToOrReceiveFrom(level.getBlockState(posUp), Direction.DOWN)) {
            return 250;
        }
        return level.getFluidState(posUp).isEmpty() ? 250 : 0;
    }

    public static boolean isFull(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable fluidLoggable) {
            return fluidLoggable.isFull(world, pos, state);
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

    public static void onReplace(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable) {
            return;
        }
        if (block instanceof IReplaceable replaceable) {
            replaceable.onReplaced(state, level, pos);
        }
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor world, BlockPos pos, BlockState state) {
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction direction) {
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
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
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
    protected int getDropOff(LevelReader worldIn) {
        return this.levelDecreasePerBlock;
    }

    @Override
    protected float getExplosionResistance() {
        return this.explosionResistance;
    }

    @Override
    public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
        return Vec3.ZERO;
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
    protected int getSlopeFindDistance(LevelReader level) {
        return this.slopeFindDistance;
    }

    @Override
    public Fluid getSource() {
        return this.fluid.get();
    }

    public abstract Component getTextComp();

    @Override
    public int getTickDelay(LevelReader level) {
        return this.tickRate;
    }

    public boolean isEquivalentOrEmpty(BlockGetter level, BlockPos pos) {
        FluidState state = level.getFluidState(pos);
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
    public abstract boolean level(Level level, BlockPos pos, FluidState fluidState, Direction direction, FluidGeneric otherFluid, int tolerance);

    public void replacedAt(Level level, BlockPos pos, int amount) {
        Evolution.debug("{} replaced at {} with {}", this, pos, amount);
    }

    public void setBlockState(Level level, BlockPos pos, int fluidAmount) {
        pos = pos.immutable();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidLoggable fluidLoggable) {
            fluidLoggable.setBlockState(level, pos, state, this, fluidAmount);
            return;
        }
        this.setBlockStateInternal(level, pos, fluidAmount);
    }

    public void setBlockStateInternal(Level level, BlockPos pos, int amount) {
        int layers = Mth.ceil(amount / 12_500.0);
        int missing = layers * 12_500 - amount;
        boolean isFull = missing == 0;
        BlockState stateForPlacement = this.getBlockstate(layers, isFull);
        level.setBlockAndUpdate(pos, stateForPlacement);
        if (!isFull) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof TELiquid teLiquid) {
                teLiquid.setMissingLiquid(missing);
            }
            else {
                Evolution.warn("Invalid tile entity for fluid at {}: {}", pos, tile);
            }
        }
        else {
            level.removeBlockEntity(pos);
        }
        for (Direction dir : DirectionUtil.ALL) {
            BlockPos offsetPos = pos.relative(dir);
            BlockState stateAtOffset = level.getBlockState(offsetPos);
            if (stateAtOffset.getBlock() instanceof IFluidLoggable) {
                BlockUtils.scheduleBlockTick(level, offsetPos, this.tickRate);
            }
        }
    }

    @Override
    public void tick(Level level, BlockPos pos, FluidState fluidState) {
        if (this.tryFall(level, pos, fluidState)) {
            return;
        }
        this.auxList.clear();
        for (Direction direction : DirectionUtil.HORIZ_NESW) {
            if (BlockUtils.canBeReplacedByFluid(level.getBlockState(this.auxPos.set(pos).move(direction)))) {
                this.auxList.add(direction);
            }
        }
        if (this.auxList.isEmpty()) {
            this.tryToLevel(level, pos, fluidState);
            return;
        }
        BlockState state = level.getBlockState(pos);
        int tolerance = getTolerance(level, pos);
        while (!this.auxList.isEmpty()) {
            Direction direction = this.auxList.getRandomAndRemove(MathHelper.RANDOM);
            if (!canSendToOrReceiveFrom(state, direction)) {
                continue;
            }
            this.auxPos.set(pos).move(direction);
            BlockState stateAtOffset = level.getBlockState(this.auxPos);
            if (!canSendToOrReceiveFrom(stateAtOffset, direction.getOpposite())) {
                continue;
            }
            if (this.isEquivalentOrEmpty(level, this.auxPos)) {
                int apAtPos = getApparentAmount(level, this.auxPos);
                int apThis = getApparentAmount(level, pos);
                if (apAtPos >= apThis - tolerance) {
                    continue;
                }
                int apMean = Mth.ceil((apAtPos + apThis) / 2.0);
                int rlThis = getFluidAmount(level, pos, fluidState);
                int rlAtPos = getFluidAmount(level, this.auxPos, level.getFluidState(this.auxPos));
                int amountToSwap = Math.min(apMean - apAtPos, rlThis);
                if (amountToSwap == 0) {
                    break;
                }
                int stay = rlThis - amountToSwap;
                this.setBlockState(level, pos, stay);
                onReplace(level, this.auxPos, stateAtOffset);
                int receive = amountToSwap + rlAtPos;
                this.setBlockState(level, this.auxPos, receive);
                break;
            }
            Fluid fluid = level.getFluidState(this.auxPos).getType();
            if (fluid instanceof FluidGeneric fluidGeneric) {
                if (this.level(level, pos, fluidState, direction, fluidGeneric, tolerance)) {
                    break;
                }
            }
            else {
                Evolution.warn("Invalid fluid to calculate physics: {}", fluid.getRegistryName());
            }
        }
    }

    @Override
    public String toString() {
        return this.getRegistryName().toString();
    }

    public boolean tryFall(Level level, BlockPos pos, FluidState fluidState) {
        BlockState state = level.getBlockState(pos);
        if (!canSendToOrReceiveFrom(state, Direction.DOWN)) {
            return false;
        }
        BlockPos posDown = pos.below();
        if (BlockUtils.canBeReplacedByFluid(level.getBlockState(posDown))) {
            if (isFull(level, posDown)) {
                return false;
            }
            BlockState stateAtFall = level.getBlockState(posDown);
            if (!canSendToOrReceiveFrom(stateAtFall, Direction.UP)) {
                return false;
            }
            FluidState fluidAtFall = level.getFluidState(posDown);
            if (this.isEquivalentOrEmpty(level, posDown)) {
                int rlThis = getFluidAmount(level, pos, fluidState);
                int rlAtPos = getFluidAmount(level, posDown, fluidAtFall);
                int amountForFall = Math.min(rlThis + rlAtPos, getCapacity(stateAtFall));
                this.setBlockState(level, posDown, amountForFall);
                onReplace(level, posDown, stateAtFall);
                int amountRemaining = rlThis + rlAtPos - amountForFall;
                this.setBlockState(level, pos, amountRemaining);
                return true;
            }
            return this.tryFall(level, pos, fluidAtFall.getType());
        }
        return false;
    }

    public abstract boolean tryFall(Level level, BlockPos pos, Fluid other);

    private void tryToLevel(Level level, BlockPos pos, FluidState state) {
        int rlThis = getFluidAmount(level, pos, state);
        if (rlThis == 0) {
            return;
        }
        if (!canSendToOrReceiveFrom(level.getBlockState(pos), Direction.DOWN)) {
            return;
        }
        this.auxPos.set(pos).move(Direction.DOWN);
        BlockState stateDown = level.getBlockState(this.auxPos);
        FluidState fluidDown = level.getFluidState(this.auxPos);
        if (this == fluidDown.getType()) {
            this.diagList.clear();
            this.auxList.fillHorizontal();
            while (!this.auxList.isEmpty()) {
                Direction direction = this.auxList.getRandomAndRemove(MathHelper.RANDOM);
                if (!canSendToOrReceiveFrom(stateDown, direction)) {
                    continue;
                }
                this.auxPos.set(pos).move(Direction.DOWN).move(direction);
                BlockState stateAtPos = level.getBlockState(this.auxPos);
                if (!canSendToOrReceiveFrom(stateAtPos, direction.getOpposite())) {
                    continue;
                }
                FluidState fluidAtPos = level.getFluidState(this.auxPos);
                if (this == fluidAtPos.getType()) {
                    if (!isFull(level, this.auxPos)) {
                        int rlAtPos = getFluidAmount(level, this.auxPos, fluidAtPos);
                        int amountForReplacement = Math.min(rlThis + rlAtPos, getCapacity(stateAtPos));
                        this.setBlockState(level, this.auxPos, amountForReplacement);
                        rlThis = rlThis - amountForReplacement + rlAtPos;
                        if (rlThis == 0) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            return;
                        }
                        this.setBlockState(level, pos, rlThis);
                    }
                    if (direction.getAxis() == Direction.Axis.X) {
                        for (Direction dir : DirectionUtil.Z) {
                            if (!canSendToOrReceiveFrom(stateAtPos, dir)) {
                                continue;
                            }
                            this.auxPos.set(pos).move(Direction.DOWN).move(direction).move(dir);
                            BlockState stateDiag = level.getBlockState(this.auxPos);
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
                            BlockState stateDiag = level.getBlockState(this.auxPos);
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
                FluidState fluidAtPos = level.getFluidState(this.auxPos);
                if (this == fluidAtPos.getType()) {
                    if (isFull(level, this.auxPos)) {
                        continue;
                    }
                    int rlAtPos = getFluidAmount(level, this.auxPos, fluidAtPos);
                    int amountForReplacement = Math.min(rlThis + rlAtPos, getCapacity(level.getBlockState(this.auxPos)));
                    this.setBlockState(level, this.auxPos, amountForReplacement);
                    rlThis = rlThis - amountForReplacement + rlAtPos;
                    if (rlThis == 0) {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        return;
                    }
                    this.setBlockState(level, pos, rlThis);
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
