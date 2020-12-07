package tgw.evolution.blocks.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import tgw.evolution.init.EvolutionFluids;
import tgw.evolution.util.HarvestLevel;

public class BlockFreshWater extends BlockGenericFluid {

    public BlockFreshWater() {
        super(EvolutionFluids.FRESH_WATER,
              Block.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(HarvestLevel.UNBREAKABLE).noDrops());
    }
}
