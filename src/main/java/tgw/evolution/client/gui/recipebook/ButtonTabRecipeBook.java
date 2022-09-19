package tgw.evolution.client.gui.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.MouseButton;

import java.util.List;

public class ButtonTabRecipeBook extends StateSwitchingButton {

    private static final float ANIMATION_TIME = 15.0F;
    private final RecipeBookCategories category;
    private final boolean rightSide;
    private float animationTime;

    public ButtonTabRecipeBook(RecipeBookCategories category, ResourceLocation resLoc, boolean rightSide) {
        super(0, 0, 35, 27, false);
        this.category = category;
        this.initTextureValues(153, 2, 35, 0, resLoc);
        this.rightSide = rightSide;
    }

    public RecipeBookCategories getCategory() {
        return this.category;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean clicked = this.clicked(mouseX, mouseY);
                if (clicked) {
                    this.onClick(mouseX, mouseY);
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (this.animationTime > 0.0F) {
            float scale = 1.0F + 0.1F * Mth.sin(this.animationTime / ANIMATION_TIME * Mth.PI);
            matrices.pushPose();
            matrices.translate(this.x + 8, this.y + 12, 0);
            matrices.scale(1.0F, scale, 1.0F);
            matrices.translate(-(this.x + 8), -(this.y + 12), 0);
        }
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
//        RenderSystem.disableDepthTest();
        int i = this.xTexStart;
        int j = this.yTexStart;
        if (this.isStateTriggered) {
            i += this.xDiffTex;
        }
        if (this.isHoveredOrFocused()) {
            j += this.yDiffTex;
        }
        int k = this.x;
        if (this.isStateTriggered) {
            k -= 2;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.blit(matrices, k, this.y, i, j, this.width, this.height);
//        RenderSystem.enableDepthTest();
        this.renderIcon(mc.getItemRenderer());
        if (this.animationTime > 0.0F) {
            matrices.popPose();
            this.animationTime -= partialTicks;
        }
    }

    private void renderIcon(ItemRenderer itemRenderer) {
        List<ItemStack> list = this.category.getIconItems();
        int dx;
        if (this.rightSide) {
            dx = this.isStateTriggered ? 0 : -2;
        }
        else {
            dx = this.isStateTriggered ? -2 : 0;
        }
        if (list.size() == 1) {
            itemRenderer.renderAndDecorateFakeItem(list.get(0), this.x + 9 + dx, this.y + 5);
        }
        else if (list.size() == 2) {
            itemRenderer.renderAndDecorateFakeItem(list.get(0), this.x + 3 + dx, this.y + 5);
            itemRenderer.renderAndDecorateFakeItem(list.get(1), this.x + 14 + dx, this.y + 5);
        }
    }

    public void startAnimation(Minecraft mc) {
        assert mc.player != null;
        ClientRecipeBook recipeBook = mc.player.getRecipeBook();
        List<RecipeCollection> list = recipeBook.getCollection(this.category);
        if (mc.player.containerMenu instanceof RecipeBookMenu menu) {
            for (RecipeCollection collection : list) {
                List<Recipe<?>> recipes = collection.getRecipes(recipeBook.isFiltering(menu));
                for (int i = 0, l = recipes.size(); i < l; i++) {
                    if (recipeBook.willHighlight(recipes.get(i))) {
                        this.animationTime = ANIMATION_TIME;
                        return;
                    }
                }
            }
        }
    }

    public boolean updateVisibility(ClientRecipeBook collection) {
        List<RecipeCollection> list = collection.getCollection(this.category);
        this.visible = false;
        for (RecipeCollection recipecollection : list) {
            if (recipecollection.hasKnownRecipes() && recipecollection.hasFitting()) {
                this.visible = true;
                break;
            }
        }
        return this.visible;
    }
}
