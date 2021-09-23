package tgw.evolution.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.extendedinventory.ContainerPlayerInventory;

@OnlyIn(Dist.CLIENT)
public class ScreenInventoryExtended extends ScreenDisplayEffects<ContainerPlayerInventory> implements IRecipeShownListener {

    private final RecipeBookGui recipeBookGui = new RecipeBookGui();
    private boolean buttonClicked;
    private float oldMouseX;
    private float oldMouseY;
    private boolean removeRecipeBookGui;
    private boolean widthTooNarrow;

    public ScreenInventoryExtended(ContainerPlayerInventory container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        this.passEvents = true;
        this.imageWidth = 212;
    }

    @Override
    public RecipeBookGui getRecipeBookComponent() {
        return this.recipeBookGui;
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
        boolean flag = mouseX < guiLeftIn || mouseY < guiTopIn || mouseX >= guiLeftIn + this.imageWidth || mouseY >= guiTopIn + this.imageHeight;
        return this.recipeBookGui.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) &&
               flag;
    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasFarPickRange()) {
            this.minecraft.setScreen(new CreativeScreen(this.minecraft.player));
        }
        else {
            super.init();
            this.widthTooNarrow = this.width < 395;
            this.recipeBookGui.init(this.width - 34, this.height, this.minecraft, this.widthTooNarrow, this.menu);
            this.removeRecipeBookGui = true;
            this.leftPos = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
            this.children.add(this.recipeBookGui);
            this.setInitialFocus(this.recipeBookGui);
            this.addButton(new ImageButton(this.leftPos + 137,
                                           this.height / 2 - 31,
                                           20,
                                           18,
                                           0,
                                           0,
                                           19,
                                           EvolutionResources.GUI_RECIPE_BUTTON,
                                           button -> {
                                               this.recipeBookGui.initVisuals(this.widthTooNarrow);
                                               this.recipeBookGui.toggleVisibility();
                                               this.leftPos = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow,
                                                                                                      this.width,
                                                                                                      this.imageWidth);
                                               ((ImageButton) button).setPosition(this.leftPos + 137, this.height / 2 - 31);
                                               this.buttonClicked = true;
                                           }));
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
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
    public void onClose() {
        if (this.removeRecipeBookGui) {
            this.recipeBookGui.removed();
        }
        super.onClose();
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookGui.recipesUpdated();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
            this.recipeBookGui.toggleVisibility();
            super.render(matrices, mouseX, mouseY, partialTicks);
        }
        else {
            super.render(matrices, mouseX, mouseY, partialTicks);
            this.recipeBookGui.renderGhostRecipe(matrices, this.leftPos, this.topPos, false, partialTicks);
            this.recipeBookGui.render(matrices, mouseX, mouseY, partialTicks);
        }
        this.drawActivePotionEffectsTooltips(matrices,
                                             mouseX,
                                             mouseY,
                                             this.recipeBookGui.isVisible() ?
                                             (this.width - 181 - 60) / 2 - (this.widthTooNarrow ? 0 : 86) :
                                             this.leftPos);
        this.recipeBookGui.renderTooltip(matrices, this.leftPos, this.topPos, mouseX, mouseY);
        this.renderTooltip(matrices, mouseX, mouseY);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
    }

    @Override
    protected void renderBg(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(EvolutionResources.GUI_INVENTORY);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(matrices, i, j, 0, 0, this.imageWidth, this.imageHeight);
        if (!this.inventory.armor.get(EvolutionResources.HELMET).isEmpty()) {
            this.blit(matrices, i + 43, j + 7, 212, 0, 18, 18);
        }
        if (!this.inventory.armor.get(EvolutionResources.CHESTPLATE).isEmpty()) {
            this.blit(matrices, i + 43, j + 25, 212, 0, 18, 18);
        }
        if (!this.inventory.armor.get(EvolutionResources.LEGGINGS).isEmpty()) {
            this.blit(matrices, i + 43, j + 43, 212, 0, 18, 18);
        }
        if (!this.inventory.armor.get(EvolutionResources.BOOTS).isEmpty()) {
            this.blit(matrices, i + 43, j + 61, 212, 0, 18, 18);
        }
        if (!this.menu.handler.getStackInSlot(EvolutionResources.CLOAK).isEmpty()) {
            this.blit(matrices, i + 43, j + 25, 212, 0, 18, 18);
            this.blit(matrices, i + 25, j + 25, 212, 0, 18, 18);
        }
        InventoryScreen.renderEntityInInventory(i + 88, j + 75, 30, i + 88 - this.oldMouseX, j + 75 - 50 - this.oldMouseY, this.minecraft.player);
    }

    @Override
    protected void renderLabels(MatrixStack matrices, int mouseX, int mouseY) {
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        this.recipeBookGui.slotClicked(slot);
    }

    @Override
    public void tick() {
        if (this.minecraft.gameMode.hasFarPickRange()) {
            this.minecraft.setScreen(new CreativeScreen(this.minecraft.player));
        }
        else {
            this.recipeBookGui.tick();
            super.tick();
        }
    }
}
