package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.RockVariant;

public class BlockGravel extends BlockPhysics implements IRockVariant, ISloppable {

    private final RockVariant variant;

    public BlockGravel(RockVariant variant) {
        super(Properties.of(Material.SAND).strength(2.0F, 0.6F).sound(SoundType.GRAVEL));
        this.variant = variant;
    }

    @Override
    public boolean canSlope(BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSlopeFail() {
        return false;
    }

    @Override
    public SoundEvent fallingSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.5f;
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        return this.rockVariant().getMass() / 2;
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }

    @Override
    public float slopeChance() {
        return 1;
    }

    @Override
    public boolean slopes() {
        return true;
    }
}
