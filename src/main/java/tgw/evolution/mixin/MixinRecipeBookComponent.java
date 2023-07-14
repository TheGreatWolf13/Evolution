package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(RecipeBookComponent.class)
public abstract class MixinRecipeBookComponent extends GuiComponent {

    @Shadow protected Minecraft minecraft;

    /**
     * @author TheGreatWolf
     * @reason Avoid iterator allocation
     */
    @Overwrite
    public void recipesShown(List<Recipe<?>> recipes) {
        assert this.minecraft.player != null;
        for (int i = 0, len = recipes.size(); i < len; i++) {
            this.minecraft.player.removeRecipeHighlight(recipes.get(i));
        }
    }
}
