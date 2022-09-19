package tgw.evolution.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.recipebook.ComponentRecipeBook;
import tgw.evolution.client.gui.recipebook.ComponentRecipeBookSmall;
import tgw.evolution.client.gui.recipebook.IRecipeBook;
import tgw.evolution.client.gui.recipebook.IRecipeBookUpdateListener;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.extendedinventory.ContainerInventory;
import tgw.evolution.util.math.MathHelper;

@OnlyIn(Dist.CLIENT)
public class ScreenInventory extends ScreenDisplayEffects<ContainerInventory> implements IRecipeBookUpdateListener {

    private final ComponentRecipeBook recipeBook = new ComponentRecipeBookSmall();
    private final ResourceLocation recipeBookIcon = Evolution.getResource("textures/gui/recipe_button.png");
    private final Component textCrafting = new TranslatableComponent("evolution.gui.crafting");
    private final Component textEquipment = new TranslatableComponent("evolution.gui.inventory.equipment");
    private final Component textInventory = new TranslatableComponent("evolution.gui.inventory");
    private boolean buttonClicked;
    private float oldMouseX;
    private float oldMouseY;
    private boolean removeRecipeBook;
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

    @Override
    public IRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        if (MathHelper.isMouseInArea(mouseX, mouseY, guiLeft, guiTop, this.imageWidth, this.imageHeight)) {
            return false;
        }
        if (MathHelper.isMouseInArea(mouseX, mouseY, guiLeft + this.imageWidth, guiTop + 7, 80, 57)) {
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
            this.addRenderableWidget(new ImageButton(this.leftPos + 140, this.topPos + 78, 20, 18, 0, 0, 19, this.recipeBookIcon, button -> {
                this.recipeBook.toggleVisibility();
                this.leftPos = this.recipeBook.updateScreenPosition(this.width, this.imageWidth);
                ((ImageButton) button).setPosition(this.leftPos + 140, this.topPos + 78);
                this.buttonClicked = true;
            }));
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBook.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return (!this.widthTooNarrow || !this.recipeBook.isVisible()) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        }
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public void onClose() {
        if (this.removeRecipeBook) {
            this.recipeBook.removed();
        }
        super.onClose();
    }

    @Override
    public void recipesUpdated() {
        this.recipeBook.recipesUpdated();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        if (this.recipeBook.isVisible() && this.widthTooNarrow) {
            this.recipeBook.toggleVisibility();
            super.render(matrices, mouseX, mouseY, partialTicks);
        }
        else {
            this.recipeBook.render(matrices, mouseX, mouseY, partialTicks);
            super.render(matrices, mouseX, mouseY, partialTicks);
            this.recipeBook.renderGhostRecipe(matrices, this.leftPos, this.topPos, false, partialTicks);
        }
        this.drawActivePotionEffectsTooltips(matrices, mouseX, mouseY, this.leftPos);
        this.recipeBook.renderTooltip(matrices, this.leftPos, this.topPos, mouseX, mouseY);
        this.renderTooltip(matrices, mouseX, mouseY);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
    }

    @Override
    protected void renderBg(PoseStack matrices, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_INVENTORY);
        int x = this.leftPos;
        int y = this.topPos;
        this.blit(matrices, x, y, 0, 0, 256, this.imageHeight);
        assert this.minecraft != null;
        assert this.minecraft.player != null;
        InventoryScreen.renderEntityInInventory(x + 88, y + 80, 30, x + 88 - this.oldMouseX, y + 80 - 50 - this.oldMouseY, this.minecraft.player);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        //Coordinates here are relative to this.leftPos and this.topPos
        float middle = this.imageWidth / 2.0f;
        GUIUtils.drawCenteredStringNoShadow(matrices, this.font, this.textInventory, middle, 88, 0x40_4040);
        GUIUtils.drawCenteredStringNoShadow(matrices, this.font, this.textEquipment, middle, 5, 0x40_4040);
        GUIUtils.drawCenteredStringNoShadow(matrices, this.font, this.textCrafting, 177 + 72 / 2.0f, 12, 0x40_4040);
    }

    @Override
    public void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        this.recipeBook.slotClicked(slot);
    }
}
