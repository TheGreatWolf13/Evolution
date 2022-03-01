package tgw.evolution.util.constants;

public enum RockType {
    SEDIMENTARY(14.0F, 4),
    METAMORPHIC(15.0F, 5),
    IGNEOUS_INTRUSIVE(16.0F, 6),
    IGNEOUS_EXTRUSIVE(16.0F, 6);

    private final float hardness;
    private final int rangeStone;

    RockType(float hardness, int rangeStone) {
        this.hardness = hardness;
        this.rangeStone = rangeStone;
    }

    public float getHardness() {
        return this.hardness;
    }

    public int getRangeStone() {
        return this.rangeStone;
    }
}

