package tgw.evolution.inventory;

public enum SlotType {
    HAT(ContainerExtendedHandler.HAT),
    BODY(ContainerExtendedHandler.BODY),
    LEGS(ContainerExtendedHandler.LEGS),
    FEET(ContainerExtendedHandler.FEET),
    CLOAK(ContainerExtendedHandler.CLOAK),
    MASK(ContainerExtendedHandler.MASK),
    BACK(ContainerExtendedHandler.BACK),
    TACTICAL(ContainerExtendedHandler.TACTICAL);

    private final int[] validSlots;

    SlotType(int... validSlots) {
        this.validSlots = validSlots;
    }

    public boolean hasSlot(int slot) {
        for (int s : this.validSlots) {
            if (s == slot) {
                return true;
            }
        }
        return false;
    }

    public int[] getValidSlots() {
        return this.validSlots;
    }
}
