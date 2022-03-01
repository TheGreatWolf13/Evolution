package tgw.evolution.blocks;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.RockVariant;

public class BlockStoneBricks extends BlockGravity implements IRockVariant {

    private final RockVariant variant;

    public BlockStoneBricks(RockVariant variant) {
        super(Properties.of(Material.STONE).strength(variant.getRockType().getHardness(), 8.0F).sound(SoundType.STONE), variant.getMass());
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
    public int getHarvestLevel(BlockState state) {
        return HarvestLevel.LOW_METAL;
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
