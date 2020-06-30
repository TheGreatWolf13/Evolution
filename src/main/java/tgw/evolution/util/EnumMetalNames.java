package tgw.evolution.util;

public enum EnumMetalNames {
    COPPER(8920, HarvestLevel.COPPER, 30F, 10F);
    private final int density;
    private final int harvestLevel;
    private final float hardness;
    private final float resistance;

    EnumMetalNames(int density, int harvestLevel, float hardness, float resistance) {
        this.density = density;
        this.harvestLevel = harvestLevel;
        this.hardness = hardness;
        this.resistance = resistance;
    }

    public int getDensity() {
        return this.density;
    }

    public int getHarvestLevel() {
        return this.harvestLevel;
    }

    public float getHardness() {
        return this.hardness;
    }

    public float getResistance() {
        return this.resistance;
    }
}
