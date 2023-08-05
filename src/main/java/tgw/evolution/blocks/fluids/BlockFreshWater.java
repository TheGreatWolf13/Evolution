package tgw.evolution.blocks.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionFluids;

public class BlockFreshWater extends BlockGenericFluid {
    public BlockFreshWater() {
        super(EvolutionFluids.FRESH_WATER, Block.Properties.of(Material.WATER).noCollission().noDrops());
    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        return null;
    }

    @Override
    public double getMass(Level level, int x, int y, int z, BlockState state) {
        return 997 / 8.0 * state.getValue(LEVEL);
    }

    @Override
    public int tryDisplaceIn(Level level, BlockPos pos, BlockState state, FluidGeneric otherFluid, int amount) {
        switch (otherFluid.getId()) {
            case FluidGeneric.SALT_WATER -> {
                int amountAlreadyAtPos = FluidGeneric.getFluidAmount(level, pos, level.getFluidState(pos));
                int capacity = FluidGeneric.getCapacity(state);
                int placed = Math.min(amountAlreadyAtPos + amount, capacity);
                EvolutionFluids.SALT_WATER.setBlockState(level, pos, placed);
                amount = amount - placed + amountAlreadyAtPos;
                return amount;
            }
        }
        Evolution.warn("Try displace of " + this + " with " + otherFluid + " is not yet implemented!");
        return amount;
    }
}
