package tgw.evolution.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.Evolution;
import tgw.evolution.EvolutionClient;
import tgw.evolution.blocks.tileentities.KnappingRecipe;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketCSSetKnappingType;
import tgw.evolution.util.constants.RockVariant;

public class ScreenKnapping extends Screen {

    private final long pos;
    private final ResourceLocation resBackground = Evolution.getResource("textures/gui/knapping.png");
    private final ItemStack[] stacks = new ItemStack[KnappingRecipe.VALUES.length - 1];
    private Button button;
    private int cornerX;
    private int cornerY;
    private float scrollOffs;
    private boolean scrolling;
    private int selectedRecipeIndex = -1;
    private int startIndex;

    public ScreenKnapping(long pos, RockVariant variant) {
        super(new TranslatableComponent("evolution.gui.knapping"));
        this.pos = pos;
        for (int i = 0; i < this.stacks.length; i++) {
            //noinspection ObjectAllocationInLoop
            this.stacks[i] = variant.getKnappedStack(KnappingRecipe.VALUES[i + 1]);
        }
    }

    public static void open(long pos, RockVariant variant) {
        Minecraft.getInstance().setScreen(new ScreenKnapping(pos, variant));
    }

//    private void drawItemStack(ItemStack stack, int x, int y, @Nullable String altText) {
//        PoseStack internalMat = RenderSystem.getModelViewStack();
//        internalMat.translate(0, 0, 32);
//        this.setBlitOffset(200);
//        this.itemRenderer.blitOffset = 200.0F;
//        this.itemRenderer.renderGuiItem(stack, x, y);
//        this.itemRenderer.renderGuiItemDecorations(this.font, stack, x, y, altText);
//        this.setBlitOffset(0);
//        this.itemRenderer.blitOffset = 0.0F;
//        RenderSystem.applyModelViewMatrix();
//    }

    protected int getOffscreenRows() {
        return (this.stacks.length + 6 - 1) / 6 - 3;
    }

    @Override
    protected void init() {
        this.cornerX = (this.width - 176) / 2;
        this.cornerY = (this.height - 84) / 2;
        this.makeButton();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isScrollBarActive() {
        return this.stacks.length > 18;
    }

    private void makeButton() {
        this.button = new Button(this.cornerX + 143, this.cornerY + 32, 20, 20, EvolutionTexts.EMPTY, b -> {
            if (this.selectedRecipeIndex != -1) {
                this.setTile(KnappingRecipe.VALUES[this.selectedRecipeIndex + 1]);
            }
        }, (b, m, mx, my) -> {
            if (this.selectedRecipeIndex != -1) {
                this.renderTooltip(m, this.stacks[this.selectedRecipeIndex], mx, my);
            }
        });
        this.button.active = this.selectedRecipeIndex != -1;
        this.addRenderableWidget(this.button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        this.scrolling = false;
        int dx = this.cornerX + 9;
        int dy = this.cornerY + 15;
        int maxIndex = Math.min(this.startIndex + 18, this.stacks.length);
        for (int i = this.startIndex; i < maxIndex; i++) {
            int index = i - this.startIndex;
            double x = mouseX - (dx + index % 6 * 18);
            //noinspection IntegerDivisionInFloatingPointContext
            double y = mouseY - (dy + index / 6 * 18);
            if (i != this.selectedRecipeIndex && x >= 0 && y >= 0 && x < 18.0 && y < 18.0) {
                assert this.minecraft != null;
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.selectedRecipeIndex = i;
                this.removeWidget(this.button);
                this.makeButton();
                return true;
            }
        }
        dx = this.cornerX + 120;
        dy = this.cornerY + 15;
        if (mouseX >= dx && mouseX < dx + 12 && mouseY >= dy && mouseY < dy + 54) {
            this.scrolling = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, @MouseButton int button, double dragX, double dragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int minY = this.cornerY + 15;
            int maxY = minY + 54;
            this.scrollOffs = ((float) mouseY - minY - 7.5F) / ((maxY - minY) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows() + 0.5) * 6;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isScrollBarActive()) {
            int rows = this.getOffscreenRows();
            float f = (float) delta / rows;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * rows + 0.5) * 6;
        }
        return true;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, this.resBackground);
        this.blit(matrices, this.cornerX, this.cornerY, 0, 0, 176, 84);
        int k = (int) (39 * this.scrollOffs);
        this.blit(matrices, this.cornerX + 120, this.cornerY + 15 + k, 176, this.isScrollBarActive() ? 0 : 15, 12, 15);
        int x = this.cornerX + 9;
        int y = this.cornerY + 15;
        int index = this.startIndex + 18;
        this.renderButtons(matrices, mouseX, mouseY, x, y, index);
        this.renderRecipes(x, y, index);
        GUIUtils.drawCenteredStringNoShadow(matrices, this.font, this.title, this.width / 2.0f, this.cornerY + 5, 0x40_4040);
        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    private void renderButtons(PoseStack matrices, int mouseX, int mouseY, int x, int y, int lastVisibleElementIndex) {
        for (int i = this.startIndex; i < lastVisibleElementIndex && i < this.stacks.length; i++) {
            int index = i - this.startIndex;
            int dx = x + index % 6 * 18;
            int dy = y + index / 6 * 18;
            int texY = 0;
            if (i == this.selectedRecipeIndex) {
                texY += 18;
            }
            else if (mouseX >= dx && mouseY >= dy && mouseX < dx + 18 && mouseY < dy + 18) {
                texY += 36;
            }
            this.blit(matrices, dx, dy, 188, texY, 18, 18);
        }
    }

    private void renderRecipes(int x, int y, int recipeIndexOffsetMax) {
        assert this.minecraft != null;
        for (int i = this.startIndex; i < recipeIndexOffsetMax && i < this.stacks.length; i++) {
            int index = i - this.startIndex;
            int dx = x + index % 6 * 18 + 1;
            int dy = y + index / 6 * 18 + 1;
            this.minecraft.getItemRenderer().renderAndDecorateItem(this.stacks[i], dx, dy);
        }
        if (this.selectedRecipeIndex != -1) {
            this.minecraft.getItemRenderer().renderAndDecorateItem(this.stacks[this.selectedRecipeIndex], x + 136, y + 19);
        }
    }

    private void setTile(KnappingRecipe type) {
        EvolutionClient.sendToServer(new PacketCSSetKnappingType(this.pos, type));
        assert this.minecraft != null;
        this.minecraft.setScreen(null);
    }
}
