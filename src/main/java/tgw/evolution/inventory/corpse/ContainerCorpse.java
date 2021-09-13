package tgw.evolution.inventory.corpse;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.init.EvolutionContainers;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.IEvolutionContainer;

public class ContainerCorpse extends Container implements IEvolutionContainer {

    private final EntityPlayerCorpse corpse;

    public ContainerCorpse(int windowId, EntityPlayerCorpse corpse, PlayerInventory inventory) {
        super(EvolutionContainers.CORPSE.get(), windowId);
        this.corpse = corpse;
        IItemHandler playerInventory = new InvWrapper(inventory);
        this.layoutPlayerInventorySlots(playerInventory, 8, 140);
        if (corpse != null) {
            IItemHandler corpseHandler = corpse.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(IllegalStateException::new);
            this.addArmorSlots(corpseHandler);
            this.addClothesSlots(corpseHandler);
            this.addEquipmentSlots(corpseHandler);
            this.addSlotBox(corpseHandler, 13, 8, 54, 9, 18, 4, 18);
            if (!corpse.level.isClientSide) {
                corpse.onOpen(inventory.player);
            }
        }
    }

    private void addArmorSlots(IItemHandler handler) {
        this.addSlot(new SlotItemHandler(handler, 0, 8, 18) {
            @Override
            public ItemStack onTake(PlayerEntity player, ItemStack stackTaken) {
                ContainerCorpse.this.corpse.setSlot(EquipmentSlotType.HEAD, this.getItem().copy());
                return super.onTake(player, stackTaken);
            }
        }.setBackground(AtlasTexture.LOCATION_BLOCKS, EvolutionResources.SLOT_ARMOR[EvolutionResources.HELMET]));
        this.addSlot(new SlotItemHandler(handler, 1, 26, 18) {
            @Override
            public ItemStack onTake(PlayerEntity player, ItemStack stackTaken) {
                ContainerCorpse.this.corpse.setSlot(EquipmentSlotType.CHEST, this.getItem().copy());
                return super.onTake(player, stackTaken);
            }
        }.setBackground(AtlasTexture.LOCATION_BLOCKS, EvolutionResources.SLOT_ARMOR[EvolutionResources.CHESTPLATE]));
        this.addSlot(new SlotItemHandler(handler, 2, 44, 18) {
            @Override
            public ItemStack onTake(PlayerEntity player, ItemStack stackTaken) {
                ContainerCorpse.this.corpse.setSlot(EquipmentSlotType.LEGS, this.getItem().copy());
                return super.onTake(player, stackTaken);
            }
        }.setBackground(AtlasTexture.LOCATION_BLOCKS, EvolutionResources.SLOT_ARMOR[EvolutionResources.LEGGINGS]));
        this.addSlot(new SlotItemHandler(handler, 3, 62, 18) {
            @Override
            public ItemStack onTake(PlayerEntity player, ItemStack stackTaken) {
                ContainerCorpse.this.corpse.setSlot(EquipmentSlotType.FEET, this.getItem().copy());
                return super.onTake(player, stackTaken);
            }
        }.setBackground(AtlasTexture.LOCATION_BLOCKS, EvolutionResources.SLOT_ARMOR[EvolutionResources.BOOTS]));
    }

    private void addClothesSlots(IItemHandler handler) {
        this.addSlot(new SlotItemHandler(handler, 4, 98, 18).setBackground(AtlasTexture.LOCATION_BLOCKS,
                                                                           EvolutionResources.SLOT_EXTENDED[EvolutionResources.FEET]));
        this.addSlot(new SlotItemHandler(handler, 5, 116, 18).setBackground(AtlasTexture.LOCATION_BLOCKS,
                                                                            EvolutionResources.SLOT_EXTENDED[EvolutionResources.LEGS]));
        this.addSlot(new SlotItemHandler(handler, 6, 134, 18).setBackground(AtlasTexture.LOCATION_BLOCKS,
                                                                            EvolutionResources.SLOT_EXTENDED[EvolutionResources.BODY]));
        this.addSlot(new SlotItemHandler(handler, 7, 152, 18).setBackground(AtlasTexture.LOCATION_BLOCKS,
                                                                            EvolutionResources.SLOT_EXTENDED[EvolutionResources.HAT]));
    }

    private void addEquipmentSlots(IItemHandler handler) {
        this.addSlot(new SlotItemHandler(handler, 8, 44, 36).setBackground(AtlasTexture.LOCATION_BLOCKS,
                                                                           EvolutionResources.SLOT_EXTENDED[EvolutionResources.MASK]));
        this.addSlot(new SlotItemHandler(handler, 9, 62, 36).setBackground(AtlasTexture.LOCATION_BLOCKS,
                                                                           EvolutionResources.SLOT_EXTENDED[EvolutionResources.CLOAK]));
        this.addSlot(new SlotItemHandler(handler, 10, 80, 36) {
            @Override
            public ItemStack onTake(PlayerEntity player, ItemStack stackTaken) {
                ContainerCorpse.this.corpse.setSlot(EquipmentSlotType.OFFHAND, this.getItem().copy());
                return super.onTake(player, stackTaken);
            }
        }.setBackground(AtlasTexture.LOCATION_BLOCKS, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD));
        this.addSlot(new SlotItemHandler(handler, 11, 98, 36).setBackground(AtlasTexture.LOCATION_BLOCKS,
                                                                            EvolutionResources.SLOT_EXTENDED[EvolutionResources.BACK]));
        this.addSlot(new SlotItemHandler(handler, 12, 116, 36).setBackground(AtlasTexture.LOCATION_BLOCKS,
                                                                             EvolutionResources.SLOT_EXTENDED[EvolutionResources.TACTICAL]));
    }

    @Override
    public void addSlot(IItemHandler handler, int index, int x, int y) {
        if (index == 40 + this.corpse.getSelected()) {
            this.addSlot(new SlotItemHandler(handler, index, x, y) {
                @Override
                public ItemStack onTake(PlayerEntity player, ItemStack stackTaken) {
                    ContainerCorpse.this.corpse.setSlot(EquipmentSlotType.MAINHAND, this.getItem().copy());
                    return super.onTake(player, stackTaken);
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
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
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
    public void removed(PlayerEntity player) {
        if (!player.level.isClientSide) {
            this.corpse.onClose(player);
            this.corpse.tryDespawn();
        }
        super.removed(player);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return this.corpse.distanceToSqr(player) <= 64;
    }
}
