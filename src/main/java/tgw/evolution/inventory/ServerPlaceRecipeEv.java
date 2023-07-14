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
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.lists.BiIIArrayList;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

public final class ServerPlaceRecipeEv {

    private ServerPlaceRecipeEv() {
    }

    public static void addItemToSlot(RecipeBookMenu<?> menu,
                                     Inventory inventory,
                                     BiIIArrayList ingredients,
                                     int ingredientCounter,
                                     int slotId,
                                     int maxAmount) {
        Slot slot = menu.getSlot(slotId);
        ItemStack stack = StackedContents.fromStackingIndex(ingredients.getLeft(ingredientCounter));
        if (!stack.isEmpty()) {
            int totalAmount = maxAmount * ingredients.getRight(ingredientCounter);
            for (int i = 0; i < totalAmount; ++i) {
                moveItemToGrid(inventory, slot, stack);
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

    private static int getStackSize(RecipeBookMenu<?> menu, boolean placeAll, int maxPossible, boolean recipeMatches, int increment) {
        if (placeAll) {
            return maxPossible;
        }
        int i = 1;
        if (recipeMatches) {
            i = 64;
            for (int j = 0; j < menu.getGridWidth() * menu.getGridHeight() + 1; ++j) {
                if (j != menu.getResultSlotIndex()) {
                    ItemStack itemstack = menu.getSlot(j).getItem();
                    if (!itemstack.isEmpty() && i > itemstack.getCount() / increment) {
                        i = itemstack.getCount() / increment;
                    }
                }
            }
            if (i < 64) {
                i++;
            }
        }
        return i;
    }

    private static void handleRecipeClicked(StackedContentsEv helper,
                                            RecipeBookMenu<?> menu,
                                            Inventory inventory,
                                            Recipe recipe,
                                            boolean placeAll) {
        boolean recipeMatches = menu.recipeMatches(recipe);
        BiIIArrayList itemIdsList = new BiIIArrayList();
        int maxPossible = helper.getBiggestCraftableStack(recipe, itemIdsList);
        int minimumIngredientCount = Integer.MAX_VALUE;
        for (int i = 0, len = itemIdsList.size(); i < len; i++) {
            int count = itemIdsList.getRight(i);
            if (count != 0 && count < minimumIngredientCount) {
                minimumIngredientCount = count;
            }
        }
//        if (recipeMatches) {
//            for (int j = 0; j < menu.getGridHeight() * menu.getGridWidth() + 1; ++j) {
//                if (j != menu.getResultSlotIndex()) {
//                    ItemStack stack = menu.getSlot(j).getItem();
//                    if (!stack.isEmpty()) {
//                        if (Math.min(maxPossible, stack.getMaxStackSize()) < stack.getCount() + 1) {
//                            return;
//                        }
//                    }
//                }
//            }
//        }
        int stackSize = getStackSize(menu, placeAll, maxPossible, recipeMatches, minimumIngredientCount);
        if (helper.canCraft(recipe, itemIdsList, stackSize)) {
            int newSize = stackSize;
            for (int j = 0, l = itemIdsList.size(); j < l; j++) {
                ItemStack stack = StackedContents.fromStackingIndex(itemIdsList.getLeft(j));
                if (stack.isEmpty()) {
                    continue;
                }
                int maxSize = stack.getMaxStackSize() / itemIdsList.getRight(j);
                if (maxSize < newSize) {
                    newSize = maxSize;
                }
            }
            if (helper.canCraft(recipe, itemIdsList, newSize)) {
                clearGrid(menu, inventory);
                placeRecipe(menu,
                            inventory,
                            menu.getGridWidth(),
                            menu.getGridHeight(),
                            menu.getResultSlotIndex(),
                            recipe,
                            itemIdsList,
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
                    ItemStack stackAlreadyInGrid = slotToFill.getItem();
                    stackAlreadyInGrid.grow(1);
                    slotToFill.set(stackAlreadyInGrid);
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
                                    BiIIArrayList ingredients,
                                    int maxAmount) {
        int newWidth = width;
        int newHeight = height;
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            newWidth = shapedRecipe.getWidth();
            newHeight = shapedRecipe.getHeight();
        }
        int k1 = 0;
        int ingredientCounter = 0;
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
                if (ingredientCounter == ingredients.size()) {
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
                    addItemToSlot(container, inventory, ingredients, ingredientCounter++, k1, maxAmount);
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
                StackedContentsEv recipeItemHelper = new StackedContentsEv();
                player.getInventory().fillStackedContents(recipeItemHelper);
                menu.fillCraftSlotsStackedContents(recipeItemHelper);
                if (recipeItemHelper.canCraft(recipe, (BiIIArrayList) null)) {
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