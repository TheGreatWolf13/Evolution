package tgw.evolution.inventory;

import net.minecraft.world.entity.EquipmentSlot;

public enum AdditionalSlotType {
    CLOTHES_HEAD("clothes_head", SlotType.CLOTHES_HEAD.getIndex()),
    CLOTHES_CHEST("clothes_chest", SlotType.CLOTHES_CHEST.getIndex()),
    CLOTHES_LEGS("clothes_legs", SlotType.CLOTHES_LEGS.getIndex()),
    CLOTHES_FEET("clothes_feet", SlotType.CLOTHES_FEET.getIndex()),
    FACE("face", SlotType.FACE.getIndex()),
    NECK("neck", SlotType.NECK.getIndex()),
    BACK("back", SlotType.BACK.getIndex()),
    BELT("belt", SlotType.BELT.getIndex()),
    ARMOR_SHOULDER_LEFT("armor_shoulder_left", SlotType.ARMOR_SHOULDER_LEFT.getIndex()),
    ARMOR_SHOULDER_RIGHT("armor_shoulder_right", SlotType.ARMOR_SHOULDER_RIGHT.getIndex()),
    ARMOR_ARM_LEFT("armor_arm_left", SlotType.ARMOR_ARM_LEFT.getIndex()),
    ARMOR_ARM_RIGHT("armor_arm_right", SlotType.ARMOR_ARM_RIGHT.getIndex()),
    ARMOR_HAND_LEFT("armor_hand_left", SlotType.ARMOR_HAND_LEFT.getIndex()),
    ARMOR_HAND_RIGHT("armor_hand_right", SlotType.ARMOR_HAND_RIGHT.getIndex());

    public static final AdditionalSlotType[] VALUES = values();
    public static final EquipmentSlot[] SLOTS = EquipmentSlot.values();
    private final String name;
    private final int slotId;

    AdditionalSlotType(String name, int slotId) {
        this.name = name;
        this.slotId = slotId;
    }

    public String getName() {
        return this.name;
    }

    public int getSlotId() {
        return this.slotId;
    }

    public boolean isSlot(int slot) {
        return this.slotId == slot;
    }
}
