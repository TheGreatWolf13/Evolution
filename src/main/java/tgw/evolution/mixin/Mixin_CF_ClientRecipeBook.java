package tgw.evolution.mixin;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.inventory.RecipeCategory;
import tgw.evolution.items.IAdditionalEquipment;
import tgw.evolution.items.IItemFluidContainer;
import tgw.evolution.items.IMelee;
import tgw.evolution.items.ItemBlock;
import tgw.evolution.patches.PatchClientRecipeBook;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.R2OEnumMap;
import tgw.evolution.util.collection.maps.R2OMap;
import tgw.evolution.util.collection.maps.RecipeGrouper;

import java.util.List;
import java.util.Map;

@Mixin(ClientRecipeBook.class)
public abstract class Mixin_CF_ClientRecipeBook extends RecipeBook implements PatchClientRecipeBook {

    @Shadow @DeleteField private List<RecipeCollection> allCollections;
    @Unique private OList<RecipeCollection> allRecipes;
    @Shadow @DeleteField private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab;
    @Unique private R2OMap<RecipeCategory, OList<RecipeCollection>> recipesByTab;

    @ModifyConstructor
    public Mixin_CF_ClientRecipeBook() {
        this.allRecipes = OList.emptyList();
        this.recipesByTab = R2OMap.emptyMap();
    }

    @Contract(value = "_ -> fail", pure = true)
    @Overwrite
    @DeleteMethod
    private static Map<RecipeBookCategories, List<List<Recipe<?>>>> categorizeAndGroupRecipes(Iterable<Recipe<?>> recipes) {
        throw new AbstractMethodError();
    }

    @Unique
    private static R2OMap<RecipeCategory, OList<OList<Recipe<?>>>> categorizeAndGroupRecipes_(Iterable<Recipe<?>> recipes) {
        R2OMap<RecipeCategory, OList<OList<Recipe<?>>>> recipeLists = new R2OEnumMap<>(RecipeCategory.VALUES);
        RecipeGrouper table = new RecipeGrouper();
        for (Recipe<?> recipe : recipes) {
            if (!recipe.isSpecial()) {
                RecipeCategory category = getCategory_(recipe);
                String group = recipe.getGroup();
                if (group.isEmpty()) {
                    OList<OList<Recipe<?>>> lists = recipeLists.get(category);
                    if (lists == null) {
                        lists = new OArrayList<>();
                        recipeLists.put(category, lists);
                    }
                    lists.add(OList.of(recipe));
                }
                else {
                    OList<Recipe<?>> recipeList = table.get(category, group);
                    if (recipeList == null) {
                        recipeList = new OArrayList<>();
                        table.put(category, group, recipeList);
                        OList<OList<Recipe<?>>> lists = recipeLists.get(category);
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

    @Contract(value = "_ -> fail", pure = true)
    @Overwrite
    @DeleteMethod
    private static RecipeBookCategories getCategory(Recipe<?> recipe) {
        throw new AbstractMethodError();
    }

    @Unique
    private static RecipeCategory getCategory_(Recipe<?> recipe) {
        RecipeType<?> type = recipe.getType();
        if (type == RecipeType.SMELTING) {
            if (recipe.getResultItem().getItem().isEdible()) {
                return RecipeCategory.FURNACE_FOOD;
            }
            return isBlock(recipe) ? RecipeCategory.FURNACE_BLOCKS : RecipeCategory.FURNACE_MISC;
        }
        if (type == RecipeType.BLASTING) {
            return isBlock(recipe) ? RecipeCategory.BLAST_FURNACE_BLOCKS : RecipeCategory.BLAST_FURNACE_MISC;
        }
        if (type == RecipeType.SMOKING) {
            return RecipeCategory.SMOKER_FOOD;
        }
        if (type == RecipeType.STONECUTTING) {
            return RecipeCategory.STONECUTTER;
        }
        if (type == RecipeType.CAMPFIRE_COOKING) {
            return RecipeCategory.CAMPFIRE;
        }
        Item item = recipe.getResultItem().getItem();
        if (item instanceof ItemBlock || item instanceof BlockItem) {
            return RecipeCategory.CRAFTING_BUILDING_BLOCKS;
        }
        if (item instanceof IMelee || item instanceof IAdditionalEquipment || item instanceof IItemFluidContainer) {
            return RecipeCategory.CRAFTING_EQUIPMENT;
        }
        return RecipeCategory.CRAFTING_MISC;
    }

    @Unique
    private static boolean isBlock(Recipe<?> recipe) {
        Item item = recipe.getResultItem().getItem();
        return item instanceof ItemBlock || item instanceof BlockItem;
    }

    @Override
    public OList<RecipeCollection> getAllRecipes() {
        return this.allRecipes;
    }

    @Overwrite
    @DeleteMethod
    public List<RecipeCollection> getCollection(RecipeBookCategories categories) {
        throw new AbstractMethodError();
    }

    @Override
    public OList<RecipeCollection> getCollection(RecipeCategory category) {
        return this.recipesByTab.getOrDefault(category, OList.emptyList());
    }

    @Overwrite
    @DeleteMethod
    public List<RecipeCollection> getCollections() {
        throw new AbstractMethodError();
    }

    @Overwrite
    public void setupCollections(Iterable<Recipe<?>> recipes) {
        R2OMap<RecipeCategory, OList<OList<Recipe<?>>>> recipeLists = categorizeAndGroupRecipes_(recipes);
        R2OMap<RecipeCategory, OList<RecipeCollection>> byTab = new R2OEnumMap<>(RecipeCategory.VALUES);
        OList<RecipeCollection> allRecipes = new OArrayList<>();
        for (R2OMap.Entry<RecipeCategory, OList<OList<Recipe<?>>>> e = recipeLists.fastEntries(); e != null; e = recipeLists.fastEntries()) {
            OList<OList<Recipe<?>>> value = e.value();
            int len = value.size();
            //noinspection ObjectAllocationInLoop
            OList<RecipeCollection> list = new OArrayList<>(len);
            for (int i = 0; i < len; ++i) {
                //noinspection ObjectAllocationInLoop
                RecipeCollection recipeCollection = new RecipeCollection(value.get(i));
                allRecipes.add(recipeCollection);
                list.add(recipeCollection);
            }
            list.trimCollection();
            byTab.put(e.key(), list.view());
        }
        R2OMap<RecipeCategory, OList<RecipeCategory>> aggregate = RecipeCategory.AGGREGATE_CATEGORIES;
        for (R2OMap.Entry<RecipeCategory, OList<RecipeCategory>> e = aggregate.fastEntries(); e != null; e = aggregate.fastEntries()) {
            OList<RecipeCategory> value = e.value();
            //noinspection ObjectAllocationInLoop
            OList<RecipeCollection> list = new OArrayList<>();
            for (int i = 0, len = value.size(); i < len; ++i) {
                RecipeCategory category = value.get(i);
                OList<RecipeCollection> collections = byTab.get(category);
                if (collections != null && !collections.isEmpty()) {
                    list.addAll(collections);
                }
            }
            list.trimCollection();
            byTab.put(e.key(), list.view());
        }
        byTab.trimCollection();
        this.recipesByTab = byTab.view();
        allRecipes.trimCollection();
        this.allRecipes = allRecipes.view();
    }
}
