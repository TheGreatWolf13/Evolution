package tgw.evolution.inventory.extendedinventory;

import com.google.common.collect.*;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import tgw.evolution.items.IAdditionalEquipment;
import tgw.evolution.items.IItemFluidContainer;
import tgw.evolution.items.IMelee;
import tgw.evolution.items.ItemBlock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EvolutionRecipeBook extends ClientRecipeBook {

    private List<RecipeList> allCollections = Lists.newArrayList();
    private Map<RecipeBookCategories, List<RecipeList>> collectionsByTab = Maps.newHashMap();

    private static Map<RecipeBookCategories, List<List<IRecipe<?>>>> categorizeAndGroupRecipes(Iterable<IRecipe<?>> recipes) {
        Map<RecipeBookCategories, List<List<IRecipe<?>>>> recipeLists = Maps.newHashMap();
        Table<RecipeBookCategories, String, List<IRecipe<?>>> table = HashBasedTable.create();
        for (IRecipe<?> recipe : recipes) {
            if (!recipe.isSpecial()) {
                RecipeBookCategories category = getCategory(recipe);
                String group = recipe.getGroup();
                if (group.isEmpty()) {
                    recipeLists.computeIfAbsent(category, cat -> Lists.newArrayList()).add(ImmutableList.of(recipe));
                }
                else {
                    List<IRecipe<?>> recipeList = table.get(category, group);
                    if (recipeList == null) {
                        recipeList = Lists.newArrayList();
                        table.put(category, group, recipeList);
                        recipeLists.computeIfAbsent(category, cat -> Lists.newArrayList()).add(recipeList);
                    }
                    recipeList.add(recipe);
                }
            }
        }
        return recipeLists;
    }

    private static RecipeBookCategories getCategory(IRecipe<?> recipe) {
        IRecipeType<?> irecipetype = recipe.getType();
        if (irecipetype == IRecipeType.SMELTING) {
            if (recipe.getResultItem().getItem().isEdible()) {
                return RecipeBookCategories.FURNACE_FOOD;
            }
            return recipe.getResultItem().getItem() instanceof BlockItem ? RecipeBookCategories.FURNACE_BLOCKS : RecipeBookCategories.FURNACE_MISC;
        }
        if (irecipetype == IRecipeType.BLASTING) {
            return recipe.getResultItem().getItem() instanceof BlockItem ?
                   RecipeBookCategories.BLAST_FURNACE_BLOCKS :
                   RecipeBookCategories.BLAST_FURNACE_MISC;
        }
        if (irecipetype == IRecipeType.SMOKING) {
            return RecipeBookCategories.SMOKER_FOOD;
        }
        if (irecipetype == IRecipeType.STONECUTTING) {
            return RecipeBookCategories.STONECUTTER;
        }
        if (irecipetype == IRecipeType.CAMPFIRE_COOKING) {
            return RecipeBookCategories.CAMPFIRE;
        }
        Item item = recipe.getResultItem().getItem();
        if (item instanceof ItemBlock) {
            return RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
        }
        if (item instanceof IMelee || item instanceof IAdditionalEquipment || item instanceof IItemFluidContainer) {
            return RecipeBookCategories.CRAFTING_EQUIPMENT;
        }
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeList> getCollection(RecipeBookCategories category) {
        return this.collectionsByTab.getOrDefault(category, Collections.emptyList());
    }

    @Override
    public List<RecipeList> getCollections() {
        return this.allCollections;
    }

    @Override
    public void setupCollections(Iterable<IRecipe<?>> recipes) {
        Map<RecipeBookCategories, List<List<IRecipe<?>>>> recipeLists = categorizeAndGroupRecipes(recipes);
        Map<RecipeBookCategories, List<RecipeList>> listOfRecipeLists = Maps.newHashMap();
        ImmutableList.Builder<RecipeList> builder = ImmutableList.builder();
        recipeLists.forEach((category, lists) -> {
            Stream<RecipeList> recipeStream = lists.stream().map(RecipeList::new);
            listOfRecipeLists.put(category, recipeStream.peek(builder::add).collect(ImmutableList.toImmutableList()));
        });
        RecipeBookCategories.AGGREGATE_CATEGORIES.forEach((category, categoryList) -> listOfRecipeLists.put(category,
                                                                                                            (List<RecipeList>) categoryList.stream()
                                                                                                                                           .flatMap(
                                                                                                                                                   cat -> ((List) listOfRecipeLists.getOrDefault(
                                                                                                                                                           cat,
                                                                                                                                                           ImmutableList.of())).stream())
                                                                                                                                           .collect(
                                                                                                                                                   ImmutableList.toImmutableList())));
        this.collectionsByTab = ImmutableMap.copyOf(listOfRecipeLists);
        this.allCollections = builder.build();
    }

    private void setupList(RecipeBookCategories category, RecipeList recipeList) {
        this.collectionsByTab.computeIfAbsent(category, categories -> Lists.newArrayList()).add(recipeList);
    }
}
