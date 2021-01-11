package tgw.evolution.blocks.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
import tgw.evolution.util.DirectionDiagonal;
import tgw.evolution.util.DirectionDiagonalList;
import tgw.evolution.util.DirectionList;
import tgw.evolution.util.MathHelper;

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
    private final BlockRenderLayer renderLayer;
    private final int slopeFindDistance;
    private final int tickRate;

    protected FluidGeneric(Properties properties, int mass) {
        this.fluid = properties.still;
        this.builder = properties.attributes;
        this.block = properties.block;
        this.slopeFindDistance = Properties.SLOPE_FIND_DISTANCE;
        this.levelDecreasePerBlock = Properties.LEVEL_DECREASE_PER_BLOCK;
        this.explosionResistance = Properties.EXPLOSION_RESISTANCE;
        this.renderLayer = properties.renderLayer;
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
        IFluidState fluidState = world.getFluidState(pos);
        if (block instanceof BlockGenericFluid) {
            int layers = fluidState.get(LEVEL_1_8);
            int amount = 12_500 * layers;
            if (!fluidState.get(FALLING)) {
                TileEntity tile = world.getTileEntity(pos);
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

    public static int getFluidAmount(World world, BlockPos pos, IFluidState state) {
        if (state.isEmpty()) {
            return 0;
        }
        BlockState stateAtPos = world.getBlockState(pos);
        Block block = stateAtPos.getBlock();
        if (block instanceof BlockGenericFluid) {
            int layers = state.get(LEVEL_1_8);
            int amount = 12_500 * layers;
            if (!state.get(FALLING)) {
                TileEntity tile = world.getTileEntity(pos);
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
        BlockPos posUp = pos.up();
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
        IFluidState fluidState = world.getFluidState(pos);
        if (fluidState.isEmpty()) {
            return false;
        }
        if (!fluidState.get(FALLING)) {
            return false;
        }
        return fluidState.get(LEVEL_1_8) == 8;
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
    protected void beforeReplacingBlock(IWorld world, BlockPos pos, BlockState state) {
    }

    @Override
    protected boolean canDisplace(IFluidState state, IBlockReader world, BlockPos pos, Fluid fluidIn, Direction direction) {
        return false;
    }

    @Override
    protected boolean canSourcesMultiply() {
        return false;
    }

    @Override
    protected FluidAttributes createAttributes() {
        return this.builder.build(this);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Fluid, IFluidState> builder) {
        super.fillStateContainer(builder);
        builder.add(LEVEL_1_8);
    }

    @Override
    protected BlockState getBlockState(IFluidState state) {
        return this.block.get()
                         .getDefaultState()
                         .with(BlockGenericFluid.LEVEL, state.get(LEVEL_1_8))
                         .with(BlockGenericFluid.FULL, state.get(FALLING));
    }

    public BlockState getBlockstate(int level, boolean full) {
        if (level == 0) {
            return Blocks.AIR.getDefaultState();
        }
        return this.getFluidState(level, full).getBlockState();
    }

    @Override
    protected float getExplosionResistance() {
        return this.explosionResistance;
    }

    @Override
    public Item getFilledBucket() {
        return Items.AIR;
    }

    @Override
    public Vec3d getFlow(IBlockReader world, BlockPos pos, IFluidState state) {
        return Vec3d.ZERO;
    }

    @Override
    public Fluid getFlowingFluid() {
        return this.fluid.get();
    }

    public IFluidState getFluidState(int level, boolean full) {
        return this.getStillFluid().getDefaultState().with(LEVEL_1_8, level).with(FALLING, full);
    }

    public abstract byte getId();

    @Override
    protected int getLevelDecreasePerBlock(IWorldReader worldIn) {
        return this.levelDecreasePerBlock;
    }

    public int getMass() {
        return this.mass;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return this.renderLayer;
    }

    @Override
    protected int getSlopeFindDistance(IWorldReader worldIn) {
        return this.slopeFindDistance;
    }

    @Override
    public Fluid getStillFluid() {
        return this.fluid.get();
    }

    public abstract ITextComponent getTextComp();

    @Override
    public int getTickRate(IWorldReader world) {
        return this.tickRate;
    }

    public boolean isEquivalentOrEmpty(World world, BlockPos pos) {
        IFluidState state = world.getFluidState(pos);
        if (state.isEmpty()) {
            return true;
        }
        return state.getFluid() == this;
    }

    @Override
    public boolean isEquivalentTo(Fluid fluid) {
        return fluid == this.fluid.get();
    }

    /**
     * Return true if the operation was successful and the loop should break.
     * Return false if the operation was not successful and the loop should continue.
     */
    public abstract boolean level(World world, BlockPos pos, IFluidState fluidState, Direction direction, FluidGeneric otherFluid, int tolerance);

    public void replacedAt(World world, BlockPos pos, int amount) {
        Evolution.LOGGER.debug("{} replaced at {} with {}", this, pos, amount);
    }

    public void setBlockState(World world, BlockPos pos, int fluidAmount) {
        pos = pos.toImmutable();
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
        world.setBlockState(pos, stateForPlacement);
        if (!isFull) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TELiquid) {
                ((TELiquid) tile).setMissingLiquid(missing);
            }
            else {
                Evolution.LOGGER.warn("Invalid tile entity for fluid at {}: {}", pos, tile);
            }
        }
        else {
            world.removeTileEntity(pos);
        }
        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.offset(dir);
            BlockState stateAtOffset = world.getBlockState(offsetPos);
            if (stateAtOffset.getBlock() instanceof IFluidLoggable) {
                BlockUtils.scheduleBlockTick(world, offsetPos, this.tickRate);
            }
        }
    }

    @Override
    public void tick(World world, BlockPos pos, IFluidState fluidState) {
        if (this.tryFall(world, pos, fluidState)) {
            return;
        }
        this.auxList.clear();
        for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
            if (BlockUtils.canBeReplacedByFluid(world.getBlockState(this.auxPos.setPos(pos).move(direction)))) {
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
            this.auxPos.setPos(pos).move(direction);
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
            Fluid fluid = world.getFluidState(this.auxPos).getFluid();
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

    public boolean tryFall(World world, BlockPos pos, IFluidState fluidState) {
        BlockState state = world.getBlockState(pos);
        if (!canSendToOrReceiveFrom(state, Direction.DOWN)) {
            return false;
        }
        BlockPos posDown = pos.down();
        if (BlockUtils.canBeReplacedByFluid(world.getBlockState(posDown))) {
            if (isFull(world, posDown)) {
                return false;
            }
            BlockState stateAtFall = world.getBlockState(posDown);
            if (!canSendToOrReceiveFrom(stateAtFall, Direction.UP)) {
                return false;
            }
            IFluidState fluidAtFall = world.getFluidState(posDown);
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
            return this.tryFall(world, pos, fluidAtFall.getFluid());
        }
        return false;
    }

    public abstract boolean tryFall(World world, BlockPos pos, Fluid other);

    private void tryToLevel(World world, BlockPos pos, IFluidState state) {
        int rlThis = getFluidAmount(world, pos, state);
        if (rlThis == 0) {
            return;
        }
        if (!canSendToOrReceiveFrom(world.getBlockState(pos), Direction.DOWN)) {
            return;
        }
        this.auxPos.setPos(pos).move(Direction.DOWN);
        BlockState stateDown = world.getBlockState(this.auxPos);
        IFluidState fluidDown = world.getFluidState(this.auxPos);
        if (this == fluidDown.getFluid()) {
            this.diagList.clear();
            this.auxList.fillHorizontal();
            while (!this.auxList.isEmpty()) {
                Direction direction = this.auxList.getRandomAndRemove(MathHelper.RANDOM);
                if (!canSendToOrReceiveFrom(stateDown, direction)) {
                    continue;
                }
                this.auxPos.setPos(pos).move(Direction.DOWN).move(direction);
                BlockState stateAtPos = world.getBlockState(this.auxPos);
                if (!canSendToOrReceiveFrom(stateAtPos, direction.getOpposite())) {
                    continue;
                }
                IFluidState fluidAtPos = world.getFluidState(this.auxPos);
                if (this == fluidAtPos.getFluid()) {
                    if (!isFull(world, this.auxPos)) {
                        int rlAtPos = getFluidAmount(world, this.auxPos, fluidAtPos);
                        int amountForReplacement = MathHelper.clampMax(rlThis + rlAtPos, getCapacity(stateAtPos));
                        this.setBlockState(world, this.auxPos, amountForReplacement);
                        rlThis = rlThis - amountForReplacement + rlAtPos;
                        if (rlThis == 0) {
                            world.setBlockState(pos, Blocks.AIR.getDefaultState());
                            return;
                        }
                        this.setBlockState(world, pos, rlThis);
                    }
                    if (direction.getAxis() == Direction.Axis.X) {
                        for (Direction dir : MathHelper.DIRECTIONS_Z) {
                            if (!canSendToOrReceiveFrom(stateAtPos, dir)) {
                                continue;
                            }
                            this.auxPos.setPos(pos).move(Direction.DOWN).move(direction).move(dir);
                            BlockState stateDiag = world.getBlockState(this.auxPos);
                            if (!canSendToOrReceiveFrom(stateDiag, dir.getOpposite())) {
                                continue;
                            }
                            this.diagList.add(DirectionDiagonal.sum(dir, direction));
                        }
                    }
                    else {
                        for (Direction dir : MathHelper.DIRECTIONS_X) {
                            if (!canSendToOrReceiveFrom(stateAtPos, dir)) {
                                continue;
                            }
                            this.auxPos.setPos(pos).move(Direction.DOWN).move(direction).move(dir);
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
                diagonal.movePos(this.auxPos.setPos(pos).move(Direction.DOWN));
                IFluidState fluidAtPos = world.getFluidState(this.auxPos);
                if (this == fluidAtPos.getFluid()) {
                    if (isFull(world, this.auxPos)) {
                        continue;
                    }
                    int rlAtPos = getFluidAmount(world, this.auxPos, fluidAtPos);
                    int amountForReplacement = MathHelper.clampMax(rlThis + rlAtPos, getCapacity(world.getBlockState(this.auxPos)));
                    this.setBlockState(world, this.auxPos, amountForReplacement);
                    rlThis = rlThis - amountForReplacement + rlAtPos;
                    if (rlThis == 0) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
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
        private final BlockRenderLayer renderLayer = BlockRenderLayer.TRANSLUCENT;
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
