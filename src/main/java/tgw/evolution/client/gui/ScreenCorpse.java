package tgw.evolution.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.inventory.corpse.ContainerCorpse;
import tgw.evolution.util.FullDate;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.Metric;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ScreenCorpse extends ContainerScreen<ContainerCorpse> {

    private static final ItemStack TAB_0_STACK = new ItemStack(Items.CHEST);
    private static final ItemStack TAB_1_STACK = new ItemStack(Items.SKELETON_SKULL);
    private final List<IReorderingProcessor> deathMessage;
    private final ITextComponent gameDateOfDeath;
    private final ITextComponent systemDateOfDeath;
    private int messageEnd;
    private int messageStart;
    private int selectedTab;
    private int tabX;
    private int tabY;

    public ScreenCorpse(ContainerCorpse container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        this.imageHeight = 222;
        FullDate gameDate = new FullDate(container.getCorpse().getGameDeathTime());
        Date systemDate = new Date(container.getCorpse().getSystemDeathTime());
        this.deathMessage = Minecraft.getInstance().font.split(container.getCorpse().getDeathMessage(), this.imageWidth - 12);
        this.gameDateOfDeath = new TranslationTextComponent("evolution.gui.corpse.gameDateOfDeath", gameDate.getDisplayName());
        this.systemDateOfDeath = new TranslationTextComponent("evolution.gui.corpse.systemDateOfDeath",
                                                              Metric.getDateFormatter(Minecraft.getInstance()
                                                                                               .getLanguageManager()
                                                                                               .getSelected()
                                                                                               .getJavaLocale()).format(systemDate));
    }

    protected void drawTabs(MatrixStack matrices) {
        this.minecraft.getTextureManager().bind(EvolutionResources.GUI_TABS);
        this.blit(matrices, this.tabX, this.tabY, 128, this.selectedTab == 0 ? 92 : 64, 32, 28);
        this.blit(matrices, this.tabX, this.tabY + 32, 128, this.selectedTab == 1 ? 92 : 64, 32, 28);
        this.minecraft.getItemRenderer().renderAndDecorateItem(null, TAB_0_STACK, this.tabX + 6, this.tabY + 5);
        this.minecraft.getItemRenderer().renderAndDecorateItem(null, TAB_1_STACK, this.tabX + 6, this.tabY + 32 + 5);
    }

    @Nullable
    private Style getClickedComponentStyleAt(int mouseX, int mouseY) {
        int deltaY = mouseY - this.messageStart;
        int times = 0;
        while (deltaY >= 10) {
            deltaY -= 10;
            times++;
        }
        times = MathHelper.clamp(times, 0, this.deathMessage.size() - 1);
        return this.font.getSplitter().componentStyleAtWidth(this.deathMessage.get(times), mouseX - this.leftPos - 6);
    }

    @Override
    public void init(Minecraft mc, int width, int height) {
        super.init(mc, width, height);
        this.tabX = (this.width - this.imageWidth) / 2 + this.imageWidth - 4;
        this.tabY = (this.height - this.imageHeight) / 2 + 8;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (MathHelper.isMouseInsideBox(mouseX, mouseY, this.tabX, this.tabY, this.tabX + 32, this.tabY + 28)) {
                this.setSelectedTab(0);
                return true;
            }
            if (MathHelper.isMouseInsideBox(mouseX, mouseY, this.tabX, this.tabY + 32, this.tabX + 32, this.tabY + 32 + 28)) {
                this.setSelectedTab(1);
                return true;
            }
        }
        if (this.selectedTab != 0) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        if (this.selectedTab == 0) {
            super.render(matrices, mouseX, mouseY, partialTicks);
        }
        else {
            this.renderDeathTab(matrices);
        }
        this.drawTabs(matrices);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(EvolutionResources.GUI_CORPSE);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrices, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderDeathTab(MatrixStack matrices) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(EvolutionResources.GUI_CORPSE_DEATH);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrices, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        GUIUtils.drawCenteredStringNoShadow(matrices,
                                            this.font,
                                            this.menu.getCorpse().getName(),
                                            this.leftPos + this.imageWidth / 2.0f,
                                            this.topPos + 6.0f,
                                            0x40_4040);
        float y = this.topPos + 20.0f;
        for (IReorderingProcessor processor : this.font.split(this.gameDateOfDeath, this.imageWidth - 12)) {
            this.font.draw(matrices, processor, this.leftPos + 6.0f, y, 0x40_4040);
            y += 10;
        }
        y += 5;
        for (IReorderingProcessor processor : this.font.split(this.systemDateOfDeath, this.imageWidth - 12)) {
            this.font.draw(matrices, processor, this.leftPos + 6.0f, y, 0x40_4040);
            y += 10;
        }
        y += 5;
        this.messageStart = (int) y;
        for (IReorderingProcessor processor : this.deathMessage) {
            this.font.draw(matrices, processor, this.leftPos + 6.0f, y, 0x40_4040);
            y += 10;
        }
        this.messageEnd = (int) y;
    }

    @Override
    protected void renderLabels(MatrixStack matrices, int mouseX, int mouseY) {
        GUIUtils.drawCenteredStringNoShadow(matrices, this.font, this.menu.getCorpse().getName(), this.imageWidth / 2.0f, 6.0f, 0x40_4040);
        GUIUtils.drawCenteredStringNoShadow(matrices,
                                            this.font,
                                            this.inventory.getDisplayName(),
                                            this.imageWidth / 2.0f,
                                            this.imageHeight - 94.0f,
                                            0x40_4040);
    }

    @Override
    protected void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if (MathHelper.isMouseInsideBox(mouseX, mouseY, this.tabX, this.tabY, this.tabX + 32, this.tabY + 28)) {
            this.renderTooltip(matrices, EvolutionTexts.GUI_CORPSE_TAB_INVENTORY, mouseX, mouseY);
            return;
        }
        if (MathHelper.isMouseInsideBox(mouseX, mouseY, this.tabX, this.tabY + 32, this.tabX + 32, this.tabY + 32 + 28)) {
            this.renderTooltip(matrices, EvolutionTexts.GUI_CORPSE_TAB_DEATH, mouseX, mouseY);
            return;
        }
        if (this.selectedTab == 1) {
            if (MathHelper.isMouseInsideBox(mouseX,
                                            mouseY,
                                            this.leftPos + 6,
                                            this.messageStart,
                                            this.leftPos + this.imageWidth - 6,
                                            this.messageEnd)) {
                Style style = this.getClickedComponentStyleAt(mouseX, mouseY);
                this.renderComponentHoverEffect(matrices, style, mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(matrices, mouseX, mouseY);
    }

    protected void setSelectedTab(int selectedTab) {
        if (this.selectedTab != selectedTab) {
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.selectedTab = selectedTab;
        }
    }
}
