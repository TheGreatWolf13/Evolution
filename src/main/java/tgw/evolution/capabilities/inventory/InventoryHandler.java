package tgw.evolution.capabilities.inventory;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import tgw.evolution.items.IAdditionalEquipment;

import java.util.Map;

public class InventoryHandler extends ItemStackHandler implements IInventory {

    private static final int CLOTH_SLOTS = 8;
    private final Player player;
    private boolean[] changed = new boolean[CLOTH_SLOTS];

    public InventoryHandler(Player player) {
        super(CLOTH_SLOTS);
        this.player = player;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = super.extractItem(slot, amount, simulate);
        if (!this.player.level.isClientSide) {
            Item item = stack.getItem();
            if (item instanceof IAdditionalEquipment additionalEquipment) {
                for (Map.Entry<Attribute, AttributeModifier> entry : additionalEquipment.getAttributes(stack).reference2ObjectEntrySet()) {
                    AttributeInstance instance = this.player.getAttribute(entry.getKey());
                    assert instance != null;
                    instance.removeModifier(entry.getValue());
                }
            }
        }
        return stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack, LivingEntity player) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!(stack.getItem() instanceof IAdditionalEquipment item)) {
            return false;
        }
        return item.getValidSlot().isSlot(slot);
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
    public void setStackInSlot(int slot, ItemStack stack) {
        if (!this.player.level.isClientSide) {
            Item item = stack.getItem();
            if (item instanceof IAdditionalEquipment additionalEquipment) {
                for (Map.Entry<Attribute, AttributeModifier> entry : additionalEquipment.getAttributes(stack).reference2ObjectEntrySet()) {
                    AttributeInstance instance = this.player.getAttribute(entry.getKey());
                    assert instance != null;
                    instance.addPermanentModifier(entry.getValue());
                }
            }
        }
        super.setStackInSlot(slot, stack);
    }
}
