package tgw.evolution.mixin;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(RecipeBookTabButton.class)
public abstract class MixinRecipeBookTabButton extends StateSwitchingButton {

    @Shadow private float animationTime;
    @Shadow @Final private RecipeBookCategories category;

    public MixinRecipeBookTabButton(int pX, int pY, int pWidth, int pHeight, boolean pInitialState) {
        super(pX, pY, pWidth, pHeight, pInitialState);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid iterator allocation
     */
    @Overwrite
    public void startAnimation(Minecraft mc) {
        assert mc.player != null;
        ClientRecipeBook recipeBook = mc.player.getRecipeBook();
        List<RecipeCollection> list = recipeBook.getCollection(this.category);
        if (mc.player.containerMenu instanceof RecipeBookMenu menu) {
            for (int i = 0, len = list.size(); i < len; i++) {
                RecipeCollection recipeCollection = list.get(i);
                List<Recipe<?>> recipes = recipeCollection.getRecipes(recipeBook.isFiltering(menu));
                for (int j = 0, len2 = recipes.size(); j < len2; j++) {
                    Recipe<?> recipe = recipes.get(j);
                    if (recipeBook.willHighlight(recipe)) {
                        this.animationTime = 15.0F;
                        return;
                    }
                }
            }
        }
    }
}
