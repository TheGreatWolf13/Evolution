package tgw.evolution.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.RockVariant;

public class BlockClay extends BlockPhysics implements IStructural, IFallable {

    public BlockClay() {
        super(Properties.of(Material.CLAY).strength(2.0F, 0.6F).sound(SoundType.GRAVEL));
    }

    @Override
    public boolean canMakeABeamWith(BlockState thisState, BlockState otherState) {
        Block block = otherState.getBlock();
        return block == EvolutionBlocks.CLAY || block == EvolutionBlocks.GRASSES.get(RockVariant.CLAY);
    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        return EvolutionSounds.SOIL_COLLAPSE;
    }

    @Override
    public BeamType getBeamType(BlockState state) {
        return BeamType.CARDINAL_BEAM;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.6F;
    }

    @Override
    public int getIntegrity(BlockState state) {
        return 1;
    }

    @Override
    public Stabilization getStabilization(BlockState state) {
        return Stabilization.BEAM;
    }
}
