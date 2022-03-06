package tgw.evolution.util.toast;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.KnappingRecipe;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

import java.util.ArrayList;
import java.util.List;

public final class Toasts {

    private static final Int2ObjectMap<ToastHolderRecipe[]> RECIPES = new Int2ObjectOpenHashMap<>();
    private static final Reference2IntMap<Item> RECIPE_TRIGGERS = new Reference2IntOpenHashMap<>();
    private static int index;

    private Toasts() {
    }

    public static ToastHolderRecipe[] getHolderForId(int id) {
        return RECIPES.get(id);
    }

    public static int getRecipeIdFor(Item trigger) {
        return RECIPE_TRIGGERS.getOrDefault(trigger, -1);
    }

    public static void register() {
        ItemStack knapping = new ItemStack(EvolutionItems.ALL_ROCK.get(RockVariant.ANDESITE).get());
        for (RockVariant rock : RockVariant.VALUES) {
            Item rockItem;
            try {
                rockItem = rock.getRock().asItem();
            }
            catch (IllegalStateException e) {
                continue;
            }
            //noinspection ObjectAllocationInLoop
            List<ToastHolderRecipe> rockRecipes = new ArrayList<>(KnappingRecipe.VALUES.length - 1);
            for (KnappingRecipe recipe : KnappingRecipe.VALUES) {
                if (recipe == KnappingRecipe.NULL) {
                    continue;
                }
                //noinspection ObjectAllocationInLoop
                rockRecipes.add(new ToastHolderRecipe(knapping, rock.getKnappedStack(recipe)));
            }
            registerRecipe(rockItem, rockRecipes);
        }
        ItemStack chopping = new ItemStack(EvolutionItems.ALL_CHOPPING_BLOCK.get(WoodVariant.ACACIA).get());
        for (WoodVariant wood : WoodVariant.VALUES) {
            Item woodItem = wood.getLogItem();
            //noinspection ObjectAllocationInLoop
            registerRecipe(woodItem, new ToastHolderRecipe(chopping, wood.getFirewood()));
        }
        Evolution.info("Registered custom toasts");
    }

    public static void registerRecipe(Item trigger, ToastHolderRecipe... toasts) {
        registerRecipe(index++, trigger, toasts);
    }

    public static void registerRecipe(Item trigger, List<ToastHolderRecipe> toasts) {
        registerRecipe(index++, trigger, toasts);
    }

    public static void registerRecipe(int id, Item trigger, ToastHolderRecipe... toasts) {
        if (toasts.length == 0) {
            throw new IllegalStateException("Cannot register empty toast!");
        }
        RECIPE_TRIGGERS.put(trigger, id);
        RECIPES.put(id, toasts);
    }

    public static void registerRecipe(int id, Item trigger, List<ToastHolderRecipe> toasts) {
        registerRecipe(id, trigger, toasts.toArray(new ToastHolderRecipe[0]));
    }
}
