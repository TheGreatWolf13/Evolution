package tgw.evolution.client.renderer.ambient;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tgw.evolution.Evolution;
import tgw.evolution.util.EarthHelper;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.MoonPhase;
import tgw.evolution.util.Vec3f;
import tgw.evolution.world.dimension.DimensionOverworld;

public class SkyRenderer implements IRenderHandler {

    private static final ResourceLocation MOON_PHASES_TEXTURES = Evolution.location("textures/environment/moon_phases.png");
    private static final ResourceLocation MOONLIGHT_TEXTURES = Evolution.location("textures/environment/moonlight_phases.png");
    private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("textures/environment/sun.png");
    private static final float SCALE_OF_CELESTIAL = 20.0f;
    private final boolean vboEnabled;
    private final VertexBuffer skyVBO;
    private final VertexBuffer starVBO;
    private final VertexBuffer sky2VBO;
    private final int glSkyList;
    private final int glSkyList2;
    private final int starGLCallList;
    private final DimensionOverworld dimension;

    public SkyRenderer(WorldRenderer worldRenderer) {
        this.vboEnabled = GLX.useVbo();
        this.skyVBO = ObfuscationReflectionHelper.getPrivateValue(WorldRenderer.class, worldRenderer, "field_175012_t");
        this.starVBO = ObfuscationReflectionHelper.getPrivateValue(WorldRenderer.class, worldRenderer, "field_175013_s");
        this.sky2VBO = ObfuscationReflectionHelper.getPrivateValue(WorldRenderer.class, worldRenderer, "field_175011_u");
        this.glSkyList = ObfuscationReflectionHelper.getPrivateValue(WorldRenderer.class, worldRenderer, "field_72771_w");
        this.glSkyList2 = ObfuscationReflectionHelper.getPrivateValue(WorldRenderer.class, worldRenderer, "field_72781_x");
        this.starGLCallList = ObfuscationReflectionHelper.getPrivateValue(WorldRenderer.class, worldRenderer, "field_72772_v");
        this.dimension = (DimensionOverworld) Minecraft.getInstance().world.dimension;
    }

    @Override
    public void render(int ticks, float partialTicks, ClientWorld world, Minecraft mc) {
        GlStateManager.disableTexture();
        float latitude = this.dimension.getLatitude();
        float sunAngle = this.dimension.calculateCelestialAngle(0, 0);
        float sunCelestialRadius = this.dimension.getSunCelestialRadius();
        float sunSeasonalOffset = this.dimension.getSunSeasonalOffset();
        float sunElevation = this.dimension.getSunElevationAngle();
        Vec3f skyColor = EarthHelper.getSkyColor(world, mc.gameRenderer.getActiveRenderInfo().getBlockPos(), partialTicks, sunElevation);
        GlStateManager.color3f(skyColor.x, skyColor.y, skyColor.z);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        GlStateManager.depthMask(false);
        GlStateManager.enableFog();
        GlStateManager.color3f(skyColor.x, skyColor.y, skyColor.z);
        if (this.vboEnabled) {
            this.skyVBO.bindBuffer();
            GlStateManager.enableClientState(32_884);
            GlStateManager.vertexPointer(3, 5_126, 12, 0);
            this.skyVBO.drawArrays(7);
            VertexBuffer.unbindBuffer();
            GlStateManager.disableClientState(32_884);
        }
        else {
            GlStateManager.callList(this.glSkyList);
        }
        GlStateManager.disableFog();
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                         GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                         GlStateManager.SourceFactor.ONE,
                                         GlStateManager.DestFactor.ZERO);
        RenderHelper.disableStandardItemLighting();
        float[] sunriseColors = this.dimension.calcSunriseSunsetColors(0, 0);
        if (sunriseColors != null) {
            GlStateManager.disableTexture();
            GlStateManager.shadeModel(7_425);
            //Pushed matrix to draw sunrise / sunset
            GlStateManager.pushMatrix();
            GlStateManager.rotatef(-90.0f, 1.0F, 0.0F, 0.0F);
            float sunAzimuth = MathHelper.radToDeg((float) MathHelper.atan2(EarthHelper.sunX, EarthHelper.sunZ)) + 180;
            GlStateManager.rotatef(sunAzimuth, 0.0F, 0.0F, 1.0F);
            bufferBuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.pos(0, 100, 0).color(sunriseColors[0], sunriseColors[1], sunriseColors[2], sunriseColors[3]).endVertex();
            for (int j = 0; j <= 16; ++j) {
                float f6 = j * MathHelper.TAU / 16.0F;
                float f7 = MathHelper.sin(f6);
                float f8 = MathHelper.cos(f6);
                bufferBuilder.pos(f7 * 120.0F, f8 * 120.0F, -f8 * 80.0F * sunriseColors[3])
                             .color(sunriseColors[0], sunriseColors[1], sunriseColors[2], 0.0F)
                             .endVertex();
            }
            tessellator.draw();
            GlStateManager.popMatrix();
            //Popped matrix of sunrise / sunset
            GlStateManager.shadeModel(7_424);
        }
        GlStateManager.enableTexture();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                         GlStateManager.DestFactor.ONE,
                                         GlStateManager.SourceFactor.ONE,
                                         GlStateManager.DestFactor.ZERO);
        //Pushed matrix to draw the sun
        GlStateManager.pushMatrix();
        float rainStrength = 1.0F - world.getRainStrength(partialTicks);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, rainStrength);
        GlStateManager.rotatef(-90.0f, 0.0F, 1.0F, 0.0F);
        //Translate the sun in the sky based on season.
        GlStateManager.rotatef(latitude, 0.0f, 0.0f, 1.0f);
        GlStateManager.translatef(sunSeasonalOffset, 0, 0);
        GlStateManager.rotatef(360.0f * sunAngle + 180, 1.0F, 0.0F, 0.0F);
        //Draw the sun
        mc.textureManager.bindTexture(SUN_TEXTURES);
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(-SCALE_OF_CELESTIAL, sunCelestialRadius, -SCALE_OF_CELESTIAL).tex(0, 0).endVertex();
        bufferBuilder.pos(SCALE_OF_CELESTIAL, sunCelestialRadius, -SCALE_OF_CELESTIAL).tex(1, 0).endVertex();
        bufferBuilder.pos(SCALE_OF_CELESTIAL, sunCelestialRadius, SCALE_OF_CELESTIAL).tex(1, 1).endVertex();
        bufferBuilder.pos(-SCALE_OF_CELESTIAL, sunCelestialRadius, SCALE_OF_CELESTIAL).tex(0, 1).endVertex();
        tessellator.draw();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableFog();
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        //Poped matrix of the sun
        //Pushed the matrix to draw the moon and stars
        GlStateManager.pushMatrix();
        GlStateManager.rotatef(-90.0f, 0.0F, 1.0F, 0.0F);
        //Translate the moon in the sky based on monthly amplitude.
        GlStateManager.rotatef(latitude, 0.0f, 0.0f, 1.0f);
        float moonMonthlyOffset = this.dimension.getMoonMonthlyOffset();
        float moonAngle = this.dimension.calculateMoonAngle();
        float moonCelestialRadius = this.dimension.getMoonCelestialRadius();
        GlStateManager.translatef(moonMonthlyOffset, 0, 0);
        GlStateManager.rotatef(360.0f * moonAngle + 180, 1.0F, 0.0F, 0.0F);
        //Draw stars
        GlStateManager.disableTexture();
        float starBrightness = 1 - this.dimension.getSunBrightnessPure(partialTicks) * rainStrength;
        if (starBrightness > 0.0F) {
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, starBrightness);
            if (this.vboEnabled) {
                this.starVBO.bindBuffer();
                GlStateManager.enableClientState(32_884);
                GlStateManager.vertexPointer(3, 5_126, 12, 0);
                this.starVBO.drawArrays(7);
                VertexBuffer.unbindBuffer();
                GlStateManager.disableClientState(32_884);
            }
            else {
                GlStateManager.callList(this.starGLCallList);
            }
        }
        //Finish drawing stars
        GlStateManager.enableTexture();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ZERO);
        //Draw the moon
        MoonPhase phase = this.dimension.getMoonPhase();
        float textureX0 = phase.getTextureX();
        float textureY0 = phase.getTextureY();
        float textureX1 = textureX0 + 0.2f;
        float textureY1 = textureY0 + 0.25f;
        float skyColorSum = skyColor.x + skyColor.y + skyColor.z;
        if (skyColorSum == 0.0f) {
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, rainStrength);
            mc.textureManager.bindTexture(MOONLIGHT_TEXTURES);
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferBuilder.pos(SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).tex(textureX0, textureY0).endVertex();
            bufferBuilder.pos(SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).tex(textureX1, textureY0).endVertex();
            bufferBuilder.pos(-SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).tex(textureX1, textureY1).endVertex();
            bufferBuilder.pos(-SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).tex(textureX0, textureY1).endVertex();
            tessellator.draw();
        }
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                         GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                         GlStateManager.SourceFactor.ONE,
                                         GlStateManager.DestFactor.ZERO);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, rainStrength);
        mc.textureManager.bindTexture(MOON_PHASES_TEXTURES);
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).tex(textureX0, textureY0).endVertex();
        bufferBuilder.pos(SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).tex(textureX1, textureY0).endVertex();
        bufferBuilder.pos(-SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).tex(textureX1, textureY1).endVertex();
        bufferBuilder.pos(-SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).tex(textureX0, textureY1).endVertex();
        tessellator.draw();
        //Finish drawing moon
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableFog();
        GlStateManager.popMatrix();
        //Popped the matrix to draw the moon
        GlStateManager.disableTexture();
        GlStateManager.color3f(0.0F, 0.0F, 0.0F);
        double distanceAboveTheHorizon = mc.player.getEyePosition(partialTicks).y - world.getHorizon();
        if (distanceAboveTheHorizon < 0.0D) {
            //Pushed matrix to draw the dark void
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, 12.0F, 0.0F);
            if (this.vboEnabled) {
                this.sky2VBO.bindBuffer();
                GlStateManager.enableClientState(32_884);
                GlStateManager.vertexPointer(3, 5_126, 12, 0);
                this.sky2VBO.drawArrays(7);
                VertexBuffer.unbindBuffer();
                GlStateManager.disableClientState(32_884);
            }
            else {
                GlStateManager.callList(this.glSkyList2);
            }
            GlStateManager.popMatrix();
            //Popped matrix of the dark void
        }
        GlStateManager.color3f(skyColor.x * 0.2F + 0.04F, skyColor.y * 0.2F + 0.04F, skyColor.z * 0.6F + 0.1F);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0.0F, -((float) (distanceAboveTheHorizon - 16.0D)), 0.0F);
        GlStateManager.callList(this.glSkyList2);
        GlStateManager.popMatrix();
        GlStateManager.enableTexture();
        GlStateManager.depthMask(true);
    }
}
