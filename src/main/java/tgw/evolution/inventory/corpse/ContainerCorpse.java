package tgw.evolution.inventory.corpse;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionContainers;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.*;

public class ContainerCorpse extends AbstractContainerMenu implements IEvolutionContainer {

    private final BasicContainer corpse;

    /**
     * This constructor is called on the client
     */
    public ContainerCorpse(int windowId, Inventory inventory) {
        this(windowId, inventory, new BasicContainer(AdditionalSlotType.VALUES.length + 36 + 4 + 1) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack;
            }

            @Override
            public void onTake(int slot, Player player, ItemStack stackTaken, ItemStack newStack) {
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        });
    }

    /**
     * This constructor is called on the server directly and by the client via the constructor above.
     */
    public ContainerCorpse(int windowId, Inventory inventory, BasicContainer corpse) {
        super(EvolutionContainers.CORPSE, windowId);
        this.corpse = corpse;
        this.layoutPlayerInventorySlots(inventory, 8, 154);
        this.addArmorSlots(corpse);
        this.addClothesSlots(corpse);
        this.addEquipmentSlots(corpse);
        this.addSlotBox(corpse, 19, 8, 71, 9, 18, 4, 18);
        if (!inventory.player.level.isClientSide) {
            corpse.startOpen(inventory.player);
        }
    }

    private void addArmorSlots(BasicContainer holder) {
        this.addSlot(new SlotTexturedHandler(holder, 0, 8, 33, EvolutionResources.SLOT_ARMOR[SlotType.ARMOR_HEAD.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 1, 26, 33, EvolutionResources.SLOT_ARMOR[SlotType.ARMOR_CHEST.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 2, 44, 33, EvolutionResources.SLOT_ARMOR[SlotType.ARMOR_LEGS.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 3, 62, 33, EvolutionResources.SLOT_ARMOR[SlotType.ARMOR_FEET.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 13, 17, 51, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_SHOULDER_RIGHT.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 12, 17, 15, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_SHOULDER_LEFT.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 15, 35, 51, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_ARM_RIGHT.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 14, 35, 15, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_ARM_LEFT.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 17, 53, 51, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_HAND_RIGHT.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 16, 53, 15, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_HAND_LEFT.getIndex()]));
    }

    private void addClothesSlots(BasicContainer holder) {
        this.addSlot(new SlotTexturedHandler(holder, 7, 98, 51, EvolutionResources.SLOT_EXTENDED[SlotType.CLOTHES_FEET.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 6, 116, 51, EvolutionResources.SLOT_EXTENDED[SlotType.CLOTHES_LEGS.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 5, 134, 51, EvolutionResources.SLOT_EXTENDED[SlotType.CLOTHES_CHEST.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 4, 152, 51, EvolutionResources.SLOT_EXTENDED[SlotType.CLOTHES_HEAD.getIndex()]));
    }

    private void addEquipmentSlots(BasicContainer holder) {
        this.addSlot(new SlotTexturedHandler(holder, 11, 107, 33, EvolutionResources.SLOT_EXTENDED[SlotType.BELT.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 10, 125, 33, EvolutionResources.SLOT_EXTENDED[SlotType.BACK.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 8, 143, 33, EvolutionResources.SLOT_EXTENDED[SlotType.FACE.getIndex()]));
        this.addSlot(new SlotTexturedHandler(holder, 18, 116, 15, EvolutionResources.SLOT_OFFHAND));
        this.addSlot(new SlotTexturedHandler(holder, 9, 134, 15, EvolutionResources.SLOT_EXTENDED[SlotType.NECK.getIndex()]));
    }

    @Override
    public void addSlot(Container container, int index, int x, int y) {
        if (container == this.corpse && this.corpse.needsHandler(index)) {
            this.addSlot(new SlotTexturedHandler(this.corpse, index, x, y, null));
        }
        else {
            this.addSlot(new Slot(container, index, x, y));
        }
    }

    public Container getCorpse() {
        return this.corpse;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            if (index > 35) {
                if (!this.moveItemStackTo(stackInSlot, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, stack);
            }
            else if (index < 9) {
                if (!this.moveItemStackTo(stackInSlot, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, stack);
            }
            else {
                if (!this.moveItemStackTo(stackInSlot, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, stack);
            }
            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }
            slot.onTake(player, stackInSlot);
            if (stackInSlot.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level.isClientSide) {
            this.corpse.stopOpen(player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.corpse.stillValid(player);
    }
}
