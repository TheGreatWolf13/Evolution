package tgw.evolution.client.gui.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.Evolution;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.util.collection.lists.OArrayList;

import java.util.List;

public class ButtonRecipe extends AbstractWidget {

    public static final int TICKS_TO_SWAP = 30;
    private static final float ANIMATION_TIME = 15.0F;
    private static final int BACKGROUND_SIZE = 25;
    private final ResourceLocation resBackground = Evolution.getResource("textures/gui/recipe_book.png");
    private final Component textMoreRecipes = new TranslatableComponent("evolution.gui.recipebook.moreRecipes");
    private float animationTime;
    private RecipeBook book;
    private RecipeCollection collection;
    private int currentIndex;
    private RecipeBookMenu<?> menu;
    private float time;

    public ButtonRecipe() {
        super(0, 0, BACKGROUND_SIZE, BACKGROUND_SIZE, TextComponent.EMPTY);
    }

    public RecipeCollection getCollection() {
        return this.collection;
    }

    private List<Recipe<?>> getOrderedRecipes() {
        List<Recipe<?>> list = this.collection.getDisplayRecipes(true);
        if (!this.book.isFiltering(this.menu)) {
            list.addAll(this.collection.getDisplayRecipes(false));
        }
        return list;
    }

    public Recipe<?> getRecipe() {
        List<Recipe<?>> list = this.getOrderedRecipes();
        return list.get(this.currentIndex);
    }

    public List<Component> getTooltipText(Screen pScreen) {
        ItemStack result = this.getOrderedRecipes().get(this.currentIndex).getResultItem();
        List<Component> list = new OArrayList<>(pScreen.getTooltipFromItem(result));
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            list.add(this.textMoreRecipes);
        }
        return list;
    }

    @Override
    public int getWidth() {
        return BACKGROUND_SIZE;
    }

    public void init(RecipeCollection recipeCollection, PageRecipeBook page) {
        this.collection = recipeCollection;
        assert page.getMinecraft().player != null;
        this.menu = (RecipeBookMenu) page.getMinecraft().player.containerMenu;
        this.book = page.getRecipeBook();
        List<Recipe<?>> list = recipeCollection.getRecipes(this.book.isFiltering(this.menu));
        for (int i = 0, l = list.size(); i < l; i++) {
            if (this.book.willHighlight(list.get(i))) {
                page.recipesShown(list);
                this.animationTime = ANIMATION_TIME;
                break;
            }
        }
    }

    public boolean isOnlyOption() {
        return this.getOrderedRecipes().size() == 1;
    }

    @Override
    protected boolean isValidClickButton(@MouseButton int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_1 || button == GLFW.GLFW_MOUSE_BUTTON_2;
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (!Screen.hasControlDown()) {
            this.time += partialTicks;
        }
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
        RenderSystem.setShaderTexture(0, this.resBackground);
        int i = 29;
        if (!this.collection.hasCraftable()) {
            i += BACKGROUND_SIZE;
        }
        int j = 206;
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            j += BACKGROUND_SIZE;
        }
        boolean isAnimating = this.animationTime > 0.0F;
        PoseStack internalMat = RenderSystem.getModelViewStack();
        if (isAnimating) {
            float scale = 1.0F + 0.1F * Mth.sin(this.animationTime / ANIMATION_TIME * Mth.PI);
            internalMat.pushPose();
            internalMat.translate(this.x + 8, this.y + 12, 0);
            internalMat.scale(scale, scale, 1.0F);
            internalMat.translate(-(this.x + 8), -(this.y + 12), 0);
            RenderSystem.applyModelViewMatrix();
            this.animationTime -= partialTicks;
        }
        this.blit(matrices, this.x, this.y, i, j, this.width, this.height);
        List<Recipe<?>> list = this.getOrderedRecipes();
        this.currentIndex = Mth.floor(this.time / TICKS_TO_SWAP) % list.size();
        ItemStack itemstack = list.get(this.currentIndex).getResultItem();
        int k = 4;
        if (this.collection.hasSingleResultItem() && this.getOrderedRecipes().size() > 1) {
            mc.getItemRenderer().renderAndDecorateItem(itemstack, this.x + k + 1, this.y + k + 1, 0, 10);
            --k;
        }
        mc.getItemRenderer().renderAndDecorateFakeItem(itemstack, this.x + k, this.y + k);
        if (isAnimating) {
            internalMat.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        ItemStack result = this.getOrderedRecipes().get(this.currentIndex).getResultItem();
        narrationElementOutput.add(NarratedElementType.TITLE, new TranslatableComponent("narration.recipe", result.getHoverName()));
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            narrationElementOutput.add(NarratedElementType.USAGE, new TranslatableComponent("narration.button.usage.hovered"),
                                       new TranslatableComponent("narration.recipe.usage.more"));
        }
        else {
            narrationElementOutput.add(NarratedElementType.USAGE, new TranslatableComponent("narration.button.usage.hovered"));
        }
    }
}
