package tgw.evolution.util.collection.maps;

import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.inventory.RecipeCategory;
import tgw.evolution.util.collection.lists.OList;

public class RecipeGrouper {

    private @Nullable O2OMap<String, OList<Recipe<?>>>[] maps;

    public @Nullable OList<Recipe<?>> get(RecipeCategory category, String group) {
        if (this.maps == null) {
            return null;
        }
        O2OMap<String, OList<Recipe<?>>> map = this.maps[category.ordinal()];
        if (map == null) {
            return null;
        }
        return map.get(group);
    }

    public void put(RecipeCategory category, String group, OList<Recipe<?>> recipeList) {
        if (this.maps == null) {
            this.maps = new O2OMap[RecipeCategory.VALUES.length];
        }
        int index = category.ordinal();
        O2OMap<String, OList<Recipe<?>>> map = this.maps[index];
        if (map == null) {
            map = new O2OHashMap<>();
            this.maps[index] = map;
        }
        map.put(group, recipeList);
    }
}
