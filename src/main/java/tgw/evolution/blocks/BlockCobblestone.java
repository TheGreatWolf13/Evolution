package tgw.evolution.blocks;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.util.constants.HarvestLevels;
import tgw.evolution.util.constants.RockVariant;

public class BlockCobblestone extends BlockGravity implements IRockVariant {

    private final RockVariant variant;

    public BlockCobblestone(RockVariant variant) {
        super(Properties.of(Material.STONE).strength(variant.getRockType().getHardness(), 6.0F).sound(SoundType.STONE), variant.getMass());
        this.variant = variant;
    }

    @Override
    public boolean canSlope() {
        return true;
    }

    @Override
    public boolean canSlopeFail() {
        return true;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0F;
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return HarvestLevels.LOW_METAL;
    }

    @Override
    public RockVariant getVariant() {
        return this.variant;
    }

    @Override
    public float slopeChance() {
        return 0.5f;
    }
}
