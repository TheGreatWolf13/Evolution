package tgw.evolution.inventory;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.IShapedRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tgw.evolution.inventory.extendedinventory.ContainerPlayerInventory;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public final class ServerRecipePlacerEv {

    private static final Logger LOGGER = LogManager.getLogger();

    private ServerRecipePlacerEv() {
    }

    public static void addItemToSlot(RecipeBookMenu<?> container, Inventory inventory, Iterator<Integer> ingredients, int slotIn, int maxAmount) {
        Slot slot = container.getSlot(slotIn);
        ItemStack itemstack = StackedContents.fromStackingIndex(ingredients.next());
        if (!itemstack.isEmpty()) {
            for (int i = 0; i < maxAmount; ++i) {
                moveItemToGrid(inventory, slot, itemstack);
            }
        }
    }

    private static void clearGrid(RecipeBookMenu<?> container, Inventory inventory) {
        for (int i = 0; i < container.getGridWidth() * container.getGridHeight() + 1; i++) {
            if (i != container.getResultSlotIndex() ||
                !(container instanceof CraftingMenu) && !(container instanceof InventoryMenu) && !(container instanceof ContainerPlayerInventory)) {
                moveItemToInventory(container, inventory, i);
            }
        }
        container.clearCraftingContent();
    }

    private static int getAmountOfFreeSlotsInInventory(Inventory inventory) {
        int i = 0;
        for (ItemStack itemstack : inventory.items) {
            if (itemstack.isEmpty()) {
                ++i;
            }
        }
        return i;
    }

    private static int getStackSize(RecipeBookMenu<?> container, boolean placeAll, int maxPossible, boolean recipeMatches) {
        if (placeAll) {
            return maxPossible;
        }
        int i = 1;
        if (recipeMatches) {
            i = 64;
            for (int j = 0; j < container.getGridWidth() * container.getGridHeight() + 1; ++j) {
                if (j != container.getResultSlotIndex()) {
                    ItemStack itemstack = container.getSlot(j).getItem();
                    if (!itemstack.isEmpty() && i > itemstack.getCount()) {
                        i = itemstack.getCount();
                    }
                }
            }
            if (i < 64) {
                i++;
            }
        }
        return i;
    }

    private static void handleRecipeClicked(StackedContents helper,
                                            RecipeBookMenu<?> container,
                                            Inventory inventory,
                                            Recipe recipe,
                                            boolean placeAll) {
        boolean flag = container.recipeMatches(recipe);
        int i = helper.getBiggestCraftableStack(recipe, null);
        if (flag) {
            for (int j = 0; j < container.getGridHeight() * container.getGridWidth() + 1; ++j) {
                if (j != container.getResultSlotIndex()) {
                    ItemStack itemstack = container.getSlot(j).getItem();
                    if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
                        return;
                    }
                }
            }
        }
        int j1 = getStackSize(container, placeAll, i, flag);
        IntList intlist = new IntArrayList();
        if (helper.canCraft(recipe, intlist, j1)) {
            int k = j1;
            for (int l : intlist) {
                int i1 = StackedContents.fromStackingIndex(l).getMaxStackSize();
                if (i1 < k) {
                    k = i1;
                }
            }
            if (helper.canCraft(recipe, intlist, k)) {
                clearGrid(container, inventory);
                placeRecipe(container,
                            inventory,
                            container.getGridWidth(),
                            container.getGridHeight(),
                            container.getResultSlotIndex(),
                            recipe,
                            intlist.iterator(),
                            k);
            }
        }
    }

    private static void moveItemToGrid(Inventory inventory, Slot slotToFill, ItemStack ingredientIn) {
        int i = inventory.findSlotMatchingUnusedItem(ingredientIn);
        if (i != -1) {
            ItemStack itemstack = inventory.getItem(i).copy();
            if (!itemstack.isEmpty()) {
                if (itemstack.getCount() > 1) {
                    inventory.removeItem(i, 1);
                }
                else {
                    inventory.removeItemNoUpdate(i);
                }
                itemstack.setCount(1);
                if (slotToFill.getItem().isEmpty()) {
                    slotToFill.set(itemstack);
                }
                else {
                    slotToFill.getItem().grow(1);
                }
            }
        }
    }

    private static void moveItemToInventory(RecipeBookMenu<?> container, Inventory inventory, int slot) {
        ItemStack itemstack = container.getSlot(slot).getItem();
        if (!itemstack.isEmpty()) {
            for (; itemstack.getCount() > 0; container.getSlot(slot).remove(1)) {
                int i = inventory.getSlotWithRemainingSpace(itemstack);
                if (i == -1) {
                    i = inventory.getFreeSlot();
                }
                ItemStack itemstack1 = itemstack.copy();
                itemstack1.setCount(1);
                if (!inventory.add(i, itemstack1)) {
                    LOGGER.error("Can't find any space for item in the inventory");
                }
            }
        }
    }

    private static void placeRecipe(RecipeBookMenu<?> container,
                                    Inventory inventory,
                                    int width,
                                    int height,
                                    int outputSlot,
                                    Recipe<?> recipe,
                                    Iterator<Integer> ingredients,
                                    int maxAmount) {
        int i = width;
        int j = height;
        if (recipe instanceof IShapedRecipe<?> shapedrecipe) {
            i = shapedrecipe.getRecipeWidth();
            j = shapedrecipe.getRecipeHeight();
        }
        int k1 = 0;
        for (int k = 0; k < height; ++k) {
            if (k1 == outputSlot) {
                ++k1;
            }
            boolean flag = j < height / 2.0F;
            int l = Mth.floor(height / 2.0F - j / 2.0F);
            if (flag && l > k) {
                k1 += width;
                ++k;
            }
            for (int i1 = 0; i1 < width; ++i1) {
                if (!ingredients.hasNext()) {
                    return;
                }
                flag = i < width / 2.0F;
                l = Mth.floor(width / 2.0F - i / 2.0F);
                int j1 = i;
                boolean flag1 = i1 < i;
                if (flag) {
                    j1 = l + i;
                    flag1 = l <= i1 && i1 < l + i;
                }
                if (flag1) {
                    addItemToSlot(container, inventory, ingredients, k1, maxAmount);
                }
                else if (j1 == i1) {
                    k1 += width - i1;
                    break;
                }
                ++k1;
            }
        }
    }

    public static void recipeClicked(RecipeBookMenu<?> container, ServerPlayer player, @Nullable Recipe<?> recipe, boolean placeAll) {
        if (recipe != null && player.getRecipeBook().contains(recipe)) {
            if (testClearGrid(container, player.getInventory()) || player.isCreative()) {
                StackedContents recipeItemHelper = new StackedContents();
                recipeItemHelper.clear();
                player.getInventory().fillStackedContents(recipeItemHelper);
                container.fillCraftSlotsStackedContents(recipeItemHelper);
                if (recipeItemHelper.canCraft(recipe, null)) {
                    handleRecipeClicked(recipeItemHelper, container, player.getInventory(), recipe, placeAll);
                }
                else {
                    clearGrid(container, player.getInventory());
                    player.connection.send(new ClientboundPlaceGhostRecipePacket(player.containerMenu.containerId, recipe));
                }
                player.getInventory().setChanged();
            }
        }
    }

    private static boolean testClearGrid(RecipeBookMenu<?> container, Inventory inventory) {
        List<ItemStack> list = Lists.newArrayList();
        int i = getAmountOfFreeSlotsInInventory(inventory);
        for (int j = 0; j < container.getGridWidth() * container.getGridHeight() + 1; j++) {
            if (j != container.getResultSlotIndex()) {
                ItemStack itemstack = container.getSlot(j).getItem().copy();
                if (!itemstack.isEmpty()) {
                    int k = inventory.getSlotWithRemainingSpace(itemstack);
                    if (k == -1 && list.size() <= i) {
                        for (ItemStack itemstack1 : list) {
                            if (itemstack1.sameItem(itemstack) &&
                                itemstack1.getCount() != itemstack1.getMaxStackSize() &&
                                itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
                                itemstack1.grow(itemstack.getCount());
                                itemstack.setCount(0);
                                break;
                            }
                        }
                        if (!itemstack.isEmpty()) {
                            if (list.size() >= i) {
                                return false;
                            }
                            list.add(itemstack);
                        }
                    }
                    else if (k == -1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}