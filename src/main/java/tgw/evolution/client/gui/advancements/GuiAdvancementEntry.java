package tgw.evolution.client.gui.advancements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiAdvancementEntry extends GuiComponent {

    protected static final int ADVANCEMENT_SIZE = 26;
    private static final int CORNER_SIZE = 10;
    private static final int WIDGET_WIDTH = 256;
    private static final int WIDGET_HEIGHT = 26;
    private static final int TITLE_SIZE = 32;
    private static final int ICON_OFFSET = 128;
    private static final int ICON_SIZE = 26;
    private final Advancement advancement;
    private final GuiAdvancementTab betterAdvancementTabGui;
    private final OList<GuiAdvancementEntry> children = new OArrayList<>();
    private final DisplayInfo displayInfo;
    private final Minecraft mc;
    private final ResourceLocation resWidgets = Evolution.getResource("textures/gui/widgets.png");
    private final String title;
    protected int x;
    protected int y;
    private AdvancementProgress advancementProgress;
    private CriterionGrid criterionGrid;
    private List<FormattedCharSequence> description;
    private @Nullable GuiAdvancementEntry parent;
    private int width;

    public GuiAdvancementEntry(GuiAdvancementTab advancementTab, Minecraft mc, Advancement advancement, DisplayInfo displayInfo) {
        this.betterAdvancementTabGui = advancementTab;
        this.advancement = advancement;
        this.displayInfo = displayInfo;
        this.mc = mc;
        this.title = displayInfo.getTitle().getString(163);
        this.x = Mth.floor(displayInfo.getX() * 32.0F);
        this.y = Mth.floor(displayInfo.getY() * 27.0F);
        this.refreshHover();
    }

    public void addGuiAdvancement(GuiAdvancementEntry betterAdvancementEntryGui) {
        this.children.add(betterAdvancementEntryGui);
    }

    public void attachToParent() {
        if (this.parent == null && this.advancement.getParent() != null) {
            this.parent = this.getFirstVisibleParent(this.advancement);
            if (this.parent != null) {
                this.parent.addGuiAdvancement(this);
            }
        }
    }

    public void draw(PoseStack matrices, int scrollX, int scrollY) {
        if (!this.displayInfo.isHidden() || this.advancementProgress != null && this.advancementProgress.isDone()) {
            float percent = this.advancementProgress == null ? 0.0F : this.advancementProgress.getPercent();
            AdvancementWidgetType advancementState = percent >= 1.0f ? AdvancementWidgetType.OBTAINED : AdvancementWidgetType.UNOBTAINED;
            RenderSystem.setShaderTexture(0, this.resWidgets);
            GUIUtils.setColor(0xff_ffff);
            RenderSystem.enableBlend();
            this.setBlitOffset(1);
            this.blit(matrices,
                      scrollX + this.x + 3,
                      scrollY + this.y,
                      this.displayInfo.getFrame().getTexture(),
                      ICON_OFFSET + ICON_SIZE * (advancementState == AdvancementWidgetType.OBTAINED ? 0 : 1),
                      ICON_SIZE,
                      ICON_SIZE);
            this.setBlitOffset(0);
            this.mc.getItemRenderer().renderAndDecorateFakeItem(this.displayInfo.getIcon(), scrollX + this.x + 8, scrollY + this.y + 5);
        }
        else if (this.displayInfo.isHidden()) {
            RenderSystem.setShaderTexture(0, this.resWidgets);
            RenderSystem.enableBlend();
            this.setBlitOffset(1);
            this.blit(matrices,
                      scrollX + this.x + 3,
                      scrollY + this.y,
                      this.displayInfo.getFrame().getTexture(),
                      ICON_OFFSET + ICON_SIZE * 3,
                      ICON_SIZE,
                      ICON_SIZE);
            this.setBlitOffset(0);
            GUIUtils.renderAndDecorateFakeItemLighting(this.mc.getItemRenderer(),
                                                       this.displayInfo.getIcon(),
                                                       scrollX + this.x + 8,
                                                       scrollY + this.y + 5,
                                                       0);
        }
        for (int i = 0, l = this.children.size(); i < l; i++) {
            this.children.get(i).draw(matrices, scrollX, scrollY);
        }
    }

    public void drawConnection(PoseStack matrices, GuiAdvancementEntry parent, int scrollX, int scrollY, boolean drawInside) {
        boolean isCompleted = this.advancementProgress != null && this.advancementProgress.isDone();
        int innerLineColor = isCompleted ? 0xffff_ffff : 0xff33_3333;
        int startX = scrollX + parent.x + ADVANCEMENT_SIZE / 2;
        int endXHalf = scrollX + parent.x + ADVANCEMENT_SIZE + 6;
        int startY = scrollY + parent.y + ADVANCEMENT_SIZE / 2;
        int endX = scrollX + this.x + ADVANCEMENT_SIZE / 2;
        int endY = scrollY + this.y + ADVANCEMENT_SIZE / 2;
        if (drawInside) {
            int borderLineColor = 0xFF00_0000;
            this.hLine(matrices, endXHalf + 1, startX, startY - 1, borderLineColor);
            this.hLine(matrices, endXHalf + 1, startX, startY, borderLineColor);
            this.hLine(matrices, endXHalf, startX, startY + 1, borderLineColor);
            this.hLine(matrices, endX, endXHalf - 1, endY - 1, borderLineColor);
            this.hLine(matrices, endX, endXHalf - 1, endY, borderLineColor);
            this.hLine(matrices, endX, endXHalf - 1, endY + 1, borderLineColor);
            this.vLine(matrices, endXHalf - 1, endY, startY, borderLineColor);
            this.vLine(matrices, endXHalf + 1, endY, startY, borderLineColor);
        }
        else {
            GUIUtils.hLine(matrices, endXHalf, startX, startY, innerLineColor, isCompleted);
            GUIUtils.hLine(matrices, endX, endXHalf, endY, innerLineColor, isCompleted);
            GUIUtils.vLine(matrices, endXHalf, endY, startY, innerLineColor, isCompleted);
        }
    }

    public void drawConnectivity(PoseStack matrices, int scrollX, int scrollY, boolean drawInside) {
        if (this.parent != null) {
            this.drawConnection(matrices, this.parent, scrollX, scrollY, drawInside);
        }
        for (int i = 0, l = this.children.size(); i < l; i++) {
            this.children.get(i).drawConnectivity(matrices, scrollX, scrollY, drawInside);
        }
    }

    public void drawHover(PoseStack matrices, int scrollX, int scrollY, int left, int top) {
        this.refreshHover();
        boolean drawLeft = left + scrollX + this.x + this.width + ADVANCEMENT_SIZE >= this.betterAdvancementTabGui.getScreen().width;
        String s = this.advancementProgress == null ? null : this.advancementProgress.getProgressText();
        int i = s == null ? 0 : this.mc.font.width(s);
        boolean drawTop;
        if (this.criterionGrid.height < this.betterAdvancementTabGui.getScreen().height) {
            drawTop = top + scrollY + this.y + this.description.size() * (this.mc.font.lineHeight + 1) + this.criterionGrid.height + 50 >=
                      this.betterAdvancementTabGui.getScreen().height;
        }
        else {
            drawTop = false;
        }
        float percentageObtained = this.advancementProgress == null ? 0.0F : this.advancementProgress.getPercent();
        int j = Mth.floor(percentageObtained * this.width);
        AdvancementWidgetType stateTitleLeft;
        AdvancementWidgetType stateTitleRight;
        AdvancementWidgetType stateIcon;
        if (percentageObtained >= 1.0F) {
            j = this.width / 2;
            stateTitleLeft = AdvancementWidgetType.OBTAINED;
            stateTitleRight = AdvancementWidgetType.OBTAINED;
            stateIcon = AdvancementWidgetType.OBTAINED;
        }
        else if (j < 2) {
            j = this.width / 2;
            stateTitleLeft = AdvancementWidgetType.UNOBTAINED;
            stateTitleRight = AdvancementWidgetType.UNOBTAINED;
            stateIcon = AdvancementWidgetType.UNOBTAINED;
        }
        else if (j > this.width - 2) {
            j = this.width / 2;
            stateTitleLeft = AdvancementWidgetType.OBTAINED;
            stateTitleRight = AdvancementWidgetType.OBTAINED;
            stateIcon = AdvancementWidgetType.UNOBTAINED;
        }
        else {
            stateTitleLeft = AdvancementWidgetType.OBTAINED;
            stateTitleRight = AdvancementWidgetType.UNOBTAINED;
            stateIcon = AdvancementWidgetType.UNOBTAINED;
        }
        int k = this.width - j;
        RenderSystem.setShaderTexture(0, this.resWidgets);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        int drawY = scrollY + this.y;
        int drawX;
        if (drawLeft) {
            drawX = scrollX + this.x - this.width + ADVANCEMENT_SIZE + 6;
        }
        else {
            drawX = scrollX + this.x;
        }
        int boxHeight = TITLE_SIZE + this.description.size() * (this.mc.font.lineHeight + 1) + this.criterionGrid.height;
        if (!this.description.isEmpty()) {
            if (drawTop) {
                this.render9Sprite(matrices,
                                   drawX,
                                   drawY + ADVANCEMENT_SIZE - boxHeight,
                                   this.width,
                                   boxHeight,
                                   CORNER_SIZE,
                                   WIDGET_WIDTH,
                                   WIDGET_HEIGHT,
                                   0,
                                   52);
            }
            else {
                this.render9Sprite(matrices, drawX, drawY, this.width, boxHeight, CORNER_SIZE, WIDGET_WIDTH, WIDGET_HEIGHT, 0, 52);
            }
        }
        GUIUtils.setColor(0xff_ffff);
        int leftSide = Math.min(j, WIDGET_WIDTH - 16);
        this.blit(matrices, drawX, drawY, 0, (stateTitleLeft == AdvancementWidgetType.OBTAINED ? 0 : 1) * WIDGET_HEIGHT, leftSide, WIDGET_HEIGHT);
        if (leftSide < j) {
            this.blit(matrices,
                      drawX + leftSide,
                      drawY,
                      16,
                      (stateTitleLeft == AdvancementWidgetType.OBTAINED ? 0 : 1) * WIDGET_HEIGHT,
                      j - leftSide,
                      WIDGET_HEIGHT);
        }
        GUIUtils.setColor(0xff_ffff);
        int rightSide = Math.min(k, WIDGET_WIDTH - 4);
        this.blit(matrices,
                  drawX + j,
                  drawY,
                  WIDGET_WIDTH - rightSide,
                  (stateTitleRight == AdvancementWidgetType.OBTAINED ? 0 : 1) * WIDGET_HEIGHT,
                  rightSide,
                  WIDGET_HEIGHT);
        if (rightSide < k) {
            this.blit(matrices,
                      drawX + j + rightSide - 2,
                      drawY,
                      WIDGET_WIDTH - k + rightSide - 2,
                      (stateTitleRight == AdvancementWidgetType.OBTAINED ? 0 : 1) * WIDGET_HEIGHT,
                      k - rightSide + 2,
                      WIDGET_HEIGHT);
        }
        GUIUtils.setColor(0xff_ffff);
        this.blit(matrices,
                  scrollX + this.x + 3,
                  scrollY + this.y,
                  this.displayInfo.getFrame().getTexture(),
                  ICON_OFFSET + ICON_SIZE * (stateIcon == AdvancementWidgetType.OBTAINED ? 0 : 1),
                  ICON_SIZE,
                  ICON_SIZE);
        if (drawLeft) {
            this.mc.font.drawShadow(matrices, this.title, drawX + 5, scrollY + this.y + 9, -1);
            if (s != null) {
                this.mc.font.drawShadow(matrices, s, scrollX + this.x - i, scrollY + this.y + 9, -1);
            }
        }
        else {
            this.mc.font.drawShadow(matrices, this.title, scrollX + this.x + 32, scrollY + this.y + 9, -1);
            if (s != null) {
                this.mc.font.drawShadow(matrices, s, scrollX + this.x + this.width - i - 5, scrollY + this.y + 9, -1);
            }
        }
        int yOffset;
        if (drawTop) {
            yOffset = drawY + 26 - boxHeight + 7;
        }
        else {
            yOffset = scrollY + this.y + 10 + 17;
        }
        for (int k1 = 0; k1 < this.description.size(); ++k1) {
            this.mc.font.draw(matrices, this.description.get(k1), drawX + 5, yOffset + k1 * (this.mc.font.lineHeight + 1), 0xffaa_aaaa);
        }
        if (this.criterionGrid != null) {
            yOffset += this.description.size() * (this.mc.font.lineHeight + 1);
            int xOffset = drawX + 5;
            for (int colIndex = 0; colIndex < this.criterionGrid.columns.size(); colIndex++) {
                CriterionGrid.Column col = this.criterionGrid.columns.get(colIndex);
                for (int rowIndex = 0; rowIndex < col.cells().size(); rowIndex++) {
                    this.mc.font.draw(matrices, col.cells().get(rowIndex), xOffset, yOffset + rowIndex * (this.mc.font.lineHeight + 1), 0xffaa_aaaa);
                }
                xOffset += col.width();
            }
        }
        this.mc.getItemRenderer().renderAndDecorateFakeItem(this.displayInfo.getIcon(), scrollX + this.x + 8, scrollY + this.y + 5);
    }

    private List<FormattedCharSequence> findOptimalLines(FormattedText line, int width) {
        if (line.getString().isEmpty()) {
            return Collections.emptyList();
        }
        List<FormattedCharSequence> list = this.mc.font.split(line, width);
        if (list.size() > 1) {
            width = Math.max(width, this.betterAdvancementTabGui.getScreen().width / 4);
            list = this.mc.font.split(line, width);
        }
        while (list.size() > 5 && width < WIDGET_WIDTH * 1.5 && width < this.betterAdvancementTabGui.getScreen().width / 2.5) {
            width += width / 4;
            list = this.mc.font.split(line, width);
        }
        return list;
    }

    public void getAdvancementProgress(AdvancementProgress advancementProgress) {
        this.advancementProgress = advancementProgress;
        this.refreshHover();
    }

    @Nullable
    private GuiAdvancementEntry getFirstVisibleParent(Advancement advancement) {
        while (true) {
            advancement = advancement.getParent();
            if (advancement == null || advancement.getDisplay() != null) {
                break;
            }
        }
        if (advancement != null && advancement.getDisplay() != null) {
            return this.betterAdvancementTabGui.getAdvancementGui(advancement);
        }
        return null;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public boolean isMouseOver(double scrollX, double scrollY, double mouseX, double mouseY) {
        if (!this.displayInfo.isHidden() || this.advancementProgress != null && this.advancementProgress.isDone()) {
            double left = scrollX + this.x;
            double right = left + ADVANCEMENT_SIZE;
            double top = scrollY + this.y;
            double bottom = top + ADVANCEMENT_SIZE;
            return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
        }
        return false;
    }

    private void refreshHover() {
        int k = 0;
        if (this.advancement.getMaxCriteraRequired() > 1) {
            // Add some space for the requirement counter
            int strLengthRequirementCount = String.valueOf(this.advancement.getMaxCriteraRequired()).length();
            k = this.mc.font.width("  ") + this.mc.font.width("0") * strLengthRequirementCount * 2 + this.mc.font.width("/");
        }
        int titleWidth = 29 + this.mc.font.width(this.title) + k;
        ScreenAdvancements screen = this.betterAdvancementTabGui.getScreen();
        this.criterionGrid = CriterionGrid.findOptimalCriterionGrid(this.advancement, this.advancementProgress, screen.width / 2, this.mc.font);
        int maxWidth = Math.max(titleWidth, this.criterionGrid.width);
        this.description = this.findOptimalLines(this.displayInfo.getDescription(), maxWidth);
        for (FormattedCharSequence line : this.description) {
            maxWidth = Math.max(maxWidth, this.mc.font.width(line));
        }
        this.width = maxWidth + 8;
    }

    protected void render9Sprite(PoseStack matrices,
                                 int x,
                                 int y,
                                 int width,
                                 int height,
                                 int textureHeight,
                                 int textureWidth,
                                 int textureDistance,
                                 int textureX,
                                 int textureY) {
        // Top left corner
        this.blit(matrices, x, y, textureX, textureY, textureHeight, textureHeight);
        // Top side
        GUIUtils.renderRepeating(matrices,
                                 this,
                                 x + textureHeight,
                                 y,
                                 width - textureHeight - textureHeight,
                                 textureHeight,
                                 textureX + textureHeight,
                                 textureY,
                                 textureWidth - textureHeight - textureHeight,
                                 textureDistance);
        // Top right corner
        this.blit(matrices, x + width - textureHeight, y, textureX + textureWidth - textureHeight, textureY, textureHeight, textureHeight);
        // Bottom left corner
        this.blit(matrices, x, y + height - textureHeight, textureX, textureY + textureDistance - textureHeight, textureHeight, textureHeight);
        // Bottom side
        GUIUtils.renderRepeating(matrices,
                                 this,
                                 x + textureHeight,
                                 y + height - textureHeight,
                                 width - textureHeight - textureHeight,
                                 textureHeight,
                                 textureX + textureHeight,
                                 textureY + textureDistance - textureHeight,
                                 textureWidth - textureHeight - textureHeight,
                                 textureDistance);
        // Bottom right corner
        this.blit(matrices,
                  x + width - textureHeight,
                  y + height - textureHeight,
                  textureX + textureWidth - textureHeight,
                  textureY + textureDistance - textureHeight,
                  textureHeight,
                  textureHeight);
        // Left side
        GUIUtils.renderRepeating(matrices,
                                 this,
                                 x,
                                 y + textureHeight,
                                 textureHeight,
                                 height - textureHeight - textureHeight,
                                 textureX,
                                 textureY + textureHeight,
                                 textureWidth,
                                 textureDistance - textureHeight - textureHeight);
        // Center
        GUIUtils.renderRepeating(matrices,
                                 this,
                                 x + textureHeight,
                                 y + textureHeight,
                                 width - textureHeight - textureHeight,
                                 height - textureHeight - textureHeight,
                                 textureX + textureHeight,
                                 textureY + textureHeight,
                                 textureWidth - textureHeight - textureHeight,
                                 textureDistance - textureHeight - textureHeight);
        // Right side
        GUIUtils.renderRepeating(matrices,
                                 this,
                                 x + width - textureHeight,
                                 y + textureHeight,
                                 textureHeight,
                                 height - textureHeight - textureHeight,
                                 textureX + textureWidth - textureHeight,
                                 textureY + textureHeight,
                                 textureWidth,
                                 textureDistance - textureHeight - textureHeight);
    }
}
