package tgw.evolution.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockPlaceholder extends BlockMass {

    public BlockPlaceholder() {
        super(Properties.create(Material.IRON).hardnessAndResistance(2.0f, 3.0f).sound(SoundType.METAL), 1_000_000);
    }
}
