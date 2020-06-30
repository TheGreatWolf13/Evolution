package tgw.evolution.inventory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import tgw.evolution.items.IAdditionalEquipment;

public class ContainerExtendedHandler extends ItemStackHandler implements IExtendedItemHandler {

    public static final int HAT = 0;
    public static final int BODY = 1;
    public static final int LEGS = 2;
    public static final int FEET = 3;
    public static final int CLOAK = 4;
    public static final int MASK = 5;
    public static final int BACK = 6;
    public static final int TACTICAL = 7;

    private static final int CLOTH_SLOTS = 8;
    private boolean[] changed = new boolean[CLOTH_SLOTS];

    public ContainerExtendedHandler() {
        super(CLOTH_SLOTS);
    }

    @Override
    public void setSize(int size) {
        if (size < CLOTH_SLOTS) {
            size = CLOTH_SLOTS;
        }
        super.setSize(size);
        boolean[] old = this.changed;
        this.changed = new boolean[size];
        for (int i = 0; i < old.length && i < this.changed.length; i++) {
            this.changed[i] = old[i];
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack, LivingEntity player) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!(stack.getItem() instanceof IAdditionalEquipment)) {
            return false;
        }
        IAdditionalEquipment item = (IAdditionalEquipment) stack.getItem();
        return item.getType().hasSlot(slot);
    }

    @Override
    protected void onContentsChanged(int slot) {
        this.setChanged(slot, true);
        this.serializeNBT();
    }

    @Override
    public void setChanged(int slot, boolean change) {
        if (this.changed == null) {
            this.changed = new boolean[this.getSlots()];
        }
        this.changed[slot] = change;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }
}
