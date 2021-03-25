package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public final class GUIUtils {

    private GUIUtils() {
    }

    public static void drawCenteredString(FontRenderer font, String text, int width, int y, int color) {
        int textX = (width - font.getStringWidth(text)) / 2;
        font.drawString(text, textX, y, color);
    }

    public static void drawCenteredString(FontRenderer font, String text, int width, int y) {
        drawCenteredString(font, text, width, y, 0x40_4040);
    }

    public static void drawRect(double x, double y, double x2, double y2, double width, int color) {
        if (y > y2) {
            double tempY = y;
            double tempX = x;
            y = y2;
            x = x2;
            y2 = tempY;
            x2 = tempX;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                         GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                         GlStateManager.SourceFactor.ONE,
                                         GlStateManager.DestFactor.ZERO);
        setColor(color);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        boolean xHigh = false;
        if (x < x2) {
            xHigh = true;
        }
        bufferbuilder.pos(x, xHigh ? y + width : y, 0).endVertex();
        bufferbuilder.pos(x2, xHigh ? y2 + width : y2, 0).endVertex();
        bufferbuilder.pos(x2 + width, xHigh ? y2 : y2 + width, 0).endVertex();
        bufferbuilder.pos(x + width, xHigh ? y : y + width, 0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public static void renderRepeating(AbstractGui abstractGui,
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
                abstractGui.blit(drawX, drawY, textureX, textureY, drawWidth, drawHeight);
            }
        }
    }

    public static void setColor(int color) {
        GlStateManager.color3f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
    }
}
