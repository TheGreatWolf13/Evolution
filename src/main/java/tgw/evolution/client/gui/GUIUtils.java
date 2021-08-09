package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;
import tgw.evolution.init.EvolutionResources;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public final class GUIUtils {

    private static final Vector3f LIGHT0_POS = func_216509_a(0.2F, 1.0F, -0.7F);
    private static final Vector3f LIGHT1_POS = func_216509_a(-0.2F, 1.0F, 0.7F);
    private static final ShaderGroup SHADER_GROUP;
    private static DifficultyInstance difficulty;

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

    static {
        setDifficulty(null);
    }

    private GUIUtils() {
    }

    public static void drawCenteredSplitString(FontRenderer font, String text, int width, int y, int color, int wrapWidth) {
        for (String s : font.listFormattedStringToWidth(text, wrapWidth)) {
            drawCenteredString(font, s, width, y, color);
            y += 9;
        }
    }

    public static void drawCenteredString(FontRenderer font, String text, int width, int y, int color) {
        int textX = (width - font.getStringWidth(text)) / 2;
        font.drawString(text, textX, y, color);
    }

    public static void drawCenteredString(FontRenderer font, String text, int width, int y) {
        drawCenteredString(font, text, width, y, 0x40_4040);
    }

    public static void drawEntityOnScreen(int posX, int posY, float scale, float mouseX, float mouseY, LivingEntity entity) {
        mouseX = posX - mouseX;
        mouseY = posY - 45 - mouseY;
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepthTest();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translatef(posX, posY, 50.0F);
        GlStateManager.scalef(-scale, scale, scale);
        GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f5 = entity.prevRotationYawHead;
        float f6 = entity.rotationYawHead;
        GlStateManager.rotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(-((float) Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        boolean needsTurning = needsTurning(entity);
        entity.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F + (needsTurning ? 180 : 0);
        entity.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
        if (entity.getType() == EntityType.ENDER_DRAGON) {
            GlStateManager.rotatef(-(float) Math.atan(mouseX / 40.0F) * 20.0F + 180, 0.0f, 1.0f, 0.0f);
        }
        entity.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw + (needsTurning ? 180 : 0);
        entity.prevRotationYawHead = entity.rotationYaw;
        GlStateManager.translatef(0.0F, 0.0F, 0.0F);
        try {
            EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
            renderManager.setPlayerViewY(180.0F);
            renderManager.setRenderShadow(false);
            renderManager.renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
            renderManager.setRenderShadow(true);
        }
        finally {
            entity.renderYawOffset = f2;
            entity.rotationYaw = f3;
            entity.rotationPitch = f4;
            entity.prevRotationYawHead = f5;
            entity.rotationYawHead = f6;
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.activeTexture(GLX.GL_TEXTURE1);
            GlStateManager.disableTexture();
            GlStateManager.activeTexture(GLX.GL_TEXTURE0);
            GlStateManager.translatef(0.0F, 0.0F, 20.0F);
        }
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
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        boolean xHigh = x < x2;
        bufferbuilder.pos(x, xHigh ? y + width : y, over ? 1 : 0).endVertex();
        bufferbuilder.pos(x2, xHigh ? y2 + width : y2, over ? 1 : 0).endVertex();
        bufferbuilder.pos(x2 + width, xHigh ? y2 : y2 + width, over ? 1 : 0).endVertex();
        bufferbuilder.pos(x + width, xHigh ? y : y + width, over ? 1 : 0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public static void drawSplitStringWithShadow(FontRenderer font, String str, int x, int y, int wrapWidth, int textColor) {
        str = trimStringNewline(str);
        renderSplitStringWithShadow(font, str, x, y, wrapWidth, textColor);
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

    public static LivingEntity getEntity(World world, EntityType<?> type) {
        LivingEntity entity = (LivingEntity) type.create(world);
        if (entity instanceof MobEntity) {
            try {
                ((MobEntity) entity).onInitialSpawn(world, difficulty, SpawnReason.NATURAL, null, null);
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
        return Math.min(targetWidth / entity.getWidth(), targetHeight / entity.getHeight()) * baseScale;
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

    private static boolean needsTurning(LivingEntity entity) {
        return entity.getType() == EntityType.BAT;
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

    private static void renderItemModelIntoGUIGreyscaled(ItemRenderer itemRenderer, ItemStack stack, int x, int y, IBakedModel bakedmodel) {
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

    private static void renderSplitStringWithShadow(FontRenderer font, String str, int x, int y, int wrapWidth, int textColor) {
        for (String s : font.listFormattedStringToWidth(str, wrapWidth)) {
            float f = x;
            if (font.getBidiFlag()) {
                int i = font.getStringWidth(font.bidiReorder(s));
                f += wrapWidth - i;
            }
            font.drawStringWithShadow(s, f, y, textColor);
            y += 9;
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

    public static void setDifficulty(@Nullable BlockPos pos) {
        World world = Minecraft.getInstance().world;
        if (world != null) {
            difficulty = world.getDifficultyForLocation(pos == null ? world.getSpawnPoint() : pos);
        }
        else {
            difficulty = new DifficultyInstance(Difficulty.NORMAL, 0, 0, 0);
        }
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

    private static String trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
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
