package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import tgw.evolution.util.EnumRockNames;
import tgw.evolution.util.EnumRockVariant;
import tgw.evolution.util.HarvestLevel;

public class BlockStoneBricks extends BlockGravity implements IStoneVariant {

    private final EnumRockNames name;
    private EnumRockVariant variant;

    public BlockStoneBricks(EnumRockNames name) {
        super(Block.Properties.create(Material.ROCK)
                              .hardnessAndResistance(name.getRockType().getHardness(), 8.0F)
                              .sound(SoundType.STONE)
                              .harvestLevel(HarvestLevel.COPPER), name.getMass());
        this.name = name;
    }

    @Override
    public int beamSize() {
        return this.name.getRockType().getRangeStone() + 4;
    }

    @Override
    public int getShearStrength() {
        return (int) (this.name.getShearStrength() * 1.2);
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
}
