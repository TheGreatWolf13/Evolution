package tgw.evolution.mixin;

import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(RecipeButton.class)
public abstract class MixinRecipeButton {

    @Shadow private float animationTime;
    @Shadow private RecipeBook book;
    @Shadow private RecipeCollection collection;
    @Shadow private RecipeBookMenu<?> menu;

    /**
     * @author TheGreatWolf
     * @reason Avoid iterator allocation
     */
    @Overwrite
    public void init(RecipeCollection collection, RecipeBookPage recipeBookPage) {
        this.collection = collection;
        assert recipeBookPage.getMinecraft().player != null;
        this.menu = (RecipeBookMenu) recipeBookPage.getMinecraft().player.containerMenu;
        this.book = recipeBookPage.getRecipeBook();
        List<Recipe<?>> list = collection.getRecipes(this.book.isFiltering(this.menu));
        for (int i = 0, len = list.size(); i < len; i++) {
            Recipe<?> recipe = list.get(i);
            if (this.book.willHighlight(recipe)) {
                recipeBookPage.recipesShown(list);
                this.animationTime = 15.0F;
                break;
            }
        }
    }
}
