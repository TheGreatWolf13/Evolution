package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.RockVariant;

public class BlockStoneBricks extends BlockGravity implements IStoneVariant {

    private final RockVariant variant;

    public BlockStoneBricks(RockVariant variant) {
        super(Block.Properties.create(Material.ROCK)
                              .hardnessAndResistance(variant.getRockType().getHardness(), 8.0F)
                              .sound(SoundType.STONE)
                              .harvestLevel(HarvestLevel.COPPER), variant.getMass());
        this.variant = variant;
    }

    @Override
    public int beamSize() {
        return this.variant.getRockType().getRangeStone() + 4;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0f;
    }

    @Override
    public int getShearStrength() {
        return (int) (this.variant.getShearStrength() * 1.2);
    }

    @Override
    public RockVariant getVariant() {
        return this.variant;
    }
}
