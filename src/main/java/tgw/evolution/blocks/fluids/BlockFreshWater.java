package tgw.evolution.blocks.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.MathHelper;

public class BlockFreshWater extends BlockGenericFluid {

    public BlockFreshWater() {
        super(EvolutionFluids.FRESH_WATER, Block.Properties.of(Material.WATER).noCollission().harvestLevel(HarvestLevel.UNBREAKABLE).noDrops(), 997);
    }

    @Override
    public int tryDisplaceIn(World world, BlockPos pos, BlockState state, FluidGeneric otherFluid, int amount) {
        switch (otherFluid.getId()) {
            case FluidGeneric.SALT_WATER:
                int amountAlreadyAtPos = FluidGeneric.getFluidAmount(world, pos, world.getFluidState(pos));
                int capacity = FluidGeneric.getCapacity(state);
                int placed = MathHelper.clampMax(amountAlreadyAtPos + amount, capacity);
                EvolutionFluids.SALT_WATER.get().setBlockState(world, pos, placed);
                FluidGeneric.onReplace(world, pos, state);
                amount = amount - placed + amountAlreadyAtPos;
                return amount;
        }
        Evolution.LOGGER.warn("Try displace of " + this.getRegistryName() + " with " + otherFluid.getRegistryName() + " is not yet implemented!");
        return amount;
    }
}
