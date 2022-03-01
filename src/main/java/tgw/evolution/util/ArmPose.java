package tgw.evolution.util;

public enum ArmPose {
    EMPTY,
    ITEM,
    BLOCK,
    BOW_AND_ARROW,
    THROW_SPEAR,
    CROSSBOW_CHARGE,
    CROSSBOW_HOLD;

    public boolean isTwoHanded() {
        return switch (this) {
            case CROSSBOW_CHARGE, BOW_AND_ARROW, CROSSBOW_HOLD -> true;
            default -> false;
        };
    }
}
