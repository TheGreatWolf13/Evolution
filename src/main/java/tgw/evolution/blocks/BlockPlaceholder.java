package tgw.evolution.blocks;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class BlockPlaceholder extends BlockMass {

    public BlockPlaceholder() {
        super(Properties.of(Material.METAL).strength(2.0f, 3.0f).sound(SoundType.METAL), 1_000_000);
    }
}
