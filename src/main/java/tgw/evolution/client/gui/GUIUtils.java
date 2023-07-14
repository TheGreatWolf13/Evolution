package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import tgw.evolution.client.renderer.RenderHelper;

public final class GUIUtils {

    public static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png");
    private static final PoseStack MATRICES = new PoseStack();
    private static DifficultyInstance difficulty = new DifficultyInstance(Difficulty.NORMAL, 0, 0, 0);
    private static boolean inFillBatch;
    private static @Nullable BufferBuilder builder;
    private static boolean inBlitBatch;

    static {
        setDifficulty(null);
    }

    private GUIUtils() {
    }

    public static void blitInBatch(Matrix4f matrix,
                                   int x,
                                   int y,
                                   int blitOffset,
                                   float uOffset,
                                   float vOffset,
                                   int uWidth,
                                   int vHeight,
                                   int texHeight,
                                   int texWidth) {
        if (!inBlitBatch) {
            throw new IllegalStateException("Not in blit batch!");
        }
        innerBlitInBatch(matrix, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, texHeight, texWidth);
    }

    public static void blitInBatch(Matrix4f matrix,
                                   int x,
                                   int y,
                                   int width,
                                   int height,
                                   float uOffset,
                                   float vOffset,
                                   int uWidth,
                                   int vHeight,
                                   int texWidth,
                                   int texHeight) {
        if (!inBlitBatch) {
            throw new IllegalStateException("Not in blit batch!");
        }
        innerBlitInBatch(matrix, x, x + width, y, y + height, 0, uWidth, vHeight, uOffset, vOffset, texWidth, texHeight);
    }

    public static void disableScissor() {
        RenderSystem.disableScissor();
    }

    public static void drawCenteredStringNoShadow(PoseStack matrices, Font font, Component text, float xCentre, float y, int color) {
        FormattedCharSequence charSequence = text.getVisualOrderText();
        font.draw(matrices, charSequence, xCentre - font.width(charSequence) / 2.0f, y, color);
    }

    /**
     * Draws a textured box of any size (smallest size is borderSize * 2 square) based on a fixed size textured box with continuous borders
     * and filler. It is assumed that the desired texture ResourceLocation object has been bound using
     * Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation).
     *
     * @param poseStack     the gui pose stack
     * @param x             x axis offset
     * @param y             y axis offset
     * @param u             bound resource location image x offset
     * @param v             bound resource location image y offset
     * @param width         the desired box width
     * @param height        the desired box height
     * @param textureWidth  the width of the box texture in the resource location image
     * @param textureHeight the height of the box texture in the resource location image
     * @param topBorder     the size of the box's top border
     * @param bottomBorder  the size of the box's bottom border
     * @param leftBorder    the size of the box's left border
     * @param rightBorder   the size of the box's right border
     * @param zLevel        the zLevel to draw at
     */
    public static void drawContinuousTexturedBox(PoseStack poseStack,
                                                 int x,
                                                 int y,
                                                 int u,
                                                 int v,
                                                 int width,
                                                 int height,
                                                 int textureWidth,
                                                 int textureHeight,
                                                 int topBorder,
                                                 int bottomBorder,
                                                 int leftBorder,
                                                 int rightBorder,
                                                 float zLevel) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int fillerWidth = textureWidth - leftBorder - rightBorder;
        int fillerHeight = textureHeight - topBorder - bottomBorder;
        int canvasWidth = width - leftBorder - rightBorder;
        int canvasHeight = height - topBorder - bottomBorder;
        int xPasses = canvasWidth / fillerWidth;
        int remainderWidth = canvasWidth % fillerWidth;
        int yPasses = canvasHeight / fillerHeight;
        int remainderHeight = canvasHeight % fillerHeight;

        // Draw Border
        // Top Left
        drawTexturedModalRect(poseStack, x, y, u, v, leftBorder, topBorder, zLevel);
        // Top Right
        drawTexturedModalRect(poseStack, x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder, zLevel);
        // Bottom Left
        drawTexturedModalRect(poseStack, x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, zLevel);
        // Bottom Right
        drawTexturedModalRect(poseStack, x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth,
                              v + topBorder + fillerHeight, rightBorder, bottomBorder, zLevel);

        for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
            // Top Border
            drawTexturedModalRect(poseStack, x + leftBorder + i * fillerWidth, y, u + leftBorder, v, i == xPasses ? remainderWidth : fillerWidth,
                                  topBorder, zLevel);
            // Bottom Border
            drawTexturedModalRect(poseStack, x + leftBorder + i * fillerWidth, y + topBorder + canvasHeight, u + leftBorder,
                                  v + topBorder + fillerHeight, i == xPasses ? remainderWidth : fillerWidth, bottomBorder, zLevel);

            // Throw in some filler for good measure
            for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
                drawTexturedModalRect(poseStack, x + leftBorder + i * fillerWidth, y + topBorder + j * fillerHeight, u + leftBorder,
                                      v + topBorder, i == xPasses ? remainderWidth : fillerWidth, j == yPasses ? remainderHeight : fillerHeight,
                                      zLevel);
            }
        }

        // Side Borders
        for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
            // Left Border
            drawTexturedModalRect(poseStack, x, y + topBorder + j * fillerHeight, u, v + topBorder, leftBorder,
                                  j == yPasses ? remainderHeight : fillerHeight, zLevel);
            // Right Border
            drawTexturedModalRect(poseStack, x + leftBorder + canvasWidth, y + topBorder + j * fillerHeight, u + leftBorder + fillerWidth,
                                  v + topBorder, rightBorder, j == yPasses ? remainderHeight : fillerHeight, zLevel);
        }
    }

    /**
     * Draws a textured box of any size (smallest size is borderSize * 2 square) based on a fixed size textured box with continuous borders
     * and filler. The provided ResourceLocation object will be bound using
     * Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation).
     *
     * @param poseStack     the gui pose stack
     * @param res           the ResourceLocation object that contains the desired image
     * @param x             x axis offset
     * @param y             y axis offset
     * @param u             bound resource location image x offset
     * @param v             bound resource location image y offset
     * @param width         the desired box width
     * @param height        the desired box height
     * @param textureWidth  the width of the box texture in the resource location image
     * @param textureHeight the height of the box texture in the resource location image
     * @param topBorder     the size of the box's top border
     * @param bottomBorder  the size of the box's bottom border
     * @param leftBorder    the size of the box's left border
     * @param rightBorder   the size of the box's right border
     * @param zLevel        the zLevel to draw at
     */
    public static void drawContinuousTexturedBox(PoseStack poseStack,
                                                 ResourceLocation res,
                                                 int x,
                                                 int y,
                                                 int u,
                                                 int v,
                                                 int width,
                                                 int height,
                                                 int textureWidth,
                                                 int textureHeight,
                                                 int topBorder,
                                                 int bottomBorder,
                                                 int leftBorder,
                                                 int rightBorder,
                                                 float zLevel) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, res);
        drawContinuousTexturedBox(poseStack, x, y, u, v, width, height, textureWidth, textureHeight, topBorder, bottomBorder, leftBorder, rightBorder,
                                  zLevel);
    }

    public static void drawEntityOnScreen(int posX, int posY, float scale, float mouseX, float mouseY, LivingEntity entity) {
        renderEntityInInventory(posX, posY, scale, posX - mouseX, posY - 30 - mouseY, entity);
    }

    public static void drawRect(double x, double y, double x2, double y2, double width, int color) {
        drawRect(x, y, x2, y2, width, color, false);
    }

    public static void drawRect(double x, double y, double x2, double y2, double width, int color, boolean over) {
        if (y > y2) {
            double tempY = y;
            double tempX = x;
            y = y2;
            x = x2;
            y2 = tempY;
            x2 = tempX;
        }
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                       GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        setColor(color);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        boolean xHigh = x < x2;
        builder.vertex(x, xHigh ? y + width : y, over ? 1 : 0).endVertex();
        builder.vertex(x2, xHigh ? y2 + width : y2, over ? 1 : 0).endVertex();
        builder.vertex(x2 + width, xHigh ? y2 : y2 + width, over ? 1 : 0).endVertex();
        builder.vertex(x + width, xHigh ? y : y + width, over ? 1 : 0).endVertex();
        tessellator.end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawTexturedModalRect(PoseStack matrices, int x, int y, int u, int v, int width, int height, float zLevel) {
        final float uScale = 1.0f / 0x100;
        final float vScale = 1.0f / 0x100;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix = matrices.last().pose();
        builder.vertex(matrix, x, y + height, zLevel).uv(u * uScale, (v + height) * vScale).endVertex();
        builder.vertex(matrix, x + width, y + height, zLevel).uv((u + width) * uScale, (v + height) * vScale).endVertex();
        builder.vertex(matrix, x + width, y, zLevel).uv((u + width) * uScale, v * vScale).endVertex();
        builder.vertex(matrix, x, y, zLevel).uv(u * uScale, v * vScale).endVertex();
        tessellator.end();
    }

    public static void enableScissor(int x1, int y1, int x2, int y2) {
        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        double x = x1 * scale;
        double y = window.getScreenHeight() - y2 * scale;
        double width = (x2 - x1) * scale;
        double height = (y2 - y1) * scale;
        RenderSystem.enableScissor((int) x, (int) y, Math.max(0, (int) width), Math.max(0, (int) height));
    }

    public static void endBlitBatch() {
        if (!inBlitBatch) {
            throw new IllegalStateException("Not in blit batch!");
        }
        assert builder != null;
        builder.end();
        BufferUploader.end(builder);
        builder = null;
        inBlitBatch = false;
    }

    public static void endFillBatch() {
        if (!inFillBatch) {
            throw new IllegalStateException("Not in fill batch!");
        }
        assert builder != null;
        builder.end();
        BufferUploader.end(builder);
        builder = null;
        inFillBatch = false;
    }

    public static void fill(Matrix4f matrix, int x0, int y1, int x1, int y0, int color, boolean over) {
        if (x0 < x1) {
            int temp = x0;
            x0 = x1;
            x1 = temp;
        }
        if (y1 < y0) {
            int temp = y1;
            y1 = y0;
            y0 = temp;
        }
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        float a = (color >> 24 & 255) / 255.0F;
        builder.vertex(matrix, x0, y0, over ? 0.1f : 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x1, y0, over ? 0.1f : 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x1, y1, over ? 0.1f : 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x0, y1, over ? 0.1f : 0).color(r, g, b, a).endVertex();
        builder.end();
        BufferUploader.end(builder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void fillInBatch(Matrix4f matrix, int minX, int minY, int maxX, int maxY, int color) {
        if (!inFillBatch) {
            throw new IllegalStateException("Not in fill batch!");
        }
        innerFillInBatch(matrix, minX, minY, maxX, maxY, color);
    }

    public static void floatBlit(PoseStack matrixStack,
                                 float x,
                                 float y,
                                 int blitOffset,
                                 float uOffset,
                                 float vOffset,
                                 int uWidth,
                                 int vHeight,
                                 int textureHeight,
                                 int textureWidth) {
        innerFloatBlit(matrixStack, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    public static @Nullable LivingEntity getEntity(Level level, EntityType<?> type) {
        LivingEntity entity = (LivingEntity) type.create(level);
        if (entity instanceof Mob mob) {
            try {
                //noinspection ConstantConditions
                mob.finalizeSpawn(null, difficulty, MobSpawnType.NATURAL, null, null);
            }
            catch (Throwable ignored) {
            }
        }
        return entity;
    }

    public static float getEntityScale(LivingEntity entity, float baseScale, float targetHeight, float targetWidth) {
        if (entity.getType() == EntityType.ENDER_DRAGON) {
            targetWidth *= 3;
            targetHeight *= 2;
        }
        else if (entity.getType() == EntityType.SQUID) {
            targetHeight /= 2.5;
        }
        return Math.min(targetWidth / entity.getBbWidth(), targetHeight / entity.getBbHeight()) * baseScale;
    }

    public static void hLine(PoseStack matrices, int x0, int x1, int y, int color, boolean over) {
        if (x1 < x0) {
            int temp = x0;
            x0 = x1;
            x1 = temp;
        }
        fill(matrices.last().pose(), x0, y, x1 + 1, y + 1, color, over);
    }

    private static void innerBlitInBatch(Matrix4f matrix,
                                         int x1,
                                         int x2,
                                         int y1,
                                         int y2,
                                         int blitOffset,
                                         float u0,
                                         float u1,
                                         float v0,
                                         float v1) {
        assert builder != null;
        builder.vertex(matrix, x1, y2, blitOffset).uv(u0, v1).endVertex();
        builder.vertex(matrix, x2, y2, blitOffset).uv(u1, v1).endVertex();
        builder.vertex(matrix, x2, y1, blitOffset).uv(u1, v0).endVertex();
        builder.vertex(matrix, x1, y1, blitOffset).uv(u0, v0).endVertex();
    }

    private static void innerBlitInBatch(Matrix4f matrix,
                                         int x1,
                                         int x2,
                                         int y1,
                                         int y2,
                                         int blitOffset,
                                         int uWidth,
                                         int vHeight,
                                         float uOffset,
                                         float vOffset,
                                         int texWidth,
                                         int texHeight) {
        innerBlitInBatch(matrix, x1, x2, y1, y2, blitOffset, uOffset / texWidth, (uOffset + uWidth) / texWidth, vOffset / texHeight,
                         (vOffset + vHeight) / texHeight);
    }

    private static void innerFillInBatch(Matrix4f matrix, int minX, int minY, int maxX, int maxY, int color) {
        if (minX < maxX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }
        if (minY < maxY) {
            int j = minY;
            minY = maxY;
            maxY = j;
        }
        int a = color >> 24 & 255;
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        assert builder != null;
        builder.vertex(matrix, minX, maxY, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, maxY, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, minY, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, minX, minY, 0).color(r, g, b, a).endVertex();
    }

    private static void innerFloatBlit(PoseStack matrixStack,
                                       float x1,
                                       float x2,
                                       float y1,
                                       float y2,
                                       int blitOffset,
                                       int uWidth,
                                       int vHeight,
                                       float uOffset,
                                       float vOffset,
                                       int textureWidth,
                                       int textureHeight) {
        innerFloatBlit(matrixStack.last().pose(), x1, x2, y1, y2, blitOffset, uOffset / textureWidth, (uOffset + uWidth) / textureWidth,
                       vOffset / textureHeight, (vOffset + vHeight) / textureHeight);
    }

    public static void innerFloatBlit(Matrix4f matrix,
                                      float x1,
                                      float x2,
                                      float y1,
                                      float y2,
                                      int blitOffset,
                                      float minU,
                                      float maxU,
                                      float minV,
                                      float maxV) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x1, y2, blitOffset).uv(minU, maxV).endVertex();
        builder.vertex(matrix, x2, y2, blitOffset).uv(maxU, maxV).endVertex();
        builder.vertex(matrix, x2, y1, blitOffset).uv(maxU, minV).endVertex();
        builder.vertex(matrix, x1, y1, blitOffset).uv(minU, minV).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    public static void renderAndDecorateFakeItemLighting(ItemRenderer renderer, ItemStack stack, int x, int y, int packedLight) {
        renderItemIntoGUI(renderer, null, stack, x, y, packedLight);
    }

    public static void renderEntityInInventory(int posX, int posY, float scale, float mouseX, float mouseY, LivingEntity entity) {
        float x = (float) Math.atan(mouseX / 40.0F);
        float y = (float) Math.atan(mouseY / 40.0F);
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.pushPose();
        internalMat.translate(posX, posY, 1_050);
        internalMat.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack matrices = MATRICES;
        matrices.reset();
        matrices.translate(0, 0, 1_000);
        matrices.scale(scale, scale, scale);
        Quaternion zRot = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion xRot = Vector3f.XP.rotationDegrees(y * 20.0F);
        zRot.mul(xRot);
        matrices.mulPose(zRot);
        float bodyYaw = entity.yBodyRot;
        float yaw = entity.getYRot();
        float pitch = entity.getXRot();
        float headYaw0 = entity.yHeadRotO;
        float headYaw = entity.yHeadRot;
        entity.yBodyRot = 180.0F + x * 20.0F;
        entity.setYRot(180.0F + x * 40.0F);
        entity.setXRot(-y * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        xRot.conj();
        dispatcher.overrideCameraOrientation(xRot);
        dispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0, 0, 0, 0.0F, 1.0F, matrices, bufferSource, 0xf0_00f0));
        bufferSource.endBatch();
        dispatcher.setRenderShadow(true);
        entity.yBodyRot = bodyYaw;
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yHeadRotO = headYaw0;
        entity.yHeadRot = headYaw;
        internalMat.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    public static void renderFire(int width, int height, PoseStack matrices) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR_TEX);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        TextureAtlasSprite sprite = ModelBakery.FIRE_1.sprite();
        RenderSystem.setShaderTexture(0, sprite.atlas().location());
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float avgU = (u0 + u1) / 2.0F;
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float avgV = (v0 + v1) / 2.0F;
        float shrinkRatio = sprite.uvShrinkRatio();
        float actU0 = Mth.lerp(shrinkRatio, u0, avgU);
        float actU1 = Mth.lerp(shrinkRatio, u1, avgU);
        float actV0 = Mth.lerp(shrinkRatio, v0, avgV);
        float actV1 = Mth.lerp(shrinkRatio, v1, avgV);
        float max = width * 0.5f;
        float x0 = 0;
        float y0 = (height - max) * 0.25f;
        y0 += height - (max + y0);
        float limit = width * 0.125f;
        y0 += limit;
        float y1 = height + limit;
        Matrix4f matrix = matrices.last().pose();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        for (int i = 0; i < 2; ++i) {
            builder.vertex(matrix, x0, y1, 0).color(1.0F, 1.0F, 1.0F, 0.8F).uv(actU1, actV1).endVertex();
            builder.vertex(matrix, max + x0, y1, 0).color(1.0F, 1.0F, 1.0F, 0.8F).uv(actU0, actV1).endVertex();
            builder.vertex(matrix, max + x0, y0, 0).color(1.0F, 1.0F, 1.0F, 0.8F).uv(actU0, actV0).endVertex();
            builder.vertex(matrix, x0, y0, 0).color(1.0F, 1.0F, 1.0F, 0.8F).uv(actU1, actV0).endVertex();
            x0 = max;
            float temp = actU1;
            actU1 = actU0;
            actU0 = temp;
        }
        builder.end();
        BufferUploader.end(builder);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    private static void renderGuiItem(ItemRenderer renderer, ItemStack stack, int x, int y, BakedModel model, int packedLight) {
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.pushPose();
        internalMat.translate(x, y, 100 + renderer.blitOffset);
        internalMat.translate(8, 8, 0);
        internalMat.scale(1.0F, -1.0F, 1.0F);
        internalMat.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack matrices = MATRICES;
        matrices.reset();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean notBlockLight = !model.usesBlockLight();
        if (notBlockLight) {
            Lighting.setupForFlatItems();
        }
        renderer.render(stack, ItemTransforms.TransformType.GUI, false, matrices, buffer, packedLight, OverlayTexture.NO_OVERLAY, model);
        buffer.endBatch();
        RenderSystem.enableDepthTest();
        if (notBlockLight) {
            Lighting.setupFor3DItems();
        }
        internalMat.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private static void renderItemIntoGUI(ItemRenderer renderer, @Nullable LivingEntity entity, ItemStack stack, int x, int y, int packedLight) {
        if (!stack.isEmpty()) {
            BakedModel model = renderer.getModel(stack, null, entity, 0);
            renderer.blitOffset += 50.0F;
            try {
                renderGuiItem(renderer, stack, x, y, model, packedLight);
            }
            catch (Throwable throwable) {
                CrashReport report = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashReportCategory = report.addCategory("Item being rendered");
                crashReportCategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashReportCategory.setDetail("Item Damage", () -> String.valueOf(stack.getDamageValue()));
                crashReportCategory.setDetail("Item NBT", () -> String.valueOf(stack.getTag()));
                crashReportCategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(report);
            }
            renderer.blitOffset -= 50.0F;
        }
    }

    public static void renderRepeating(PoseStack matrices,
                                       GuiComponent abstractGui,
                                       int x,
                                       int y,
                                       int width,
                                       int height,
                                       int textureX,
                                       int textureY,
                                       int textureWidth,
                                       int textureHeight) {
        for (int i = 0; i < width; i += textureWidth) {
            int drawX = x + i;
            int drawWidth = Math.min(textureWidth, width - i);
            for (int l = 0; l < height; l += textureHeight) {
                int drawY = y + l;
                int drawHeight = Math.min(textureHeight, height - l);
                abstractGui.blit(matrices, drawX, drawY, textureX, textureY, drawWidth, drawHeight);
            }
        }
    }

    public static void renderTex(int width, int height, TextureAtlasSprite texture, PoseStack matrices) {
        RenderSystem.setShaderTexture(0, texture.atlas().location());
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR_TEX);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        float u0 = texture.getU0();
        float u1 = texture.getU1();
        float v0 = texture.getV0();
        float v1 = texture.getV1();
        int max;
        float x0;
        float y0;
        if (width == height) {
            max = width;
            x0 = 0;
            y0 = 0;
        }
        else if (width > height) {
            max = width;
            x0 = 0;
            y0 = (height - max) * 0.5f;
        }
        else {
            max = height;
            x0 = (width - max) * 0.5f;
            y0 = 0;
        }
        Matrix4f matrix = matrices.last().pose();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        builder.vertex(matrix, x0, max + y0, 0).color(0.1F, 0.1F, 0.1F, 1.0F).uv(u1, v1).endVertex();
        builder.vertex(matrix, max + x0, max + y0, 0).color(0.1F, 0.1F, 0.1F, 1.0F).uv(u0, v1).endVertex();
        builder.vertex(matrix, max + x0, y0, 0).color(0.1F, 0.1F, 0.1F, 1.0F).uv(u0, v0).endVertex();
        builder.vertex(matrix, x0, y0, 0).color(0.1F, 0.1F, 0.1F, 1.0F).uv(u1, v0).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    public static void renderWater(int width, int height, Entity entity, PoseStack matrices) {
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
        RenderSystem.enableTexture();
        RenderSystem.setShaderTexture(0, UNDERWATER_LOCATION);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        float brightness = entity.getBrightness();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(brightness, brightness, brightness, 0.2F);
        float yRot = -entity.getYRot() / 64.0F;
        float xRot = entity.getXRot() / 64.0F;
        int max;
        float x0;
        float y0;
        if (width == height) {
            max = width;
            x0 = 0;
            y0 = 0;
        }
        else if (width > height) {
            max = width;
            x0 = 0;
            y0 = (height - max) * 0.5f;
        }
        else {
            max = height;
            x0 = (width - max) * 0.5f;
            y0 = 0;
        }
        Matrix4f matrix = matrices.last().pose();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x0, max + y0, 0).uv(4.0F + yRot, 4.0F + xRot).endVertex();
        builder.vertex(matrix, max + x0, max + y0, 0).uv(0.0F + yRot, 4.0F + xRot).endVertex();
        builder.vertex(matrix, max + x0, y0, 0).uv(0.0F + yRot, 0.0F + xRot).endVertex();
        builder.vertex(matrix, x0, y0, 0).uv(4.0F + yRot, 0.0F + xRot).endVertex();
        builder.end();
        BufferUploader.end(builder);
        RenderSystem.disableBlend();
    }

    public static void setColor(int color) {
        RenderSystem.setShaderColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0f);
    }

    public static void setDifficulty(@Nullable BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            LevelData levelData = level.getLevelData();
            difficulty = level.getCurrentDifficultyAt(
                    pos == null ? new BlockPos(levelData.getXSpawn(), levelData.getYSpawn(), levelData.getZSpawn()) : pos);
        }
    }

    public static void startBlitBatch(BufferBuilder bufferBuilder) {
        if (inBlitBatch) {
            throw new IllegalStateException("Already in blit batch! Only one batch at a time!");
        }
        if (inFillBatch) {
            throw new IllegalStateException("Already in fill batch! Only one batch at a time!");
        }
        builder = bufferBuilder;
        inBlitBatch = true;
        RenderSystem.enableTexture();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    }

    public static void startFillBatch(BufferBuilder bufferBuilder) {
        if (inFillBatch) {
            throw new IllegalStateException("Already in fill batch! Only one batch at a time!");
        }
        if (inBlitBatch) {
            throw new IllegalStateException("Already in blit batch! Only one batch at a time!");
        }
        builder = bufferBuilder;
        inFillBatch = true;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

    public static void vLine(PoseStack matrices, int x, int y0, int y1, int color, boolean over) {
        if (y1 < y0) {
            int temp = y0;
            y0 = y1;
            y1 = temp;
        }
        fill(matrices.last().pose(), x, y0 + 1, x + 1, y1, color, over);
    }
}
