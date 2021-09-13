package tgw.evolution.inventory;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.network.play.server.SPlaceGhostRecipePacket;
import net.minecraftforge.common.crafting.IShapedRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tgw.evolution.inventory.extendedinventory.ContainerPlayerInventory;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public final class ServerRecipePlacerEv {

    private static final Logger LOGGER = LogManager.getLogger();

    private ServerRecipePlacerEv() {
    }

    public static void addItemToSlot(RecipeBookContainer<?> container,
                                     PlayerInventory inventory,
                                     Iterator<Integer> ingredients,
                                     int slotIn,
                                     int maxAmount) {
        Slot slot = container.getSlot(slotIn);
        ItemStack itemstack = RecipeItemHelper.fromStackingIndex(ingredients.next());
        if (!itemstack.isEmpty()) {
            for (int i = 0; i < maxAmount; ++i) {
                moveItemToGrid(inventory, slot, itemstack);
            }
        }
    }

    private static void clearGrid(RecipeBookContainer<?> container, PlayerInventory inventory) {
        for (int i = 0; i < container.getGridWidth() * container.getGridHeight() + 1; i++) {
            if (i != container.getResultSlotIndex() ||
                !(container instanceof WorkbenchContainer) &&
                !(container instanceof PlayerContainer) &&
                !(container instanceof ContainerPlayerInventory)) {
                moveItemToInventory(container, inventory, i);
            }
        }
        container.clearCraftingContent();
    }

    private static int getAmountOfFreeSlotsInInventory(PlayerInventory inventory) {
        int i = 0;
        for (ItemStack itemstack : inventory.items) {
            if (itemstack.isEmpty()) {
                ++i;
            }
        }
        return i;
    }

    private static int getStackSize(RecipeBookContainer<?> container, boolean placeAll, int maxPossible, boolean recipeMatches) {
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

    private static void handleRecipeClicked(RecipeItemHelper helper,
                                            RecipeBookContainer<?> container,
                                            PlayerInventory inventory,
                                            IRecipe recipe,
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
                int i1 = RecipeItemHelper.fromStackingIndex(l).getMaxStackSize();
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

    private static void moveItemToGrid(PlayerInventory inventory, Slot slotToFill, ItemStack ingredientIn) {
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

    private static void moveItemToInventory(RecipeBookContainer<?> container, PlayerInventory inventory, int slot) {
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

    private static void placeRecipe(RecipeBookContainer<?> container,
                                    PlayerInventory inventory,
                                    int width,
                                    int height,
                                    int outputSlot,
                                    IRecipe<?> recipe,
                                    Iterator<Integer> ingredients,
                                    int maxAmount) {
        int i = width;
        int j = height;
        if (recipe instanceof IShapedRecipe) {
            IShapedRecipe<?> shapedrecipe = (IShapedRecipe<?>) recipe;
            i = shapedrecipe.getRecipeWidth();
            j = shapedrecipe.getRecipeHeight();
        }
        int k1 = 0;
        for (int k = 0; k < height; ++k) {
            if (k1 == outputSlot) {
                ++k1;
            }
            boolean flag = j < height / 2.0F;
            int l = MathHelper.floor(height / 2.0F - j / 2.0F);
            if (flag && l > k) {
                k1 += width;
                ++k;
            }
            for (int i1 = 0; i1 < width; ++i1) {
                if (!ingredients.hasNext()) {
                    return;
                }
                flag = i < width / 2.0F;
                l = MathHelper.floor(width / 2.0F - i / 2.0F);
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

    public static void recipeClicked(RecipeBookContainer<?> container, ServerPlayerEntity player, @Nullable IRecipe<?> recipe, boolean placeAll) {
        if (recipe != null && player.getRecipeBook().contains(recipe)) {
            if (testClearGrid(container, player.inventory) || player.isCreative()) {
                RecipeItemHelper recipeItemHelper = new RecipeItemHelper();
                recipeItemHelper.clear();
                player.inventory.fillStackedContents(recipeItemHelper);
                container.fillCraftSlotsStackedContents(recipeItemHelper);
                if (recipeItemHelper.canCraft(recipe, null)) {
                    handleRecipeClicked(recipeItemHelper, container, player.inventory, recipe, placeAll);
                }
                else {
                    clearGrid(container, player.inventory);
                    player.connection.send(new SPlaceGhostRecipePacket(player.containerMenu.containerId, recipe));
                }
                player.inventory.setChanged();
            }
        }
    }

    private static boolean testClearGrid(RecipeBookContainer<?> container, PlayerInventory inventory) {
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