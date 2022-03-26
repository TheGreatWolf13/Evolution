package tgw.evolution.util.hitbox;

public enum HitboxType {
    //Body
    ALL(1.0f, false, false),
    //      Humanoid
    HEAD(1.75f, false, false),
    CHEST(1.25f, false, false),
    RIGHT_SHOULDER(1.0f, false, false),
    LEFT_SHOULDER(1.0f, false, false),
    RIGHT_ARM(0.8f, false, false),
    LEFT_ARM(0.8f, false, false),
    RIGHT_HAND(0.75f, false, false),
    LEFT_HAND(0.75f, false, false),
    RIGHT_LEG(1.0f, false, false),
    LEFT_LEG(1.0f, false, false),
    RIGHT_FOOT(0.75f, false, false),
    LEFT_FOOT(0.75f, false, false),
    //      Quadrupeds
    REAR_RIGHT_LEG(0.75f, false, false),
    REAR_LEFT_LEG(0.75f, false, false),
    FRONT_RIGHT_LEG(0.75f, false, false),
    FRONT_LEFT_LEG(0.75f, false, false),
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
