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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IReplaceable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.DirectionDiagonal;
import tgw.evolution.util.DirectionDiagonalList;
import tgw.evolution.util.DirectionList;
import tgw.evolution.util.MathHelper;

public abstract class FluidFreshWater extends ForgeFlowingFluid {

    public static final ResourceLocation FLUID_STILL = Evolution.location("block/fluid/fresh_water");
    public static final ResourceLocation FLUID_FLOWING = Evolution.location("block/fluid/fresh_water_flowing");
    private static final BlockPos.MutableBlockPos AUX_POS = new BlockPos.MutableBlockPos();
    private static final DirectionList AUX_LIST = new DirectionList();
    private static final DirectionDiagonalList DIAG_LIST = new DirectionDiagonalList();

    protected FluidFreshWater(Properties properties) {
        super(properties);
    }

    private static Properties makeProperties() {
        return new Properties(EvolutionFluids.FRESH_WATER,
                              EvolutionFluids.FRESH_WATER_FLOWING,
                              FluidAttributes.builder(FLUID_STILL, FLUID_FLOWING).color(0x40030ffc)).block(EvolutionBlocks.FRESH_WATER);
    }

    public static int getLevelFromState(IFluidState state) {
        return 8 - Math.min(state.getLevel(), 8) + (state.get(FALLING) ? 8 : 0);
    }

    public static Fluid getFluidFlowing() {
        return EvolutionFluids.FRESH_WATER_FLOWING.get();
    }

    public static IFluidState getFlowingState(int level, boolean falling) {
        return getFluidFlowing().getDefaultState().with(LEVEL_1_8, level).with(FALLING, falling);
    }

    @Override
    protected boolean canSourcesMultiply() {
        return false;
    }

    public boolean tryFall(World world, BlockPos pos, IFluidState fluidState) {
        AUX_POS.setPos(pos).move(Direction.DOWN);
        if (BlockUtils.canBeReplacedByWater(world.getBlockState(AUX_POS))) {
            BlockState stateAtFall = world.getBlockState(AUX_POS);
            if (this.isEquivalentTo(stateAtFall.getFluidState().getFluid())) {
                int levelAtFall = stateAtFall.getFluidState().getLevel();
                if (levelAtFall == 8) {
                    return false;
                }
                int currentLevel = fluidState.getLevel();
                int levelForFall = Math.min(levelAtFall + currentLevel, 8);
                world.setBlockState(AUX_POS, getFlowingState(levelForFall, false).getBlockState());
                int remainingLevel = currentLevel - levelForFall + levelAtFall;
                if (remainingLevel > 0) {
                    world.setBlockState(pos, getFlowingState(remainingLevel, false).getBlockState());
                    return true;
                }
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                return true;
            }
            if (stateAtFall.getBlock() instanceof IReplaceable) {
                ((IReplaceable) stateAtFall.getBlock()).onReplaced(stateAtFall, world, AUX_POS);
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
            if (BlockUtils.canBeReplacedByWater(world.getBlockState(AUX_POS.setPos(pos).move(direction)))) {
                AUX_LIST.add(direction);
            }
        }
        if (AUX_LIST.isEmpty()) {
            this.tryToLevel(world, pos, fluidState);
            return;
        }
        while (!AUX_LIST.isEmpty()) {
            Direction direction = AUX_LIST.getRandomAndRemove(MathHelper.RANDOM);
            AUX_POS.setPos(pos).move(direction);
            BlockState stateAtOffset = world.getBlockState(AUX_POS);
            int levelAlreadyAtPos = stateAtOffset.getFluidState().getLevel();
            int currentLevel = fluidState.getLevel();
            if (levelAlreadyAtPos >= currentLevel) {
                continue;
            }
            if (levelAlreadyAtPos + 1 == currentLevel) {
                if (BlockUtils.willFluidAllowGap(world, AUX_POS, direction, this, currentLevel)) {
                    continue;
                }
            }
            int levelForPlacement = Math.min(MathHelper.ceil((levelAlreadyAtPos + currentLevel) / 2f), 8);
            if (levelForPlacement == 0) {
                break;
            }
            int remainingLevel = currentLevel - levelForPlacement + levelAlreadyAtPos;
            if (remainingLevel > 0) {
                world.setBlockState(pos, getFlowingState(remainingLevel, false).getBlockState());
            }
            else {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
            if (stateAtOffset.getBlock() instanceof IReplaceable) {
                ((IReplaceable) stateAtOffset.getBlock()).onReplaced(stateAtOffset, world, AUX_POS);
            }
            world.setBlockState(AUX_POS, getFlowingState(levelForPlacement, false).getBlockState());
            break;
        }
    }

    public void tryToLevel(World world, BlockPos pos, IFluidState state) {
        int currentLevel = state.getLevel();
        if (currentLevel == 0) {
            return;
        }
        if (this.isEquivalentTo(world.getFluidState(AUX_POS.setPos(pos).move(Direction.DOWN)).getFluid())) {
            DIAG_LIST.clear();
            AUX_LIST.fillHorizontal();
            while (!AUX_LIST.isEmpty()) {
                Direction direction = AUX_LIST.getRandomAndRemove(MathHelper.RANDOM);
                AUX_POS.setPos(pos).move(Direction.DOWN).move(direction);
                IFluidState stateAtPos = world.getFluidState(AUX_POS);
                if (this.isEquivalentTo(stateAtPos.getFluid())) {
                    DIAG_LIST.addFromDirection(direction);
                    int levelAtPos = stateAtPos.getLevel();
                    if (levelAtPos == 8) {
                        continue;
                    }
                    int levelForReplacement = Math.min(currentLevel + levelAtPos, 8);
                    world.setBlockState(AUX_POS, getFlowingState(levelForReplacement, false).getBlockState());
                    currentLevel = currentLevel - levelForReplacement + levelAtPos;
                    if (currentLevel == 0) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        return;
                    }
                    world.setBlockState(pos, getFlowingState(currentLevel, false).getBlockState());
                }
            }
            while (!DIAG_LIST.isEmpty()) {
                DirectionDiagonal diagonal = DIAG_LIST.getRandomAndRemove(MathHelper.RANDOM);
                diagonal.movePos(AUX_POS.setPos(pos).move(Direction.DOWN));
                IFluidState stateAtPos = world.getFluidState(AUX_POS);
                if (this.isEquivalentTo(stateAtPos.getFluid())) {
                    int levelAtPos = stateAtPos.getLevel();
                    if (levelAtPos == 8) {
                        continue;
                    }
                    int levelForReplacement = Math.min(currentLevel + levelAtPos, 8);
                    world.setBlockState(AUX_POS, getFlowingState(levelForReplacement, false).getBlockState());
                    currentLevel = currentLevel - levelForReplacement + levelAtPos;
                    if (currentLevel == 0) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        return;
                    }
                    world.setBlockState(pos, getFlowingState(currentLevel, false).getBlockState());
                }
            }
        }
    }

    @Override
    public Vec3d getFlow(IBlockReader p_215663_1_, BlockPos p_215663_2_, IFluidState p_215663_3_) {
        return Vec3d.ZERO;
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
