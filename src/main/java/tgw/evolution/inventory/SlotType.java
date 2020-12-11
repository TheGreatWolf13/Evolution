package tgw.evolution.inventory;

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
