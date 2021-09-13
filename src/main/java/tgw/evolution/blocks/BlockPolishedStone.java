package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.RockVariant;

public class BlockPolishedStone extends BlockGravity implements IRockVariant {

    private final RockVariant variant;

    public BlockPolishedStone(RockVariant variant) {
        super(Properties.of(Material.STONE)
                        .strength(variant.getRockType().getHardness() / 2.0F, 6.0F)
                        .sound(SoundType.STONE)
                        .harvestLevel(HarvestLevel.STONE), variant.getMass());
        this.variant = variant;
    }

    @Override
    public int beamSize() {
        return this.variant.getRockType().getRangeStone() + 2;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0f;
    }

    @Override
    public int getShearStrength() {
        return this.variant.getShearStrength();
    }

    @Override
    public RockVariant getVariant() {
        return this.variant;
    }
}
