package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.renderer.RenderHelper;

@OnlyIn(Dist.CLIENT)
public final class GUIUtils {

    private static DifficultyInstance difficulty = new DifficultyInstance(Difficulty.NORMAL, 0, 0, 0);

    static {
        setDifficulty(null);
    }

    private GUIUtils() {
    }

    public static void drawCenteredStringNoShadow(PoseStack matrices, Font font, Component text, float xCentre, float y, int color) {
        FormattedCharSequence charSequence = text.getVisualOrderText();
        font.draw(matrices, charSequence, xCentre - font.width(charSequence) / 2.0f, y, color);
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity entity) {
        InventoryScreen.renderEntityInInventory(posX, posY, scale, posX - mouseX, posY - 30 - mouseY, entity);
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

    @Nullable
    public static LivingEntity getEntity(Level level, EntityType<?> type) {
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

    public static int getEntityScale(LivingEntity entity, float baseScale, float targetHeight, float targetWidth) {
        if (entity.getType() == EntityType.ENDER_DRAGON) {
            targetWidth *= 3;
            targetHeight *= 2;
        }
        else if (entity.getType() == EntityType.SQUID) {
            targetHeight /= 2.5;
        }
        return (int) (Math.min(targetWidth / entity.getBbWidth(), targetHeight / entity.getBbHeight()) * baseScale);
    }

    public static void hLine(PoseStack matrices, int x0, int x1, int y, int color, boolean over) {
        if (x1 < x0) {
            int temp = x0;
            x0 = x1;
            x1 = temp;
        }
        fill(matrices.last().pose(), x0, y, x1 + 1, y + 1, color, over);
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

    private static void renderGuiItem(ItemRenderer renderer, ItemStack stack, int x, int y, BakedModel model, int packedLight) {
        Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
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
        PoseStack matrices = new PoseStack();
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
                crashReportCategory.setDetail("Registry Name", () -> String.valueOf(stack.getItem().getRegistryName()));
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

    public static void vLine(PoseStack matrices, int x, int y0, int y1, int color, boolean over) {
        if (y1 < y0) {
            int temp = y0;
            y0 = y1;
            y1 = temp;
        }
        fill(matrices.last().pose(), x, y0 + 1, x + 1, y1, color, over);
    }
}
