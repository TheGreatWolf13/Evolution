package tgw.evolution.blocks.fluids;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.DirectionList;
import tgw.evolution.util.MathHelper;

public abstract class FluidFreshWater extends ForgeFlowingFluid {

    public static final ResourceLocation FLUID_STILL = Evolution.location("block/fluid/fresh_water");
    public static final ResourceLocation FLUID_FLOWING = Evolution.location("block/fluid/fresh_water_flowing");
    private static final BlockPos.MutableBlockPos AUX_POS = new BlockPos.MutableBlockPos();
    private static final DirectionList AUX_LIST = new DirectionList();

    protected FluidFreshWater(Properties properties) {
        super(properties);
    }

    private static Properties makeProperties() {
        return new Properties(EvolutionFluids.FRESH_WATER, EvolutionFluids.FRESH_WATER_FLOWING, FluidAttributes.builder(FLUID_STILL, FLUID_FLOWING).color(0xa78a0000)).bucket(EvolutionItems.fresh_water_bucket).block(EvolutionBlocks.FRESH_WATER);
    }

    public static int getLevelFromState(IFluidState state) {
        return 8 - Math.min(state.getLevel(), 8) + (state.get(FALLING) ? 8 : 0);
    }

    @Override
    public Fluid getFlowingFluid() {
        return EvolutionFluids.FRESH_WATER_FLOWING.get();
    }

    @Override
    public Fluid getStillFluid() {
        return EvolutionFluids.FRESH_WATER.get();
    }

    @Override
    protected boolean canSourcesMultiply() {
        return false;
    }

    @Override
    protected void beforeReplacingBlock(IWorld world, BlockPos pos, BlockState state) {

    }

    @Override
    public IFluidState getStillFluidState(boolean falling) {
        return this.getStillFluid().getDefaultState().with(FALLING, falling);
    }

    @Override
    public IFluidState getFlowingFluidState(int level, boolean falling) {
        return this.getFlowingFluid().getDefaultState().with(LEVEL_1_8, level).with(FALLING, falling);
    }

    public boolean tryFall(World world, BlockPos pos, IFluidState fluidState) {
        AUX_POS.setPos(pos).move(Direction.DOWN);
        if (BlockUtils.isReplaceable(world.getBlockState(AUX_POS))) {
            IFluidState stateAtFall = world.getFluidState(AUX_POS);
            if (this.isEquivalentTo(stateAtFall.getFluid())) {
                int levelAtFall = stateAtFall.getLevel();
                if (levelAtFall == 8) {
                    return false;
                }
                int currentLevel = fluidState.getLevel();
                int levelForFall = Math.min(levelAtFall + currentLevel, 8);
                world.setBlockState(AUX_POS, this.getFlowingFluidState(levelForFall, false).getBlockState());
                int remainingLevel = currentLevel - levelForFall + levelAtFall;
                if (remainingLevel > 0) {
                    world.setBlockState(pos, this.getFlowingFluidState(remainingLevel, false).getBlockState());
                    return true;
                }
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                return true;
            }
            world.setBlockState(AUX_POS, fluidState.getBlockState());
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            return true;
        }
        return false;
    }

    @Override
    public void tick(World world, BlockPos pos, IFluidState fluidState) {
        if (this.tryFall(world, pos, fluidState)) {
            return;
        }
        AUX_LIST.clear();
        for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
            if (BlockUtils.isReplaceable(world.getBlockState(AUX_POS.setPos(pos).move(direction)))) {
                AUX_LIST.add(direction);
            }
        }
        while (!AUX_LIST.isEmpty()) {
            Direction direction = AUX_LIST.getRandom(MathHelper.RANDOM);
            AUX_LIST.remove(direction);
            AUX_POS.setPos(pos).move(direction);
            IFluidState fluidAtOffset = world.getFluidState(AUX_POS);
            int levelAlreadyAtPos = fluidAtOffset.getLevel();
            int currentLevel = fluidState.getLevel();
            if (levelAlreadyAtPos >= currentLevel) {
                continue;
            }
            int levelForPlacement = Math.min((levelAlreadyAtPos + currentLevel) / 2, 8);
            if (levelForPlacement == 0) {
                break;
            }
            int remainingLevel = currentLevel - levelForPlacement + levelAlreadyAtPos;
            IFluidState fluidStateForReplacement = this.getFlowingFluidState(remainingLevel, false);
            fluidState = fluidStateForReplacement;
            IFluidState fluidStateForPlacement = this.getFlowingFluidState(levelForPlacement, false);
            BlockState blockStateForReplacement = fluidStateForReplacement.getBlockState();
            BlockState blockStateForPlacement = fluidStateForPlacement.getBlockState();
            world.setBlockState(pos, blockStateForReplacement);
            world.setBlockState(AUX_POS, blockStateForPlacement);
            break;
        }
        this.tryToLevel(world, pos, fluidState);
    }

    public void tryToLevel(World world, BlockPos pos, IFluidState state) {
        int currentLevel = state.getLevel();
        if (currentLevel == 0) {
            return;
        }
        if (this.isEquivalentTo(world.getFluidState(AUX_POS.setPos(pos).move(Direction.DOWN)).getFluid())) {
            for (Direction direction : MathHelper.DIRECTIONS_HORIZONTAL) {
                AUX_POS.setPos(pos).move(Direction.DOWN).move(direction);
                IFluidState fluidAtOffset = world.getFluidState(AUX_POS);
                int levelAtOffset = fluidAtOffset.getLevel();
                if (this.isEquivalentTo(fluidAtOffset.getFluid()) && levelAtOffset < 8) {
                    int levelForReplacement = Math.min(levelAtOffset + currentLevel, 8);
                    int remainingLevel = currentLevel - levelForReplacement + levelAtOffset;
                    world.setBlockState(AUX_POS, this.getFlowingFluidState(levelForReplacement, false).getBlockState());
                    currentLevel = remainingLevel;
                    if (currentLevel > 0) {
                        world.setBlockState(pos, this.getFlowingFluidState(currentLevel, false).getBlockState());
                        continue;
                    }
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    return;
                }
            }
        }

    }

    @Override
    protected FluidAttributes createAttributes() {
        return super.createAttributes();
    }

    @Override
    protected int getSlopeFindDistance(IWorldReader world) {
        return 4;
    }

    @Override
    protected int getLevelDecreasePerBlock(IWorldReader world) {
        return 1;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public Item getFilledBucket() {
        return EvolutionItems.placeholder_item.get();
    }

    @Override
    protected boolean canDisplace(IFluidState state, IBlockReader world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public int getTickRate(IWorldReader world) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 100;
    }

    @Override
    protected BlockState getBlockState(IFluidState state) {
        return EvolutionBlocks.FRESH_WATER.get().getDefaultState().with(FlowingFluidBlock.LEVEL, getLevelFromState(state));
    }

    public static class Flowing extends FluidFreshWater {

        public Flowing() {
            super(FluidFreshWater.makeProperties());
        }

        @Override
        protected void fillStateContainer(StateContainer.Builder<Fluid, IFluidState> builder) {
            super.fillStateContainer(builder);
            builder.add(LEVEL_1_8);
        }

        @Override
        public int getLevel(IFluidState state) {
            return state.get(LEVEL_1_8);
        }

        @Override
        public boolean isSource(IFluidState state) {
            return false;
        }
    }

    public static class Source extends FluidFreshWater {

        public Source() {
            super(FluidFreshWater.makeProperties());
        }

        @Override
        public int getLevel(IFluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(IFluidState state) {
            return true;
        }
    }
}
