package tgw.evolution.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.Evolution;
import tgw.evolution.EvolutionClient;
import tgw.evolution.client.gui.recipebook.ComponentRecipeBook;
import tgw.evolution.client.gui.recipebook.IRecipeBook;
import tgw.evolution.client.gui.recipebook.IRecipeBookUpdateListener;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.extendedinventory.ContainerInventory;
import tgw.evolution.util.math.MathHelper;

public class ScreenInventory extends ScreenDisplayEffects<ContainerInventory> implements IRecipeBookUpdateListener {

    private static final int NUM_TABS = 2;
    private boolean buttonClicked;
    private boolean justSwitchedTabs;
    private float oldMouseX;
    private float oldMouseY;
    private final ComponentRecipeBook recipeBook = new ComponentRecipeBook(this);
    private ImageButton recipeBookButton;
    private final ResourceLocation recipeBookIcon = Evolution.getResource("textures/gui/recipe_button.png");
    private boolean recipeBookVisible;
    private boolean removeRecipeBook;
    private final ResourceLocation resCrafting = Evolution.getResource("textures/gui/inventory_crafting.png");
    private int selectedTab = -1;
    private final ItemStack[] tabStacks = {
            new ItemStack(Items.CHEST),
            new ItemStack(Items.CRAFTING_TABLE)
    };
    private final Component[] tabTexts = {
            new TranslatableComponent("evolution.gui.inventory.tabInventory"),
            new TranslatableComponent("evolution.gui.inventory.tabCrafting")
    };
    private int tabX;
    private int tabY;
    private final Component textCrafting = new TranslatableComponent("evolution.gui.crafting");
    private final Component textEquipment = new TranslatableComponent("evolution.gui.inventory.equipment");
    private final Component textInventory = new TranslatableComponent("evolution.gui.inventory");
    private boolean widthTooNarrow;

    public ScreenInventory(ContainerInventory container, Inventory inv, Component name) {
        super(container, inv, name);
        this.passEvents = true;
        this.imageWidth = 176;
        this.imageHeight = 180;
    }

    @Override
    public void containerTick() {
        assert this.minecraft != null;
        assert this.minecraft.gameMode != null;
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            assert this.minecraft.player != null;
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
        }
        else {
            this.recipeBook.tick();
            super.containerTick();
        }
    }

    protected void drawTabs(PoseStack matrices) {
        assert this.minecraft != null;
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_TABS);
        for (int i = 0; i < NUM_TABS; ++i) {
            this.blit(matrices, this.tabX + 30 * i, this.tabY, 28, this.selectedTab == i ? 32 : 0, 28, 32);
        }
        for (int i = 0; i < NUM_TABS; ++i) {
            this.minecraft.getItemRenderer().renderAndDecorateItem(this.tabStacks[i], this.tabX + 6 + 30 * i, this.tabY + 7 + (this.selectedTab == i ? 0 : 2));
        }
    }

    @Override
    public IRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        if (MathHelper.isMouseInArea(mouseX, mouseY, guiLeft, guiTop, this.imageWidth, this.imageHeight)) {
            return false;
        }
        if (MathHelper.isMouseInArea(mouseX, mouseY, guiLeft + 6, guiTop - 28, NUM_TABS * 30 - 2, 28)) {
            return false;
        }
        if (this.justSwitchedTabs) {
            this.justSwitchedTabs = false;
            return false;
        }
        return this.recipeBook.hasClickedOutside(mouseX, mouseY);
    }

    @Override
    protected void init() {
        assert this.minecraft != null;
        assert this.minecraft.gameMode != null;
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            assert this.minecraft.player != null;
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
        }
        else {
            super.init();
            this.widthTooNarrow = this.width < 149 + 176; //Recipe book + inventory gui sizes
            this.recipeBook.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
            this.removeRecipeBook = true;
            this.leftPos = this.recipeBook.updateScreenPosition(this.width, this.imageWidth);
            this.addWidget(this.recipeBook);
            this.setInitialFocus(this.recipeBook);
            this.recipeBookButton = this.addRenderableWidget(new ImageButton(this.leftPos + 13, this.topPos + 41, 20, 18, 0, 0, 19, this.recipeBookIcon, button -> {
                this.recipeBook.toggleVisibility();
                this.recipeBookVisible = this.recipeBook.isVisible();
                this.leftPos = this.recipeBook.updateScreenPosition(this.width, this.imageWidth);
                this.tabX = this.leftPos + 6;
                ((ImageButton) button).setPosition(this.leftPos + 13, this.topPos + 41);
                this.buttonClicked = true;
            }));
            this.recipeBookButton.visible = this.selectedTab == 1;
            this.tabX = this.leftPos + 6;
            this.tabY = this.topPos - 28;
            this.recipeBookVisible = this.recipeBook.isVisible();
            this.setSelectedTab(EvolutionClient.getLastInventoryTab(), false);
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBook.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            for (int i = 0; i < NUM_TABS; ++i) {
                if (MathHelper.isMouseInArea(mouseX, mouseY, this.tabX + 32 * i, this.tabY, 28, 32)) {
                    this.setSelectedTab(i, true);
                    this.justSwitchedTabs = true;
                    return true;
                }
            }
        }
        if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return (!this.widthTooNarrow || !this.recipeBook.isVisible()) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, @MouseButton int button) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBook.recipesUpdated();
    }

    @Override
    public void removed() {
        if (!this.recipeBook.isVisible() && this.recipeBookVisible) {
            this.recipeBook.toggleVisibility();
        }
        if (this.removeRecipeBook) {
            this.recipeBook.removed();
        }
        super.removed();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        if (this.selectedTab == 0) {
            super.render(matrices, mouseX, mouseY, partialTicks);
            this.drawActivePotionEffectsTooltips(matrices, mouseX, mouseY, this.leftPos);
        }
        else {
            if (this.recipeBook.isVisible() && this.widthTooNarrow) {
                this.recipeBook.toggleVisibility();
                this.recipeBookVisible = this.recipeBook.isVisible();
                super.render(matrices, mouseX, mouseY, partialTicks);
            }
            else {
                this.recipeBook.render(matrices, mouseX, mouseY, partialTicks);
                super.render(matrices, mouseX, mouseY, partialTicks);
                this.recipeBook.renderGhostRecipe(matrices, this.leftPos, this.topPos, true, partialTicks);
            }
            this.recipeBook.renderTooltip(matrices, this.leftPos, this.topPos, mouseX, mouseY);
        }
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrices, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.selectedTab == 0) {
            RenderSystem.setShaderTexture(0, EvolutionResources.GUI_INVENTORY);
            int x = this.leftPos;
            int y = this.topPos;
            this.blit(matrices, x, y, 0, 0, 256, this.imageHeight);
            assert this.minecraft != null;
            assert this.minecraft.player != null;
            GUIUtils.renderEntityInInventory(x + 88, y + 80, 30, x + 88 - this.oldMouseX, y + 80 - 50 - this.oldMouseY, this.minecraft.player);
        }
        else {
            RenderSystem.setShaderTexture(0, this.resCrafting);
            this.blit(matrices, this.leftPos, this.topPos, 0, 0, 256, this.imageHeight);
        }
        this.drawTabs(matrices);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        //Coordinates here are relative to this.leftPos and this.topPos
        float middle = this.imageWidth / 2.0f;
        GUIUtils.drawCenteredStringNoShadow(matrices, this.font, this.textInventory, middle, 88, 0x40_4040);
        if (this.selectedTab == 0) {
            GUIUtils.drawCenteredStringNoShadow(matrices, this.font, this.textEquipment, middle, 5, 0x40_4040);
        }
        else {
            GUIUtils.drawCenteredStringNoShadow(matrices, this.font, this.textCrafting, middle, 5, 0x40_4040);
        }
    }

    @Override
    protected void renderTooltip(PoseStack matrices, int mouseX, int mouseY) {
        for (int i = 0; i < NUM_TABS; ++i) {
            if (MathHelper.isMouseInArea(mouseX, mouseY, this.tabX + 30 * i, this.tabY, 28, 32)) {
                this.renderTooltip(matrices, this.tabTexts[i], mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(matrices, mouseX, mouseY);
    }

    protected void setSelectedTab(int selectedTab, boolean playSound) {
        if (this.selectedTab != selectedTab) {
            assert this.minecraft != null;
            if (playSound) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            this.selectedTab = selectedTab;
            this.menu.setSelectedTab(selectedTab);
            this.recipeBookButton.visible = selectedTab == 1;
            if (this.recipeBook.isVisible()) {
                if (selectedTab != 1) {
                    this.recipeBook.toggleVisibility();
                    this.leftPos = this.recipeBook.updateScreenPosition(this.width, this.imageWidth);
                    this.tabX = this.leftPos + 6;
                }
            }
            else {
                if (selectedTab == 1 && this.recipeBookVisible) {
                    this.recipeBook.toggleVisibility();
                    this.leftPos = this.recipeBook.updateScreenPosition(this.width, this.imageWidth);
                    this.tabX = this.leftPos + 6;
                }
            }
            EvolutionClient.setLastInventoryTab(selectedTab);
        }
    }

    @Override
    protected boolean shouldDrawPotionEffects() {
        return this.selectedTab == 0;
    }

    @Override
    public void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        this.recipeBook.slotClicked(slot);
    }
}
