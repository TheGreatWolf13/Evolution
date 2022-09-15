package tgw.evolution.inventory;

import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.IShapedRecipe;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.IArrayList;
import tgw.evolution.util.collection.IList;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

import java.util.Iterator;

public final class ServerPlaceRecipeEv {

    private ServerPlaceRecipeEv() {
    }

    public static void addItemToSlot(RecipeBookMenu<?> menu, Inventory inventory, Iterator<Integer> ingredients, int slotId, int maxAmount) {
        Slot slot = menu.getSlot(slotId);
        ItemStack itemstack = StackedContents.fromStackingIndex(ingredients.next());
        if (!itemstack.isEmpty()) {
            for (int i = 0; i < maxAmount; ++i) {
                moveItemToGrid(inventory, slot, itemstack);
            }
        }
    }

    private static void clearGrid(RecipeBookMenu<?> menu, Inventory inventory) {
        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.shouldMoveToInventory(i)) {
                ItemStack itemStack = menu.getSlot(i).getItem().copy();
                inventory.placeItemBackInInventory(itemStack, false);
                menu.getSlot(i).set(itemStack);
            }
        }
        menu.clearCraftingContent();
    }

    private static int getAmountOfFreeSlotsInInventory(Inventory inventory) {
        int count = 0;
        for (int i = 0, l = inventory.items.size(); i < l; i++) {
            if (inventory.items.get(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static int getStackSize(RecipeBookMenu<?> menu, boolean placeAll, int maxPossible, boolean recipeMatches) {
        if (placeAll) {
            return maxPossible;
        }
        int i = 1;
        if (recipeMatches) {
            i = 64;
            for (int j = 0; j < menu.getGridWidth() * menu.getGridHeight() + 1; ++j) {
                if (j != menu.getResultSlotIndex()) {
                    ItemStack itemstack = menu.getSlot(j).getItem();
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
                                            RecipeBookMenu<?> menu,
                                            Inventory inventory,
                                            Recipe recipe,
                                            boolean placeAll) {
        boolean recipeMatches = menu.recipeMatches(recipe);
        int maxPossible = helper.getBiggestCraftableStack(recipe, null);
        if (recipeMatches) {
            for (int j = 0; j < menu.getGridHeight() * menu.getGridWidth() + 1; ++j) {
                if (j != menu.getResultSlotIndex()) {
                    ItemStack stack = menu.getSlot(j).getItem();
                    if (!stack.isEmpty() && Math.min(maxPossible, stack.getMaxStackSize()) < stack.getCount() + 1) {
                        return;
                    }
                }
            }
        }
        int stackSize = getStackSize(menu, placeAll, maxPossible, recipeMatches);
        IList intList = new IArrayList();
        if (helper.canCraft(recipe, intList, stackSize)) {
            int newSize = stackSize;
            for (int j = 0, l = intList.size(); j < l; j++) {
                int maxSize = StackedContents.fromStackingIndex(intList.get(j)).getMaxStackSize();
                if (maxSize < newSize) {
                    newSize = maxSize;
                }
            }
            if (helper.canCraft(recipe, intList, newSize)) {
                clearGrid(menu, inventory);
                placeRecipe(menu,
                            inventory,
                            menu.getGridWidth(),
                            menu.getGridHeight(),
                            menu.getResultSlotIndex(),
                            recipe,
                            intList.iterator(),
                            newSize);
            }
        }
    }

    private static void moveItemToGrid(Inventory inventory, Slot slotToFill, ItemStack ingredient) {
        int slotId = inventory.findSlotMatchingUnusedItem(ingredient);
        if (slotId != -1) {
            ItemStack stack = inventory.getItem(slotId).copy();
            if (!stack.isEmpty()) {
                if (stack.getCount() > 1) {
                    inventory.removeItem(slotId, 1);
                }
                else {
                    inventory.removeItemNoUpdate(slotId);
                }
                stack.setCount(1);
                if (slotToFill.getItem().isEmpty()) {
                    slotToFill.set(stack);
                }
                else {
                    slotToFill.getItem().grow(1);
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
        int newWidth = width;
        int newHeight = height;
        if (recipe instanceof IShapedRecipe<?> shapedrecipe) {
            newWidth = shapedrecipe.getRecipeWidth();
            newHeight = shapedrecipe.getRecipeHeight();
        }
        int k1 = 0;
        for (int k = 0; k < height; ++k) {
            if (k1 == outputSlot) {
                ++k1;
            }
            boolean flag = newHeight < height / 2.0F;
            int l = Mth.floor(height / 2.0F - newHeight / 2.0F);
            if (flag && l > k) {
                k1 += width;
                ++k;
            }
            for (int i1 = 0; i1 < width; ++i1) {
                if (!ingredients.hasNext()) {
                    return;
                }
                flag = newWidth < width / 2.0F;
                l = Mth.floor(width / 2.0F - newWidth / 2.0F);
                int j1 = newWidth;
                boolean flag1 = i1 < newWidth;
                if (flag) {
                    j1 = l + newWidth;
                    flag1 = l <= i1 && i1 < l + newWidth;
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

    public static void recipeClicked(RecipeBookMenu<?> menu, ServerPlayer player, @Nullable Recipe<?> recipe, boolean placeAll) {
        if (recipe != null && player.getRecipeBook().contains(recipe)) {
            if (testClearGrid(menu, player.getInventory()) || player.isCreative()) {
                StackedContents recipeItemHelper = new StackedContents();
                player.getInventory().fillStackedContents(recipeItemHelper);
                menu.fillCraftSlotsStackedContents(recipeItemHelper);
                if (recipeItemHelper.canCraft(recipe, null)) {
                    handleRecipeClicked(recipeItemHelper, menu, player.getInventory(), recipe, placeAll);
                }
                else {
                    clearGrid(menu, player.getInventory());
                    player.connection.send(new ClientboundPlaceGhostRecipePacket(player.containerMenu.containerId, recipe));
                }
                player.getInventory().setChanged();
            }
        }
    }

    private static boolean testClearGrid(RecipeBookMenu<?> container, Inventory inventory) {
        OList<ItemStack> list = new OArrayList<>();
        int freeSlots = getAmountOfFreeSlotsInInventory(inventory);
        for (int j = 0; j < container.getGridWidth() * container.getGridHeight() + 1; j++) {
            if (j != container.getResultSlotIndex()) {
                ItemStack stack = container.getSlot(j).getItem().copy();
                if (!stack.isEmpty()) {
                    int slotId = inventory.getSlotWithRemainingSpace(stack);
                    if (slotId == -1 && list.size() <= freeSlots) {
                        for (int i = 0, l = list.size(); i < l; i++) {
                            ItemStack itemStack = list.get(i);
                            if (itemStack.sameItem(stack) &&
                                itemStack.getCount() != itemStack.getMaxStackSize() &&
                                itemStack.getCount() + stack.getCount() <= itemStack.getMaxStackSize()) {
                                itemStack.grow(stack.getCount());
                                stack.setCount(0);
                                break;
                            }
                        }
                        if (!stack.isEmpty()) {
                            if (list.size() >= freeSlots) {
                                return false;
                            }
                            list.add(stack);
                        }
                    }
                    else if (slotId == -1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}