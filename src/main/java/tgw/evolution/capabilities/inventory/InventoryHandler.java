package tgw.evolution.capabilities.inventory;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.items.IAdditionalEquipment;

import java.util.Map;

public class InventoryHandler extends ItemStackHandler implements IInventory {

    private final Entity entity;

    public InventoryHandler(Entity entity) {
        super(AdditionalSlotType.VALUES.length);
        this.entity = entity;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = super.extractItem(slot, amount, simulate);
        if (!this.entity.level.isClientSide && this.entity instanceof LivingEntity living) {
            Item item = stack.getItem();
            if (item instanceof IAdditionalEquipment additionalEquipment) {
                for (Map.Entry<Attribute, AttributeModifier> entry : additionalEquipment.getAttributes(stack).reference2ObjectEntrySet()) {
                    AttributeInstance instance = living.getAttribute(entry.getKey());
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
        this.serializeNBT();
    }

    @Override
    public void setSize(int size) {
        if (size < AdditionalSlotType.VALUES.length) {
            size = AdditionalSlotType.VALUES.length;
        }
        super.setSize(size);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (!this.entity.level.isClientSide) {
            Item item = stack.getItem();
            if (item instanceof IAdditionalEquipment additionalEquipment && this.entity instanceof LivingEntity living) {
                for (Map.Entry<Attribute, AttributeModifier> entry : additionalEquipment.getAttributes(stack).reference2ObjectEntrySet()) {
                    AttributeInstance instance = living.getAttribute(entry.getKey());
                    assert instance != null;
                    instance.addPermanentModifier(entry.getValue());
                }
            }
        }
        super.setStackInSlot(slot, stack);
    }
}
