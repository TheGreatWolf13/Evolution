package tgw.evolution.inventory.extendedinventory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import tgw.evolution.items.IAdditionalEquipment;
import tgw.evolution.items.IItemFluidContainer;
import tgw.evolution.items.IMelee;
import tgw.evolution.items.ItemBlock;
import tgw.evolution.util.collection.lists.OArrayList;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EvolutionRecipeBook extends ClientRecipeBook {

    private List<RecipeCollection> allCollections = new OArrayList<>();
    private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = new EnumMap<>(RecipeBookCategories.class);

    private static Map<RecipeBookCategories, List<List<Recipe<?>>>> categorizeAndGroupRecipes(Iterable<Recipe<?>> recipes) {
        Map<RecipeBookCategories, List<List<Recipe<?>>>> recipeLists = new EnumMap<>(RecipeBookCategories.class);
        Table<RecipeBookCategories, String, List<Recipe<?>>> table = HashBasedTable.create();
        for (Recipe<?> recipe : recipes) {
            if (!recipe.isSpecial()) {
                RecipeBookCategories category = getCategory(recipe);
                String group = recipe.getGroup();
                if (group.isEmpty()) {
                    List<List<Recipe<?>>> lists = recipeLists.get(category);
                    if (lists == null) {
                        lists = new OArrayList<>();
                        recipeLists.put(category, lists);
                    }
                    lists.add(ImmutableList.of(recipe));
                }
                else {
                    List<Recipe<?>> recipeList = table.get(category, group);
                    if (recipeList == null) {
                        recipeList = new OArrayList<>();
                        table.put(category, group, recipeList);
                        List<List<Recipe<?>>> lists = recipeLists.get(category);
                        if (lists == null) {
                            lists = new OArrayList<>();
                            recipeLists.put(category, lists);
                        }
                        lists.add(recipeList);
                    }
                    recipeList.add(recipe);
                }
            }
        }
        return recipeLists;
    }

    private static RecipeBookCategories getCategory(Recipe<?> recipe) {
        RecipeType<?> irecipetype = recipe.getType();
        if (irecipetype == RecipeType.SMELTING) {
            if (recipe.getResultItem().getItem().isEdible()) {
                return RecipeBookCategories.FURNACE_FOOD;
            }
            return recipe.getResultItem().getItem() instanceof BlockItem ? RecipeBookCategories.FURNACE_BLOCKS : RecipeBookCategories.FURNACE_MISC;
        }
        if (irecipetype == RecipeType.BLASTING) {
            return recipe.getResultItem().getItem() instanceof BlockItem ?
                   RecipeBookCategories.BLAST_FURNACE_BLOCKS :
                   RecipeBookCategories.BLAST_FURNACE_MISC;
        }
        if (irecipetype == RecipeType.SMOKING) {
            return RecipeBookCategories.SMOKER_FOOD;
        }
        if (irecipetype == RecipeType.STONECUTTING) {
            return RecipeBookCategories.STONECUTTER;
        }
        if (irecipetype == RecipeType.CAMPFIRE_COOKING) {
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
    public List<RecipeCollection> getCollection(RecipeBookCategories category) {
        return this.collectionsByTab.getOrDefault(category, Collections.emptyList());
    }

    @Override
    public List<RecipeCollection> getCollections() {
        return this.allCollections;
    }

    @Override
    public void setupCollections(Iterable<Recipe<?>> recipes) {
        Map<RecipeBookCategories, List<List<Recipe<?>>>> recipeLists = categorizeAndGroupRecipes(recipes);
        Map<RecipeBookCategories, List<RecipeCollection>> listOfRecipeLists = new EnumMap<>(RecipeBookCategories.class);
        ImmutableList.Builder<RecipeCollection> builder = ImmutableList.builder();
        for (Map.Entry<RecipeBookCategories, List<List<Recipe<?>>>> entry : recipeLists.entrySet()) {
            Stream<RecipeCollection> recipeStream = entry.getValue().stream().map(RecipeCollection::new);
            //noinspection ObjectAllocationInLoop
            listOfRecipeLists.put(entry.getKey(), recipeStream.peek(builder::add).collect(ImmutableList.toImmutableList()));
        }
        for (Map.Entry<RecipeBookCategories, List<RecipeBookCategories>> entry : RecipeBookCategories.AGGREGATE_CATEGORIES.entrySet()) {
            //noinspection ObjectAllocationInLoop
            listOfRecipeLists.put(entry.getKey(),
                                  (List<RecipeCollection>) entry.getValue().stream()
                                                                .flatMap(
                                                                        cat -> ((List) listOfRecipeLists.getOrDefault(
                                                                                cat,
                                                                                ImmutableList.of())).stream())
                                                                .collect(
                                                                        ImmutableList.toImmutableList()));
        }
        this.collectionsByTab = ImmutableMap.copyOf(listOfRecipeLists);
        this.allCollections = builder.build();
    }
}
