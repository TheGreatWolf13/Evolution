package tgw.evolution.client.gui.advancements;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.advancements.AdvancementState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderHelper;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class GuiAdvancementEntry extends AbstractGui {

    protected static final int ADVANCEMENT_SIZE = 26;
    private static final int CORNER_SIZE = 10;
    private static final int WIDGET_WIDTH = 256;
    private static final int WIDGET_HEIGHT = 26;
    private static final int TITLE_SIZE = 32;
    private static final int ICON_OFFSET = 128;
    private static final int ICON_SIZE = 26;
    protected final BetterDisplayInfo betterDisplayInfo;
    private final Advancement advancement;
    private final GuiAdvancementTab betterAdvancementTabGui;
    private final List<GuiAdvancementEntry> children = Lists.newArrayList();
    private final DisplayInfo displayInfo;
    private final Minecraft minecraft;
    private final int screenScale;
    private final String title;
    protected int x;
    protected int y;
    private AdvancementProgress advancementProgress;
    private CriterionGrid criterionGrid;
    private List<String> description;
    private GuiAdvancementEntry parent;
    private int width;

    public GuiAdvancementEntry(GuiAdvancementTab advancementTab, Minecraft mc, Advancement advancement, DisplayInfo displayInfo) {
        this.betterAdvancementTabGui = advancementTab;
        this.advancement = advancement;
        this.betterDisplayInfo = advancementTab.getBetterDisplayInfo(advancement);
        this.displayInfo = displayInfo;
        this.minecraft = mc;
        this.title = mc.fontRenderer.trimStringToWidth(displayInfo.getTitle().getFormattedText(), 163);
        this.x = this.betterDisplayInfo.getPosX() != null ? this.betterDisplayInfo.getPosX() : MathHelper.floor(displayInfo.getX() * 32.0F);
        this.y = this.betterDisplayInfo.getPosY() != null ? this.betterDisplayInfo.getPosY() : MathHelper.floor(displayInfo.getY() * 27.0F);
        this.refreshHover();
        this.screenScale = mc.mainWindow.calcGuiScale(0, false);
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

    public void draw(int scrollX, int scrollY) {
        if (!this.displayInfo.isHidden() || this.advancementProgress != null && this.advancementProgress.isDone()) {
            float f = this.advancementProgress == null ? 0.0F : this.advancementProgress.getPercent();
            AdvancementState advancementState;
            if (f >= 1.0F) {
                advancementState = AdvancementState.OBTAINED;
            }
            else {
                advancementState = AdvancementState.UNOBTAINED;
            }
            this.minecraft.getTextureManager().bindTexture(EvolutionResources.GUI_WIDGETS);
            GUIUtils.setColor(this.betterDisplayInfo.getIconColor(advancementState));
            GlStateManager.enableBlend();
            this.blit(scrollX + this.x + 3,
                      scrollY + this.y,
                      this.displayInfo.getFrame().getIcon(),
                      ICON_OFFSET + ICON_SIZE * this.betterDisplayInfo.getIconYMultiplier(advancementState),
                      ICON_SIZE,
                      ICON_SIZE);
            RenderHelper.enableGUIStandardItemLighting();
            this.minecraft.getItemRenderer().renderItemAndEffectIntoGUI(null, this.displayInfo.getIcon(), scrollX + this.x + 8, scrollY + this.y + 5);
        }
        for (GuiAdvancementEntry advancementEntry : this.children) {
            advancementEntry.draw(scrollX, scrollY);
        }
    }

    /**
     * Draws connection line between this advancement and the advancement supplied in parent.
     */
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public void drawConnection(GuiAdvancementEntry parent, int scrollX, int scrollY, boolean drawInside) {
        int innerLineColor = this.advancementProgress != null && this.advancementProgress.isDone() ?
                             this.betterDisplayInfo.getCompletedLineColor() :
                             this.betterDisplayInfo.getUnCompletedLineColor();
        int borderLineColor = 0xFF00_0000;
        if (this.betterDisplayInfo.drawDirectLines()) {
            double x1 = scrollX + this.x + ADVANCEMENT_SIZE / 2 + 3;
            double y1 = scrollY + this.y + ADVANCEMENT_SIZE / 2;
            double x2 = scrollX + parent.x + ADVANCEMENT_SIZE / 2 + 3;
            double y2 = scrollY + parent.y + ADVANCEMENT_SIZE / 2;
            double width;
            boolean perpendicular = x1 == x2 || y1 == y2;
            if (!perpendicular) {
                switch (this.screenScale) {
                    case 1: {
                        width = drawInside ? 1.5 : 0.5;
                        break;
                    }
                    case 2: {
                        width = drawInside ? 2.25 : 0.75;
                        break;
                    }
                    case 3: {
                        width = drawInside ? 2 : 0.666_666_666_666_666_7;
                        break;
                    }
                    case 4: {
                        width = drawInside ? 2.125 : 0.625;
                        break;
                    }
                    default: {
                        width = drawInside ? 3 : 1;
                        break;
                    }
                }
                if (drawInside) {
                    GUIUtils.drawRect(x1 - 0.75, y1 - 0.75, x2 - 0.75, y2 - 0.75, width, borderLineColor);
                }
                else {
                    GUIUtils.drawRect(x1, y1, x2, y2, width, innerLineColor);
                }
            }
            else {
                width = drawInside ? 3 : 1;
                if (drawInside) {
                    GUIUtils.drawRect(x1 - 1, y1 - 1, x2 - 1, y2 - 1, width, borderLineColor);
                }
                else {
                    GUIUtils.drawRect(x1, y1, x2, y2, width, innerLineColor);
                }
            }
        }
        else {
            int startX = scrollX + parent.x + ADVANCEMENT_SIZE / 2;
            int endXHalf = scrollX + parent.x + ADVANCEMENT_SIZE + 6; // 6 = 32 - 26
            int startY = scrollY + parent.y + ADVANCEMENT_SIZE / 2;
            int endX = scrollX + this.x + ADVANCEMENT_SIZE / 2;
            int endY = scrollY + this.y + ADVANCEMENT_SIZE / 2;
            if (drawInside) {
                this.hLine(endXHalf, startX, startY - 1, borderLineColor);
                this.hLine(endXHalf + 1, startX, startY, borderLineColor);
                this.hLine(endXHalf, startX, startY + 1, borderLineColor);
                this.hLine(endX, endXHalf - 1, endY - 1, borderLineColor);
                this.hLine(endX, endXHalf - 1, endY, borderLineColor);
                this.hLine(endX, endXHalf - 1, endY + 1, borderLineColor);
                this.vLine(endXHalf - 1, endY, startY, borderLineColor);
                this.vLine(endXHalf + 1, endY, startY, borderLineColor);
            }
            else {
                this.hLine(endXHalf, startX, startY, innerLineColor);
                this.hLine(endX, endXHalf, endY, innerLineColor);
                this.vLine(endXHalf, endY, startY, innerLineColor);
            }
        }
    }

    public void drawConnectivity(int scrollX, int scrollY, boolean drawInside) {
        //Check if connections should be drawn at all
        if (!this.betterDisplayInfo.hideLines()) {
            //Draw connection to parent
            if (this.parent != null) {

                this.drawConnection(this.parent, scrollX, scrollY, drawInside);
            }
//            //Create and post event to get extra connections
//            final AdvancementDrawConnectionsEvent event = new AdvancementDrawConnectionsEvent(this.advancement);
//            MinecraftForge.EVENT_BUS.post(event);
//
//            //Draw extra connections from event
//            for (Advancement parent : event.getExtraConnections()) {
//                final BetterAdvancementEntryGui parentGui = this.betterAdvancementTabGui.getAdvancementGui(parent);
//
//                if (parentGui != null) {
//                    this.drawConnection(parentGui, scrollX, scrollY, drawInside);
//                }
//            }
        }
        //Draw child connections
        for (GuiAdvancementEntry advancementEntry : this.children) {
            advancementEntry.drawConnectivity(scrollX, scrollY, drawInside);
        }
    }

    public void drawHover(int scrollX, int scrollY, int left, int top) {
        this.refreshHover();
        boolean drawLeft = left + scrollX + this.x + this.width + ADVANCEMENT_SIZE >= this.betterAdvancementTabGui.getScreen().internalWidth;
        String s = this.advancementProgress == null ? null : this.advancementProgress.getProgressText();
        int i = s == null ? 0 : this.minecraft.fontRenderer.getStringWidth(s);
        boolean drawTop;
        if (!CriterionGrid.requiresShift || Screen.hasShiftDown()) {
            if (this.criterionGrid.height < this.betterAdvancementTabGui.getScreen().height) {
                drawTop = top +
                          scrollY +
                          this.y +
                          this.description.size() * this.minecraft.fontRenderer.FONT_HEIGHT +
                          this.criterionGrid.height +
                          50 >= this.betterAdvancementTabGui.getScreen().height;
            }
            else {
                // Always draw on the bottom if the grid is larger than the screen
                drawTop = false;
            }
        }
        else {
            drawTop = top + scrollY + this.y + this.description.size() * this.minecraft.fontRenderer.FONT_HEIGHT + 50 >=
                      this.betterAdvancementTabGui.getScreen().height;
        }

        float percentageObtained = this.advancementProgress == null ? 0.0F : this.advancementProgress.getPercent();
        int j = MathHelper.floor(percentageObtained * this.width);
        AdvancementState stateTitleLeft;
        AdvancementState stateTitleRight;
        AdvancementState stateIcon;
        if (percentageObtained >= 1.0F) {
            j = this.width / 2;
            stateTitleLeft = AdvancementState.OBTAINED;
            stateTitleRight = AdvancementState.OBTAINED;
            stateIcon = AdvancementState.OBTAINED;
        }
        else if (j < 2) {
            j = this.width / 2;
            stateTitleLeft = AdvancementState.UNOBTAINED;
            stateTitleRight = AdvancementState.UNOBTAINED;
            stateIcon = AdvancementState.UNOBTAINED;
        }
        else if (j > this.width - 2) {
            j = this.width / 2;
            stateTitleLeft = AdvancementState.OBTAINED;
            stateTitleRight = AdvancementState.OBTAINED;
            stateIcon = AdvancementState.UNOBTAINED;
        }
        else {
            stateTitleLeft = AdvancementState.OBTAINED;
            stateTitleRight = AdvancementState.UNOBTAINED;
            stateIcon = AdvancementState.UNOBTAINED;
        }
        int k = this.width - j;
        this.minecraft.getTextureManager().bindTexture(EvolutionResources.GUI_WIDGETS);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        int drawY = scrollY + this.y;
        int drawX;
        if (drawLeft) {
            drawX = scrollX + this.x - this.width + ADVANCEMENT_SIZE + 6;
        }
        else {
            drawX = scrollX + this.x;
        }
        int boxHeight;
        if (!CriterionGrid.requiresShift || Screen.hasShiftDown()) {
            boxHeight = TITLE_SIZE + this.description.size() * this.minecraft.fontRenderer.FONT_HEIGHT + this.criterionGrid.height;
        }
        else {
            boxHeight = TITLE_SIZE + this.description.size() * this.minecraft.fontRenderer.FONT_HEIGHT;
        }
        if (!this.description.isEmpty()) {
            if (drawTop) {
                this.render9Sprite(drawX,
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
                this.render9Sprite(drawX, drawY, this.width, boxHeight, CORNER_SIZE, WIDGET_WIDTH, WIDGET_HEIGHT, 0, 52);
            }
        }
        // Title left side
        GUIUtils.setColor(this.betterDisplayInfo.getTitleColor(stateTitleLeft));
        int left_side = Math.min(j, WIDGET_WIDTH - 16);
        this.blit(drawX, drawY, 0, this.betterDisplayInfo.getTitleYMultiplier(stateTitleLeft) * WIDGET_HEIGHT, left_side, WIDGET_HEIGHT);
        if (left_side < j) {
            this.blit(drawX + left_side,
                      drawY,
                      16,
                      this.betterDisplayInfo.getTitleYMultiplier(stateTitleLeft) * WIDGET_HEIGHT,
                      j - left_side,
                      WIDGET_HEIGHT);
        }
        // Title right side
        GUIUtils.setColor(this.betterDisplayInfo.getTitleColor(stateTitleRight));
        int right_side = Math.min(k, WIDGET_WIDTH - 16);
        this.blit(drawX + j,
                  drawY,
                  WIDGET_WIDTH - right_side,
                  this.betterDisplayInfo.getTitleYMultiplier(stateTitleRight) * WIDGET_HEIGHT,
                  right_side,
                  WIDGET_HEIGHT);
        if (right_side < k) {
            this.blit(drawX + j + right_side,
                      drawY,
                      WIDGET_WIDTH - k + right_side,
                      this.betterDisplayInfo.getTitleYMultiplier(stateTitleRight) * WIDGET_HEIGHT,
                      k - right_side,
                      WIDGET_HEIGHT);
        }
        // Advancement icon
        GUIUtils.setColor(this.betterDisplayInfo.getIconColor(stateIcon));
        this.blit(scrollX + this.x + 3,
                  scrollY + this.y,
                  this.displayInfo.getFrame().getIcon(),
                  ICON_OFFSET + ICON_SIZE * this.betterDisplayInfo.getIconYMultiplier(stateIcon),
                  ICON_SIZE,
                  ICON_SIZE);
        if (drawLeft) {
            this.minecraft.fontRenderer.drawStringWithShadow(this.title, drawX + 5, scrollY + this.y + 9, -1);
            if (s != null) {
                this.minecraft.fontRenderer.drawStringWithShadow(s, scrollX + this.x - i, scrollY + this.y + 9, -1);
            }
        }
        else {
            this.minecraft.fontRenderer.drawStringWithShadow(this.title, scrollX + this.x + 32, scrollY + this.y + 9, -1);
            if (s != null) {
                this.minecraft.fontRenderer.drawStringWithShadow(s, scrollX + this.x + this.width - i - 5, scrollY + this.y + 9, -1);
            }
        }
        int yOffset;
        if (drawTop) {
            yOffset = drawY + 26 - boxHeight + 7;
        }
        else {
            yOffset = scrollY + this.y + 9 + 17;
        }
        for (int k1 = 0; k1 < this.description.size(); ++k1) {
            this.minecraft.fontRenderer.drawString(this.description.get(k1),
                                                   drawX + 5,
                                                   yOffset + k1 * this.minecraft.fontRenderer.FONT_HEIGHT,
                                                   -5_592_406);
        }
        if (this.criterionGrid != null && !CriterionGrid.requiresShift || Screen.hasShiftDown()) {
            int xOffset = drawX + 5;
            yOffset += this.description.size() * this.minecraft.fontRenderer.FONT_HEIGHT;
            for (int colIndex = 0; colIndex < this.criterionGrid.columns.size(); colIndex++) {
                CriterionGrid.Column col = this.criterionGrid.columns.get(colIndex);
                for (int rowIndex = 0; rowIndex < col.cells.size(); rowIndex++) {
                    this.minecraft.fontRenderer.drawString(col.cells.get(rowIndex),
                                                           xOffset,
                                                           yOffset + rowIndex * this.minecraft.fontRenderer.FONT_HEIGHT,
                                                           -5_592_406);
                }
                xOffset += col.width;
            }
        }
        RenderHelper.enableGUIStandardItemLighting();
        this.minecraft.getItemRenderer().renderItemAndEffectIntoGUI(null, this.displayInfo.getIcon(), scrollX + this.x + 8, scrollY + this.y + 5);
    }

    private List<String> findOptimalLines(String line, int width) {
        if (line.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> list = this.minecraft.fontRenderer.listFormattedStringToWidth(line, width);
        if (list.size() > 1) {
            width = Math.max(width, this.betterAdvancementTabGui.getScreen().internalWidth / 4);
            list = this.minecraft.fontRenderer.listFormattedStringToWidth(line, width);
        }
        while (list.size() > 5 && width < WIDGET_WIDTH * 1.5 && width < this.betterAdvancementTabGui.getScreen().internalWidth / 2.5) {
            width += width / 4;
            list = this.minecraft.fontRenderer.listFormattedStringToWidth(line, width);
        }
        return list;
    }

    public void getAdvancementProgress(AdvancementProgress advancementProgressIn) {
        this.advancementProgress = advancementProgressIn;
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
        Minecraft mc = this.minecraft;
        int k = 0;
        if (this.advancement.getRequirementCount() > 1) {
            // Add some space for the requirement counter
            int strLengthRequirementCount = String.valueOf(this.advancement.getRequirementCount()).length();
            k = mc.fontRenderer.getStringWidth("  ") +
                mc.fontRenderer.getStringWidth("0") * strLengthRequirementCount * 2 +
                mc.fontRenderer.getStringWidth("/");
        }
        int titleWidth = 29 + mc.fontRenderer.getStringWidth(this.title) + k;
        ScreenAdvancements screen = this.betterAdvancementTabGui.getScreen();
        this.criterionGrid = CriterionGrid.findOptimalCriterionGrid(this.advancement, this.advancementProgress, screen.width / 2, mc.fontRenderer);
        int maxWidth;
        if (!CriterionGrid.requiresShift || Screen.hasShiftDown()) {
            maxWidth = Math.max(titleWidth, this.criterionGrid.width);
        }
        else {
            maxWidth = titleWidth;
        }
        String s = this.displayInfo.getDescription().getFormattedText();
        this.description = this.findOptimalLines(s, maxWidth);
        for (String line : this.description) {
            maxWidth = Math.max(maxWidth, mc.fontRenderer.getStringWidth(line));
        }
        this.width = maxWidth + 8;
    }

    protected void render9Sprite(int x,
                                 int y,
                                 int width,
                                 int height,
                                 int textureHeight,
                                 int textureWidth,
                                 int textureDistance,
                                 int textureX,
                                 int textureY) {
        // Top left corner
        this.blit(x, y, textureX, textureY, textureHeight, textureHeight);
        // Top side
        GUIUtils.renderRepeating(this,
                                 x + textureHeight,
                                 y,
                                 width - textureHeight - textureHeight,
                                 textureHeight,
                                 textureX + textureHeight,
                                 textureY,
                                 textureWidth - textureHeight - textureHeight,
                                 textureDistance);
        // Top right corner
        this.blit(x + width - textureHeight, y, textureX + textureWidth - textureHeight, textureY, textureHeight, textureHeight);
        // Bottom left corner
        this.blit(x, y + height - textureHeight, textureX, textureY + textureDistance - textureHeight, textureHeight, textureHeight);
        // Bottom side
        GUIUtils.renderRepeating(this,
                                 x + textureHeight,
                                 y + height - textureHeight,
                                 width - textureHeight - textureHeight,
                                 textureHeight,
                                 textureX + textureHeight,
                                 textureY + textureDistance - textureHeight,
                                 textureWidth - textureHeight - textureHeight,
                                 textureDistance);
        // Bottom right corner
        this.blit(x + width - textureHeight,
                  y + height - textureHeight,
                  textureX + textureWidth - textureHeight,
                  textureY + textureDistance - textureHeight,
                  textureHeight,
                  textureHeight);
        // Left side
        GUIUtils.renderRepeating(this,
                                 x,
                                 y + textureHeight,
                                 textureHeight,
                                 height - textureHeight - textureHeight,
                                 textureX,
                                 textureY + textureHeight,
                                 textureWidth,
                                 textureDistance - textureHeight - textureHeight);
        // Center
        GUIUtils.renderRepeating(this,
                                 x + textureHeight,
                                 y + textureHeight,
                                 width - textureHeight - textureHeight,
                                 height - textureHeight - textureHeight,
                                 textureX + textureHeight,
                                 textureY + textureHeight,
                                 textureWidth - textureHeight - textureHeight,
                                 textureDistance - textureHeight - textureHeight);
        // Right side
        GUIUtils.renderRepeating(this,
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
