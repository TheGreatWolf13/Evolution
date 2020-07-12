package tgw.evolution.blocks.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.util.HarvestLevel;

public class BlockFreshWater extends FlowingFluidBlock {

    public BlockFreshWater() {
        super(EvolutionFluids.FRESH_WATER_FLOWING, Block.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(HarvestLevel.UNBREAKABLE).noDrops());
    }
}
