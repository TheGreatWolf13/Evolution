package tgw.evolution.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.RockVariant;

public class BlockPolishedStone extends BlockPhysics implements IRockVariant, IFallable, IStructural {

    private final RockVariant variant;

    public BlockPolishedStone(RockVariant variant) {
        super(Properties.of(Material.STONE).strength(variant.getRockType().hardness / 2.0F, 6.0F).sound(SoundType.STONE));
        this.variant = variant;
    }

    @Override
    public boolean canMakeABeamWith(BlockState thisState, BlockState otherState) {
        return otherState.getBlock() instanceof BlockPolishedStone;
    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        return EvolutionSounds.STONE_COLLAPSE;
    }

    @Override
    public BeamType getBeamType(BlockState state) {
        return BeamType.CARDINAL_ARCH;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.8f;
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        return HarvestLevel.LOW_METAL;
    }

    @Override
    public int getIntegrity(BlockState state) {
        return this.variant.getRockType().baseIntegrity;
    }

    @Override
    public Stabilization getStabilization(BlockState state) {
        return Stabilization.ARCH;
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }
}
