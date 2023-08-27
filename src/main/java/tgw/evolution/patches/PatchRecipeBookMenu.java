package tgw.evolution.patches;

import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.inventory.RecipeCategory;
import tgw.evolution.util.collection.lists.OList;

public interface PatchRecipeBookMenu {

    default void addRecipeCategories(OList<RecipeCategory> list) {
        throw new AbstractMethodError();
    }

    /**
     * Implemented in {@link net.minecraft.world.inventory.RecipeBookMenu}. DO NOT OVERRIDE.
     */
    default @UnmodifiableView OList<RecipeCategory> recipeCategories() {
        throw new AbstractMethodError();
    }
}
