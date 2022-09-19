package tgw.evolution.client.gui.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.widgets.AdvEditBox;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.math.MathHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ComponentRecipeBook extends GuiComponent implements IRecipeBook, GuiEventListener, NarratableEntry, PlaceRecipe<Ingredient> {

    protected final GhostRecipe ghostRecipe = new GhostRecipe();
    protected final ResourceLocation resBackground;
    private final PageRecipeBook recipeBookPage;
    private final StackedContents stackedContents = new StackedContents();
    private final List<ButtonTabRecipeBook> tabButtons = new RArrayList<>();
    private final int texHeight;
    private final int texWidth;
    private final Component textSearch = new TranslatableComponent("evolution.gui.recipebook.search").withStyle(ChatFormatting.ITALIC)
                                                                                                     .withStyle(ChatFormatting.GRAY);
    private final Component textToggleAll = new TranslatableComponent("evolution.gui.recipebook.showingAll");
    private final Component textToggleCraftable = new TranslatableComponent("evolution.gui.recipebook.showingCraftable");
    protected int cornerX;
    protected int cornerY;
    protected StateSwitchingButton filterButton;
    protected RecipeBookMenu<?> menu;
    protected Minecraft minecraft;
    protected int xOffset;
    private ClientRecipeBook book;
    private int height;
    private boolean ignoreTextInput;
    private String lastSearch = "";
    private @Nullable AdvEditBox searchBox;
    private @Nullable ButtonTabRecipeBook selectedTab;
    private int timesInventoryChanged;
    private boolean visible;
    private int width;
    private boolean widthTooNarrow;

    public ComponentRecipeBook() {
        this(20, Evolution.getResource("textures/gui/recipe_book.png"), 147, 166);
    }

    public ComponentRecipeBook(int recipesPerPage, ResourceLocation loc, int texWidth, int texHeight) {
        this.recipeBookPage = new PageRecipeBook(recipesPerPage);
        this.resBackground = loc;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }

    @Override
    public void addItemToSlot(Iterator<Ingredient> it, int slotId, int maxAmount, int y, int x) {
        Ingredient ingredient = it.next();
        if (!ingredient.isEmpty()) {
            Slot slot = this.menu.slots.get(slotId);
            this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
        }
    }

    protected boolean areTabsOnTheRight() {
        return false;
    }

    @Override
    public boolean changeFocus(boolean focus) {
        return false;
    }

    @Override
    public boolean charTyped(char c, @Modifiers int mod) {
        if (this.ignoreTextInput) {
            return false;
        }
        assert this.minecraft.player != null;
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            assert this.searchBox != null;
            if (this.searchBox.charTyped(c, mod)) {
                this.checkSearchStringUpdate();
                return true;
            }
            return GuiEventListener.super.charTyped(c, mod);
        }
        return false;
    }

    private void checkSearchStringUpdate() {
        assert this.searchBox != null;
        String s = this.searchBox.getValue().toLowerCase(Locale.ROOT);
        if (!s.equals(this.lastSearch)) {
            this.updateCollections(false);
            this.lastSearch = s;
        }
    }

    private Component getFilterButtonTooltip() {
        return this.filterButton.isStateTriggered() ? this.getRecipeFilterName() : this.textToggleAll;
    }

    protected final int getHeight() {
        return this.height;
    }

    protected Component getRecipeFilterName() {
        return this.textToggleCraftable;
    }

    protected int getTabsDx() {
        return -30;
    }

    protected final int getWidth() {
        return this.width;
    }

    public boolean hasClickedOutside(double mouseX, double mouseY) {
        if (!this.isVisible()) {
            return true;
        }
        if (MathHelper.isMouseInArea(mouseX, mouseY, this.cornerX, this.cornerY, this.texWidth, this.texHeight)) {
            return false;
        }
        assert this.selectedTab != null;
        return !this.selectedTab.isHoveredOrFocused();
    }

    public final void init(int width, int height, Minecraft mc, boolean widthTooNarrow, RecipeBookMenu<?> menu) {
        this.minecraft = mc;
        this.width = width;
        this.height = height;
        this.menu = menu;
        this.widthTooNarrow = widthTooNarrow;
        assert mc.player != null;
        mc.player.containerMenu = menu;
        this.book = mc.player.getRecipeBook();
        this.timesInventoryChanged = mc.player.getInventory().getTimesChanged();
        this.visible = this.isVisibleAccordingToBookData();
        this.initCoordinates();
        if (this.visible) {
            this.initVisuals();
        }
        mc.keyboardHandler.setSendRepeatsToGui(true);
    }

    protected void initCoordinates() {
        this.xOffset = this.widthTooNarrow ? 0 : 86;
        this.cornerX = (this.width - this.texWidth) / 2 - this.xOffset;
        this.cornerY = (this.height - this.texHeight) / 2;
    }

    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(152, 41, 28, 18, this.resBackground);
    }

    protected void initVisuals() {
        this.stackedContents.clear();
        assert this.minecraft.player != null;
        this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        String s = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new AdvEditBox(this.minecraft.font, this.cornerX + 25, this.cornerY + 14, 80, 9 + 5, EvolutionTexts.EMPTY);
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(0xff_ffff);
        this.searchBox.setValue(s);
        this.recipeBookPage.init(this.minecraft, this.cornerX, this.cornerY, this.resBackground);
        this.recipeBookPage.addListener(this);
        this.filterButton = new StateSwitchingButton(this.cornerX + 110, this.cornerY + 12, 26, 16, this.book.isFiltering(this.menu));
        this.initFilterButtonTextures();
        this.tabButtons.clear();
        for (RecipeBookCategories categories : this.menu.getRecipeBookCategories()) {
            //noinspection ObjectAllocationInLoop
            this.tabButtons.add(new ButtonTabRecipeBook(categories, this.resBackground, this.areTabsOnTheRight()));
        }
        if (this.selectedTab != null) {
            ButtonTabRecipeBook r = null;
            for (int i = 0, l = this.tabButtons.size(); i < l; i++) {
                ButtonTabRecipeBook t = this.tabButtons.get(i);
                if (t.getCategory() == this.selectedTab.getCategory()) {
                    r = t;
                    break;
                }
            }
            this.selectedTab = r;
        }
        if (this.selectedTab == null) {
            this.selectedTab = this.tabButtons.get(0);
        }
        this.selectedTab.setStateTriggered(true);
        this.updateCollections(false);
        this.updateTabs();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    public boolean isVisible() {
        return this.visible;
    }

    private boolean isVisibleAccordingToBookData() {
        return this.book.isOpen(this.menu.getRecipeBookType());
    }

    protected final boolean isWidthTooNarrow() {
        return this.widthTooNarrow;
    }

    @Override
    public boolean keyPressed(@Key int key, int scan, @Modifiers int mod) {
        this.ignoreTextInput = false;
        assert this.minecraft.player != null;
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                this.setVisible(false);
                return true;
            }
            assert this.searchBox != null;
            if (this.searchBox.keyPressed(key, scan, mod)) {
                this.checkSearchStringUpdate();
                return true;
            }
            if (this.searchBox.isFocused() && this.searchBox.isVisible()) {
                return true;
            }
            if (this.minecraft.options.keyChat.matches(key, scan) && !this.searchBox.isFocused()) {
                this.ignoreTextInput = true;
                this.searchBox.setFocus(true);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean keyReleased(@Key int key, int scan, @Modifiers int mod) {
        this.ignoreTextInput = false;
        return GuiEventListener.super.keyReleased(key, scan, mod);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        assert this.minecraft.player != null;
        assert this.minecraft.gameMode != null;
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.recipeBookPage.mouseClicked(mouseX, mouseY, button, this.cornerX, this.cornerY, this.texWidth, this.texHeight)) {
                Recipe<?> recipe = this.recipeBookPage.getLastClickedRecipe();
                RecipeCollection recipecollection = this.recipeBookPage.getLastClickedRecipeCollection();
                if (recipe != null && recipecollection != null) {
                    if (!recipecollection.isCraftable(recipe) && this.ghostRecipe.getRecipe() == recipe) {
                        return false;
                    }
                    this.ghostRecipe.clear();
                    this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, recipe, Screen.hasShiftDown());
                }
                return true;
            }
            assert this.searchBox != null;
            if (this.searchBox.mouseClicked(mouseX, mouseY, button)) {
                this.checkSearchStringUpdate();
                return true;
            }
            if (this.filterButton.mouseClicked(mouseX, mouseY, button)) {
                boolean flag = this.toggleFiltering();
                this.filterButton.setStateTriggered(flag);
                this.sendUpdateSettings();
                this.updateCollections(false);
                return true;
            }
            for (int i = 0, l = this.tabButtons.size(); i < l; i++) {
                ButtonTabRecipeBook tabButton = this.tabButtons.get(i);
                if (tabButton.mouseClicked(mouseX, mouseY, button)) {
                    if (this.selectedTab != tabButton) {
                        if (this.selectedTab != null) {
                            this.selectedTab.setStateTriggered(false);
                        }
                        this.selectedTab = tabButton;
                        this.selectedTab.setStateTriggered(true);
                        this.selectedTab.playDownSound(Minecraft.getInstance().getSoundManager());
                        this.updateCollections(true);
                    }
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.visible ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void recipesShown(List<Recipe<?>> recipes) {
        assert this.minecraft.player != null;
        for (int i = 0, l = recipes.size(); i < l; i++) {
            this.minecraft.player.removeRecipeHighlight(recipes.get(i));
        }
    }

    public void recipesUpdated() {
        this.updateTabs();
        if (this.isVisible()) {
            this.updateCollections(false);
        }

    }

    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (this.isVisible()) {
            matrices.pushPose();
            RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
            RenderSystem.setShaderTexture(0, this.resBackground);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(matrices, this.cornerX, this.cornerY, 1, 1, this.texWidth, this.texHeight);
            assert this.searchBox != null;
            if (!this.searchBox.isFocused() && this.searchBox.getValue().isEmpty()) {
                drawString(matrices, this.minecraft.font, this.textSearch, this.cornerX + 25, this.cornerY + 14, -1);
            }
            else {
                this.searchBox.render(matrices, mouseX, mouseY, partialTicks);
            }
            for (int i = 0, l = this.tabButtons.size(); i < l; i++) {
                this.tabButtons.get(i).render(matrices, mouseX, mouseY, partialTicks);
            }
            this.filterButton.render(matrices, mouseX, mouseY, partialTicks);
            this.recipeBookPage.render(matrices, this.cornerX, this.cornerY, mouseX, mouseY, partialTicks);
            matrices.popPose();
        }
    }

    public void renderGhostRecipe(PoseStack matrices, int leftPos, int topPos, boolean p_100326_, float partialTicks) {
        this.ghostRecipe.render(matrices, this.minecraft, leftPos, topPos, p_100326_, partialTicks);
    }

    private void renderGhostRecipeTooltip(PoseStack matrices, int renderX, int renderY, int mouseX, int mouseY) {
        ItemStack stack = null;
        for (int i = 0; i < this.ghostRecipe.size(); i++) {
            GhostRecipe.GhostIngredient ingredient = this.ghostRecipe.get(i);
            int x = ingredient.getX() + renderX;
            int y = ingredient.getY() + renderY;
            if (mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 16) {
                stack = ingredient.getItem();
            }
        }
        if (stack != null && this.minecraft.screen != null) {
            this.minecraft.screen.renderComponentTooltip(matrices, this.minecraft.screen.getTooltipFromItem(stack), mouseX, mouseY, stack);
        }
    }

    public void renderTooltip(PoseStack matrices, int renderX, int renderY, int mouseX, int mouseY) {
        if (this.isVisible()) {
            this.recipeBookPage.renderTooltip(matrices, mouseX, mouseY);
            if (this.filterButton.isHoveredOrFocused()) {
                Component component = this.getFilterButtonTooltip();
                if (this.minecraft.screen != null) {
                    this.minecraft.screen.renderTooltip(matrices, component, mouseX, mouseY);
                }
            }
            this.renderGhostRecipeTooltip(matrices, renderX, renderY, mouseX, mouseY);
        }
    }

    protected void sendUpdateSettings() {
        if (this.minecraft.getConnection() != null) {
            RecipeBookType bookType = this.menu.getRecipeBookType();
            boolean isOpen = this.book.getBookSettings().isOpen(bookType);
            boolean isFiltering = this.book.getBookSettings().isFiltering(bookType);
            this.minecraft.getConnection().send(new ServerboundRecipeBookChangeSettingsPacket(bookType, isOpen, isFiltering));
        }
    }

    protected void setVisible(boolean visible) {
        if (visible) {
            this.initVisuals();
        }
        this.visible = visible;
        this.book.setOpen(this.menu.getRecipeBookType(), visible);
        if (!visible) {
            this.recipeBookPage.setInvisible();
        }
        this.sendUpdateSettings();
    }

    @Override
    public void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots) {
        ItemStack result = recipe.getResultItem();
        this.ghostRecipe.setRecipe(recipe);
        this.ghostRecipe.addIngredient(Ingredient.of(result), slots.get(0).x, slots.get(0).y);
        this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe,
                         recipe.getIngredients().iterator(), 0);
    }

    public void slotClicked(@Nullable Slot slot) {
        if (slot != null && slot.index < this.menu.getSize()) {
            this.ghostRecipe.clear();
            if (this.isVisible()) {
                this.updateStackedContents();
            }
        }
    }

    public void tick() {
        if (((IMinecraftPatch) Minecraft.getInstance()).isMultiplayerPaused()) {
            return;
        }
        boolean shouldBeVisible = this.isVisibleAccordingToBookData();
        if (this.isVisible() != shouldBeVisible) {
            this.setVisible(shouldBeVisible);
        }
        if (this.isVisible()) {
            assert this.minecraft.player != null;
            if (this.timesInventoryChanged != this.minecraft.player.getInventory().getTimesChanged()) {
                this.updateStackedContents();
                this.timesInventoryChanged = this.minecraft.player.getInventory().getTimesChanged();
            }
            assert this.searchBox != null;
            this.searchBox.tick();
        }
    }

    private boolean toggleFiltering() {
        RecipeBookType recipebooktype = this.menu.getRecipeBookType();
        boolean flag = !this.book.isFiltering(recipebooktype);
        this.book.setFiltering(recipebooktype, flag);
        return flag;
    }

    public void toggleVisibility() {
        this.setVisible(!this.visible);
    }

    private void updateCollections(boolean p_100383_) {
        assert this.selectedTab != null;
        List<RecipeCollection> collection = this.book.getCollection(this.selectedTab.getCategory());
        for (RecipeCollection recipeCollection : collection) {
            recipeCollection.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book);
        }
        List<RecipeCollection> newList = new RArrayList<>(collection);
        for (int i = 0; i < newList.size(); i++) {
            RecipeCollection c = newList.get(i);
            if (!c.hasKnownRecipes()) {
                newList.remove(i--);
                continue;
            }
            if (!c.hasFitting()) {
                newList.remove(i--);
            }
        }
        assert this.searchBox != null;
        String s = this.searchBox.getValue();
        if (!s.isEmpty()) {
            Set<RecipeCollection> set = new ReferenceLinkedOpenHashSet<>(
                    this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS).search(s.toLowerCase(Locale.ROOT)));
            for (int i = 0; i < newList.size(); i++) {
                if (!set.contains(newList.get(i))) {
                    newList.remove(i--);
                }
            }
        }
        if (this.book.isFiltering(this.menu)) {
            for (int i = 0; i < newList.size(); i++) {
                if (!newList.get(i).hasCraftable()) {
                    newList.remove(i--);
                }
            }
        }
        this.recipeBookPage.updateCollections(newList, p_100383_);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        List<NarratableEntry> list = new RArrayList<>();
        this.recipeBookPage.listButtons(b -> {
            if (b.isActive()) {
                list.add(b);
            }
        });
        list.add(this.searchBox);
        list.add(this.filterButton);
        list.addAll(this.tabButtons);
        Screen.NarratableSearchResult result = Screen.findNarratableWidget(list, null);
        if (result != null) {
            result.entry.updateNarration(pNarrationElementOutput.nest());
        }
    }

    public int updateScreenPosition(int width, int imageWidth) {
        if (this.visible && !this.widthTooNarrow) {
            return 177 + (width - imageWidth - 200) / 2;
        }
        return (width - imageWidth) / 2;
    }

    private void updateStackedContents() {
        this.stackedContents.clear();
        assert this.minecraft.player != null;
        this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        this.updateCollections(false);
    }

    private void updateTabs() {
        int x = this.cornerX + this.getTabsDx();
        int y = this.cornerY + 3;
        int dy = 0;
        for (int i = 0, l = this.tabButtons.size(); i < l; i++) {
            ButtonTabRecipeBook tabButton = this.tabButtons.get(i);
            RecipeBookCategories category = tabButton.getCategory();
            if (category != RecipeBookCategories.CRAFTING_SEARCH && category != RecipeBookCategories.FURNACE_SEARCH) {
                if (tabButton.updateVisibility(this.book)) {
                    tabButton.setPosition(x, y + 27 * dy++);
                    tabButton.startAnimation(this.minecraft);
                }
            }
            else {
                tabButton.visible = true;
                tabButton.setPosition(x, y + 27 * dy++);
            }
        }
    }
}
