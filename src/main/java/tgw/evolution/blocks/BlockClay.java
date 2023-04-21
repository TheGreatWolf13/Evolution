package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.init.EvolutionSounds;

public class BlockClay extends BlockPhysics {

    public BlockClay() {
        super(Properties.of(Material.CLAY).strength(2.0F, 0.6F).sound(SoundType.GRAVEL));
    }

//    @Override

    @Override
    public SoundEvent fallingSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
    }
//    public int beamSize() {
//        return 1;
//    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.6F;
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        return 2_067;
    }
}
