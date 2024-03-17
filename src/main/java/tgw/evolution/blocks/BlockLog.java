package tgw.evolution.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.WoodVariant;

import static tgw.evolution.init.EvolutionBStates.AXIS;

public class BlockLog extends BlockXYZAxis implements IWoodVariant, IStructural, IFallable {

    private final WoodVariant variant;

    public BlockLog(WoodVariant variant) {
        super(Properties.of(Material.WOOD).strength(8.0F, 2.0F).sound(SoundType.WOOD));
        this.variant = variant;
    }

    @Override
    public boolean canMakeABeamWith(BlockState thisState, BlockState otherState) {
        return otherState.getBlock() == this;
    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        return EvolutionSounds.WOOD_COLLAPSE;
    }

    @Override
    public BeamType getBeamType(BlockState state) {
        return switch (state.getValue(AXIS)) {
            case X -> BeamType.X_ARCH;
            case Y -> BeamType.NONE;
            case Z -> BeamType.Z_ARCH;
        };
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.7f;
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        return HarvestLevel.STONE;
    }

    @Override
    public int getIntegrity(BlockState state) {
        //TODO this needs balancing
        return 8;
    }

    @Override
    public Stabilization getStabilization(BlockState state) {
        return switch (state.getValue(AXIS)) {
            case X, Z -> Stabilization.ARCH;
            case Y -> Stabilization.NONE;
        };
    }

    @Override
    public WoodVariant woodVariant() {
        return this.variant;
    }
}
