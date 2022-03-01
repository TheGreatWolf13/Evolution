package tgw.evolution.inventory;

import net.minecraft.world.entity.EquipmentSlot;

public enum SlotType {
    MAINHAND,
    OFFHAND,
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    HAT,
    BODY,
    LEGS,
    FEET,
    CLOAK,
    MASK,
    BACK,
    TACTICAL;

    public static SlotType byEquipment(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> MAINHAND;
            case OFFHAND -> OFFHAND;
            case FEET -> BOOTS;
            case LEGS -> LEGGINGS;
            case CHEST -> CHESTPLATE;
            case HEAD -> HELMET;
        };
    }
}
