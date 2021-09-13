package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.RockVariant;

public class BlockCobblestone extends BlockGravity implements IRockVariant {

    private final RockVariant variant;

    public BlockCobblestone(RockVariant variant) {
        super(Properties.of(Material.STONE)
                        .strength(variant.getRockType().getHardness(), 6.0F)
                        .sound(SoundType.STONE)
                        .harvestLevel(HarvestLevel.STONE), variant.getMass());
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
    public RockVariant getVariant() {
        return this.variant;
    }

    @Override
    public float slopeChance() {
        return 0.5f;
    }
}
