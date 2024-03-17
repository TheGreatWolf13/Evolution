package tgw.evolution.util.constants;

public enum RockType {
    SEDIMENTARY(14.0F, 6),
    METAMORPHIC(15.0F, 7),
    IGNEOUS_INTRUSIVE(16.0F, 8),
    IGNEOUS_EXTRUSIVE(16.0F, 9);

    public final int baseIntegrity;
    public final float hardness;

    RockType(float hardness, int baseIntegrity) {
        this.hardness = hardness;
        this.baseIntegrity = baseIntegrity;
    }
}

