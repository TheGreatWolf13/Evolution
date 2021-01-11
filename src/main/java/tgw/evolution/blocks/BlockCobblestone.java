package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import tgw.evolution.util.EnumRockNames;
import tgw.evolution.util.EnumRockVariant;
import tgw.evolution.util.HarvestLevel;

public class BlockCobblestone extends BlockGravity implements IStoneVariant {

    private final EnumRockNames name;
    private EnumRockVariant variant;

    public BlockCobblestone(EnumRockNames name) {
        super(Block.Properties.create(Material.ROCK)
                              .hardnessAndResistance(name.getRockType().getHardness(), 6.0F)
                              .sound(SoundType.STONE)
                              .harvestLevel(HarvestLevel.STONE), name.getMass());
        this.name = name;
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
    public EnumRockNames getStoneName() {
        return this.name;
    }

    @Override
    public EnumRockVariant getVariant() {
        return this.variant;
    }

    @Override
    public void setVariant(EnumRockVariant variant) {
        this.variant = variant;
    }

    @Override
    public float slopeChance() {
        return 0.5f;
    }
}
