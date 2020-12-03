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

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public final class ServerRecipePlacerEv {

    protected static final Logger LOGGER = LogManager.getLogger();

    private ServerRecipePlacerEv() {
    }

    protected static void clear(RecipeBookContainer<?> container, PlayerInventory inventory) {
        for (int i = 0; i < container.getWidth() * container.getHeight() + 1; ++i) {
            if (i != container.getOutputSlot() ||
                !(container instanceof WorkbenchContainer) &&
                !(container instanceof PlayerContainer) &&
                !(container instanceof ContainerPlayerInventory)) {
                giveToPlayer(container, inventory, i);
            }
        }
        container.clear();
    }

    protected static void consumeIngredient(PlayerInventory inventory, Slot slotToFill, ItemStack ingredientIn) {
        int i = inventory.findSlotMatchingUnusedItem(ingredientIn);
        if (i != -1) {
            ItemStack itemstack = inventory.getStackInSlot(i).copy();
            if (!itemstack.isEmpty()) {
                if (itemstack.getCount() > 1) {
                    inventory.decrStackSize(i, 1);
                }
                else {
                    inventory.removeStackFromSlot(i);
                }
                itemstack.setCount(1);
                if (slotToFill.getStack().isEmpty()) {
                    slotToFill.putStack(itemstack);
                }
                else {
                    slotToFill.getStack().grow(1);
                }
            }
        }
    }

    private static int getEmptyPlayerSlots(PlayerInventory inventory) {
        int i = 0;
        for (ItemStack itemstack : inventory.mainInventory) {
            if (itemstack.isEmpty()) {
                ++i;
            }
        }
        return i;
    }

    protected static int getMaxAmount(RecipeBookContainer<?> container, boolean placeAll, int maxPossible, boolean recipeMatches) {
        if (placeAll) {
            return maxPossible;
        }
        int i = 1;
        if (recipeMatches) {
            i = 64;
            for (int j = 0; j < container.getWidth() * container.getHeight() + 1; ++j) {
                if (j != container.getOutputSlot()) {
                    ItemStack itemstack = container.getSlot(j).getStack();
                    if (!itemstack.isEmpty() && i > itemstack.getCount()) {
                        i = itemstack.getCount();
                    }
                }
            }
            if (i < 64) {
                ++i;
            }
        }
        return i;
    }

    protected static void giveToPlayer(RecipeBookContainer<?> container, PlayerInventory inventory, int slotIn) {
        ItemStack itemstack = container.getSlot(slotIn).getStack();
        if (!itemstack.isEmpty()) {
            for (; itemstack.getCount() > 0; container.getSlot(slotIn).decrStackSize(1)) {
                int i = inventory.storeItemStack(itemstack);
                if (i == -1) {
                    i = inventory.getFirstEmptyStack();
                }
                //noinspection ObjectAllocationInLoop
                ItemStack itemstack1 = itemstack.copy();
                itemstack1.setCount(1);
                if (!inventory.add(i, itemstack1)) {
                    LOGGER.error("Can't find any space for item in the inventory");
                }
            }
        }
    }

    public static void place(RecipeBookContainer<?> container, ServerPlayerEntity player, @Nullable IRecipe<?> recipe, boolean placeAll) {
        RecipeItemHelper recipeItemHelper = new RecipeItemHelper();
        if (recipe != null && player.getRecipeBook().isUnlocked(recipe)) {
            if (placeIntoInventory(container, player.inventory) || player.isCreative()) {
                recipeItemHelper.clear();
                player.inventory.accountStacks(recipeItemHelper);
                container.func_201771_a(recipeItemHelper);
                if (recipeItemHelper.canCraft(recipe, null)) {
                    tryPlaceRecipe(recipeItemHelper, container, player.inventory, recipe, placeAll);
                }
                else {
                    clear(container, player.inventory);
                    player.connection.sendPacket(new SPlaceGhostRecipePacket(player.openContainer.windowId, recipe));
                }
                player.inventory.markDirty();
            }
        }
    }

    private static boolean placeIntoInventory(RecipeBookContainer<?> container, PlayerInventory inventory) {
        List<ItemStack> list = Lists.newArrayList();
        int i = getEmptyPlayerSlots(inventory);
        for (int j = 0; j < container.getWidth() * container.getHeight() + 1; ++j) {
            if (j != container.getOutputSlot()) {
                //noinspection ObjectAllocationInLoop
                ItemStack itemstack = container.getSlot(j).getStack().copy();
                if (!itemstack.isEmpty()) {
                    int k = inventory.storeItemStack(itemstack);
                    if (k == -1 && list.size() <= i) {
                        for (ItemStack itemstack1 : list) {
                            if (itemstack1.isItemEqual(itemstack) &&
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
            int l = net.minecraft.util.math.MathHelper.floor(height / 2.0F - j / 2.0F);
            if (flag && l > k) {
                k1 += width;
                ++k;
            }
            for (int i1 = 0; i1 < width; ++i1) {
                if (!ingredients.hasNext()) {
                    return;
                }
                flag = i < width / 2.0F;
                l = net.minecraft.util.math.MathHelper.floor(width / 2.0F - i / 2.0F);
                int j1 = i;
                boolean flag1 = i1 < i;
                if (flag) {
                    j1 = l + i;
                    flag1 = l <= i1 && i1 < l + i;
                }
                if (flag1) {
                    setSlotContents(container, inventory, ingredients, k1, maxAmount);
                }
                else if (j1 == i1) {
                    k1 += width - i1;
                    break;
                }
                ++k1;
            }
        }
    }

    public static void setSlotContents(RecipeBookContainer<?> container,
                                       PlayerInventory inventory,
                                       Iterator<Integer> ingredients,
                                       int slotIn,
                                       int maxAmount) {
        Slot slot = container.getSlot(slotIn);
        ItemStack itemstack = RecipeItemHelper.unpack(ingredients.next());
        if (!itemstack.isEmpty()) {
            for (int i = 0; i < maxAmount; ++i) {
                consumeIngredient(inventory, slot, itemstack);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected static void tryPlaceRecipe(RecipeItemHelper helper,
                                         RecipeBookContainer<?> container,
                                         PlayerInventory inventory,
                                         IRecipe recipe,
                                         boolean placeAll) {
        boolean flag = container.matches(recipe);
        int i = helper.getBiggestCraftableStack(recipe, null);
        if (flag) {
            for (int j = 0; j < container.getHeight() * container.getWidth() + 1; ++j) {
                if (j != container.getOutputSlot()) {
                    ItemStack itemstack = container.getSlot(j).getStack();
                    if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
                        return;
                    }
                }
            }
        }
        int j1 = getMaxAmount(container, placeAll, i, flag);
        IntList intlist = new IntArrayList();
        if (helper.canCraft(recipe, intlist, j1)) {
            int k = j1;
            for (int l : intlist) {
                int i1 = RecipeItemHelper.unpack(l).getMaxStackSize();
                if (i1 < k) {
                    k = i1;
                }
            }
            if (helper.canCraft(recipe, intlist, k)) {
                clear(container, inventory);
                placeRecipe(container,
                            inventory,
                            container.getWidth(),
                            container.getHeight(),
                            container.getOutputSlot(),
                            recipe,
                            intlist.iterator(),
                            k);
            }
        }
    }
}