package tgw.evolution.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.RockVariant;

public class BlockCobblestone extends BlockPhysics implements IRockVariant, ISloppable, IFillable {

    private final RockVariant variant;

    public BlockCobblestone(RockVariant variant) {
        super(Properties.of(Material.STONE).strength(variant.getRockType().hardness, 6.0F).sound(SoundType.STONE));
        this.variant = variant;
    }

    @Override
    public boolean canSlope(BlockGetter level, int x, int y, int z) {
        return true;
    }

    @Override
    public SoundEvent fallingSound() {
        return EvolutionSounds.STONE_COLLAPSE;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.8F;
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        return HarvestLevel.LOW_METAL;
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }

    @Override
    public float slopeChance() {
        return 0.5f;
    }
}
