package tgw.evolution.client.renderer.ambient;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;
import tgw.evolution.client.Blending;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.EarthHelper;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.MoonPhase;
import tgw.evolution.util.Vec3f;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.world.dimension.DimensionOverworld;

import java.util.Random;

public class SkyRenderer implements IRenderHandler {

    private static final FieldHandler<WorldRenderer, VertexBuffer> SKYVBO_FIELD = new FieldHandler<>(WorldRenderer.class, "field_175012_t");
    private static final FieldHandler<WorldRenderer, VertexBuffer> SKY2VBO_FIELD = new FieldHandler<>(WorldRenderer.class, "field_175011_u");
    private static final FieldHandler<WorldRenderer, Integer> GLSKYLIST2_FIELD = new FieldHandler<>(WorldRenderer.class, "field_72781_x");
    private static final FieldHandler<WorldRenderer, VertexFormat> VERTEX_BUFFER_FORMAT_FIELD = new FieldHandler<>(WorldRenderer.class,
                                                                                                                   "field_175014_r");
    private static final float SCALE_OF_CELESTIAL = 20.0f;
    private final DimensionOverworld dimension;
    private final int glSkyList2;
    private final VertexBuffer sky2VBO;
    private final VertexBuffer skyVBO;
    private final VertexFormat vertexBufferFormat;
    private VertexBuffer starVBO;

    public SkyRenderer(WorldRenderer worldRenderer) {
        this.skyVBO = SKYVBO_FIELD.get(worldRenderer);
        this.sky2VBO = SKY2VBO_FIELD.get(worldRenderer);
        this.glSkyList2 = GLSKYLIST2_FIELD.get(worldRenderer);
        this.vertexBufferFormat = VERTEX_BUFFER_FORMAT_FIELD.get(worldRenderer);
        this.dimension = (DimensionOverworld) Minecraft.getInstance().world.dimension;
        this.generateStars();
    }

    private static void drawSun(Minecraft mc, Tessellator tessellator, BufferBuilder bufferBuilder, float sunCelestialRadius) {
        mc.textureManager.bindTexture(EvolutionResources.SUN);
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(-SCALE_OF_CELESTIAL, sunCelestialRadius, -SCALE_OF_CELESTIAL).tex(0, 0).endVertex();
        bufferBuilder.pos(SCALE_OF_CELESTIAL, sunCelestialRadius, -SCALE_OF_CELESTIAL).tex(1, 0).endVertex();
        bufferBuilder.pos(SCALE_OF_CELESTIAL, sunCelestialRadius, SCALE_OF_CELESTIAL).tex(1, 1).endVertex();
        bufferBuilder.pos(-SCALE_OF_CELESTIAL, sunCelestialRadius, SCALE_OF_CELESTIAL).tex(0, 1).endVertex();
        tessellator.draw();
    }

    private static void drawSunEclipse(Minecraft mc,
                                       ClientWorld world,
                                       float partialTicks,
                                       Tessellator tessellator,
                                       BufferBuilder buffer,
                                       float radius,
                                       DimensionOverworld dimension) {
        float intensity = dimension.getEclipseIntensity();
        if (intensity <= 1.0F / 81.0F) {
            drawSun(mc, tessellator, buffer, radius);
            float rainStrength = 1.0F - world.getRainStrength(partialTicks);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, rainStrength * intensity * 81.0F);
        }
        mc.textureManager.bindTexture(EvolutionResources.SOLAR_ECLIPSE);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        int amplitudeIndex = dimension.getSolarEclipseAmplitudeIndex();
        int angleIndex = dimension.getSolarEclipseAngleIndex();
        boolean invertX = false;
        if (amplitudeIndex > 0) {
            invertX = true;
            amplitudeIndex = -amplitudeIndex;
        }
        boolean invertY = false;
        if (angleIndex > 0) {
            invertY = true;
            angleIndex = -angleIndex;
        }
        float textureX0 = (amplitudeIndex + 9) / 10.0F;
        float textureY0 = (angleIndex + 9) / 10.0F;
        float textureX1 = (amplitudeIndex + 10) / 10.0F;
        float textureY1 = (angleIndex + 10) / 10.0F;
        if (invertX) {
            float temp = textureX0;
            textureX0 = textureX1;
            textureX1 = temp;
        }
        if (invertY) {
            float temp = textureY0;
            textureY0 = textureY1;
            textureY1 = temp;
        }
        buffer.pos(-SCALE_OF_CELESTIAL, radius, -SCALE_OF_CELESTIAL).tex(textureX0, textureY0).endVertex();
        buffer.pos(SCALE_OF_CELESTIAL, radius, -SCALE_OF_CELESTIAL).tex(textureX1, textureY0).endVertex();
        buffer.pos(SCALE_OF_CELESTIAL, radius, SCALE_OF_CELESTIAL).tex(textureX1, textureY1).endVertex();
        buffer.pos(-SCALE_OF_CELESTIAL, radius, SCALE_OF_CELESTIAL).tex(textureX0, textureY1).endVertex();
        tessellator.draw();
    }

    private static void renderStars(BufferBuilder bufferBuilder) {
        Random random = new Random(10_842L);
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        for (int starNumber = 0; starNumber < 1_500; ++starNumber) {
            double dx = random.nextFloat() * 2.0F - 1.0F;
            double dy = random.nextFloat() * 2.0F - 1.0F;
            double dz = random.nextFloat() * 2.0F - 1.0F;
            double lengthSquared = dx * dx + dy * dy + dz * dz;
            if (lengthSquared < 1.0 && lengthSquared > 0.01) {
                lengthSquared = 1.0 / Math.sqrt(lengthSquared);
                dx *= lengthSquared;
                dy *= lengthSquared;
                dz *= lengthSquared;
                double x = dx * 100.0;
                double y = dy * 100.0;
                double z = dz * 100.0;
                if (-SCALE_OF_CELESTIAL <= x && x <= SCALE_OF_CELESTIAL) {
                    if (-SCALE_OF_CELESTIAL <= z && z <= SCALE_OF_CELESTIAL) {
                        continue;
                    }
                }
                double d3 = 0.15F + random.nextFloat() * 0.1F;
                double d8 = Math.atan2(dx, dz);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(dx * dx + dz * dz), dy);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);
                for (int vertex = 0; vertex < 4; ++vertex) {
                    double d18 = ((vertex & 2) - 1) * d3;
                    double d19 = ((vertex + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d24 = 0.0 * d12 - d21 * d13;
                    double deltaY = d21 * d12 + 0.0 * d13;
                    double deltaZ = d22 * d9 + d24 * d10;
                    double deltaX = d24 * d9 - d22 * d10;
                    bufferBuilder.pos(x + deltaX, y + deltaY, z + deltaZ).endVertex();
                }
            }
        }
    }

    private void generateStars() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        if (this.starVBO != null) {
            this.starVBO.deleteGlBuffers();
        }
        this.starVBO = new VertexBuffer(this.vertexBufferFormat);
        renderStars(bufferBuilder);
        bufferBuilder.finishDrawing();
        bufferBuilder.reset();
        this.starVBO.bufferData(bufferBuilder.getByteBuffer());
    }

    @Override
    public void render(int ticks, float partialTicks, ClientWorld world, Minecraft mc) {
        GlStateManager.disableTexture();
        float latitude = this.dimension.getLatitude();
        float sunAngle = this.dimension.calculateCelestialAngle(0, 0);
        float sunCelestialRadius = this.dimension.getSunCelestialRadius();
        float sunSeasonalOffset = this.dimension.getSunSeasonalOffset();
        Vec3f skyColor = EarthHelper.getSkyColor(world, mc.gameRenderer.getActiveRenderInfo().getBlockPos(), partialTicks, this.dimension);
        GlStateManager.color3f(skyColor.x, skyColor.y, skyColor.z);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        GlStateManager.depthMask(false);
        GlStateManager.enableFog();
        GlStateManager.color3f(skyColor.x, skyColor.y, skyColor.z);
        this.skyVBO.bindBuffer();
        GlStateManager.enableClientState(GL11.GL_VERTEX_ARRAY);
        GlStateManager.vertexPointer(3, GL11.GL_FLOAT, 12, 0);
        this.skyVBO.drawArrays(GL11.GL_QUADS);
        VertexBuffer.unbindBuffer();
        GlStateManager.disableClientState(GL11.GL_VERTEX_ARRAY);
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
        Blending.ADDITIVE_ALPHA.apply();
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
        if (this.dimension.isInSolarEclipse()) {
            drawSunEclipse(mc, world, partialTicks, tessellator, bufferBuilder, sunCelestialRadius, this.dimension);
        }
        else {
            drawSun(mc, tessellator, bufferBuilder, sunCelestialRadius);
        }
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableFog();
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        Blending.ADDITIVE_ALPHA.apply();
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
            this.starVBO.bindBuffer();
            GlStateManager.enableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.vertexPointer(3, GL11.GL_FLOAT, 12, 0);
            this.starVBO.drawArrays(GL11.GL_QUADS);
            VertexBuffer.unbindBuffer();
            GlStateManager.disableClientState(GL11.GL_VERTEX_ARRAY);
        }
        //Finish drawing stars
        GlStateManager.enableTexture();
        Blending.DEFAULT.apply();
        //Draw the moon
        MoonPhase phase = this.dimension.getMoonPhase();
        float textureX0 = phase.getTextureX();
        float textureY0 = phase.getTextureY();
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, rainStrength);
        mc.textureManager.bindTexture(EvolutionResources.MOONLIGHT);
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).tex(textureX0, textureY0).endVertex();
        float textureX1 = textureX0 + 0.2f;
        bufferBuilder.pos(SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).tex(textureX1, textureY0).endVertex();
        float textureY1 = textureY0 + 0.25f;
        bufferBuilder.pos(-SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).tex(textureX1, textureY1).endVertex();
        bufferBuilder.pos(-SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).tex(textureX0, textureY1).endVertex();
        tessellator.draw();
        Blending.DEFAULT.apply();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, rainStrength);
        mc.textureManager.bindTexture(EvolutionResources.MOON_PHASES);
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
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
        if (distanceAboveTheHorizon < 0.0) {
            //Pushed matrix to draw the dark void
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, 12.0F, 0.0F);
            this.sky2VBO.bindBuffer();
            GlStateManager.enableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.vertexPointer(3, GL11.GL_FLOAT, 12, 0);
            this.sky2VBO.drawArrays(GL11.GL_QUADS);
            VertexBuffer.unbindBuffer();
            GlStateManager.disableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.popMatrix();
            //Popped matrix of the dark void
        }
        GlStateManager.color3f(skyColor.x * 0.2F + 0.04F, skyColor.y * 0.2F + 0.04F, skyColor.z * 0.6F + 0.1F);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0.0F, -((float) (distanceAboveTheHorizon - 16.0)), 0.0F);
        GlStateManager.callList(this.glSkyList2);
        GlStateManager.popMatrix();
        GlStateManager.enableTexture();
        GlStateManager.depthMask(true);
    }
}
