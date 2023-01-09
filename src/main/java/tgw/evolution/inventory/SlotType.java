package tgw.evolution.inventory;

import net.minecraft.world.entity.EquipmentSlot;

public enum SlotType {
    HAND_MAIN(0),
    HAND_OFF(1),
    ARMOR_HEAD(3),
    ARMOR_CHEST(2),
    ARMOR_LEGS(1),
    ARMOR_FEET(0),
    ARMOR_SHOULDER_LEFT(8),
    ARMOR_SHOULDER_RIGHT(9),
    ARMOR_ARM_LEFT(10),
    ARMOR_ARM_RIGHT(11),
    ARMOR_HAND_LEFT(12),
    ARMOR_HAND_RIGHT(13),
    CLOTHES_HEAD(0),
    CLOTHES_CHEST(1),
    CLOTHES_LEGS(2),
    CLOTHES_FEET(3),
    FACE(4),
    NECK(5),
    BACK(6),
    BELT(7);

    public static final SlotType[] VALUES = values();

    private final int index;

    SlotType(int index) {
        this.index = index;
    }

    public static SlotType byEquipment(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> HAND_MAIN;
            case OFFHAND -> HAND_OFF;
            case FEET -> ARMOR_FEET;
            case LEGS -> ARMOR_LEGS;
            case CHEST -> ARMOR_CHEST;
            case HEAD -> ARMOR_HEAD;
        };
    }

    public int getIndex() {
        return this.index;
    }
}
