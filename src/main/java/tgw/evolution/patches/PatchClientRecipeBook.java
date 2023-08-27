package tgw.evolution.patches;

import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import tgw.evolution.inventory.RecipeCategory;
import tgw.evolution.util.collection.lists.OList;

public interface PatchClientRecipeBook {

    default OList<RecipeCollection> getAllRecipes() {
        throw new AbstractMethodError();
    }

    default OList<RecipeCollection> getCollection(RecipeCategory category) {
        throw new AbstractMethodError();
    }
}
