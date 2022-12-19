package tgw.evolution.util.hitbox;

public enum HitboxType {
    //Generic
    ALL(1.0f, Float.POSITIVE_INFINITY, true, true, true),
    EQUIP(0.0f, Float.POSITIVE_INFINITY, false, false, false),
    NONE(0.0f, Float.POSITIVE_INFINITY, false, false, false),
    //      Humanoid
    HEAD(1.75f, 0.6f, true, true, true),
    CHEST(1.25f, 0.5f, true, true, true),
    SHOULDER_RIGHT(0.875f, 0.35f, true, false, true),
    SHOULDER_LEFT(0.875f, 0.35f, true, false, true),
    ARM_RIGHT(0.75f, 0.3f, true, false, true),
    ARM_LEFT(0.75f, 0.3f, true, false, true),
    HAND_RIGHT(0.625f, 0.4f, true, false, true),
    HAND_LEFT(0.625f, 0.4f, true, false, true),
    LEG_RIGHT(1.0f, 0.3f, true, false, true),
    LEG_LEFT(1.0f, 0.3f, true, false, true),
    FOOT_RIGHT(0.75f, 0.4f, true, false, true),
    FOOT_LEFT(0.75f, 0.4f, true, false, true),
    //      Body Parts
    NOSE(0.4f, Float.POSITIVE_INFINITY, false, false, false),
    //      Quadrupeds
    LEG_FRONT_RIGHT(0.75f, 0.3f, true, false, true),
    LEG_FRONT_LEFT(0.75f, 0.3f, true, false, true),
    LEG_HIND_RIGHT(0.75f, 0.3f, true, false, true),
    LEG_HIND_LEFT(0.75f, 0.3f, true, false, true),
    //      Octopeds
    LEG_FRONT_MIDDLE_RIGHT(0.75F, 0.3f, true, false, true),
    LEG_FRONT_MIDDLE_LEFT(0.75f, 0.3f, true, false, true),
    LEG_HIND_MIDDLE_RIGHT(0.75f, 0.3f, true, false, true),
    LEG_HIND_MIDDLE_LEFT(0.75f, 0.3f, true, false, true);

    /**
     * Bit 0: canBleed; <br>
     * Bit 1: isLethal; <br>
     * Bit 2: isFracturable; <br>
     */
    private final byte flags;
    private final float multiplier;
    private final float woundPercentage;

    HitboxType(float multiplier, float woundPercentage, boolean canBleed, boolean isLethal, boolean isFracturable) {
        this.multiplier = multiplier;
        this.woundPercentage = woundPercentage;
        byte temp = canBleed ? (byte) 1 : 0;
        if (isLethal) {
            temp |= 2;
        }
        if (isFracturable) {
            temp |= 4;
        }
        this.flags = temp;
    }

    public boolean canBleed() {
        return (this.flags & 1) != 0;
    }

    public float getMultiplier() {
        return this.multiplier;
    }

    public float getWoundPercentage() {
        return this.woundPercentage;
    }

    public boolean isFracturable() {
        return (this.flags & 4) != 0;
    }

    public boolean isLethal() {
        return (this.flags & 2) != 0;
    }
}
