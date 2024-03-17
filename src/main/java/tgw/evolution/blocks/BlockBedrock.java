package tgw.evolution.blocks;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.util.constants.HarvestLevel;

public class BlockBedrock extends BlockPhysics implements IStable {

    public BlockBedrock() {
        super(Properties.of(Material.STONE).strength(-1.0F, 3_600_000.0F).noDrops().isValidSpawn_(BlockUtils.NEVER_SPAWN));
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        return HarvestLevel.UNBREAKABLE;
    }
}
