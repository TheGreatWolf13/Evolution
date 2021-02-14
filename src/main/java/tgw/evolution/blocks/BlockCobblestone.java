package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.RockVariant;

public class BlockCobblestone extends BlockGravity implements IStoneVariant {

    private final RockVariant variant;

    public BlockCobblestone(RockVariant variant) {
        super(Block.Properties.create(Material.ROCK)
                              .hardnessAndResistance(variant.getRockType().getHardness(), 6.0F)
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
    public RockVariant getVariant() {
        return this.variant;
    }

    @Override
    public float slopeChance() {
        return 0.5f;
    }
}
