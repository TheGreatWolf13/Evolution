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
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.blocks.tileentities.TELiquid;
import tgw.evolution.util.DirectionDiagonal;
import tgw.evolution.util.DirectionDiagonalList;
import tgw.evolution.util.DirectionList;
import tgw.evolution.util.MathHelper;

import java.util.function.Supplier;

public abstract class FluidGeneric extends FlowingFluid {
    public static final int FRESH_WATER = 1;
    private final Supplier<? extends Fluid> fluid;
    private final Supplier<? extends BlockGenericFluid> block;
    private final FluidAttributes.Builder builder;
    private final int slopeFindDistance;
    private final int levelDecreasePerBlock;
    private final float explosionResistance;
    private final BlockRenderLayer renderLayer;
    private final int tickRate;
    private final BlockPos.MutableBlockPos auxPos = new BlockPos.MutableBlockPos();
    private final DirectionList auxList = new DirectionList();
    private final DirectionDiagonalList diagList = new DirectionDiagonalList();

    protected FluidGeneric(Properties properties) {
        this.fluid = properties.still;
        this.builder = properties.attributes;
        this.block = properties.block;
        this.slopeFindDistance = Properties.SLOPE_FIND_DISTANCE;
        this.levelDecreasePerBlock = Properties.LEVEL_DECREASE_PER_BLOCK;
        this.explosionResistance = Properties.EXPLOSION_RESISTANCE;
        this.renderLayer = properties.renderLayer;
        this.tickRate = Properties.TICK_RATE;
    }

    public static int getFluidAmount(World world, BlockPos pos, IFluidState state) {
        if (state.isEmpty()) {
            return 0;
        }
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

    public static boolean isFull(IFluidState state) {
        if (!state.get(FALLING)) {
            return false;
        }
        return state.get(LEVEL_1_8) == 8;
    }

    @Override
    protected void beforeReplacingBlock(IWorld world, BlockPos pos, BlockState state) {
        TileEntity tileentity = state.getBlock().hasTileEntity() ? world.getTileEntity(pos) : null;
        Block.spawnDrops(state, world.getWorld(), pos, tileentity);
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

    public abstract int getId();

    @Override
    protected int getLevelDecreasePerBlock(IWorldReader worldIn) {
        return this.levelDecreasePerBlock;
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

    @Override
    public boolean isEquivalentTo(Fluid fluid) {
        return fluid == this.fluid.get();
    }

    public void setBlockState(World world, BlockPos pos, int fluidAmount) {
        int layers = MathHelper.ceil(fluidAmount / 12_500.0f);
        int missing = layers * 12_500 - fluidAmount;
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
    }

    @Override
    public void tick(World world, BlockPos pos, IFluidState fluidState) {
        if (this.tryFall(world, pos, fluidState)) {
            return;
        }
        this.auxList.clear();
        for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
            if (BlockUtils.canBeReplacedByWater(world.getBlockState(this.auxPos.setPos(pos).move(direction)))) {
                this.auxList.add(direction);
            }
        }
        if (this.auxList.isEmpty()) {
            this.tryToLevel(world, pos, fluidState);
            return;
        }
        while (!this.auxList.isEmpty()) {
            Direction direction = this.auxList.getRandomAndRemove(MathHelper.RANDOM);
            this.auxPos.setPos(pos).move(direction);
            BlockState stateAtOffset = world.getBlockState(this.auxPos);
            IFluidState fluidAtOffset = stateAtOffset.getFluidState();
            int amountAlreadyAtPos = getFluidAmount(world, this.auxPos, fluidAtOffset);
            int currentAmount = getFluidAmount(world, pos, fluidState);
            if (amountAlreadyAtPos >= currentAmount - 250) {
                continue;
            }
            int amountForPlacement = MathHelper.clampMax(MathHelper.ceil((amountAlreadyAtPos + currentAmount) / 2.0f), 100_000);
            if (amountForPlacement == 0) {
                break;
            }
            int remainingAmount = currentAmount - amountForPlacement + amountAlreadyAtPos;
            if (remainingAmount > 0) {
                this.setBlockState(world, pos, remainingAmount);
            }
            else {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
            if (stateAtOffset.getBlock() instanceof IReplaceable) {
                ((IReplaceable) stateAtOffset.getBlock()).onReplaced(stateAtOffset, world, this.auxPos);
            }
            this.setBlockState(world, this.auxPos, amountForPlacement);
            break;
        }
    }

    public boolean tryFall(World world, BlockPos pos, IFluidState fluidState) {
        this.auxPos.setPos(pos).move(Direction.DOWN);
        if (BlockUtils.canBeReplacedByWater(world.getBlockState(this.auxPos))) {
            BlockState stateAtFall = world.getBlockState(this.auxPos);
            IFluidState fluidAtFall = stateAtFall.getFluidState();
            int amount = getFluidAmount(world, pos, fluidState);
            if (this.isEquivalentTo(fluidAtFall.getFluid())) {
                if (isFull(fluidAtFall)) {
                    return false;
                }
                int amountAtFall = getFluidAmount(world, this.auxPos, fluidAtFall);
                int amountForFall = MathHelper.clampMax(amount + amountAtFall, 100_000);
                this.setBlockState(world, this.auxPos, amountForFall);
                int amountRemaining = amount + amountAtFall - amountForFall;
                if (amountRemaining > 0) {
                    this.setBlockState(world, pos, amountRemaining);
                    return true;
                }
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                return true;
            }
            if (stateAtFall.getBlock() instanceof IReplaceable) {
                ((IReplaceable) stateAtFall.getBlock()).onReplaced(stateAtFall, world, this.auxPos);
            }
            this.setBlockState(world, this.auxPos, amount);
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            return true;
        }
        return false;
    }

    private void tryToLevel(World world, BlockPos pos, IFluidState state) {
        int currentAmount = getFluidAmount(world, pos, state);
        if (currentAmount == 0) {
            return;
        }
        if (this.isEquivalentTo(world.getFluidState(this.auxPos.setPos(pos).move(Direction.DOWN)).getFluid())) {
            this.diagList.clear();
            this.auxList.fillHorizontal();
            while (!this.auxList.isEmpty()) {
                Direction direction = this.auxList.getRandomAndRemove(MathHelper.RANDOM);
                this.auxPos.setPos(pos).move(Direction.DOWN).move(direction);
                IFluidState stateAtPos = world.getFluidState(this.auxPos);
                if (this.isEquivalentTo(stateAtPos.getFluid())) {
                    this.diagList.addFromDirection(direction);
                    int amountAtPos = getFluidAmount(world, this.auxPos, stateAtPos);
                    if (amountAtPos == 100_000) {
                        continue;
                    }
                    int amountForReplacement = MathHelper.clampMax(currentAmount + amountAtPos, 100_000);
                    this.setBlockState(world, this.auxPos, amountForReplacement);
                    currentAmount = currentAmount - amountForReplacement + amountAtPos;
                    if (currentAmount == 0) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        return;
                    }
                    this.setBlockState(world, pos, currentAmount);
                }
            }
            while (!this.diagList.isEmpty()) {
                DirectionDiagonal diagonal = this.diagList.getRandomAndRemove(MathHelper.RANDOM);
                diagonal.movePos(this.auxPos.setPos(pos).move(Direction.DOWN));
                IFluidState stateAtPos = world.getFluidState(this.auxPos);
                if (this.isEquivalentTo(stateAtPos.getFluid())) {
                    int amountAtPos = getFluidAmount(world, this.auxPos, stateAtPos);
                    if (amountAtPos == 100_000) {
                        continue;
                    }
                    int amountForReplacement = MathHelper.clampMax(currentAmount + amountAtPos, 100_000);
                    this.setBlockState(world, this.auxPos, amountForReplacement);
                    currentAmount = currentAmount - amountForReplacement + amountAtPos;
                    if (currentAmount == 0) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        return;
                    }
                    this.setBlockState(world, pos, currentAmount);
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
        private final BlockRenderLayer renderLayer = BlockRenderLayer.TRANSLUCENT;
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
