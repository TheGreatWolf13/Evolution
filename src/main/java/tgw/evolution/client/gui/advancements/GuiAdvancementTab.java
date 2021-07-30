package tgw.evolution.client.gui.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class GuiAdvancementTab extends AbstractGui {

    public static boolean doFade = true;
    protected final Map<Advancement, GuiAdvancementEntry> guis = Maps.newLinkedHashMap();
    private final Advancement advancement;
    private final BetterDisplayInfoRegistry betterDisplayInfos;
    private final DisplayInfo display;
    private final ItemStack icon;
    private final int index;
    private final Minecraft minecraft;
    private final GuiAdvancementEntry root;
    private final ScreenAdvancements screen;
    private final String title;
    private final AdvancementTabType type;
    protected int scrollX;
    protected int scrollY;
    private boolean centered;
    private float fade;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;

    public GuiAdvancementTab(Minecraft mc,
                             ScreenAdvancements screenAdvancements,
                             AdvancementTabType type,
                             int index,
                             Advancement advancement,
                             DisplayInfo displayInfo) {
        this.minecraft = mc;
        this.screen = screenAdvancements;
        this.type = type;
        this.index = index;
        this.advancement = advancement;
        this.display = displayInfo;
        this.icon = displayInfo.getIcon();
        this.title = displayInfo.getTitle().getFormattedText();
        this.betterDisplayInfos = new BetterDisplayInfoRegistry();
        this.root = new GuiAdvancementEntry(this, mc, advancement, displayInfo);
        this.addGuiAdvancement(this.root, advancement);
    }

    @Nullable
    public static GuiAdvancementTab create(Minecraft mc,
                                           ScreenAdvancements screenAdvancements,
                                           int index,
                                           Advancement advancement,
                                           int width,
                                           int height) {
        if (advancement.getDisplay() == null) {
            return null;
        }
        AdvancementTabType tabType = AdvancementTabType.getTabType(width, height, index);
        if (tabType == null) {
            return null;
        }
        return new GuiAdvancementTab(mc, screenAdvancements, tabType, index, advancement, advancement.getDisplay());
    }

    public void addAdvancement(Advancement advancement) {
        if (advancement.getDisplay() != null) {
            GuiAdvancementEntry advancementEntry = new GuiAdvancementEntry(this, this.minecraft, advancement, advancement.getDisplay());
            this.addGuiAdvancement(advancementEntry, advancement);
        }
    }

    private void addGuiAdvancement(GuiAdvancementEntry advancementEntry, Advancement advancement) {
        this.guis.put(advancement, advancementEntry);
        int left = advancementEntry.getX();
        int top = advancementEntry.getY();
        this.minX = Math.min(this.minX, left);
        int right = left + 28;
        this.maxX = Math.max(this.maxX, right);
        this.minY = Math.min(this.minY, top);
        int bottom = top + 27;
        this.maxY = Math.max(this.maxY, bottom);
        for (GuiAdvancementEntry gui : this.guis.values()) {
            gui.attachToParent();
        }
    }

    public void drawContents(int width, int height) {
        if (!this.centered) {
            this.scrollX = (width - (this.maxX + this.minX)) / 2;
            this.scrollY = (height - (this.maxY + this.minY)) / 2;
            this.centered = true;
        }
        GlStateManager.depthFunc(518);
        fill(0, 0, width, height, -16_777_216);
        GlStateManager.depthFunc(515);
        ResourceLocation resourcelocation = this.display.getBackground();
        if (resourcelocation != null) {
            this.minecraft.getTextureManager().bindTexture(resourcelocation);
        }
        else {
            this.minecraft.getTextureManager().bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
        }
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.scrollX % 16;
        int j = this.scrollY % 16;
        for (int k = -1; k <= 1 + width / 16; k++) {
            int l = -1;
            for (; l <= height / 16; l++) {
                blit(i + 16 * k, j + 16 * l, 0.0F, 0.0F, 16, 16, 16, 16);
            }
            blit(i + 16 * k, j + 16 * l, 0.0F, 0.0F, 16, height % 16, 16, 16);
        }
        this.root.drawConnectivity(this.scrollX, this.scrollY, true);
        this.root.drawConnectivity(this.scrollX, this.scrollY, false);
        this.root.draw(this.scrollX, this.scrollY);
    }

    public void drawIcon(int left, int top, int width, int height, ItemRenderer renderItem) {
        this.type.drawIcon(left, top, width, height, this.index, renderItem, this.icon);
    }

    public void drawTab(int left, int top, int width, int height, boolean selected) {
        this.type.draw(this, left, top, width, height, selected, this.index);
    }

    public void drawToolTips(int mouseX, int mouseY, int left, int top, int width, int height) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(0.0F, 0.0F, 200.0F);
        fill(0, 0, width, height, MathHelper.floor(this.fade * 255.0F) << 24);
        boolean flag = false;
        if (mouseX > 0 && mouseX < width && mouseY > 0 && mouseY < height) {
            for (GuiAdvancementEntry betterAdvancementEntryGui : this.guis.values()) {
                if (betterAdvancementEntryGui.isMouseOver(this.scrollX, this.scrollY, mouseX, mouseY)) {
                    flag = true;
                    betterAdvancementEntryGui.drawHover(this.scrollX, this.scrollY, left, top);
                    break;
                }
            }
        }
        GlStateManager.popMatrix();
        if (doFade && flag) {
            this.fade = MathHelper.clamp(this.fade + 0.02F, 0.0F, 0.3F);
        }
        else {
            this.fade = MathHelper.clamp(this.fade - 0.04F, 0.0F, 1.0F);
        }
    }

    public Advancement getAdvancement() {
        return this.advancement;
    }

    @Nullable
    public GuiAdvancementEntry getAdvancementGui(Advancement advancement) {
        return this.guis.get(advancement);
    }

    public BetterDisplayInfo getBetterDisplayInfo(Advancement advancement) {
        return this.betterDisplayInfos.get(advancement);
    }

    public ScreenAdvancements getScreen() {
        return this.screen;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isMouseOver(int left, int top, int width, int height, double mouseX, double mouseY) {
        return this.type.isMouseOver(left, top, width, height, this.index, mouseX, mouseY);
    }

    public void scroll(double scrollX, double scrollY, int width, int height) {
        if (this.maxX - this.minX > width) {
            this.scrollX = (int) Math.round(MathHelper.clamp(this.scrollX + scrollX, -(this.maxX - width), -this.minX));
        }
        if (this.maxY - this.minY > height) {
            this.scrollY = (int) Math.round(MathHelper.clamp(this.scrollY + scrollY, -(this.maxY - height), -this.minY));
        }
    }
}
