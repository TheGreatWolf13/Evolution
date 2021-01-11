package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.SoundEvent;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.EnumRockNames;
import tgw.evolution.util.EnumRockVariant;

public class BlockSand extends BlockGravity implements IStoneVariant {

    private final EnumRockNames name;
    private EnumRockVariant variant;

    public BlockSand(EnumRockNames name) {
        super(Block.Properties.create(Material.SAND).hardnessAndResistance(1.0F, 0.5F).sound(SoundType.SAND), name.getMass() / 8);
        this.name = name;
    }

    @Override
    public boolean canSlope() {
        return true;
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
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
