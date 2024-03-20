package tgw.evolution.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;

public class BlockGrassClay extends BlockGenericSpreadable implements IStructural, IFallable {

    public BlockGrassClay() {
        super(Properties.of(Material.CLAY).color(MaterialColor.GRASS).strength(3.0F, 0.6F).sound(SoundType.GRASS));
    }

    @Override
    public boolean canMakeABeamWith(BlockState thisState, BlockState otherState) {
        Block block = otherState.getBlock();
        return block == EvolutionBlocks.GRASS_CLAY || block == EvolutionBlocks.CLAY;
    }

    @Override
    public BlockState deadBlockState() {
        return EvolutionBlocks.CLAY.defaultBlockState();
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
    public int getIntegrity(BlockState state) {
        return 1;
    }

    @Override
    public Stabilization getStabilization(BlockState state) {
        return Stabilization.NONE;
    }
}
