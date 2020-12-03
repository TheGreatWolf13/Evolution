package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import tgw.evolution.Evolution;
import tgw.evolution.inventory.extendedinventory.ContainerPlayerInventory;

public class ScreenInventoryExtended extends DisplayEffectsScreen<ContainerPlayerInventory> implements IRecipeShownListener {

    private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");
    private static final ResourceLocation INVENTORY_TEXTURE = Evolution.location("textures/gui/inventory.png");
    private final RecipeBookGui recipeBookGui = new RecipeBookGui();
    private boolean buttonClicked;
    private float oldMouseX;
    private float oldMouseY;
    private boolean removeRecipeBookGui;
    private boolean widthTooNarrow;

    public ScreenInventoryExtended(ContainerPlayerInventory container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        this.passEvents = true;
        this.xSize = 212;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(INVENTORY_TEXTURE);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.blit(i, j, 0, 0, this.xSize, this.ySize);
        if (!this.playerInventory.armorInventory.get(3).isEmpty()) {
            this.blit(i + 43, j + 7, 212, 0, 18, 18);
        }
        if (!this.playerInventory.armorInventory.get(2).isEmpty()) {
            this.blit(i + 43, j + 25, 212, 0, 18, 18);
        }
        if (!this.playerInventory.armorInventory.get(1).isEmpty()) {
            this.blit(i + 43, j + 43, 212, 0, 18, 18);
        }
        if (!this.playerInventory.armorInventory.get(0).isEmpty()) {
            this.blit(i + 43, j + 61, 212, 0, 18, 18);
        }
        InventoryScreen.drawEntityOnScreen(i + 88, j + 75, 30, i + 88 - this.oldMouseX, j + 75 - 50 - this.oldMouseY, this.minecraft.player);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getFormattedText(), 97.0F, 8.0F, 4_210_752);
    }

    @Override
    public RecipeBookGui getRecipeGui() {
        return this.recipeBookGui;
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        super.handleMouseClick(slotIn, slotId, mouseButton, type);
        this.recipeBookGui.slotClicked(slotIn);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
        boolean flag = mouseX < guiLeftIn || mouseY < guiTopIn || mouseX >= guiLeftIn + this.xSize || mouseY >= guiTopIn + this.ySize;
        return this.recipeBookGui.func_195604_a(mouseX, mouseY, this.guiLeft, this.guiTop, this.xSize, this.ySize, mouseButton) && flag;
    }

    @Override
    protected void init() {
        if (this.minecraft.playerController.isInCreativeMode()) {
            this.minecraft.displayGuiScreen(new CreativeScreen(this.minecraft.player));
        }
        else {
            super.init();
            this.widthTooNarrow = this.width < 395;
            this.recipeBookGui.init(this.width - 34, this.height, this.minecraft, this.widthTooNarrow, this.container);
            this.removeRecipeBookGui = true;
            this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
            this.children.add(this.recipeBookGui);
            this.setFocusedDefault(this.recipeBookGui);
            this.addButton(new ImageButton(this.guiLeft + 137, this.height / 2 - 31, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, p_214086_1_ -> {
                this.recipeBookGui.initSearchBar(this.widthTooNarrow);
                this.recipeBookGui.toggleVisibility();
                this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
                ((ImageButton) p_214086_1_).setPosition(this.guiLeft + 137, this.height / 2 - 31);
                this.buttonClicked = true;
            }));
        }
    }

    @Override
    protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isPointInRegion(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        if (this.recipeBookGui.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
            return true;
        }
        return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) &&
               super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
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
    public void recipesUpdated() {
        this.recipeBookGui.recipesUpdated();
    }

    @Override
    public void removed() {
        if (this.removeRecipeBookGui) {
            this.recipeBookGui.removed();
        }
        super.removed();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        this.hasActivePotionEffects = !this.recipeBookGui.isVisible();
        if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
            this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        }
        else {
            super.render(mouseX, mouseY, partialTicks);
            this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, false, partialTicks);
        }
        this.recipeBookGui.render(mouseX, mouseY, partialTicks);
        this.recipeBookGui.renderTooltip(this.guiLeft + 18, this.guiTop, mouseX, mouseY);
        this.func_212932_b(this.recipeBookGui);
        this.renderHoveredToolTip(mouseX, mouseY);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
    }

    @Override
    public void tick() {
        if (this.minecraft.playerController.isInCreativeMode()) {
            this.minecraft.displayGuiScreen(new CreativeScreen(this.minecraft.player));
        }
        else {
            this.recipeBookGui.tick();
        }
    }
}
