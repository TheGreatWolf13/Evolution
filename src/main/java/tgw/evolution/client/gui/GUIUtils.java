package tgw.evolution.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.DirectionUtil;
import tgw.evolution.util.XoRoShiRoRandom;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class GUIUtils {

    private static final ShaderGroup SHADER_GROUP;
    private static final XoRoShiRoRandom RANDOM = new XoRoShiRoRandom();
    private static DifficultyInstance difficulty;

    static {
        ShaderGroup shader;
        try {
            shader = new ShaderGroup(Minecraft.getInstance().getTextureManager(),
                                     Minecraft.getInstance().getResourceManager(),
                                     Minecraft.getInstance().getMainRenderTarget(),
                                     EvolutionResources.SHADER_DESATURATE_75);
        }
        catch (IOException e) {
            shader = null;
        }
        SHADER_GROUP = shader;
    }

    static {
        setDifficulty(null);
    }

    private GUIUtils() {
    }

    public static void drawCenteredStringNoShadow(MatrixStack matrices, FontRenderer font, ITextComponent text, float xCentre, float y, int color) {
        IReorderingProcessor ireorderingprocessor = text.getVisualOrderText();
        font.draw(matrices, ireorderingprocessor, xCentre - font.width(ireorderingprocessor) / 2.0f, y, color);
    }

    public static void drawEntityOnScreen(int posX, int posY, float scale, float mouseX, float mouseY, LivingEntity entity) {
        mouseX = posX - mouseX;
        mouseY = posY - 45 - mouseY;
        float atanMouseX = (float) Math.atan(mouseX / 40);
        float atanMouseY = (float) Math.atan(mouseY / 40);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(posX, posY, 1_050.0F);
        RenderSystem.scalef(1.0f, 1.0f, -1.0f);
        MatrixStack matrices = new MatrixStack();
        matrices.translate(0, 0, 1_000);
        matrices.scale(scale, scale, scale);
        Quaternion quatZ = Vector3f.ZP.rotationDegrees(0.0F);
        Quaternion quatX = Vector3f.XP.rotationDegrees(atanMouseY * 20.0F);
        quatZ.mul(quatX);
        matrices.mulPose(quatZ);
        RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float oldYawOffset = entity.yBodyRot;
        float oldYaw = entity.yRot;
        float oldPitch = entity.xRot;
        float oldPrevYawHead = entity.yHeadRotO;
        float oldYawHead = entity.yHeadRot;
        boolean needsTurning = needsTurning(entity);
        entity.yBodyRot = atanMouseX * 20.0F + (needsTurning ? 0 : 180);
        entity.yRot = atanMouseX * 40.0F + 180;
        if (entity.getType() == EntityType.ENDER_DRAGON) {
            matrices.mulPose(Vector3f.YP.rotationDegrees(-atanMouseX * 20.0F));
        }
        entity.xRot = -atanMouseY * 20.0F;
        entity.yHeadRot = entity.yRot + (needsTurning ? 180 : 0);
        entity.yHeadRotO = entity.yRot;
        EntityRendererManager renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        quatX.conj();
        renderManager.overrideCameraOrientation(quatX);
        renderManager.setRenderShadow(false);
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> renderManager.render(entity, 0, 0, 0, 0.0f, 1.0F, matrices, buffer, 0xf0_00f0));
        buffer.endBatch();
        renderManager.setRenderShadow(true);
        entity.yBodyRot = oldYawOffset;
        entity.yRot = oldYaw;
        entity.xRot = oldPitch;
        entity.yHeadRotO = oldPrevYawHead;
        entity.yHeadRot = oldYawHead;
        RenderSystem.popMatrix();
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
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                       GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                       GlStateManager.SourceFactor.ONE,
                                       GlStateManager.DestFactor.ZERO);
        setColor(color);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        boolean xHigh = x < x2;
        builder.vertex(x, xHigh ? y + width : y, over ? 1 : 0).endVertex();
        builder.vertex(x2, xHigh ? y2 + width : y2, over ? 1 : 0).endVertex();
        builder.vertex(x2 + width, xHigh ? y2 : y2 + width, over ? 1 : 0).endVertex();
        builder.vertex(x + width, xHigh ? y : y + width, over ? 1 : 0).endVertex();
        tessellator.end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
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
        BufferBuilder builder = Tessellator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        float a = (color >> 24 & 255) / 255.0F;
        builder.vertex(matrix, x0, y0, over ? 0.1f : 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x1, y0, over ? 0.1f : 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x1, y1, over ? 0.1f : 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x0, y1, over ? 0.1f : 0).color(r, g, b, a).endVertex();
        builder.end();
        WorldVertexBufferUploader.end(builder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void floatBlit(MatrixStack matrixStack,
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

    public static LivingEntity getEntity(World world, EntityType<?> type) {
        LivingEntity entity = (LivingEntity) type.create(world);
        if (entity instanceof MobEntity) {
            try {
                ((MobEntity) entity).finalizeSpawn(null, difficulty, SpawnReason.NATURAL, null, null);
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

    public static void hLine(MatrixStack matrices, int x0, int x1, int y, int color) {
        hLine(matrices, x0, x1, y, color, false);
    }

    public static void hLine(MatrixStack matrices, int x0, int x1, int y, int color, boolean over) {
        if (x1 < x0) {
            int temp = x0;
            x0 = x1;
            x1 = temp;
        }
        fill(matrices.last().pose(), x0, y, x1 + 1, y + 1, color, over);
    }

    private static void innerFloatBlit(MatrixStack matrixStack,
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
        innerFloatBlit(matrixStack.last().pose(),
                       x1,
                       x2,
                       y1,
                       y2,
                       blitOffset,
                       uOffset / textureWidth,
                       (uOffset + uWidth) / textureWidth,
                       vOffset / textureHeight,
                       (vOffset + vHeight) / textureHeight);
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
        BufferBuilder builder = Tessellator.getInstance().getBuilder();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        builder.vertex(matrix, x1, y2, blitOffset).uv(minU, maxV).endVertex();
        builder.vertex(matrix, x2, y2, blitOffset).uv(maxU, maxV).endVertex();
        builder.vertex(matrix, x2, y1, blitOffset).uv(maxU, minV).endVertex();
        builder.vertex(matrix, x1, y1, blitOffset).uv(minU, minV).endVertex();
        builder.end();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.end(builder);
    }

    private static boolean needsTurning(LivingEntity entity) {
        return entity.getType() == EntityType.BAT;
    }

    public static void renderItemAndEffectIntoGuiWithoutEntity(ItemRenderer renderer, ItemStack stack, int x, int y, int packedLight) {
        renderItemIntoGUI(renderer, null, stack, x, y, packedLight);
    }

    public static void renderItemGreyscaled(ItemStack stack, IBakedModel model) {
        if (!stack.isEmpty()) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef(-0.5f, -0.5f, -0.5f);
            if (model.isCustomRenderer()) {
                MatrixStack matrices = new MatrixStack();
                IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                stack.getItem()
                     .getItemStackTileEntityRenderer()
                     .renderByItem(stack, ItemCameraTransforms.TransformType.GUI, matrices, buffer, 0xf0_00f0, OverlayTexture.NO_OVERLAY);
                buffer.endBatch();
            }
            else {
                MatrixStack matrices = new MatrixStack();
                IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                RenderType renderType = RenderTypeLookup.getRenderType(stack, true);
                IVertexBuilder modelBuffer = ItemRenderer.getFoilBuffer(buffer, renderType, true, stack.hasFoil());
                renderModelGreyscaled(matrices, modelBuffer, model, 0xf0_00f0, OverlayTexture.NO_OVERLAY);
                buffer.endBatch();
            }
            RenderSystem.popMatrix();
        }
    }

    private static void renderItemIntoGUI(ItemRenderer renderer,
                                          @Nullable LivingEntity livingEntity,
                                          ItemStack stack,
                                          int x,
                                          int y,
                                          int packedLight) {
        if (!stack.isEmpty()) {
            renderer.blitOffset += 50.0F;
            try {
                renderItemModelIntoGUI(renderer, stack, x, y, renderer.getModel(stack, null, livingEntity), packedLight);
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
                crashReportCategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashReportCategory.setDetail("Registry Name", () -> String.valueOf(stack.getItem().getRegistryName()));
                crashReportCategory.setDetail("Item Damage", () -> String.valueOf(stack.getDamageValue()));
                crashReportCategory.setDetail("Item NBT", () -> String.valueOf(stack.getTag()));
                crashReportCategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashReport);
            }
            renderer.blitOffset -= 50.0F;
        }
    }

    private static void renderItemModelIntoGUI(ItemRenderer renderer, ItemStack stack, int x, int y, IBakedModel model, int packedLight) {
        RenderSystem.pushMatrix();
        Minecraft.getInstance().textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
        Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS).setBlurMipmap(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.translatef(x, y, 100.0F + renderer.blitOffset);
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);
        MatrixStack matrices = new MatrixStack();
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !model.usesBlockLight();
        if (flag) {
            RenderHelper.setupForFlatItems();
        }
        renderer.render(stack, ItemCameraTransforms.TransformType.GUI, false, matrices, buffer, packedLight, OverlayTexture.NO_OVERLAY, model);
        buffer.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            RenderHelper.setupFor3DItems();
        }
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
    }

    private static void renderModelGreyscaled(MatrixStack matrices, IVertexBuilder buffer, IBakedModel model, int packedLight, int packedOverlay) {
        renderModelGreyscaled(matrices, buffer, model, packedLight, packedOverlay, 0x6666_6666);
    }

    private static void renderModelGreyscaled(MatrixStack matrices,
                                              IVertexBuilder buffer,
                                              IBakedModel model,
                                              int packedLight,
                                              int packedOverlay,
                                              int color) {
        for (Direction direction : DirectionUtil.ALL) {
            renderQuads(matrices, buffer, model.getQuads(null, direction, RANDOM.setSeedAndReturn(42L)), packedLight, packedOverlay, color);
        }
        renderQuads(matrices, buffer, model.getQuads(null, null, RANDOM.setSeedAndReturn(42L)), packedLight, packedOverlay, color);
    }

    private static void renderQuads(MatrixStack matrixStack,
                                    IVertexBuilder buffer,
                                    List<BakedQuad> quads,
                                    int packedLight,
                                    int packedOverlay,
                                    int color) {
        MatrixStack.Entry matrixEntry = matrixStack.last();
        for (BakedQuad bakedQuad : quads) {
            float red = (color >> 16 & 255) / 255.0F;
            float green = (color >> 8 & 255) / 255.0F;
            float blue = (color & 255) / 255.0F;
            buffer.addVertexData(matrixEntry, bakedQuad, red, green, blue, packedLight, packedOverlay, true);
        }
    }

    public static void renderRepeating(MatrixStack matrices,
                                       AbstractGui abstractGui,
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

    public static void setColor(int color) {
        RenderSystem.color3f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
    }

    public static void setDifficulty(@Nullable BlockPos pos) {
        World world = Minecraft.getInstance().level;
        if (world != null) {
            IWorldInfo worldInfo = world.getLevelData();
            difficulty = world.getCurrentDifficultyAt(pos == null ?
                                                      new BlockPos(worldInfo.getXSpawn(), worldInfo.getYSpawn(), worldInfo.getZSpawn()) :
                                                      pos);
        }
        else {
            difficulty = new DifficultyInstance(Difficulty.NORMAL, 0, 0, 0);
        }
    }

    public static void stopUseShader() {
        if (SHADER_GROUP != null) {
            SHADER_GROUP.close();
        }
    }

    public static void vLine(MatrixStack matrices, int x, int y0, int y1, int color) {
        vLine(matrices, x, y0, y1, color, false);
    }

    public static void vLine(MatrixStack matrices, int x, int y0, int y1, int color, boolean over) {
        if (y1 < y0) {
            int temp = y0;
            y0 = y1;
            y1 = temp;
        }
        fill(matrices.last().pose(), x, y0 + 1, x + 1, y1, color, over);
    }
}
