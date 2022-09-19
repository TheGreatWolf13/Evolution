package tgw.evolution.inventory.extendedinventory;

import com.google.common.collect.Lists;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.capabilities.inventory.CapabilityInventory;
import tgw.evolution.capabilities.inventory.IInventory;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.EvolutionContainers;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.ServerPlaceRecipeEv;
import tgw.evolution.inventory.SlotArmor;
import tgw.evolution.inventory.SlotExtended;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.items.IAdditionalEquipment;
import tgw.evolution.patches.IAbstractContainerMenuPatch;

import java.util.List;
import java.util.Optional;

public class ContainerInventory extends RecipeBookMenu<CraftingContainer> {

    public final IInventory handler;
    public final boolean isClientSide;
    private final CraftingContainer craftingContainer = new CraftingContainer(this, 2, 2);
    private final Player player;
    private final ResultContainer resultContainer = new ResultContainer();

    @SuppressWarnings("ObjectAllocationInLoop")
    public ContainerInventory(int windowId, Inventory inventory) {
        super(EvolutionContainers.EXTENDED_INVENTORY.get(), windowId);
        this.player = inventory.player;
        ((IAbstractContainerMenuPatch) this).setPlayer(this.player);
        this.isClientSide = this.player.level.isClientSide;
        this.handler = EvolutionCapabilities.getCapabilityOrThrow(this.player, CapabilityInventory.INSTANCE);
        //Crafting result slot
        this.addSlot(new ResultSlot(inventory.player, this.craftingContainer, this.resultContainer, 0, 232, 31));
        //Crafting slots
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 2; i++) {
                this.addSlot(new Slot(this.craftingContainer, i + j * 2, 178 + i * 18, 22 + j * 18));
            }
        }
        //Armor slots
        for (int k = 0; k < 4; ++k) {
            EquipmentSlot equip = switch (k) {
                case 0 -> EquipmentSlot.HEAD;
                case 1 -> EquipmentSlot.CHEST;
                case 2 -> EquipmentSlot.LEGS;
                case 3 -> EquipmentSlot.FEET;
                default -> throw new IllegalStateException("Unexpected value: " + k);
            };
            this.addSlot(new SlotArmor(inventory, 39 - k, 26, 15 + k * 18, equip, this.player));
        }
        //Main inventory slots
        for (int j = 0; j < 3; ++j) {
            for (int i = 0; i < 9; ++i) {
                this.addSlot(new Slot(inventory, i + (j + 1) * 9, 8 + i * 18, 98 + j * 18));
            }
        }
        //Hotbar slots
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 156));
        }
        //Offhand slot
        this.addSlot(new Slot(inventory, 40, 152, 51).setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_OFFHAND));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.ARMOR_SHOULDER_LEFT.getIndex(), 44, 24));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.ARMOR_SHOULDER_RIGHT.getIndex(), 8, 24));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.ARMOR_ARM_LEFT.getIndex(), 44, 42));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.ARMOR_ARM_RIGHT.getIndex(), 8, 42));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.ARMOR_HAND_LEFT.getIndex(), 44, 60));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.ARMOR_HAND_RIGHT.getIndex(), 8, 60));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.CLOTHES_HEAD.getIndex(), 116, 15));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.CLOTHES_CHEST.getIndex(), 116, 33));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.CLOTHES_LEGS.getIndex(), 116, 51));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.CLOTHES_FEET.getIndex(), 116, 69));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.FACE.getIndex(), 134, 24));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.BACK.getIndex(), 134, 42));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.BELT.getIndex(), 134, 60));
        this.addSlot(new SlotExtended(this.player, this.handler, SlotType.NECK.getIndex(), 152, 33));
    }

    protected static void slotChangedCraftingGrid(AbstractContainerMenu container,
                                                  Level level,
                                                  Player player,
                                                  CraftingContainer craftingInventory,
                                                  ResultContainer resultInventory) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = ItemStack.EMPTY;
            //noinspection ConstantConditions
            Optional<CraftingRecipe> optional = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInventory, level);
            if (optional.isPresent()) {
                CraftingRecipe recipe = optional.get();
                if (resultInventory.setRecipeUsed(level, serverPlayer, recipe)) {
                    stack = recipe.assemble(craftingInventory);
                }
            }
            resultInventory.setItem(0, stack);
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(container.containerId, container.incrementStateId(), 0, stack));
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultContainer && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void clearCraftingContent() {
        this.resultContainer.clearContent();
        this.craftingContainer.clearContent();
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents itemHelper) {
        this.craftingContainer.fillStackedContents(itemHelper);
    }

    @Override
    public int getGridHeight() {
        return this.craftingContainer.getHeight();
    }

    @Override
    public int getGridWidth() {
        return this.craftingContainer.getWidth();
    }

    @Override
    public List<RecipeBookCategories> getRecipeBookCategories() {
        return Lists.newArrayList(RecipeBookCategories.CRAFTING_SEARCH,
                                  RecipeBookCategories.CRAFTING_EQUIPMENT,
                                  RecipeBookCategories.CRAFTING_BUILDING_BLOCKS,
                                  RecipeBookCategories.CRAFTING_MISC);
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    public int getResultSlotIndex() {
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getSize() {
        return 5;
    }

    @Override
    public void handlePlacement(boolean placeAll, Recipe<?> recipe, ServerPlayer player) {
        ServerPlaceRecipeEv.recipeClicked(this, player, recipe, placeAll);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            EquipmentSlot equipmentSlotType = Mob.getEquipmentSlotForItem(stack);
            if (index == 0) {
                if (!this.moveItemStackTo(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, stack);
            }
            else if (index < 5) {
                if (!this.moveItemStackTo(stackInSlot, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index < 9) {
                if (!this.moveItemStackTo(stackInSlot, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (equipmentSlotType.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(8 - equipmentSlotType.getIndex()).hasItem()) {
                int i = 8 - equipmentSlotType.getIndex();
                if (!this.moveItemStackTo(stackInSlot, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (stackInSlot.getItem() instanceof IAdditionalEquipment item && index < 45) {
                int validSlot = item.getValidSlot().getSlotId();
                this.moveItemStackTo(stackInSlot, 46 + validSlot, 47 + validSlot, false);
            }
            else if (equipmentSlotType == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem()) {
                if (!this.moveItemStackTo(stackInSlot, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index < 36) {
                if (!this.moveItemStackTo(stackInSlot, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index < 45) {
                if (!this.moveItemStackTo(stackInSlot, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(stackInSlot, 9, 45, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }
            if (stackInSlot.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stackInSlot);
            if (index == 0) {
                player.drop(stackInSlot, false);
            }
        }
        return stack;
    }

    @Override
    public boolean recipeMatches(Recipe<? super CraftingContainer> recipe) {
        return recipe.matches(this.craftingContainer, this.player.level);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.resultContainer.clearContent();
        if (!player.level.isClientSide) {
            this.clearContainer(player, this.craftingContainer);
        }
    }

    @Override
    public boolean shouldMoveToInventory(int index) {
        return index != this.getResultSlotIndex();
    }

    @Override
    public void slotsChanged(Container container) {
        slotChangedCraftingGrid(this, this.player.level, this.player, this.craftingContainer, this.resultContainer);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
