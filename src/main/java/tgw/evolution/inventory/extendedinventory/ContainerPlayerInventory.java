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
import tgw.evolution.capabilities.inventory.CapabilityExtendedInventory;
import tgw.evolution.init.EvolutionContainers;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.ServerRecipePlacerEv;
import tgw.evolution.inventory.SlotExtended;
import tgw.evolution.items.IAdditionalEquipment;
import tgw.evolution.patches.IAbstractContainerMenuPatch;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class ContainerPlayerInventory extends RecipeBookMenu<CraftingContainer> {

    private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public final IExtendedInventory handler;
    public final boolean isClientSide;
    private final CraftingContainer craftingContainer = new CraftingContainer(this, 2, 2);
    private final Player player;
    private final ResultContainer resultContainer = new ResultContainer();

    public ContainerPlayerInventory(int windowId, Inventory inventory) {
        super(EvolutionContainers.EXTENDED_INVENTORY.get(), windowId);
        this.player = inventory.player;
        ((IAbstractContainerMenuPatch) this).setPlayer(this.player);
        this.isClientSide = this.player.level.isClientSide;
        this.handler = this.player.getCapability(CapabilityExtendedInventory.INSTANCE).orElseThrow(IllegalStateException::new);
        //Crafting result slot
        this.addSlot(new ResultSlot(inventory.player, this.craftingContainer, this.resultContainer, 0, 161, 62));
        //Crafting slots
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                //noinspection ObjectAllocationInLoop
                this.addSlot(new Slot(this.craftingContainer, j + i * 2, 152 + j * 18, 8 + i * 18));
            }
        }
        //Armor slots
        for (int k = 0; k < 4; ++k) {
            EquipmentSlot equipmentSlot = VALID_EQUIPMENT_SLOTS[k];
            //noinspection ObjectAllocationInLoop
            this.addSlot(new Slot(inventory, 39 - k, 26, 8 + k * 18) {

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPickup(Player player) {
                    if (equipmentSlot == EquipmentSlot.CHEST) {
                        return ContainerPlayerInventory.this.handler.getStackInSlot(EvolutionResources.CLOAK).isEmpty();
                    }
                    ItemStack stack = this.getItem();
                    return !stack.isEmpty();
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (equipmentSlot == EquipmentSlot.CHEST) {
                        if (!ContainerPlayerInventory.this.handler.getStackInSlot(EvolutionResources.CLOAK).isEmpty()) {
                            return false;
                        }
                    }
                    return stack.canEquip(equipmentSlot, ContainerPlayerInventory.this.player);
                }
            }).setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_ARMOR[equipmentSlot.getIndex()]);
        }
        //Main inventory slots
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                //noinspection ObjectAllocationInLoop
                this.addSlot(new Slot(inventory, j1 + (l + 1) * 9, 26 + j1 * 18, 84 + l * 18));
            }
        }
        //Hotbar slots
        for (int i1 = 0; i1 < 9; ++i1) {
            //noinspection ObjectAllocationInLoop
            this.addSlot(new Slot(inventory, i1, 26 + i1 * 18, 142));
        }
        //Shield slot
        this.addSlot(new Slot(inventory, 40, 116, 62).setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.HAT, 44, 8));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.BODY, 44, 26));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.LEGS, 44, 44));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.FEET, 44, 62));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.CLOAK, 8, 26));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.MASK, 116, 8));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.BACK, 116, 26));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.TACTICAL, 116, 44));
    }

    protected static void slotChangedCraftingGrid(AbstractContainerMenu container,
                                                  Level level,
                                                  Player player,
                                                  CraftingContainer craftingInventory,
                                                  ResultContainer resultInventory) {
        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            ItemStack stack = ItemStack.EMPTY;
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

    @Nonnull
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
        ServerRecipePlacerEv.recipeClicked(this, player, recipe, placeAll);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
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
