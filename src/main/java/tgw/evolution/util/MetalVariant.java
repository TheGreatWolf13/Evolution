package tgw.evolution.util;

public enum MetalVariant {
    COPPER(8_920, HarvestLevel.COPPER, 30.0F, 10.0F);
    private final int density;
    private final float hardness;
    private final int harvestLevel;
    private final float resistance;

    MetalVariant(int density, int harvestLevel, float hardness, float resistance) {
        this.density = density;
        this.harvestLevel = harvestLevel;
        this.hardness = hardness;
        this.resistance = resistance;
    }

    public int getDensity() {
        return this.density;
    }

    public float getHardness() {
        return this.hardness;
    }

    public int getHarvestLevel() {
        return this.harvestLevel;
    }

    public float getResistance() {
        return this.resistance;
    }
}
