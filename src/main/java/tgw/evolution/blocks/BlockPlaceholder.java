package tgw.evolution.blocks;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class BlockPlaceholder extends BlockPhysics {

    public BlockPlaceholder() {
        super(Properties.of(Material.METAL).strength(2.0f, 3.0f).noDrops().sound(SoundType.METAL).lightLevel(s -> 0xFF0));
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0f;
    }
}
