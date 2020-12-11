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
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.capabilities.inventory.PlayerInventoryCapability;
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
    public final boolean isLocalWorld;
    private final CraftingInventory craftingInventory = new CraftingInventory(this, 2, 2);
    private final CraftResultInventory craftingResult = new CraftResultInventory();
    private final PlayerEntity player;

    public ContainerPlayerInventory(int windowId, PlayerInventory inventory) {
        super(EvolutionContainers.EXTENDED_INVENTORY.get(), windowId);
        this.player = inventory.player;
        this.isLocalWorld = this.player.world.isRemote;
        this.handler = this.player.getCapability(PlayerInventoryCapability.CAPABILITY_EXTENDED_INVENTORY).orElseThrow(IllegalStateException::new);
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
                public boolean canTakeStack(PlayerEntity playerIn) {
                    if (equipmentslottype == EquipmentSlotType.CHEST) {
                        return ContainerPlayerInventory.this.handler.getStackInSlot(EvolutionResources.CLOAK).isEmpty();
                    }
                    ItemStack stack = this.getStack();
                    return !stack.isEmpty();
                }

                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                @OnlyIn(Dist.CLIENT)
                public String getSlotTexture() {
                    return EvolutionResources.SLOT_ARMOR[equipmentslottype.getIndex()];
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    if (equipmentslottype == EquipmentSlotType.CHEST) {
                        if (!ContainerPlayerInventory.this.handler.getStackInSlot(EvolutionResources.CLOAK).isEmpty()) {
                            return false;
                        }
                    }
                    return stack.canEquip(equipmentslottype, ContainerPlayerInventory.this.player);
                }
            });
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
        this.addSlot(new Slot(inventory, 40, 116, 62) {

            @Override
            @OnlyIn(Dist.CLIENT)
            public String getSlotTexture() {
                return EvolutionResources.SLOT_SHIELD;
            }
        });
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.HAT, 44, 8));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.BODY, 44, 26));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.LEGS, 44, 44));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.FEET, 44, 62));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.CLOAK, 8, 26));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.MASK, 116, 8));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.BACK, 116, 26));
        this.addSlot(new SlotExtended(this.player, this.handler, EvolutionResources.TACTICAL, 116, 44));
    }

    protected static void func_217066_a(int window,
                                        World world,
                                        PlayerEntity player,
                                        CraftingInventory craftingInventory,
                                        CraftResultInventory resultInventory) {
        if (!world.isRemote) {
            ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
            ItemStack stack = ItemStack.EMPTY;
            Optional<ICraftingRecipe> optional = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftingInventory, world);
            if (optional.isPresent()) {
                ICraftingRecipe recipe = optional.get();
                if (resultInventory.canUseRecipe(world, playerMP, recipe)) {
                    stack = recipe.getCraftingResult(craftingInventory);
                }
            }
            resultInventory.setInventorySlotContents(0, stack);
            playerMP.connection.sendPacket(new SSetSlotPacket(window, 0, stack));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.craftingResult && super.canMergeSlot(stack, slot);
    }

    @Override
    public void clear() {
        this.craftingResult.clear();
        this.craftingInventory.clear();
    }

    @Override
    public void func_201771_a(RecipeItemHelper itemHelper) {
        this.craftingInventory.fillStackedContents(itemHelper);
    }

    @Override
    public void func_217056_a(boolean placeAll, IRecipe<?> recipe, ServerPlayerEntity player) {
        ServerRecipePlacerEv.place(this, player, recipe, placeAll);
    }

    @Override
    public int getHeight() {
        return this.craftingInventory.getHeight();
    }

    @Override
    public int getOutputSlot() {
        return 0;
    }

    @Nonnull
    @Override
    public List<RecipeBookCategories> getRecipeBookCategories() {
        return Lists.newArrayList(RecipeBookCategories.SEARCH,
                                  RecipeBookCategories.EQUIPMENT,
                                  RecipeBookCategories.BUILDING_BLOCKS,
                                  RecipeBookCategories.MISC);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getSize() {
        return 5;
    }

    @Override
    public int getWidth() {
        return this.craftingInventory.getWidth();
    }

    @Override
    public boolean matches(IRecipe<? super CraftingInventory> recipeIn) {
        return recipeIn.matches(this.craftingInventory, this.player.world);
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.craftingResult.clear();
        if (!playerIn.world.isRemote) {
            this.clearContainer(playerIn, playerIn.world, this.craftingInventory);
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        func_217066_a(this.windowId, this.player.world, this.player, this.craftingInventory, this.craftingResult);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            EquipmentSlotType equipmentSlotType = MobEntity.getSlotForItemStack(stack);
            if (index == 0) {
                if (!this.mergeItemStack(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stackInSlot, stack);
            }
            else if (index < 5) {
                if (!this.mergeItemStack(stackInSlot, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index < 9) {
                if (!this.mergeItemStack(stackInSlot, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (equipmentSlotType.getSlotType() == EquipmentSlotType.Group.ARMOR &&
                     !this.inventorySlots.get(8 - equipmentSlotType.getIndex()).getHasStack()) {
                int i = 8 - equipmentSlotType.getIndex();
                if (!this.mergeItemStack(stackInSlot, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (stackInSlot.getItem() instanceof IAdditionalEquipment && index < 45) {
                IAdditionalEquipment item = (IAdditionalEquipment) stackInSlot.getItem();
                for (int validSlot : item.getType().getValidSlots()) {
                    this.mergeItemStack(stackInSlot, 46 + validSlot, 47 + validSlot, false);
                }
            }
            else if (equipmentSlotType == EquipmentSlotType.OFFHAND && !this.inventorySlots.get(45).getHasStack()) {
                if (!this.mergeItemStack(stackInSlot, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index < 36) {
                if (!this.mergeItemStack(stackInSlot, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index < 45) {
                if (!this.mergeItemStack(stackInSlot, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(stackInSlot, 9, 45, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            }
            else {
                slot.onSlotChanged();
            }
            if (stackInSlot.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack itemstack2 = slot.onTake(player, stackInSlot);
            if (index == 0) {
                player.dropItem(itemstack2, false);
            }
        }
        return stack;
    }
}
