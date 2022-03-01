package tgw.evolution.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
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
