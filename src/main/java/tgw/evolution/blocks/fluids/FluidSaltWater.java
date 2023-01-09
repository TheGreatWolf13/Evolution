package tgw.evolution.blocks.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidAttributes;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public abstract class FluidSaltWater extends FluidGeneric {

    protected FluidSaltWater(Properties properties) {
        super(properties, 1_023);
    }

    private static Properties makeProperties() {
        return new Properties(EvolutionFluids.SALT_WATER,
                              FluidAttributes.builder(EvolutionResources.FLUID_FRESH_WATER, EvolutionResources.FLUID_FRESH_WATER)
                                             .color(0x4009_00b3)).block(EvolutionBlocks.SALT_WATER);
    }

    @Override
    public int getAmount(FluidState state) {
        return state.getValue(LEVEL);
    }

    @Override
    public byte getId() {
        return SALT_WATER;
    }

    @Override
    public Component getTextComp() {
        return EvolutionTexts.FLUID_SALT_WATER;
    }

    @Override
    public boolean level(Level level, BlockPos pos, FluidState fluidState, Direction direction, FluidGeneric otherFluid, int tolerance) {
        BlockPos.MutableBlockPos auxPos = new BlockPos.MutableBlockPos();
        auxPos.set(pos).move(direction);
        BlockState stateAtOffset = level.getBlockState(auxPos);
        //noinspection SwitchStatementWithTooFewBranches
        switch (otherFluid.getId()) {
            case FRESH_WATER -> {
                int apAtPos = getApparentAmount(level, auxPos);
                int apThis = getApparentAmount(level, pos);
                if (apAtPos >= apThis - tolerance) {
                    return false;
                }
                int apMean = Mth.ceil((apAtPos + apThis) / 2.0);
                int rlThis = getFluidAmount(level, pos, fluidState);
                int rlAtPos = getFluidAmount(level, auxPos, level.getFluidState(auxPos));
                int amountToSwap = Math.min(apMean - apAtPos, rlThis);
                if (amountToSwap == 0) {
                    return true;
                }
                int stay = rlThis - amountToSwap;
                this.setBlockState(level, pos, stay);
                int receive = amountToSwap + rlAtPos;
                this.setBlockState(level, auxPos, receive);
                return true;
            }
        }
        Evolution.warn("Level of " + this.getRegistryName() + " with " + otherFluid.getRegistryName() + " is not yet implemented!");
        return false;
    }

    @Override
    public boolean tryFall(Level level, BlockPos pos, Fluid otherFluid) {
        Evolution.warn("Try fall of " + this.getRegistryName() + " with " + otherFluid.getRegistryName() + " is not yet implemented!");
        return false;
    }

    public static class Flowing extends FluidSaltWater {

        public Flowing() {
            super(FluidSaltWater.makeProperties());
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends FluidSaltWater {

        public Source() {
            super(FluidSaltWater.makeProperties());
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
