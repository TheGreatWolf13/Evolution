package tgw.evolution.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.gui.overlays.Overlays;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.Blending;
import tgw.evolution.util.collection.lists.IArrayList;
import tgw.evolution.util.collection.lists.IList;

import java.util.List;

public class EvolutionGui extends Gui {

    private final MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
    private final EvolutionDebugScreenOverlay debugScreen = new EvolutionDebugScreenOverlay(this.minecraft);
    private final IList rightWidthCache = new IArrayList();
    protected int leftHeight = 39;
    protected int rightHeight = 39;

    public EvolutionGui(Minecraft mc) {
        super(mc);
    }

    public boolean animateOverlayMessageColor() {
        return this.animateOverlayMessageColor;
    }

    /**
     * Make public.
     */
    @Override
    public void displayScoreboardSidebar(PoseStack matrices, Objective objetive) {
        super.displayScoreboardSidebar(matrices, objetive);
    }

    /**
     * Make public
     */
    @Override
    public void drawBackdrop(PoseStack matrices, Font font, int heightOffset, int messageWidth, int color) {
        super.drawBackdrop(matrices, font, heightOffset, messageWidth, color);
    }

    public int getLeftHeightAndIncrease() {
        return this.getLeftHeightAndIncrease(10);
    }

    public int getLeftHeightAndIncrease(int delta) {
        int ret = this.leftHeight;
        this.leftHeight += delta;
        return ret;
    }

    public @Nullable Component getOverlayMessage() {
        return this.overlayMessageString;
    }

    public int getOverlayMessageTime() {
        return this.overlayMessageTime;
    }

    public int getRightHeightAndIncrease() {
        int ret = this.rightHeight;
        this.rightHeight += 10;
        return ret;
    }

    public float getScopeScale() {
        return this.scopeScale;
    }

    public @Nullable Component getSubtitle() {
        return this.subtitle;
    }

    public @Nullable Component getTitle() {
        return this.title;
    }

    public int getTitleFadeInTime() {
        return this.titleFadeInTime;
    }

    public int getTitleFadeOutTime() {
        return this.titleFadeOutTime;
    }

    public int getTitleStayTime() {
        return this.titleStayTime;
    }

    public int getTitleTime() {
        return this.titleTime;
    }

    @Override
    public void render(PoseStack matrices, float partialTicks) {
        this.screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        this.screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        this.rightHeight = 39;
        this.leftHeight = 39;
        this.random.setSeed(this.tickCount * 312_871L);
        Overlays.renderAllHud(this.minecraft, this, matrices, partialTicks, this.screenWidth, this.screenHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderFPSGraph(PoseStack matrices) {
        this.debugScreen.render(matrices);
    }

    public void renderHUDText(PoseStack matrices) {
        Blending.DEFAULT_1_0.apply();
        if (this.minecraft.isDemo()) {
            assert this.minecraft.level != null;
            long time = this.minecraft.level.getGameTime();
            Component demo;
            if (time >= 120_500L) {
                demo = DEMO_EXPIRED_TEXT;
            }
            else {
                demo = new TranslatableComponent("demo.remainingTime",
                                                 StringUtil.formatTickDuration((int) (120_500L - this.minecraft.level.getGameTime())));
            }
            int width = this.getFont().width(demo);
            this.getFont().drawShadow(matrices, demo, this.screenWidth - width - 10, 5.0F, 0xff_ffff);
        }
        ProfilerFiller profiler = this.minecraft.getProfiler();
        if (this.minecraft.options.renderDebug) {
            profiler.push("update");
            this.debugScreen.update();
            profiler.popPush("left");
            List<String> left = this.debugScreen.getLeft();
            profiler.popPush("right");
            List<String> right = this.debugScreen.getRight();
            profiler.popPush("fill");
            Font font = this.minecraft.font;
            Matrix4f matrix = matrices.last().pose();
            GUIUtils.startFillBatch(Tesselator.getInstance().getBuilder());
            int top = 2;
            for (int i = 0, len = left.size(); i < len; i++) {
                String msg = left.get(i);
                if (!StringUtil.isNullOrEmpty(msg)) {
                    GUIUtils.fillInBatch(matrix, 1, top - 1, 2 + font.width(msg) + 1, top + font.lineHeight - 1, 0x9050_5050);
                }
                top += font.lineHeight;
            }
            this.rightWidthCache.clear();
            top = 2;
            for (int i = 0, len = right.size(); i < len; i++) {
                String msg = right.get(i);
                if (!StringUtil.isNullOrEmpty(msg)) {
                    int w = font.width(msg);
                    int x = this.screenWidth - 2 - w;
                    GUIUtils.fillInBatch(matrix, x - 1, top - 1, x + w + 1, top + font.lineHeight - 1, 0x9050_5050);
                    this.rightWidthCache.add(i, w);
                }
                else {
                    this.rightWidthCache.add(i, 0);
                }
                top += font.lineHeight;
            }
            GUIUtils.endFillBatch();
            profiler.popPush("text");
            top = 2;
            for (int i = 0, len = left.size(); i < len; i++) {
                String msg = left.get(i);
                if (!StringUtil.isNullOrEmpty(msg)) {
                    font.drawInBatch(msg, 2, top, 0xe0_e0e0, false, matrix, this.buffer, false, 0, 0xf0_00f0, false);
                }
                top += font.lineHeight;
            }
            top = 2;
            for (int i = 0, len = right.size(); i < len; i++) {
                String msg = right.get(i);
                if (!StringUtil.isNullOrEmpty(msg)) {
                    int w = this.rightWidthCache.getInt(i);
                    int x = this.screenWidth - 2 - w;
                    font.drawInBatch(msg, x, top, 0xe0_e0e0, false, matrix, this.buffer, false, 0, 0xf0_00f0, false);
                }
                top += font.lineHeight;
            }
            profiler.push("end");
            this.buffer.endBatch();
            profiler.pop();
            profiler.pop();
        }
    }

    @Override
    public void renderJumpMeter(PoseStack matrices, int x) {
        assert this.minecraft.player != null;
        float jumpScale = this.minecraft.player.getJumpRidingScale();
        int force = (int) (jumpScale * 183.0F);
        int y = this.screenHeight - 32 + 3;
        this.blit(matrices, x, y, 0, 84, 182, 5);
        if (force > 0) {
            this.blit(matrices, x, y, 0, 89, force, 5);
        }
    }

    public void renderSubtitles(PoseStack matrices) {
        this.subtitleOverlay.render(matrices);
    }

    /**
     * Make public and simplify
     */
    @Override
    public void renderTextureOverlay(ResourceLocation texture, float alpha) {
        this.setupOverlayRenderState(Blending.DEFAULT_1_0, false, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(0, this.screenHeight, -90).uv(0, 1).endVertex();
        builder.vertex(this.screenWidth, this.screenHeight, -90).uv(1, 1).endVertex();
        builder.vertex(this.screenWidth, 0, -90).uv(1, 0).endVertex();
        builder.vertex(0, 0, -90).uv(0, 0).endVertex();
        tesselator.end();
    }

    public void setScopeScale(float scale) {
        this.scopeScale = scale;
    }

    public void setupOverlayRenderState(@Nullable Blending blend, boolean depthText) {
        this.setupOverlayRenderState(blend, depthText, Gui.GUI_ICONS_LOCATION);
    }

    public void setupOverlayRenderState(@Nullable Blending blend, boolean depthTest, @Nullable ResourceLocation texture) {
        if (blend != null) {
            RenderSystem.enableBlend();
            blend.apply();
        }
        else {
            RenderSystem.disableBlend();
        }
        if (depthTest) {
            RenderSystem.enableDepthTest();
        }
        else {
            RenderSystem.disableDepthTest();
        }
        if (texture != null) {
            RenderSystem.enableTexture();
            RenderSystem.setShaderTexture(0, texture);
        }
        else {
            RenderSystem.disableTexture();
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
    }

    public static final class EvolutionDebugScreenOverlay extends DebugScreenOverlay {
        private final Minecraft mc;

        private EvolutionDebugScreenOverlay(Minecraft mc) {
            super(mc);
            this.mc = mc;
        }

        @Override
        protected void drawGameInformation(PoseStack poseStack) {
        }

        @Override
        protected void drawSystemInformation(PoseStack poseStack) {
        }

        private List<String> getLeft() {
            List<String> ret = this.getGameInformation();
            ret.add("");
            boolean hasServer = this.mc.getSingleplayerServer() != null;
            ret.add("Debug: Pie [shift]: " +
                    (this.mc.options.renderDebugCharts ? "visible" : "hidden") +
                    (hasServer ? " FPS + TPS" : " FPS") +
                    " [alt]: " +
                    (this.mc.options.renderFpsChart ? "visible" : "hidden"));
            ret.add("For help: press F3 + Q");
            return ret;
        }

        private List<String> getRight() {
            return this.getSystemInformation();
        }

        @Override
        public void render(PoseStack matrices) {
            if (this.mc.options.renderFpsChart) {
                int width = this.mc.getWindow().getGuiScaledWidth();
                this.drawChart(matrices, this.mc.getFrameTimer(), 0, width / 2, true);
                IntegratedServer server = this.mc.getSingleplayerServer();
                if (server != null) {
                    this.drawChart(matrices, server.getFrameTimer(), width - Math.min(width / 2, 240), width / 2, false);
                }
            }
        }

        public void update() {
            Entity entity = this.mc.player;
            assert entity != null;
            this.block = entity.pick(20, 0.0F, false);
            this.liquid = entity.pick(20, 0.0F, true);
        }
    }
}
