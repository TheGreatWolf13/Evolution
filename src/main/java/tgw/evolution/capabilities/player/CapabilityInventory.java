package tgw.evolution.capabilities.player;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.inventory.BasicContainer;
import tgw.evolution.items.IAdditionalEquipment;
import tgw.evolution.util.collection.maps.R2OMap;

public class CapabilityInventory extends BasicContainer {

    private final Entity entity;

    public CapabilityInventory(Entity entity) {
        super(AdditionalSlotType.VALUES.length);
        this.entity = entity;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
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
    }

    @Override
    public void onTake(int slot, Player player, ItemStack stackTaken, ItemStack newStack) {
        //TODO implementation

    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = super.removeItem(slot, amount);
        if (!this.entity.level.isClientSide && this.entity instanceof LivingEntity living) {
            Item item = stack.getItem();
            if (item instanceof IAdditionalEquipment additionalEquipment) {
                R2OMap<Attribute, AttributeModifier> attributes = additionalEquipment.getAttributes(stack);
                for (R2OMap.Entry<Attribute, AttributeModifier> e = attributes.fastEntries(); e != null; e = attributes.fastEntries()) {
                    AttributeInstance instance = living.getAttribute(e.key());
                    assert instance != null;
                    instance.removeModifier(e.value());
                }
            }
        }
        return stack;
    }

    public void set(CapabilityInventory old) {
        NonNullList<ItemStack> oldItems = old.items;
        for (int i = 0, len = oldItems.size(); i < len; ++i) {
            this.items.set(i, oldItems.get(i));
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!this.entity.level.isClientSide) {
            Item item = stack.getItem();
            if (item instanceof IAdditionalEquipment additionalEquipment && this.entity instanceof LivingEntity living) {
                R2OMap<Attribute, AttributeModifier> attributes = additionalEquipment.getAttributes(stack);
                for (R2OMap.Entry<Attribute, AttributeModifier> e = attributes.fastEntries(); e != null; e = attributes.fastEntries()) {
                    AttributeInstance instance = living.getAttribute(e.key());
                    assert instance != null;
                    instance.addPermanentModifier(e.value());
                }
            }
        }
        super.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
