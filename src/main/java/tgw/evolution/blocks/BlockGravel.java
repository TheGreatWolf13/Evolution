package tgw.evolution.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.RockVariant;

public class BlockGravel extends BlockPhysics implements IRockVariant, ISloppable, IFillable {

    private final RockVariant variant;

    public BlockGravel(RockVariant variant) {
        super(Properties.of(Material.SAND).strength(2.0F, 0.6F).sound(SoundType.GRAVEL));
        this.variant = variant;
    }

    @Override
    public boolean canSlope(BlockGetter level, int x, int y, int z) {
        return true;
    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        return EvolutionSounds.SOIL_COLLAPSE;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.57f;
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }

    @Override
    public float slopeChance() {
        return 1;
    }
}
