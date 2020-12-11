package tgw.evolution.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.recipebook.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.client.config.GuiUtils;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.MethodHandler;

import java.util.List;

public class GuiRecipeBook extends RecipeBookGui {

    private static final FieldHandler<RecipeBookPage, RecipeWidget> HOVERED_BUTTON_FIELD = new FieldHandler<>(RecipeBookPage.class, "field_194201_b");
    private static final FieldHandler<RecipeBookPage, RecipeOverlayGui> OVERLAY_FIELD = new FieldHandler<>(RecipeBookPage.class, "field_194202_c");
    private static final MethodHandler<RecipeWidget, List<IRecipe<?>>> GET_ORDERED_RECIPES_METHOD = new MethodHandler<>(RecipeWidget.class,
                                                                                                                        "func_193927_f");
    private static final FieldHandler<RecipeWidget, Integer> CURRENT_INDEX_FIELD = new FieldHandler<>(RecipeWidget.class, "field_193932_t");

    private void pageRenderTooltip(int mouseX, int mouseY) {
        RecipeWidget hoveredButton = HOVERED_BUTTON_FIELD.get(this.recipeBookPage);
        if (this.mc.currentScreen != null && hoveredButton != null && !OVERLAY_FIELD.get(this.recipeBookPage).isVisible()) {
            GuiUtils.preItemToolTip(GET_ORDERED_RECIPES_METHOD.call(hoveredButton).get(CURRENT_INDEX_FIELD.get(hoveredButton)).getRecipeOutput());
            this.mc.currentScreen.renderTooltip(hoveredButton.getToolTipText(this.mc.currentScreen), mouseX, mouseY);
            GuiUtils.postItemToolTip();
        }
    }

    private void renderGhostRecipeTooltip(int x, int y, int mouseX, int mouseY) {
        ItemStack stack = null;
        for (int i = 0; i < this.ghostRecipe.size(); ++i) {
            GhostRecipe.GhostIngredient ghostIngredient = this.ghostRecipe.get(i);
            int j = ghostIngredient.getX() + x;
            int k = ghostIngredient.getY() + y;
            if (mouseX >= j && mouseY >= k && mouseX < j + 16 && mouseY < k + 16) {
                stack = ghostIngredient.getItem();
            }
        }
        if (stack != null && this.mc.currentScreen != null) {
            this.renderTooltip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderTooltip(int x, int y, int mouseX, int mouseY) {
        if (this.isVisible()) {
            this.pageRenderTooltip(mouseX, mouseY);
            if (this.toggleRecipesBtn.isHovered()) {
                String s = this.func_205703_f();
                if (this.mc.currentScreen != null) {
                    this.mc.currentScreen.renderTooltip(s, mouseX, mouseY);
                }
            }
            this.renderGhostRecipeTooltip(x, y, mouseX, mouseY);
        }
    }

    public void renderTooltip(ItemStack stack, int mouseX, int mouseY) {
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        GuiUtils.preItemToolTip(stack);
        this.mc.currentScreen.renderTooltip(this.mc.currentScreen.getTooltipFromItem(stack),
                                            mouseX,
                                            mouseY,
                                            font == null ? this.mc.fontRenderer : font);
        GuiUtils.postItemToolTip();
    }
}
