package tgw.evolution.client.gui.advancements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionResources;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScreenAdvancements extends Screen implements ClientAdvancements.Listener {

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
    private final ClientAdvancements advManager;
    private final ResourceLocation resWindow = Evolution.getResource("textures/gui/window.png");
    private final Map<Advancement, GuiAdvancementTab> tabs = new LinkedHashMap<>();
    private @Nullable GuiAdvancementEntry advConnectedToMouse;
    private boolean isScrolling;
    private @Nullable GuiAdvancementTab selectedTab;
    private float zoom = MIN_ZOOM;

    public ScreenAdvancements(ClientAdvancements advManager) {
        super(NarratorChatListener.NO_TITLE);
        this.advManager = advManager;
    }

    public @Nullable GuiAdvancementEntry getAdvancementGui(Advancement advancement) {
        GuiAdvancementTab advancementTab = this.getTab(advancement);
        return advancementTab == null ? null : advancementTab.getAdvancementGui(advancement);
    }

    private @Nullable GuiAdvancementTab getTab(Advancement advancement) {
        while (advancement.getParent() != null) {
            advancement = advancement.getParent();
        }
        return this.tabs.get(advancement);
    }

    @Override
    protected void init() {
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
    public boolean keyPressed(@Key int keyCode, int scanCode, @Modifiers int modifiers) {
        assert this.minecraft != null;
        if (this.minecraft.options.keyAdvancements.matches(keyCode, scanCode)) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            int top = TOP;
            for (GuiAdvancementTab betterAdvancementTabGui : this.tabs.values()) {
                if (betterAdvancementTabGui.isMouseOver(SIDE, top, this.width - 2 * SIDE, this.height - top - BOTTOM, mouseX, mouseY)) {
                    this.advManager.setSelectedTab(betterAdvancementTabGui.getAdvancement(), true);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseDeltaX, double mouseDeltaY) {
        if (button != 0) {
            this.isScrolling = false;
            return false;
        }
        if (!this.isScrolling) {
            if (this.advConnectedToMouse != null) {
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
                this.selectedTab.scroll(mouseDeltaX, mouseDeltaY, this.width - 2 * SIDE - 3 * PADDING, this.height - TOP - BOTTOM - 3 * PADDING);
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
    public void onAddAdvancementRoot(Advancement advancement) {
        assert this.minecraft != null;
        GuiAdvancementTab advancementTab = GuiAdvancementTab.create(this.minecraft,
                                                                    this,
                                                                    this.tabs.size(),
                                                                    advancement,
                                                                    this.width - 2 * SIDE,
                                                                    this.height - TOP - SIDE);
        if (advancementTab != null) {
            this.tabs.put(advancement, advancementTab);
        }
    }

    @Override
    public void onAddAdvancementTask(Advancement advancement) {
        GuiAdvancementTab betterAdvancementTabGui = this.getTab(advancement);
        if (betterAdvancementTabGui != null) {
            betterAdvancementTabGui.addAdvancement(advancement);
        }
    }

    @Override
    public void onAdvancementsCleared() {
        this.tabs.clear();
        this.selectedTab = null;
    }

    @Override
    public void onClose() {
        this.advManager.setListener(null);
        assert this.minecraft != null;
        ClientPacketListener clientPlayNetHandler = this.minecraft.getConnection();
        if (clientPlayNetHandler != null) {
            clientPlayNetHandler.send(ServerboundSeenAdvancementsPacket.closedScreen());
        }
        super.onClose();
    }

    @Override
    public void onRemoveAdvancementRoot(Advancement advancementIn) {
    }

    @Override
    public void onRemoveAdvancementTask(Advancement advancementIn) {
    }

    @Override
    public void onSelectedTabChanged(@Nullable Advancement advancement) {
        //noinspection VariableNotUsedInsideIf
        if (this.selectedTab != null) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        this.selectedTab = this.tabs.get(advancement);
    }

    @Override
    public void onUpdateAdvancementProgress(Advancement advancement, AdvancementProgress progress) {
        GuiAdvancementEntry advancementEntry = this.getAdvancementGui(advancement);
        if (advancementEntry != null) {
            advancementEntry.getAdvancementProgress(progress);
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        int right = this.width - SIDE;
        int bottom = this.height - SIDE;
        this.renderBackground(matrices);
        int top = TOP;
        int left = SIDE;
        this.renderInside(matrices, left, top, right, bottom);
        this.renderWindow(matrices, left, top, right, bottom);
        if (this.advConnectedToMouse == null) {
            this.renderToolTips(matrices, mouseX, mouseY, left, top, right, bottom);
        }
        if (this.advConnectedToMouse != null && this.selectedTab != null) {
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
    }

    private void renderInside(PoseStack matrices, int left, int top, int right, int bottom) {
        GuiAdvancementTab advancementTab = this.selectedTab;
        int boxLeft = left + PADDING;
        int boxTop = top + 2 * PADDING;
        int boxRight = right - PADDING;
        int boxBottom = bottom - PADDING;
        int width = boxRight - boxLeft;
        int height = boxBottom - boxTop;
        if (advancementTab == null) {
            fill(matrices, boxLeft, boxTop, boxRight, boxBottom, 0xff00_0000);
            String s = I18n.get("advancements.empty");
            int i = this.font.width(s);
            this.font.draw(matrices, s, boxLeft + (width - i) / 2.0f, boxTop + height / 2.0f - this.font.lineHeight, -1);
            this.font.draw(matrices,
                           ":(",
                           boxLeft + (width - this.font.width(":(")) / 2.0f,
                           boxTop + height / 2.0f + this.font.lineHeight,
                           0xffff_ffff);
        }
        else {
            PoseStack internalMat = RenderSystem.getModelViewStack();
            internalMat.pushPose();
            internalMat.translate(boxLeft, boxTop, 0);
            RenderSystem.applyModelViewMatrix();
            advancementTab.drawContents(matrices, width, height);
            internalMat.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.disableDepthTest();
        }
    }

    private void renderToolTips(PoseStack matrices, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.selectedTab != null) {
            PoseStack internalMat = RenderSystem.getModelViewStack();
            internalMat.pushPose();
            internalMat.translate(left + PADDING, top + 2 * PADDING, 400);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            this.selectedTab.drawToolTips(matrices,
                                          mouseX - left - PADDING,
                                          mouseY - top - 2 * PADDING,
                                          left,
                                          top,
                                          right - left - 2 * PADDING,
                                          bottom - top - 3 * PADDING);
            RenderSystem.disableDepthTest();
            internalMat.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        if (this.tabs.size() > 1) {
            int width = right - left;
            int height = bottom - top;
            for (GuiAdvancementTab tab : this.tabs.values()) {
                if (tab.isMouseOver(left, top, width, height, mouseX, mouseY)) {
                    this.renderTooltip(matrices, tab.getTitle(), mouseX, mouseY);
                }
            }
        }
    }

    public void renderWindow(PoseStack matrices, int left, int top, int right, int bottom) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
        RenderSystem.setShaderTexture(0, this.resWindow);
        // Top left corner
        this.blit(matrices, left, top, 0, 0, CORNER_SIZE, CORNER_SIZE);
        // Top side
        GUIUtils.renderRepeating(matrices,
                                 this,
                                 left + CORNER_SIZE,
                                 top,
                                 this.width - CORNER_SIZE - 2 * SIDE - CORNER_SIZE,
                                 CORNER_SIZE,
                                 CORNER_SIZE,
                                 0,
                                 WIDTH - CORNER_SIZE - CORNER_SIZE,
                                 CORNER_SIZE);
        // Top right corner
        this.blit(matrices, right - CORNER_SIZE, top, WIDTH - CORNER_SIZE, 0, CORNER_SIZE, CORNER_SIZE);
        // Left side
        GUIUtils.renderRepeating(matrices,
                                 this,
                                 left,
                                 top + CORNER_SIZE,
                                 CORNER_SIZE,
                                 bottom - top - 2 * CORNER_SIZE,
                                 0,
                                 CORNER_SIZE,
                                 CORNER_SIZE,
                                 HEIGHT - CORNER_SIZE - CORNER_SIZE);
        // Right side
        GUIUtils.renderRepeating(matrices,
                                 this,
                                 right - CORNER_SIZE,
                                 top + CORNER_SIZE,
                                 CORNER_SIZE,
                                 bottom - top - 2 * CORNER_SIZE,
                                 WIDTH - CORNER_SIZE,
                                 CORNER_SIZE,
                                 CORNER_SIZE,
                                 HEIGHT - CORNER_SIZE - CORNER_SIZE);
        // Bottom left corner
        this.blit(matrices, left, bottom - CORNER_SIZE, 0, HEIGHT - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE);
        // Bottom side
        GUIUtils.renderRepeating(matrices,
                                 this,
                                 left + CORNER_SIZE,
                                 bottom - CORNER_SIZE,
                                 this.width - CORNER_SIZE - 2 * SIDE - CORNER_SIZE,
                                 CORNER_SIZE,
                                 CORNER_SIZE,
                                 HEIGHT - CORNER_SIZE,
                                 WIDTH - CORNER_SIZE - CORNER_SIZE,
                                 CORNER_SIZE);
        // Bottom right corner
        this.blit(matrices, right - CORNER_SIZE, bottom - CORNER_SIZE, WIDTH - CORNER_SIZE, HEIGHT - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE);
        if (this.tabs.size() > 1) {
            RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
            RenderSystem.setShaderTexture(0, EvolutionResources.GUI_TABS);
            int width = right - left;
            int height = bottom - top;
            for (GuiAdvancementTab tab : this.tabs.values()) {
                tab.drawTab(matrices, left, top, width, height, tab == this.selectedTab);
            }
            RenderSystem.defaultBlendFunc();
            for (GuiAdvancementTab tab : this.tabs.values()) {
                tab.drawIcon(left, top, width, height, this.itemRenderer);
            }
            RenderSystem.disableBlend();
        }
        String windowTitle = I18n.get("gui.advancements");
        if (this.selectedTab != null) {
            windowTitle += " - " + this.selectedTab.getTitle().getString();
        }
        this.font.draw(matrices, windowTitle, left + 8, top + 6, 0x40_4040);
    }
}
