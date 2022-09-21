package tgw.evolution.inventory.corpse;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.EvolutionContainers;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.IEvolutionContainer;
import tgw.evolution.inventory.SlotType;

public class ContainerCorpse extends AbstractContainerMenu implements IEvolutionContainer {

    private final EntityPlayerCorpse corpse;

    public ContainerCorpse(int windowId, EntityPlayerCorpse corpse, Inventory inventory) {
        super(EvolutionContainers.CORPSE.get(), windowId);
        this.corpse = corpse;
        IItemHandler playerInventory = new InvWrapper(inventory);
        this.layoutPlayerInventorySlots(playerInventory, 8, 154);
        IItemHandler corpseHandler = EvolutionCapabilities.getCapabilityOrThrow(corpse, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        this.addArmorSlots(corpseHandler);
        this.addClothesSlots(corpseHandler);
        this.addEquipmentSlots(corpseHandler);
        this.addSlotBox(corpseHandler, 19, 8, 71, 9, 18, 4, 18);
        if (!corpse.level.isClientSide) {
            corpse.onOpen(inventory.player);
        }
    }

    private void addArmorSlots(IItemHandler handler) {
        this.addSlot(new SlotItemHandler(handler, 0, 8, 33) {
            @Override
            public void onTake(Player player, ItemStack stackTaken) {
                ContainerCorpse.this.corpse.setSlot(EquipmentSlot.HEAD, this.getItem().copy());
                super.onTake(player, stackTaken);
            }
        }.setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_ARMOR[SlotType.ARMOR_HEAD.getIndex()]));
        this.addSlot(new SlotItemHandler(handler, 1, 26, 33) {
            @Override
            public void onTake(Player player, ItemStack stackTaken) {
                ContainerCorpse.this.corpse.setSlot(EquipmentSlot.CHEST, this.getItem().copy());
                super.onTake(player, stackTaken);
            }
        }.setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_ARMOR[SlotType.ARMOR_CHEST.getIndex()]));
        this.addSlot(new SlotItemHandler(handler, 2, 44, 33) {
            @Override
            public void onTake(Player player, ItemStack stackTaken) {
                ContainerCorpse.this.corpse.setSlot(EquipmentSlot.LEGS, this.getItem().copy());
                super.onTake(player, stackTaken);
            }
        }.setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_ARMOR[SlotType.ARMOR_LEGS.getIndex()]));
        this.addSlot(new SlotItemHandler(handler, 3, 62, 33) {
            @Override
            public void onTake(Player player, ItemStack stackTaken) {
                ContainerCorpse.this.corpse.setSlot(EquipmentSlot.FEET, this.getItem().copy());
                super.onTake(player, stackTaken);
            }
        }.setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_ARMOR[SlotType.ARMOR_FEET.getIndex()]));
        this.addSlot(new SlotItemHandler(handler, 13, 17, 51))
            .setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_SHOULDER_RIGHT.getIndex()]);
        this.addSlot(new SlotItemHandler(handler, 12, 17, 15))
            .setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_SHOULDER_LEFT.getIndex()]);
        this.addSlot(new SlotItemHandler(handler, 15, 35, 51))
            .setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_ARM_RIGHT.getIndex()]);
        this.addSlot(new SlotItemHandler(handler, 14, 35, 15))
            .setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_ARM_LEFT.getIndex()]);
        this.addSlot(new SlotItemHandler(handler, 17, 53, 51))
            .setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_HAND_RIGHT.getIndex()]);
        this.addSlot(new SlotItemHandler(handler, 16, 53, 15))
            .setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_EXTENDED[SlotType.ARMOR_HAND_LEFT.getIndex()]);
    }

    private void addClothesSlots(IItemHandler handler) {
        this.addSlot(new SlotItemHandler(handler, 7, 98, 51).setBackground(InventoryMenu.BLOCK_ATLAS,
                                                                           EvolutionResources.SLOT_EXTENDED[SlotType.CLOTHES_FEET.getIndex()]));
        this.addSlot(new SlotItemHandler(handler, 6, 116, 51).setBackground(InventoryMenu.BLOCK_ATLAS,
                                                                            EvolutionResources.SLOT_EXTENDED[SlotType.CLOTHES_LEGS.getIndex()]));
        this.addSlot(new SlotItemHandler(handler, 5, 134, 51).setBackground(InventoryMenu.BLOCK_ATLAS,
                                                                            EvolutionResources.SLOT_EXTENDED[SlotType.CLOTHES_CHEST.getIndex()]));
        this.addSlot(new SlotItemHandler(handler, 4, 152, 51).setBackground(InventoryMenu.BLOCK_ATLAS,
                                                                            EvolutionResources.SLOT_EXTENDED[SlotType.CLOTHES_HEAD.getIndex()]));
    }

    private void addEquipmentSlots(IItemHandler handler) {
        this.addSlot(new SlotItemHandler(handler, 11, 107, 33).setBackground(InventoryMenu.BLOCK_ATLAS,
                                                                             EvolutionResources.SLOT_EXTENDED[SlotType.BELT.getIndex()]));
        this.addSlot(new SlotItemHandler(handler, 10, 125, 33).setBackground(InventoryMenu.BLOCK_ATLAS,
                                                                             EvolutionResources.SLOT_EXTENDED[SlotType.BACK.getIndex()]));
        this.addSlot(new SlotItemHandler(handler, 8, 143, 33).setBackground(InventoryMenu.BLOCK_ATLAS,
                                                                            EvolutionResources.SLOT_EXTENDED[SlotType.FACE.getIndex()]));
        this.addSlot(new SlotItemHandler(handler, 18, 116, 15) {
            @Override
            public void onTake(Player player, ItemStack stackTaken) {
                ContainerCorpse.this.corpse.setSlot(EquipmentSlot.OFFHAND, this.getItem().copy());
                super.onTake(player, stackTaken);
            }
        }.setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_OFFHAND));
        this.addSlot(new SlotItemHandler(handler, 9, 134, 15).setBackground(InventoryMenu.BLOCK_ATLAS,
                                                                            EvolutionResources.SLOT_EXTENDED[SlotType.NECK.getIndex()]));
    }

    @Override
    public void addSlot(IItemHandler handler, int index, int x, int y) {
        if (index == 46 + this.corpse.getSelected()) {
            this.addSlot(new SlotItemHandler(handler, index, x, y) {
                @Override
                public void onTake(Player player, ItemStack stackTaken) {
                    ContainerCorpse.this.corpse.setSlot(EquipmentSlot.MAINHAND, this.getItem().copy());
                    super.onTake(player, stackTaken);
                }
            });
        }
        else {
            this.addSlot(new SlotItemHandler(handler, index, x, y));
        }
    }

    public EntityPlayerCorpse getCorpse() {
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
        if (!player.level.isClientSide) {
            this.corpse.onClose(player);
            this.corpse.tryDespawn();
        }
        super.removed(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.corpse.distanceToSqr(player) <= 64;
    }
}
