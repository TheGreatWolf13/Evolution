package tgw.evolution.client.gui.recipebook;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeShownListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.util.collection.lists.OArrayList;

import java.util.List;
import java.util.function.Consumer;

public class PageRecipeBook {

    private final List<ButtonRecipe> buttons;
    private final int itemsPerPage;
    private final OverlayRecipeComponent overlay = new OverlayRecipeComponent();
    private final List<RecipeShownListener> showListeners = new OArrayList<>();
    private StateSwitchingButton backButton;
    private int currentPage;
    private StateSwitchingButton forwardButton;
    private @Nullable ButtonRecipe hoveredButton;
    private @Nullable Recipe<?> lastClickedRecipe;
    private @Nullable RecipeCollection lastClickedRecipeCollection;
    private Minecraft minecraft;
    private RecipeBook recipeBook;
    private List<RecipeCollection> recipeCollections = ImmutableList.of();
    private int totalPages;

    public PageRecipeBook(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        this.buttons = new OArrayList<>(itemsPerPage);
        for (int i = 0; i < itemsPerPage; ++i) {
            //noinspection ObjectAllocationInLoop
            this.buttons.add(new ButtonRecipe());
        }
    }

    public void addListener(RecipeShownListener listener) {
        this.showListeners.remove(listener);
        this.showListeners.add(listener);
    }

    public @Nullable Recipe<?> getLastClickedRecipe() {
        return this.lastClickedRecipe;
    }

    public @Nullable RecipeCollection getLastClickedRecipeCollection() {
        return this.lastClickedRecipeCollection;
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public RecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    public void init(Minecraft mc, int x, int y, ResourceLocation resLoc) {
        this.minecraft = mc;
        assert mc.player != null;
        this.recipeBook = mc.player.getRecipeBook();
        for (int i = 0, l = this.buttons.size(); i < l; i++) {
            this.buttons.get(i).setPosition(x + 11 + 25 * (i % 5), y + 31 + 25 * (i / 5));
        }
        this.forwardButton = new StateSwitchingButton(x + 93, y + 35 + 25 * (this.itemsPerPage / 5), 12, 17, false);
        this.forwardButton.initTextureValues(1, 208, 13, 18, resLoc);
        this.backButton = new StateSwitchingButton(x + 38, y + 35 + 25 * (this.itemsPerPage / 5), 12, 17, true);
        this.backButton.initTextureValues(1, 208, 13, 18, resLoc);
    }

    protected void listButtons(Consumer<AbstractWidget> consumer) {
        consumer.accept(this.forwardButton);
        consumer.accept(this.backButton);
        this.buttons.forEach(consumer);
    }

    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button, int cornerX, int cornerY, int width, int height) {
        this.lastClickedRecipe = null;
        this.lastClickedRecipeCollection = null;
        if (this.overlay.isVisible()) {
            if (this.overlay.mouseClicked(mouseX, mouseY, button)) {
                this.lastClickedRecipe = this.overlay.getLastRecipeClicked();
                this.lastClickedRecipeCollection = this.overlay.getRecipeCollection();
            }
            else {
                this.overlay.setVisible(false);
            }
            return true;
        }
        if (this.forwardButton.mouseClicked(mouseX, mouseY, button)) {
            ++this.currentPage;
            this.updateButtonsForPage();
            return true;
        }
        if (this.backButton.mouseClicked(mouseX, mouseY, button)) {
            --this.currentPage;
            this.updateButtonsForPage();
            return true;
        }
        for (int i = 0, l = this.buttons.size(); i < l; i++) {
            ButtonRecipe btn = this.buttons.get(i);
            if (btn.mouseClicked(mouseX, mouseY, button)) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                    this.lastClickedRecipe = btn.getRecipe();
                    this.lastClickedRecipeCollection = btn.getCollection();
                }
                else if (button == GLFW.GLFW_MOUSE_BUTTON_2 && !this.overlay.isVisible() && !btn.isOnlyOption()) {
                    this.overlay.init(this.minecraft, btn.getCollection(), btn.x, btn.y, cornerX + width / 2, cornerY + 13 + height / 2,
                                      btn.getWidth());
                }
                return true;
            }
        }
        return false;
    }

    public void recipesShown(List<Recipe<?>> recipes) {
        for (int i = 0, l = this.showListeners.size(); i < l; i++) {
            this.showListeners.get(i).recipesShown(recipes);
        }
    }

    public void render(PoseStack matrices, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (this.totalPages > 1) {
            String pages = this.currentPage + 1 + "/" + this.totalPages;
            int width = this.minecraft.font.width(pages);
            //noinspection IntegerDivisionInFloatingPointContext
            this.minecraft.font.draw(matrices, pages, x - width / 2.0f + 73, y + 39 + 25 * (this.itemsPerPage / 5), 0xffff_ffff);
        }
        this.hoveredButton = null;
        for (int i = 0, l = this.buttons.size(); i < l; i++) {
            ButtonRecipe buttonRecipe = this.buttons.get(i);
            buttonRecipe.render(matrices, mouseX, mouseY, partialTicks);
            if (buttonRecipe.visible && buttonRecipe.isHoveredOrFocused()) {
                this.hoveredButton = buttonRecipe;
            }
        }
        this.backButton.render(matrices, mouseX, mouseY, partialTicks);
        this.forwardButton.render(matrices, mouseX, mouseY, partialTicks);
        this.overlay.render(matrices, mouseX, mouseY, partialTicks);
    }

    public void renderTooltip(PoseStack matrices, int x, int y) {
        if (this.minecraft.screen != null && this.hoveredButton != null && !this.overlay.isVisible()) {
            this.minecraft.screen.renderComponentTooltip(matrices, this.hoveredButton.getTooltipText(this.minecraft.screen), x, y/*,
                                                         this.hoveredButton.getRecipe().getResultItem()*/);
        }
    }

    public void setInvisible() {
        this.overlay.setVisible(false);
    }

    private void updateArrowButtons() {
        this.forwardButton.visible = this.totalPages > 1 && this.currentPage < this.totalPages - 1;
        this.backButton.visible = this.totalPages > 1 && this.currentPage > 0;
    }

    private void updateButtonsForPage() {
        int i = this.itemsPerPage * this.currentPage;
        for (int j = 0; j < this.buttons.size(); j++) {
            ButtonRecipe recipeButton = this.buttons.get(j);
            if (i + j < this.recipeCollections.size()) {
                RecipeCollection recipecollection = this.recipeCollections.get(i + j);
                recipeButton.init(recipecollection, this);
                recipeButton.visible = true;
            }
            else {
                recipeButton.visible = false;
            }
        }
        this.updateArrowButtons();
    }

    public void updateCollections(List<RecipeCollection> recipeCollections, boolean p_100438_) {
        this.recipeCollections = recipeCollections;
        this.totalPages = Mth.ceil((double) recipeCollections.size() / this.itemsPerPage);
        if (this.totalPages <= this.currentPage || p_100438_) {
            this.currentPage = 0;
        }
        this.updateButtonsForPage();
    }
}
