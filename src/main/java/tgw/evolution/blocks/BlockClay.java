package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.SoundEvent;
import tgw.evolution.init.EvolutionSounds;

public class BlockClay extends BlockGravity {

    public BlockClay() {
        super(Properties.of(Material.CLAY).strength(2.0F, 0.6F).sound(SoundType.GRAVEL), 2_067);
    }

    @Override
    public int beamSize() {
        return 1;
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.45F;
    }
}
