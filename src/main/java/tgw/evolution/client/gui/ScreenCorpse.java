package tgw.evolution.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.Evolution;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.corpse.ContainerCorpse;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Metric;
import tgw.evolution.util.time.FullDate;

import java.util.Date;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ScreenCorpse extends AbstractContainerScreen<ContainerCorpse> {

    private final List<FormattedCharSequence> deathMessage;
    private final Component gameDateOfDeath;
    private final ResourceLocation resDeath = Evolution.getResource("textures/gui/corpse_death.png");
    private final ResourceLocation resInv = Evolution.getResource("textures/gui/corpse.png");
    private final Component systemDateOfDeath;
    private final ItemStack tab0Stack = new ItemStack(Items.CHEST);
    private final ItemStack tab1Stack = new ItemStack(Items.SKELETON_SKULL);
    private final Component textTabDeath = new TranslatableComponent("evolution.gui.corpse.tabDeath");
    private final Component textTabInventory = new TranslatableComponent("evolution.gui.corpse.tabInventory");
    private int messageEnd;
    private int messageStart;
    private int selectedTab;
    private int tabX;
    private int tabY;

    public ScreenCorpse(ContainerCorpse container, Inventory inv, Component title) {
        super(container, inv, title);
        this.imageHeight = 222;
        FullDate gameDate = new FullDate(container.getCorpse().getGameDeathTime());
        Date systemDate = new Date(container.getCorpse().getSystemDeathTime());
        this.deathMessage = Minecraft.getInstance().font.split(container.getCorpse().getDeathMessage(), this.imageWidth - 12);
        this.gameDateOfDeath = new TranslatableComponent("evolution.gui.corpse.gameDateOfDeath", gameDate.getDisplayName());
        this.systemDateOfDeath = new TranslatableComponent("evolution.gui.corpse.systemDateOfDeath",
                                                           Metric.getDateFormatter(Minecraft.getInstance()
                                                                                            .getLanguageManager()
                                                                                            .getSelected()
                                                                                            .getJavaLocale()).format(systemDate));
    }

    protected void drawTabs(PoseStack matrices) {
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_TABS);
        this.blit(matrices, this.tabX, this.tabY, 128, this.selectedTab == 0 ? 92 : 64, 32, 28);
        this.blit(matrices, this.tabX, this.tabY + 32, 128, this.selectedTab == 1 ? 92 : 64, 32, 28);
        this.minecraft.getItemRenderer().renderAndDecorateItem(this.tab0Stack, this.tabX + 6, this.tabY + 5);
        this.minecraft.getItemRenderer().renderAndDecorateItem(this.tab1Stack, this.tabX + 6, this.tabY + 32 + 5);
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
    protected void init() {
        super.init();
        this.tabX = (this.width - this.imageWidth) / 2 + this.imageWidth - 4;
        this.tabY = (this.height - this.imageHeight) / 2 + 8;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
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
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
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
    protected void renderBg(PoseStack matrices, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.resInv);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrices, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderDeathTab(PoseStack matrices) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.resDeath);
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
        for (FormattedCharSequence processor : this.font.split(this.gameDateOfDeath, this.imageWidth - 12)) {
            this.font.draw(matrices, processor, this.leftPos + 6.0f, y, 0x40_4040);
            y += 10;
        }
        y += 5;
        for (FormattedCharSequence processor : this.font.split(this.systemDateOfDeath, this.imageWidth - 12)) {
            this.font.draw(matrices, processor, this.leftPos + 6.0f, y, 0x40_4040);
            y += 10;
        }
        y += 5;
        this.messageStart = (int) y;
        for (FormattedCharSequence processor : this.deathMessage) {
            this.font.draw(matrices, processor, this.leftPos + 6.0f, y, 0x40_4040);
            y += 10;
        }
        this.messageEnd = (int) y;
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        GUIUtils.drawCenteredStringNoShadow(matrices, this.font, this.menu.getCorpse().getName(), this.imageWidth / 2.0f, 6.0f, 0x40_4040);
        GUIUtils.drawCenteredStringNoShadow(matrices,
                                            this.font,
                                            this.playerInventoryTitle,
                                            this.imageWidth / 2.0f,
                                            this.imageHeight - 94.0f,
                                            0x40_4040);
    }

    @Override
    protected void renderTooltip(PoseStack matrices, int mouseX, int mouseY) {
        if (MathHelper.isMouseInsideBox(mouseX, mouseY, this.tabX, this.tabY, this.tabX + 32, this.tabY + 28)) {
            this.renderTooltip(matrices, this.textTabInventory, mouseX, mouseY);
            return;
        }
        if (MathHelper.isMouseInsideBox(mouseX, mouseY, this.tabX, this.tabY + 32, this.tabX + 32, this.tabY + 32 + 28)) {
            this.renderTooltip(matrices, this.textTabDeath, mouseX, mouseY);
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
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.selectedTab = selectedTab;
        }
    }
}
