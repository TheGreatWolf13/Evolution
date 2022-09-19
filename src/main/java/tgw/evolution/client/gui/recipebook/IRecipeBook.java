package tgw.evolution.client.gui.recipebook;

import net.minecraft.client.gui.screens.recipebook.RecipeShownListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public interface IRecipeBook extends RecipeShownListener {

    void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots);
}
