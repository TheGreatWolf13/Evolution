package tgw.evolution.inventory;

import net.minecraft.world.entity.EquipmentSlot;
import tgw.evolution.init.EvolutionResources;

public enum AdditionalSlotType {
    HAT("hat", EvolutionResources.HAT),
    BODY("body", EvolutionResources.BODY),
    LEGS("legs", EvolutionResources.LEGS),
    FEET("feet", EvolutionResources.FEET),
    CLOAK("cloak", EvolutionResources.CLOAK),
    MASK("mask", EvolutionResources.MASK),
    BACK("back", EvolutionResources.BACK),
    TACTICAL("tactical", EvolutionResources.TACTICAL);

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
