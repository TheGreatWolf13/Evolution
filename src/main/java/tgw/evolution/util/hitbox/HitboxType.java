package tgw.evolution.util.hitbox;

public enum HitboxType {
    //Body
    ALL(1.0f, false, false),
    //      Humanoid
    HEAD(1.75f, false, false),
    CHEST(1.25f, false, false),
    SHOULDER_RIGHT(0.875f, false, false),
    SHOULDER_LEFT(0.875f, false, false),
    ARM_RIGHT(0.75f, false, false),
    ARM_LEFT(0.75f, false, false),
    HAND_RIGHT(0.625f, false, false),
    HAND_LEFT(0.625f, false, false),
    LEG_RIGHT(1.0f, false, false),
    LEG_LEFT(1.0f, false, false),
    FOOT_RIGHT(0.75f, false, false),
    FOOT_LEFT(0.75f, false, false),
    //      Quadrupeds
    LEG_FRONT_RIGHT(0.75f, false, false),
    LEG_FRONT_LEFT(0.75f, false, false),
    LEG_HIND_RIGHT(0.75f, false, false),
    LEG_HIND_LEFT(0.75f, false, false),
    //Equipment
    AXE(0.0f, true, true),
    BLADE(0.0f, true, true),
    SPEAR(0.0f, true, true);

    private final boolean endsAttack;
    private final boolean isEquipment;
    private final float multiplier;

    HitboxType(float multiplier, boolean endsAttack, boolean isEquipment) {
        this.multiplier = multiplier;
        this.endsAttack = endsAttack;
        this.isEquipment = isEquipment;
    }

    public float getMultiplier() {
        return this.multiplier;
    }
}
