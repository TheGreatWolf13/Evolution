package tgw.evolution.inventory.extendedinventory;

import com.google.common.collect.Lists;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.capabilities.inventory.CapabilityExtendedInventory;
import tgw.evolution.init.EvolutionContainers;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.ServerRecipePlacerEv;
import tgw.evolution.inventory.SlotExtended;
import tgw.evolution.items.IAdditionalEquipment;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class ContainerPlayerInventory extends RecipeBookContainer<CraftingInventory> {

    private static final EquipmentSlotType[] VALID_EQUIPMENT_SLOTS = {EquipmentSlotType.HEAD,
                                                                      EquipmentSlotType.CHEST,
                                                                      EquipmentSlotType.LEGS,
                                                                      EquipmentSlotType.FEET};
    public final IExtendedItemHandler handler;
    public final boolean isClientSide;
    private final CraftingInventory craftingInventory = new CraftingInventory(this, 2, 2);
    private final CraftResultInventory craftingResult = new CraftResultInventory();
    private final PlayerEntity player;

    public ContainerPlayerInventory(int windowId, PlayerInventory inventory) {
        super(EvolutionContainers.EXTENDED_INVENTORY.get(), windowId);
        this.player = inventory.player;
        this.isClientSide = this.player.level.isClientSide;
        this.handler = this.player.getCapability(CapabilityExtendedInventory.INSTANCE).orElseThrow(IllegalStateException::new);
        //Crafting result slot
        this.addSlot(new CraftingResultSlot(inventory.player, this.craftingInventory, this.craftingResult, 0, 161, 62));
        //Crafting slots
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                //noinspection ObjectAllocationInLoop
                this.addSlot(new Slot(this.craftingInventory, j + i * 2, 152 + j * 18, 8 + i * 18));
            }
        }
        //Armor slots
        for (int k = 0; k < 4; ++k) {
            EquipmentSlotType equipmentslottype = VALID_EQUIPMENT_SLOTS[k];
            //noinspection ObjectAllocationInLoop
            this.addSlot(new Slot(inventory, 39 - k, 26, 8 + k * 18) {

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPickup(PlayerEntity player) {
                    if (equipmentslottype == EquipmentSlotType.CHEST) {
                        return ContainerPlayerInventory.this.handler.getStackInSlot(EvolutionResources.CLOAK).isEmpty();
                    }
                    ItemStack stack = this.getItem();
                    return !stack.isEmpty();
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (equipmentslottype == EquipmentSlotType.CHEST) {
                        if (!ContainerPlayerInventory.this.handler.getStackInSlot(EvolutionResources.CLOAK).isEmpty()) {
                            return false;
                        }
                    }
                    return stack.canEquip(equipmentslottype, ContainerPlayerInventory.this.player);
                }
            }).setBackground(PlayerContainer.BLOCK_ATLAS, EvolutionResources.SLOT_ARMOR[equipmentslottype.getIndex()]);
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
        this.addSlot(new Slot(inventory, 40, 116, 62).setBackground(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.HAT, 44, 8));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.BODY, 44, 26));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.LEGS, 44, 44));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.FEET, 44, 62));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.CLOAK, 8, 26));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.MASK, 116, 8));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.BACK, 116, 26));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.TACTICAL, 116, 44));
    }

    protected static void slotChangedCraftingGrid(int window,
                                                  World world,
                                                  PlayerEntity player,
                                                  CraftingInventory craftingInventory,
                                                  CraftResultInventory resultInventory) {
        if (!world.isClientSide) {
            ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
            ItemStack stack = ItemStack.EMPTY;
            Optional<ICraftingRecipe> optional = world.getServer().getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, craftingInventory, world);
            if (optional.isPresent()) {
                ICraftingRecipe recipe = optional.get();
                if (resultInventory.setRecipeUsed(world, playerMP, recipe)) {
                    stack = recipe.assemble(craftingInventory);
                }
            }
            resultInventory.setItem(0, stack);
            playerMP.connection.send(new SSetSlotPacket(window, 0, stack));
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.craftingResult && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void clearCraftingContent() {
        this.craftingResult.clearContent();
        this.craftingInventory.clearContent();
    }

    @Override
    public void fillCraftSlotsStackedContents(RecipeItemHelper itemHelper) {
        this.craftingInventory.fillStackedContents(itemHelper);
    }

    @Override
    public int getGridHeight() {
        return this.craftingInventory.getHeight();
    }

    @Override
    public int getGridWidth() {
        return this.craftingInventory.getWidth();
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
    public RecipeBookCategory getRecipeBookType() {
        return RecipeBookCategory.CRAFTING;
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
    public void handlePlacement(boolean placeAll, IRecipe<?> recipe, ServerPlayerEntity player) {
        ServerRecipePlacerEv.recipeClicked(this, player, recipe, placeAll);
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            EquipmentSlotType equipmentSlotType = MobEntity.getEquipmentSlotForItem(stack);
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
            else if (equipmentSlotType.getType() == EquipmentSlotType.Group.ARMOR && !this.slots.get(8 - equipmentSlotType.getIndex()).hasItem()) {
                int i = 8 - equipmentSlotType.getIndex();
                if (!this.moveItemStackTo(stackInSlot, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (stackInSlot.getItem() instanceof IAdditionalEquipment && index < 45) {
                IAdditionalEquipment item = (IAdditionalEquipment) stackInSlot.getItem();
                for (int validSlot : item.getType().getValidSlots()) {
                    this.moveItemStackTo(stackInSlot, 46 + validSlot, 47 + validSlot, false);
                }
            }
            else if (equipmentSlotType == EquipmentSlotType.OFFHAND && !this.slots.get(45).hasItem()) {
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
            ItemStack itemstack2 = slot.onTake(player, stackInSlot);
            if (index == 0) {
                player.drop(itemstack2, false);
            }
        }
        return stack;
    }

    @Override
    public boolean recipeMatches(IRecipe<? super CraftingInventory> recipe) {
        return recipe.matches(this.craftingInventory, this.player.level);
    }

    @Override
    public void removed(PlayerEntity player) {
        super.removed(player);
        this.craftingResult.clearContent();
        if (!player.level.isClientSide) {
            this.clearContainer(player, player.level, this.craftingInventory);
        }
    }

    @Override
    public void slotsChanged(IInventory inventory) {
        slotChangedCraftingGrid(this.containerId, this.player.level, this.player, this.craftingInventory, this.craftingResult);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return true;
    }
}
