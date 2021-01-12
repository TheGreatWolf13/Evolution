package tgw.evolution.client.gui.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ClientAdvancementManager;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CSeenAdvancementsPacket;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.init.EvolutionResources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class ScreenAdvancements extends Screen implements ClientAdvancementManager.IListener {

    private static final int WIDTH = 252;
    private static final int HEIGHT = 140;
    private static final int CORNER_SIZE = 30;
    private static final int SIDE = 30;
    private static final int TOP = 40;
    private static final int BOTTOM = 30;
    private static final int PADDING = 9;
    private static final float MIN_ZOOM = 1;
    private static final float MAX_ZOOM = 2;
    private static final float ZOOM_STEP = 0.2F;
    public static boolean showDebugCoordinates;
    public static int uiScaling = 100;
    private final ClientAdvancementManager advManager;
    private final Map<Advancement, GuiAdvancementTab> tabs = Maps.newLinkedHashMap();
    protected int internalHeight;
    protected int internalWidth;
    @Nullable
    private GuiAdvancementEntry advConnectedToMouse;
    private boolean isScrolling;
    @Nullable
    private GuiAdvancementTab selectedTab;
    private float zoom = MIN_ZOOM;

    public ScreenAdvancements(ClientAdvancementManager advManager) {
        super(NarratorChatListener.EMPTY);
        this.advManager = advManager;
    }

    @Override
    public void advancementsCleared() {
        this.tabs.clear();
        this.selectedTab = null;
    }

    @Nullable
    public GuiAdvancementEntry getAdvancementGui(Advancement advancement) {
        GuiAdvancementTab advancementTab = this.getTab(advancement);
        return advancementTab == null ? null : advancementTab.getAdvancementGui(advancement);
    }

    @Nullable
    private GuiAdvancementTab getTab(@Nonnull Advancement advancement) {
        while (advancement.getParent() != null) {
            advancement = advancement.getParent();
        }
        return this.tabs.get(advancement);
    }

    @Override
    protected void init() {
        this.internalHeight = this.height * uiScaling / 100;
        this.internalWidth = this.width * uiScaling / 100;
        this.tabs.clear();
        this.selectedTab = null;
        this.advManager.setListener(this);
        if (this.selectedTab == null && !this.tabs.isEmpty()) {
            this.advManager.setSelectedTab(this.tabs.values().iterator().next().getAdvancement(), true);
        }
        else {
            this.advManager.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getAdvancement(), true);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == this.minecraft.gameSettings.keyBindAdvancements.getKey().getKeyCode()) {
            this.minecraft.displayGuiScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int modifiers) {
        if (modifiers == 0) {
            int left = SIDE + (this.width - this.internalWidth) / 2;
            int top = TOP + (this.height - this.internalHeight) / 2;
            for (GuiAdvancementTab betterAdvancementTabGui : this.tabs.values()) {
                if (betterAdvancementTabGui.isMouseOver(left,
                                                        top,
                                                        this.internalWidth - 2 * SIDE,
                                                        this.internalHeight - top - BOTTOM,
                                                        mouseX,
                                                        mouseY)) {
                    this.advManager.setSelectedTab(betterAdvancementTabGui.getAdvancement(), true);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseDeltaX, double mouseDeltaY) {
        int left = SIDE + (this.width - this.internalWidth) / 2;
        int top = TOP + (this.height - this.internalHeight) / 2;
        if (button != 0) {
            this.isScrolling = false;
            return false;
        }
        if (!this.isScrolling) {
            if (this.advConnectedToMouse == null) {
                boolean inGui = mouseX < left + this.internalWidth - 2 * SIDE - PADDING &&
                                mouseX > left + PADDING &&
                                mouseY < top + this.internalHeight - TOP + 1 &&
                                mouseY > top + 2 * PADDING;
                if (this.selectedTab != null && inGui) {
                    for (GuiAdvancementEntry betterAdvancementEntryGui : this.selectedTab.guis.values()) {
                        if (betterAdvancementEntryGui.isMouseOver(this.selectedTab.scrollX,
                                                                  this.selectedTab.scrollY,
                                                                  mouseX - left - PADDING,
                                                                  mouseY - top - 2 * PADDING)) {
                            if (betterAdvancementEntryGui.betterDisplayInfo.allowDragging()) {
                                this.advConnectedToMouse = betterAdvancementEntryGui;
                                break;
                            }
                        }
                    }
                }
            }
            else {
                this.advConnectedToMouse.x = (int) Math.round(this.advConnectedToMouse.x + mouseDeltaX);
                this.advConnectedToMouse.y = (int) Math.round(this.advConnectedToMouse.y + mouseDeltaY);
            }
        }
        else {
            this.advConnectedToMouse = null;
        }
        if (this.advConnectedToMouse == null) {
            if (!this.isScrolling) {
                this.isScrolling = true;
            }
            else if (this.selectedTab != null) {
                this.selectedTab.scroll(mouseDeltaX,
                                        mouseDeltaY,
                                        this.internalWidth - 2 * SIDE - 3 * PADDING,
                                        this.internalHeight - TOP - BOTTOM - 3 * PADDING);
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        int wheel = (int) scroll;
        if (wheel < 0 && this.zoom > MIN_ZOOM) {
            this.zoom -= ZOOM_STEP;
        }
        else if (wheel > 0 && this.zoom < MAX_ZOOM) {
            this.zoom += ZOOM_STEP;
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public void nonRootAdvancementAdded(Advancement advancement) {
        GuiAdvancementTab betterAdvancementTabGui = this.getTab(advancement);
        if (betterAdvancementTabGui != null) {
            betterAdvancementTabGui.addAdvancement(advancement);
        }
    }

    @Override
    public void nonRootAdvancementRemoved(Advancement advancementIn) {
    }

    @Override
    public void onUpdateAdvancementProgress(Advancement advancement, AdvancementProgress progress) {
        GuiAdvancementEntry advancementEntry = this.getAdvancementGui(advancement);
        if (advancementEntry != null) {
            advancementEntry.getAdvancementProgress(progress);
        }
    }

    @Override
    public void removed() {
        this.advManager.setListener(null);
        ClientPlayNetHandler clientPlayNetHandler = this.minecraft.getConnection();
        if (clientPlayNetHandler != null) {
            clientPlayNetHandler.sendPacket(CSeenAdvancementsPacket.closedScreen());
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        int left = SIDE + (this.width - this.internalWidth) / 2;
        int top = TOP + (this.height - this.internalHeight) / 2;
        int right = this.internalWidth - SIDE + (this.width - this.internalWidth) / 2;
        int bottom = this.internalHeight - SIDE + (this.height - this.internalHeight) / 2;
        this.renderBackground();
        this.renderInside(left, top, right, bottom);
        this.renderWindow(left, top, right, bottom);
        //Don't draw tool tips if dragging an advancement
        if (this.advConnectedToMouse == null) {
            this.renderToolTips(mouseX, mouseY, left, top, right, bottom);
        }
        //Draw guide lines to all advancements at 45 or 90 degree angles.
        if (this.advConnectedToMouse != null) {
            for (GuiAdvancementEntry advancementEntry : this.selectedTab.guis.values()) {
                if (advancementEntry != this.advConnectedToMouse) {
                    int x1 = advancementEntry.x + left + PADDING + this.selectedTab.scrollX + 3;
                    int x2 = this.advConnectedToMouse.x + left + PADDING + this.selectedTab.scrollX + 3;
                    int y1 = advancementEntry.y + top + 2 * PADDING + this.selectedTab.scrollY;
                    int y2 = this.advConnectedToMouse.y + top + 2 * PADDING + this.selectedTab.scrollY;
                    int centerX1 = advancementEntry.x + left + PADDING + this.selectedTab.scrollX + 3 + GuiAdvancementEntry.ADVANCEMENT_SIZE / 2;
                    int centerX2 = this.advConnectedToMouse.x +
                                   left +
                                   PADDING +
                                   this.selectedTab.scrollX +
                                   3 +
                                   GuiAdvancementEntry.ADVANCEMENT_SIZE / 2;
                    int centerY1 = advancementEntry.y + top + 2 * PADDING + this.selectedTab.scrollY + GuiAdvancementEntry.ADVANCEMENT_SIZE / 2;
                    int centerY2 = this.advConnectedToMouse.y +
                                   top +
                                   2 * PADDING +
                                   this.selectedTab.scrollY +
                                   GuiAdvancementEntry.ADVANCEMENT_SIZE / 2;
                    double degrees = Math.toDegrees(Math.atan2(centerX1 - centerX2, centerY1 - centerY2));
                    if (degrees < 0) {
                        degrees += 360;
                    }
                    if (advancementEntry.x == this.advConnectedToMouse.x) {
                        if (y1 > y2) {
                            //Draw right
                            GUIUtils.drawRect(x1, y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, x2, y2, 1, 0x00_FF00);
                            //Draw bottom for bottom
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              x2,
                                              y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                            //Draw top for bottom
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, y1, x2, y1, 1, 0x00_FF00);
                            //Draw bottom for top
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              x2,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                            //Draw top for top
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, y2, x2, y2, 1, 0x00_FF00);
                            //Draw left
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2,
                                              1,
                                              0x00_FF00);
                        }
                        else {
                            //Draw right
                            GUIUtils.drawRect(x1, y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, x2, y1, 1, 0x00_FF00);
                            //Draw bottom for bottom
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              x2,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                            //Draw top for bottom
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, y2, x2, y2, 1, 0x00_FF00);
                            //Draw bottom for top
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              x2,
                                              y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                            //Draw top for top
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, y1, x2, y1, 1, 0x00_FF00);
                            //Draw left
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1,
                                              1,
                                              0x00_FF00);
                        }
                    }
                    if (advancementEntry.y == this.advConnectedToMouse.y) {
                        if (x1 > x2) {
                            //Draw top
                            GUIUtils.drawRect(x2, y1, x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, y2, 1, 0x00_FF00);
                            //Draw left for right
                            GUIUtils.drawRect(x1, y1, x1, y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, 1, 0x00_FF00);
                            //Draw right for right
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1,
                                              x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                            //Draw left for left
                            GUIUtils.drawRect(x2, y1, x2, y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, 1, 0x00_FF00);
                            //Draw right for left
                            GUIUtils.drawRect(x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1,
                                              x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                            //Draw bottom
                            GUIUtils.drawRect(x2,
                                              y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                        }
                        else {
                            //Draw left
                            GUIUtils.drawRect(x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, y1, x1, y2, 1, 0x00_FF00);
                            //Draw left for right
                            GUIUtils.drawRect(x2, y1, x2, y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, 1, 0x00_FF00);
                            //Draw right for right
                            GUIUtils.drawRect(x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1,
                                              x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                            //Draw left for left
                            GUIUtils.drawRect(x1, y1, x1, y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, 1, 0x00_FF00);
                            //Draw right for left
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1,
                                              x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                            //Draw right
                            GUIUtils.drawRect(x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              x1,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                        }
                    }
                    if (degrees == 45 || degrees == 135 || degrees == 225 || degrees == 315) {
                        //Draw lines around each advancement
                        //First
                        //Top
                        GUIUtils.drawRect(x1, y1, x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, y1, 1, 0x00_FF00);
                        //Bottom
                        GUIUtils.drawRect(x1,
                                          y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          1,
                                          0x00_FF00);
                        //Left
                        GUIUtils.drawRect(x1, y1, x1, y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, 1, 0x00_FF00);
                        //Right
                        GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          y1,
                                          x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          1,
                                          0x00_FF00);
                        //Second
                        //Top
                        GUIUtils.drawRect(x2, y2, x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, y2, 1, 0x00_FF00);
                        //Bottom
                        GUIUtils.drawRect(x2,
                                          y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          1,
                                          0x00_FF00);
                        //Left
                        GUIUtils.drawRect(x2, y2, x2, y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1, 1, 0x00_FF00);
                        //Right
                        GUIUtils.drawRect(x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          y2,
                                          x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                          1,
                                          0x00_FF00);

                        if (degrees == 45 || degrees == 225) {
                            GUIUtils.drawRect(x1,
                                              y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              x2,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1,
                                              x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2,
                                              1,
                                              0x00_FF00);
                        }
                        else {
                            GUIUtils.drawRect(x1, y1, x2, y2, 1, 0x00_FF00);
                            GUIUtils.drawRect(x1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y1 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              x2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              y2 + GuiAdvancementEntry.ADVANCEMENT_SIZE - 1,
                                              1,
                                              0x00_FF00);
                        }
                    }
                }
            }
        }
        if (showDebugCoordinates &&
            this.selectedTab != null &&
            mouseX < this.internalWidth - SIDE - PADDING &&
            mouseX > SIDE + PADDING &&
            mouseY < this.internalHeight - top + 1 &&
            mouseY > top + PADDING * 2) {
            //If dragging an advancement, draw coordinates of advancement being moved instead of mouse coordinates
            if (this.advConnectedToMouse != null) {
                //-3 and -1 are needed to have the coordinates be rendered where the advancement starts being rendered, rather than its real position.
                int currentX = this.advConnectedToMouse.x + left + PADDING + this.selectedTab.scrollX + 3 + 1;
                int currentY = this.advConnectedToMouse.y + top + 2 * PADDING + this.selectedTab.scrollY - this.font.FONT_HEIGHT + 1;
                this.font.drawString(this.advConnectedToMouse.x + "," + this.advConnectedToMouse.y, currentX, currentY, 0x00_0000);
            }
            else {
                //Draws a string containing the current position above the mouse. Locked to inside the advancement window.
                int xMouse = mouseX - left - PADDING;
                int yMouse = mouseY - top - 2 * PADDING;
                //-3 and -1 are needed to have the position be where the advancement starts being rendered, rather than its real position.
                int currentX = xMouse - this.selectedTab.scrollX - 3 - 1;
                int currentY = yMouse - this.selectedTab.scrollY - 1;
                this.font.drawString(currentX + "," + currentY, mouseX, mouseY - this.font.FONT_HEIGHT, 0x00_0000);
            }
        }
    }

    private void renderInside(int left, int top, int right, int bottom) {
        GuiAdvancementTab advancementTab = this.selectedTab;
        int boxLeft = left + PADDING;
        int boxTop = top + 2 * PADDING;
        int boxRight = right - PADDING;
        int boxBottom = bottom - PADDING;
        int width = boxRight - boxLeft;
        int height = boxBottom - boxTop;
        if (advancementTab == null) {
            fill(boxLeft, boxTop, boxRight, boxBottom, -16_777_216);
            String s = I18n.format("advancements.empty");
            int i = this.font.getStringWidth(s);
            //noinspection IntegerDivisionInFloatingPointContext
            this.font.drawString(s, boxLeft + (width - i) / 2, boxTop + height / 2 - this.font.FONT_HEIGHT, -1);
            //noinspection IntegerDivisionInFloatingPointContext
            this.font.drawString(":(", boxLeft + (width - this.font.getStringWidth(":(")) / 2, boxTop + height / 2 + this.font.FONT_HEIGHT, -1);
        }
        else {
            GlStateManager.pushMatrix();
            GlStateManager.translated((float) boxLeft, (float) boxTop, -400.0F);
            GlStateManager.enableDepthTest();
            advancementTab.drawContents(width, height);
            GlStateManager.popMatrix();
            GlStateManager.depthFunc(515);
            GlStateManager.disableDepthTest();
        }
    }

    private void renderToolTips(int mouseX, int mouseY, int left, int top, int right, int bottom) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.selectedTab != null) {
            GlStateManager.pushMatrix();
            GlStateManager.enableDepthTest();
            GlStateManager.translated((float) (left + PADDING), (float) (top + 2 * PADDING), 400.0F);
            this.selectedTab.drawToolTips(mouseX - left - PADDING,
                                          mouseY - top - 2 * PADDING,
                                          left,
                                          top,
                                          right - left - 2 * PADDING,
                                          bottom - top - 3 * PADDING);
            GlStateManager.disableDepthTest();
            GlStateManager.popMatrix();
        }
        if (this.tabs.size() > 1) {
            int width = right - left;
            int height = bottom - top;
            for (GuiAdvancementTab tab : this.tabs.values()) {
                if (tab.isMouseOver(left, top, width, height, mouseX, mouseY)) {
                    this.renderTooltip(tab.getTitle(), mouseX, mouseY);
                }
            }
        }
    }

    public void renderWindow(int left, int top, int right, int bottom) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        RenderHelper.disableStandardItemLighting();
        this.minecraft.getTextureManager().bindTexture(EvolutionResources.GUI_WINDOW);
        // Top left corner
        this.blit(left, top, 0, 0, CORNER_SIZE, CORNER_SIZE);
        // Top side
        GUIUtils.renderRepeating(this,
                                 left + CORNER_SIZE,
                                 top,
                                 this.internalWidth - CORNER_SIZE - 2 * SIDE - CORNER_SIZE,
                                 CORNER_SIZE,
                                 CORNER_SIZE,
                                 0,
                                 WIDTH - CORNER_SIZE - CORNER_SIZE,
                                 CORNER_SIZE);
        // Top right corner
        this.blit(right - CORNER_SIZE, top, WIDTH - CORNER_SIZE, 0, CORNER_SIZE, CORNER_SIZE);
        // Left side
        GUIUtils.renderRepeating(this,
                                 left,
                                 top + CORNER_SIZE,
                                 CORNER_SIZE,
                                 bottom - top - 2 * CORNER_SIZE,
                                 0,
                                 CORNER_SIZE,
                                 CORNER_SIZE,
                                 HEIGHT - CORNER_SIZE - CORNER_SIZE);
        // Right side
        GUIUtils.renderRepeating(this,
                                 right - CORNER_SIZE,
                                 top + CORNER_SIZE,
                                 CORNER_SIZE,
                                 bottom - top - 2 * CORNER_SIZE,
                                 WIDTH - CORNER_SIZE,
                                 CORNER_SIZE,
                                 CORNER_SIZE,
                                 HEIGHT - CORNER_SIZE - CORNER_SIZE);
        // Bottom left corner
        this.blit(left, bottom - CORNER_SIZE, 0, HEIGHT - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE);
        // Bottom side
        GUIUtils.renderRepeating(this,
                                 left + CORNER_SIZE,
                                 bottom - CORNER_SIZE,
                                 this.internalWidth - CORNER_SIZE - 2 * SIDE - CORNER_SIZE,
                                 CORNER_SIZE,
                                 CORNER_SIZE,
                                 HEIGHT - CORNER_SIZE,
                                 WIDTH - CORNER_SIZE - CORNER_SIZE,
                                 CORNER_SIZE);
        // Bottom right corner
        this.blit(right - CORNER_SIZE, bottom - CORNER_SIZE, WIDTH - CORNER_SIZE, HEIGHT - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE);
        if (this.tabs.size() > 1) {
            this.minecraft.getTextureManager().bindTexture(EvolutionResources.GUI_TABS);
            int width = right - left;
            int height = bottom - top;
            for (GuiAdvancementTab tab : this.tabs.values()) {
                tab.drawTab(left, top, width, height, tab == this.selectedTab);
            }
            GlStateManager.enableRescaleNormal();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                             GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                             GlStateManager.SourceFactor.ONE,
                                             GlStateManager.DestFactor.ZERO);
            RenderHelper.enableGUIStandardItemLighting();
            for (GuiAdvancementTab tab : this.tabs.values()) {
                tab.drawIcon(left, top, width, height, this.itemRenderer);
            }
            GlStateManager.disableBlend();
        }
        String windowTitle = I18n.format("gui.advancements");
        if (this.selectedTab != null) {
            windowTitle += " - " + this.selectedTab.getTitle();
        }
        this.font.drawString(windowTitle, left + 8, top + 6, 4_210_752);
    }

    @Override
    public void rootAdvancementAdded(Advancement advancement) {
        GuiAdvancementTab advancementTab = GuiAdvancementTab.create(this.minecraft,
                                                                    this,
                                                                    this.tabs.size(),
                                                                    advancement,
                                                                    this.internalWidth - 2 * SIDE,
                                                                    this.internalHeight - TOP - SIDE);
        if (advancementTab != null) {
            this.tabs.put(advancement, advancementTab);
        }
    }

    @Override
    public void rootAdvancementRemoved(Advancement advancementIn) {
    }

    @Override
    public void setSelectedTab(@Nullable Advancement advancement) {
        this.selectedTab = this.tabs.get(advancement);
    }
}
