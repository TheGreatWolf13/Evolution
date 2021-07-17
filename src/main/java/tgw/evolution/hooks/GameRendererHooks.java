package tgw.evolution.hooks;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public final class GameRendererHooks {

    private GameRendererHooks() {
    }

    @EvolutionHook
    public static void drawNameplate(FontRenderer fontRenderer,
                                     String name,
                                     float x,
                                     float y,
                                     float z,
                                     int verticalShift,
                                     float viewerYaw,
                                     float viewerPitch,
                                     boolean isSneaking) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(x, y, z);
        GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(viewerPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scalef(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                         GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                         GlStateManager.SourceFactor.ONE,
                                         GlStateManager.DestFactor.ZERO);
        int nameMiddle = fontRenderer.getStringWidth(name) / 2;
        GlStateManager.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        float acessibilityTextBackground = Minecraft.getInstance().gameSettings.func_216840_a(0.25F);
        builder.pos(-nameMiddle - 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, acessibilityTextBackground).endVertex();
        builder.pos(-nameMiddle - 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, acessibilityTextBackground).endVertex();
        builder.pos(nameMiddle + 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, acessibilityTextBackground).endVertex();
        builder.pos(nameMiddle + 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, acessibilityTextBackground).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture();
        GlStateManager.depthMask(true);
        fontRenderer.drawString(name, -fontRenderer.getStringWidth(name) / 2.0f, verticalShift, isSneaking ? 0x20ff_ffff : 0xffff_ffff);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
