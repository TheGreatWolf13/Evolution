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
        switch (this) {
            case CROSSBOW_CHARGE:
            case BOW_AND_ARROW:
            case CROSSBOW_HOLD: {
                return true;
            }
        }
        return false;
    }
}
