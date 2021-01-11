package tgw.evolution.blocks.fluids;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.MathHelper;

public abstract class FluidSaltWater extends FluidGeneric {

    protected FluidSaltWater(Properties properties) {
        super(properties, 1_023);
    }

    private static Properties makeProperties() {
        return new Properties(EvolutionFluids.SALT_WATER,
                              FluidAttributes.builder(EvolutionResources.FRESH_WATER, EvolutionResources.FRESH_WATER).color(0x4009_00b3)).block(
                EvolutionBlocks.SALT_WATER);
    }

    @Override
    public byte getId() {
        return SALT_WATER;
    }

    @Override
    public int getLevel(IFluidState state) {
        return state.get(LEVEL_1_8);
    }

    @Override
    public ITextComponent getTextComp() {
        return EvolutionTexts.FLUID_SALT_WATER;
    }

    @Override
    public boolean level(World world, BlockPos pos, IFluidState fluidState, Direction direction, FluidGeneric otherFluid, int tolerance) {
        BlockPos.MutableBlockPos auxPos = new BlockPos.MutableBlockPos(pos).move(direction);
        BlockState stateAtOffset = world.getBlockState(auxPos);
        //noinspection SwitchStatementWithTooFewBranches
        switch (otherFluid.getId()) {
            case FRESH_WATER:
                int apAtPos = getApparentAmount(world, auxPos);
                int apThis = getApparentAmount(world, pos);
                if (apAtPos >= apThis - tolerance) {
                    return false;
                }
                int apMean = MathHelper.ceil((apAtPos + apThis) / 2.0);
                int rlThis = getFluidAmount(world, pos, fluidState);
                int rlAtPos = getFluidAmount(world, auxPos, world.getFluidState(auxPos));
                int amountToSwap = MathHelper.clampMax(apMean - apAtPos, rlThis);
                if (amountToSwap == 0) {
                    return true;
                }
                int stay = rlThis - amountToSwap;
                this.setBlockState(world, pos, stay);
                onReplace(world, auxPos, stateAtOffset);
                int receive = amountToSwap + rlAtPos;
                this.setBlockState(world, auxPos, receive);
                return true;
        }
        Evolution.LOGGER.warn("Level of " + this.getRegistryName() + " with " + otherFluid.getRegistryName() + " is not yet implemented!");
        return false;
    }

    @Override
    public boolean tryFall(World world, BlockPos pos, Fluid otherFluid) {
        Evolution.LOGGER.warn("Try fall of " + this.getRegistryName() + " with " + otherFluid.getRegistryName() + " is not yet implemented!");
        return false;
    }

    public static class Flowing extends FluidSaltWater {

        public Flowing() {
            super(FluidSaltWater.makeProperties());
        }

        @Override
        public boolean isSource(IFluidState state) {
            return false;
        }
    }

    public static class Source extends FluidSaltWater {

        public Source() {
            super(FluidSaltWater.makeProperties());
        }

        @Override
        public boolean isSource(IFluidState state) {
            return true;
        }
    }
}
