package tgw.evolution.inventory;

import net.minecraft.inventory.EquipmentSlotType;
import tgw.evolution.init.EvolutionResources;

public enum SlotType {
    HAT(EvolutionResources.HAT),
    BODY(EvolutionResources.BODY),
    LEGS(EvolutionResources.LEGS),
    FEET(EvolutionResources.FEET),
    CLOAK(EvolutionResources.CLOAK),
    MASK(EvolutionResources.MASK),
    BACK(EvolutionResources.BACK),
    TACTICAL(EvolutionResources.TACTICAL);

    public static final SlotType[] VALUES = values();
    public static final EquipmentSlotType[] SLOTS = EquipmentSlotType.values();

    private final int[] validSlots;

    SlotType(int... validSlots) {
        this.validSlots = validSlots;
    }

    public int[] getValidSlots() {
        return this.validSlots;
    }

    public boolean hasSlot(int slot) {
        for (int s : this.validSlots) {
            if (s == slot) {
                return true;
            }
        }
        return false;
    }
}
