package tgw.evolution.inventory.extendedinventory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import tgw.evolution.items.IAdditionalEquipment;
import tgw.evolution.items.IItemFluidContainer;
import tgw.evolution.items.IMelee;
import tgw.evolution.items.ItemBlock;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EvolutionRecipeBook extends ClientRecipeBook {

    private final RecipeManager recipeManager;
    private final Map<RecipeBookCategories, List<RecipeList>> recipesByCategory = Maps.newHashMap();
    private final List<RecipeList> allRecipes = Lists.newArrayList();

    public EvolutionRecipeBook(RecipeManager recipeManager) {
        super(recipeManager);
        this.recipeManager = recipeManager;
    }

    private static RecipeBookCategories getCategory(IRecipe<?> recipe) {
        IRecipeType<?> irecipetype = recipe.getType();
        if (irecipetype == IRecipeType.SMELTING) {
            if (recipe.getRecipeOutput().getItem().isFood()) {
                return RecipeBookCategories.FURNACE_FOOD;
            }
            return recipe.getRecipeOutput().getItem() instanceof BlockItem ? RecipeBookCategories.FURNACE_BLOCKS : RecipeBookCategories.FURNACE_MISC;
        }
        if (irecipetype == IRecipeType.BLASTING) {
            return recipe.getRecipeOutput().getItem() instanceof BlockItem ?
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
        Item item = recipe.getRecipeOutput().getItem();
        if (item instanceof ItemBlock) {
            return RecipeBookCategories.BUILDING_BLOCKS;
        }
        if (item instanceof IMelee || item instanceof IAdditionalEquipment || item instanceof IItemFluidContainer) {
            return RecipeBookCategories.EQUIPMENT;
        }
        return RecipeBookCategories.MISC;
    }

    @Override
    public List<RecipeList> getRecipes() {
        return this.allRecipes;
    }

    @Override
    public List<RecipeList> getRecipes(RecipeBookCategories category) {
        return this.recipesByCategory.getOrDefault(category, Collections.emptyList());
    }

    private RecipeList newRecipeList(RecipeBookCategories categories) {
        RecipeList recipelist = new RecipeList();
        this.allRecipes.add(recipelist);
        this.recipesByCategory.computeIfAbsent(categories, category -> Lists.newArrayList()).add(recipelist);
        if (categories != RecipeBookCategories.FURNACE_BLOCKS &&
            categories != RecipeBookCategories.FURNACE_FOOD &&
            categories != RecipeBookCategories.FURNACE_MISC) {
            if (categories != RecipeBookCategories.BLAST_FURNACE_BLOCKS && categories != RecipeBookCategories.BLAST_FURNACE_MISC) {
                if (categories == RecipeBookCategories.SMOKER_FOOD) {
                    this.setupList(RecipeBookCategories.SMOKER_SEARCH, recipelist);
                }
                else if (categories == RecipeBookCategories.STONECUTTER) {
                    this.setupList(RecipeBookCategories.STONECUTTER, recipelist);
                }
                else if (categories == RecipeBookCategories.CAMPFIRE) {
                    this.setupList(RecipeBookCategories.CAMPFIRE, recipelist);
                }
                else {
                    this.setupList(RecipeBookCategories.SEARCH, recipelist);
                }
            }
            else {
                this.setupList(RecipeBookCategories.BLAST_FURNACE_SEARCH, recipelist);
            }
        }
        else {
            this.setupList(RecipeBookCategories.FURNACE_SEARCH, recipelist);
        }
        return recipelist;
    }

    @Override
    public void rebuildTable() {
        this.allRecipes.clear();
        this.recipesByCategory.clear();
        Table<RecipeBookCategories, String, RecipeList> table = HashBasedTable.create();
        for (IRecipe<?> recipe : this.recipeManager.getRecipes()) {
            if (!recipe.isDynamic()) {
                RecipeBookCategories category = getCategory(recipe);
                String s = recipe.getGroup();
                RecipeList recipelist;
                if (s.isEmpty()) {
                    recipelist = this.newRecipeList(category);
                }
                else {
                    recipelist = table.get(category, s);
                    if (recipelist == null) {
                        recipelist = this.newRecipeList(category);
                        table.put(category, s, recipelist);
                    }
                }
                recipelist.add(recipe);
            }
        }
    }

    private void setupList(RecipeBookCategories category, RecipeList recipeList) {
        this.recipesByCategory.computeIfAbsent(category, categories -> Lists.newArrayList()).add(recipeList);
    }
}
