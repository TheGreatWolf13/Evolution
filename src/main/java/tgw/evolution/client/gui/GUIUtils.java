package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;
import tgw.evolution.init.EvolutionResources;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public final class GUIUtils {

    private static final Vector3f LIGHT0_POS = func_216509_a(0.2F, 1.0F, -0.7F);
    private static final Vector3f LIGHT1_POS = func_216509_a(-0.2F, 1.0F, 0.7F);
    private static final ShaderGroup SHADER_GROUP;

    static {
        ShaderGroup shader;
        try {
            shader = new ShaderGroup(Minecraft.getInstance().getTextureManager(),
                                     Minecraft.getInstance().getResourceManager(),
                                     Minecraft.getInstance().getFramebuffer(),
                                     EvolutionResources.SHADER_DESATURATE_75);
        }
        catch (IOException e) {
            shader = null;
        }
        SHADER_GROUP = shader;
    }

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
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                         GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                         GlStateManager.SourceFactor.ONE,
                                         GlStateManager.DestFactor.ZERO);
        setColor(color);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        boolean xHigh = x < x2;
        bufferbuilder.pos(x, xHigh ? y + width : y, over ? 1 : 0).endVertex();
        bufferbuilder.pos(x2, xHigh ? y2 + width : y2, over ? 1 : 0).endVertex();
        bufferbuilder.pos(x2 + width, xHigh ? y2 : y2 + width, over ? 1 : 0).endVertex();
        bufferbuilder.pos(x + width, xHigh ? y : y + width, over ? 1 : 0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public static void fill(int x0, int y1, int x1, int y0, int color, boolean over) {
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
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                         GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                         GlStateManager.SourceFactor.ONE,
                                         GlStateManager.DestFactor.ZERO);
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        float a = (color >> 24 & 255) / 255.0F;
        GlStateManager.color4f(r, g, b, a);
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x0, y0, over ? 0.1 : 0).endVertex();
        bufferbuilder.pos(x1, y0, over ? 0.1 : 0).endVertex();
        bufferbuilder.pos(x1, y1, over ? 0.1 : 0).endVertex();
        bufferbuilder.pos(x0, y1, over ? 0.1 : 0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    private static Vector3f func_216509_a(float p_216509_0_, float p_216509_1_, float p_216509_2_) {
        Vector3f vector3f = new Vector3f(p_216509_0_, p_216509_1_, p_216509_2_);
        vector3f.normalize();
        return vector3f;
    }

    public static void hLine(int x0, int x1, int y, int color) {
        hLine(x0, x1, y, color, false);
    }

    public static void hLine(int x0, int x1, int y, int color, boolean over) {
        if (x1 < x0) {
            int temp = x0;
            x0 = x1;
            x1 = temp;
        }
        fill(x0, y, x1 + 1, y + 1, color, over);
    }

    public static void renderItemAndEffectIntoGUIGreyscaled(ItemRenderer itemRenderer, @Nullable LivingEntity entity, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            itemRenderer.zLevel += 50.0F;
            try {
                renderItemModelIntoGUIGreyscaled(itemRenderer, stack, x, y, itemRenderer.getItemModelWithOverrides(stack, null, entity));
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Rendering item");
                CrashReportCategory crashReportCategory = crashReport.makeCategory("Item being rendered");
                crashReportCategory.func_189529_a("Item Type", () -> String.valueOf(stack.getItem()));
                crashReportCategory.func_189529_a("Registry Name", () -> String.valueOf(stack.getItem().getRegistryName()));
                crashReportCategory.func_189529_a("Item Damage", () -> String.valueOf(stack.getDamage()));
                crashReportCategory.func_189529_a("Item NBT", () -> String.valueOf(stack.getTag()));
                crashReportCategory.func_189529_a("Item Foil", () -> String.valueOf(stack.hasEffect()));
                throw new ReportedException(crashReport);
            }
            itemRenderer.zLevel -= 50.0F;
        }
    }

    public static void renderItemGreyscaled(ItemRenderer itemRenderer, ItemStack stack, IBakedModel model) {
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
            if (model.isBuiltInRenderer()) {
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
            }
            else {
                renderModelGreyscaled(itemRenderer, model, stack);
                if (stack.hasEffect()) {
                    ItemRenderer.renderEffect(Minecraft.getInstance().textureManager,
                                              () -> renderModelGreyscaled(itemRenderer, model, 0x6666_6666),
                                              8);
                }
            }
            GlStateManager.popMatrix();
        }
    }

    protected static void renderItemModelIntoGUIGreyscaled(ItemRenderer itemRenderer, ItemStack stack, int x, int y, IBakedModel bakedmodel) {
        GlStateManager.pushMatrix();
        Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        setupGuiTransform(itemRenderer, x, y, bakedmodel.isGui3d());
        bakedmodel = ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
        renderItemGreyscaled(itemRenderer, stack, bakedmodel);
        GlStateManager.disableAlphaTest();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }

    private static void renderModelGreyscaled(ItemRenderer itemRenderer, IBakedModel model, ItemStack stack) {
        renderModelGreyscaled(itemRenderer, model, 0x6666_6666, stack);
    }

    private static void renderModelGreyscaled(ItemRenderer itemRenderer, IBakedModel model, int color) {
        renderModelGreyscaled(itemRenderer, model, color, ItemStack.EMPTY);
    }

    private static void renderModelGreyscaled(ItemRenderer itemRenderer, IBakedModel model, int color, ItemStack stack) {
        if (ForgeConfig.CLIENT.allowEmissiveItems.get()) {
            ForgeHooksClient.renderLitItem(itemRenderer, model, color, stack);
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        Random random = new Random();
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            itemRenderer.renderQuads(bufferbuilder, model.getQuads(null, direction, random), color, stack);
        }
        random.setSeed(42L);
        itemRenderer.renderQuads(bufferbuilder, model.getQuads(null, null, random), color, stack);
        tessellator.draw();
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

    public static void renderTooltip(Screen screen, List<String> tooltips, int mouseX, int mouseY) {
        GuiUtils.drawHoveringText(tooltips, mouseX, mouseY, screen.width, screen.height, -1, Minecraft.getInstance().fontRenderer);
    }

    public static void renderTooltip(Screen screen, List<String> tooltips, int mouseX, int mouseY, int textLimit) {
        GuiUtils.drawHoveringText(tooltips, mouseX, mouseY, screen.width, screen.height, textLimit, Minecraft.getInstance().fontRenderer);
    }

    public static void setColor(int color) {
        GlStateManager.color3f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
    }

    private static void setupGuiTransform(ItemRenderer itemRenderer, int xPosition, int yPosition, boolean isGui3d) {
        GlStateManager.translatef(xPosition, yPosition, 100.0F + itemRenderer.zLevel);
        GlStateManager.translatef(8.0F, 8.0F, 0.0F);
        GlStateManager.scalef(1.0F, -1.0F, 1.0F);
        GlStateManager.scalef(16.0F, 16.0F, 16.0F);
        if (isGui3d) {
            GlStateManager.enableLighting();
        }
        else {
            GlStateManager.disableLighting();
        }
    }

    public static void stopUseShader() {
        if (SHADER_GROUP != null) {
            SHADER_GROUP.close();
        }
    }

    public static void vLine(int x, int y0, int y1, int color) {
        vLine(x, y0, y1, color, false);
    }

    public static void vLine(int x, int y0, int y1, int color, boolean over) {
        if (y1 < y0) {
            int temp = y0;
            y0 = y1;
            y1 = temp;
        }
        fill(x, y0 + 1, x + 1, y1, color, over);
    }
}
