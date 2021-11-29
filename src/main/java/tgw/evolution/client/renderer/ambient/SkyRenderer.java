package tgw.evolution.client.renderer.ambient;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ISkyRenderHandler;
import org.lwjgl.opengl.GL11;
import tgw.evolution.client.util.Blending;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.earth.EarthHelper;
import tgw.evolution.util.earth.MoonPhase;
import tgw.evolution.util.earth.PlanetsHelper;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3f;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.world.dimension.DimensionOverworld;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class SkyRenderer implements ISkyRenderHandler {

    private static final FieldHandler<WorldRenderer, VertexBuffer> SKYVBO_FIELD = new FieldHandler<>(WorldRenderer.class, "field_175012_t");
    private static final FieldHandler<WorldRenderer, VertexBuffer> SKY2VBO_FIELD = new FieldHandler<>(WorldRenderer.class, "field_175011_u");
    private static final FieldHandler<WorldRenderer, VertexFormat> VERTEX_BUFFER_FORMAT_FIELD = new FieldHandler<>(WorldRenderer.class,
                                                                                                                   "field_175014_r");
    private static final float SCALE_OF_CELESTIAL = 20.0f;
    private static final Quaternion SKY_PRE_TRANSFORM = Vector3f.YP.rotationDegrees(-90.0f);
    private static final Quaternion SKY_DAWN_DUSK_TRANSFORM = Vector3f.XP.rotationDegrees(-90.0f);
    private final DimensionOverworld dimension;
    private final VertexBuffer sky2VBO;
    private final VertexBuffer skyVBO;
    private final VertexFormat skyVertexFormat = DefaultVertexFormats.POSITION;
    private final VertexFormat vertexBufferFormat;
    private VertexBuffer starVBO;

    public SkyRenderer(WorldRenderer worldRenderer, DimensionOverworld dimension) {
        this.skyVBO = SKYVBO_FIELD.get(worldRenderer);
        this.sky2VBO = SKY2VBO_FIELD.get(worldRenderer);
        this.vertexBufferFormat = VERTEX_BUFFER_FORMAT_FIELD.get(worldRenderer);
        this.dimension = dimension;
        this.generateStars();
    }

    private static void buildStars(BufferBuilder builder) {
        Random random = new Random(10_842L);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        for (int starNumber = 0; starNumber < 2_500; starNumber++) {
            double dx = random.nextFloat() * 2.0F - 1.0F;
            double dy = random.nextFloat() * 2.0F - 1.0F;
            double dz = random.nextFloat() * 2.0F - 1.0F;
            double lengthSquared = dx * dx + dy * dy + dz * dz;
            if (lengthSquared < 1.0 && lengthSquared > 0.01) {
                lengthSquared = 1.0 / Math.sqrt(lengthSquared);
                dx *= lengthSquared;
                dy *= lengthSquared;
                dz *= lengthSquared;
                double x = dx * EarthHelper.CELESTIAL_SPHERE_RADIUS * 1.2;
                double y = dy * EarthHelper.CELESTIAL_SPHERE_RADIUS * 1.2;
                double z = dz * EarthHelper.CELESTIAL_SPHERE_RADIUS * 1.2;
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
                    double d24 = -d21 * d13;
                    double deltaY = d21 * d12;
                    double deltaZ = d22 * d9 + d24 * d10;
                    double deltaX = d24 * d9 - d22 * d10;
                    builder.vertex(x + deltaX, y + deltaY, z + deltaZ).endVertex();
                }
            }
        }
    }

    private static void drawLine(Matrix4f matrix, BufferBuilder builder, float celestialRadius) {
        builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 0; i < 45; i++) {
            builder.vertex(matrix, 0, celestialRadius * MathHelper.cosDeg(8 * i), celestialRadius * MathHelper.sinDeg(8 * i)).endVertex();
        }
        builder.end();
        WorldVertexBufferUploader.end(builder);
    }

    private static void drawMoonlight(Minecraft mc,
                                      BufferBuilder builder,
                                      Matrix4f moonMatrix,
                                      float starBrightness,
                                      float moonCelestialRadius,
                                      float x0,
                                      float y0,
                                      float x1,
                                      float y1) {
        Blending.DEFAULT.apply();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, starBrightness);
        mc.textureManager.bind(EvolutionResources.ENVIRONMENT_MOONLIGHT);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).uv(x0, y0).endVertex();
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).uv(x1, y0).endVertex();
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).uv(x1, y1).endVertex();
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).uv(x0, y1).endVertex();
        builder.end();
        WorldVertexBufferUploader.end(builder);
    }

    private static void drawPlanet(BufferBuilder builder, Matrix4f matrix, float radius, float angularSize) {
        builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        builder.vertex(matrix, -angularSize / 3, radius, angularSize).endVertex();
        builder.vertex(matrix, -angularSize, radius, angularSize / 3).endVertex();
        builder.vertex(matrix, -angularSize, radius, -angularSize / 3).endVertex();
        builder.vertex(matrix, -angularSize / 3, radius, -angularSize).endVertex();
        builder.vertex(matrix, angularSize / 3, radius, -angularSize).endVertex();
        builder.vertex(matrix, angularSize, radius, -angularSize / 3).endVertex();
        builder.vertex(matrix, angularSize, radius, angularSize / 3).endVertex();
        builder.vertex(matrix, angularSize / 3, radius, angularSize).endVertex();
        builder.end();
        WorldVertexBufferUploader.end(builder);
    }

    private static void drawPole(Matrix4f matrix, BufferBuilder builder, float celestialRadius) {
        builder.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        builder.vertex(matrix, celestialRadius, 0, 0).endVertex();
        for (int i = 0; i < 8; i++) {
            builder.vertex(matrix, celestialRadius, MathHelper.sin(i), MathHelper.cos(i)).endVertex();
        }
        builder.end();
        WorldVertexBufferUploader.end(builder);
    }

    private static void drawSolarEclipse(Minecraft mc,
                                         ClientWorld world,
                                         float partialTicks,
                                         Matrix4f sunMatrix,
                                         BufferBuilder builder,
                                         float radius,
                                         DimensionOverworld dimension) {
        float intensity = dimension.getSolarEclipseIntensity();
        if (intensity <= 1.0F / 81.0F) {
            drawSun(mc, sunMatrix, builder, radius);
            float rainStrength = 1.0F - world.getRainLevel(partialTicks);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, rainStrength * intensity * 81.0F);
        }
        mc.textureManager.bind(EvolutionResources.ENVIRONMENT_SOLAR_ECLIPSE);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        int declinationIndex = dimension.getSolarEclipseDeclinationIndex();
        int rightAscIndex = dimension.getSolarEclipseRightAscensionIndex();
        boolean invertX = false;
        if (declinationIndex > 0) {
            invertX = true;
            declinationIndex = -declinationIndex;
        }
        boolean invertY = false;
        if (rightAscIndex > 0) {
            invertY = true;
            rightAscIndex = -rightAscIndex;
        }
        float textureX0 = (declinationIndex + 9) / 10.0F;
        float textureY0 = (rightAscIndex + 9) / 10.0F;
        float textureX1 = (declinationIndex + 10) / 10.0F;
        float textureY1 = (rightAscIndex + 10) / 10.0F;
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
        builder.vertex(sunMatrix, -SCALE_OF_CELESTIAL, radius, -SCALE_OF_CELESTIAL).uv(textureX0, textureY0).endVertex();
        builder.vertex(sunMatrix, SCALE_OF_CELESTIAL, radius, -SCALE_OF_CELESTIAL).uv(textureX1, textureY0).endVertex();
        builder.vertex(sunMatrix, SCALE_OF_CELESTIAL, radius, SCALE_OF_CELESTIAL).uv(textureX1, textureY1).endVertex();
        builder.vertex(sunMatrix, -SCALE_OF_CELESTIAL, radius, SCALE_OF_CELESTIAL).uv(textureX0, textureY1).endVertex();
        builder.end();
        WorldVertexBufferUploader.end(builder);
    }

    private static void drawSquare(Matrix4f matrix, BufferBuilder builder, float celestialRadius) {
        builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        builder.vertex(matrix, -2.5f, celestialRadius, 2.5f).endVertex();
        builder.vertex(matrix, -2.5f, celestialRadius, -2.5f).endVertex();
        builder.vertex(matrix, 2.5f, celestialRadius, -2.5f).endVertex();
        builder.vertex(matrix, 2.5f, celestialRadius, 2.5f).endVertex();
        builder.end();
        WorldVertexBufferUploader.end(builder);
    }

    private static void drawSun(Minecraft mc, Matrix4f sunMatrix, BufferBuilder builder, float sunCelestialRadius) {
        mc.textureManager.bind(EvolutionResources.ENVIRONMENT_SUN);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        builder.vertex(sunMatrix, -SCALE_OF_CELESTIAL, sunCelestialRadius, -SCALE_OF_CELESTIAL).uv(0, 0).endVertex();
        builder.vertex(sunMatrix, SCALE_OF_CELESTIAL, sunCelestialRadius, -SCALE_OF_CELESTIAL).uv(1, 0).endVertex();
        builder.vertex(sunMatrix, SCALE_OF_CELESTIAL, sunCelestialRadius, SCALE_OF_CELESTIAL).uv(1, 1).endVertex();
        builder.vertex(sunMatrix, -SCALE_OF_CELESTIAL, sunCelestialRadius, SCALE_OF_CELESTIAL).uv(0, 1).endVertex();
        builder.end();
        WorldVertexBufferUploader.end(builder);
    }

    private void drawLunarEclipse(Minecraft mc, ClientWorld world, float partialTicks, Matrix4f moonMatrix, BufferBuilder builder, float radius) {
        MoonPhase phase = this.dimension.getEclipsePhase();
        float textureX0 = phase.getTextureX();
        float textureY0 = phase.getTextureY();
        float textureX1 = textureX0 + 0.2f;
        float textureY1 = textureY0 + 0.25f;
        float rainStrength = 1.0f - world.getRainLevel(partialTicks);
        drawMoonlight(mc,
                      builder,
                      moonMatrix,
                      1.0F - rainStrength * this.dimension.getSunBrightness(partialTicks),
                      radius,
                      textureX0,
                      textureY0,
                      textureX1,
                      textureY1);
        Blending.DEFAULT.apply();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, rainStrength);
        mc.textureManager.bind(EvolutionResources.ENVIRONMENT_LUNAR_ECLIPSE);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        int declinationIndex = -this.dimension.getLunarEclipseDeclinationIndex();
        int rightAscIndex = this.dimension.getLunarEclipseRightAscensionIndex();
        textureX0 = (rightAscIndex + 9) / 19.0F;
        textureY0 = (declinationIndex + 9) / 19.0F;
        textureX1 = (rightAscIndex + 10) / 19.0F;
        textureY1 = (declinationIndex + 10) / 19.0F;
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, radius, -SCALE_OF_CELESTIAL).uv(textureX0, textureY1).endVertex();
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, radius, -SCALE_OF_CELESTIAL).uv(textureX0, textureY0).endVertex();
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, radius, SCALE_OF_CELESTIAL).uv(textureX1, textureY0).endVertex();
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, radius, SCALE_OF_CELESTIAL).uv(textureX1, textureY1).endVertex();
        builder.end();
        WorldVertexBufferUploader.end(builder);
    }

    private void drawMoon(Minecraft mc,
                          BufferBuilder builder,
                          Matrix4f moonMatrix,
                          float rainStrength,
                          float moonCelestialRadius,
                          float partialTicks) {
        MoonPhase phase = this.dimension.getMoonPhase();
        float textureX0 = phase.getTextureX();
        float textureY0 = phase.getTextureY();
        float textureX1 = textureX0 + 0.2f;
        float textureY1 = textureY0 + 0.25f;
        drawMoonlight(mc,
                      builder,
                      moonMatrix,
                      1 - this.dimension.getSunBrightness(partialTicks) * rainStrength,
                      moonCelestialRadius,
                      textureX0,
                      textureY0,
                      textureX1,
                      textureY1);
        Blending.DEFAULT.apply();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, rainStrength);
        mc.textureManager.bind(EvolutionResources.ENVIRONMENT_MOON);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).uv(textureX0, textureY0).endVertex();
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).uv(textureX1, textureY0).endVertex();
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).uv(textureX1, textureY1).endVertex();
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).uv(textureX0, textureY1).endVertex();
        builder.end();
        WorldVertexBufferUploader.end(builder);
    }

    private void drawMoonShadow(Minecraft mc, BufferBuilder builder, Matrix4f moonMatrix, float moonCelestialRadius, Vec3f skyColor) {
        RenderSystem.enableAlphaTest();
        Blending.DEFAULT.apply();
        float altitude = this.dimension.getMoonAltitude();
        if (altitude >= 82) {
            Vec3f lastFogColor = this.dimension.getLastFogColor();
            float skyMult = MathHelper.clamp((90 - altitude) / 8.0f, 0, 1);
            float fogMult = 1.0f - skyMult;
            RenderSystem.color3f(lastFogColor.x * fogMult + skyColor.x * skyMult,
                                 lastFogColor.y * fogMult + skyColor.y * skyMult,
                                 lastFogColor.z * fogMult + skyColor.z * skyMult);
        }
        else {
            RenderSystem.disableFog();
            RenderSystem.color3f(skyColor.x * 1.05f, skyColor.y * 1.05f, skyColor.z * 1.05f);
        }
        mc.textureManager.bind(EvolutionResources.ENVIRONMENT_MOON_SHADOW);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        float textureX0 = 0.0f;
        float textureY0 = 0.0f;
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).uv(textureX0, textureY0).endVertex();
        float textureX1 = 1.0f;
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).uv(textureX1, textureY0).endVertex();
        float textureY1 = 1.0f;
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).uv(textureX1, textureY1).endVertex();
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).uv(textureX0, textureY1).endVertex();
        builder.end();
        WorldVertexBufferUploader.end(builder);
    }

    private void generateStars() {
        BufferBuilder builder = Tessellator.getInstance().getBuilder();
        if (this.starVBO != null) {
            this.starVBO.close();
        }
        this.starVBO = new VertexBuffer(this.vertexBufferFormat);
        buildStars(builder);
        builder.end();
        this.starVBO.upload(builder);
    }

    @Override
    public void render(int ticks, float partialTicks, MatrixStack matrices, ClientWorld world, Minecraft mc) {
        mc.getProfiler().push("init");
        this.dimension.setWorld(world);
        RenderSystem.disableTexture();
        float latitude = this.dimension.getLatitude();
        Quaternion latitudeTransform = Vector3f.ZP.rotationDegrees(latitude);
        float sunRightAscension = this.dimension.getSunRightAscension();
        float sunCelestialRadius = this.dimension.getSunCelestialRadius();
        float sunDeclinationOffset = this.dimension.getSunDeclinationOffset();
        float rainStrength = 1.0F - world.getRainLevel(partialTicks);
        Vec3f skyColor = EarthHelper.getSkyColor(world, mc.gameRenderer.getMainCamera().getBlockPosition(), partialTicks, this.dimension);
        float moonDeclinationOffset = this.dimension.getMoonDeclinationOffset();
        float moonRightAscension = this.dimension.getMoonRightAscension();
        float moonCelestialRadius = this.dimension.getMoonCelestialRadius();
        float sunBrightness = this.dimension.getSunBrightness(partialTicks);
        mc.getProfiler().popPush("dome");
        BufferBuilder builder = Tessellator.getInstance().getBuilder();
        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);
        RenderSystem.enableFog();
        RenderSystem.color3f(skyColor.x, skyColor.y, skyColor.z);
        this.skyVBO.bind();
        this.skyVertexFormat.setupBufferState(0L);
        this.skyVBO.draw(matrices.last().pose(), GL11.GL_QUADS);
        VertexBuffer.unbind();
        this.skyVertexFormat.clearBufferState();
        //Render background stars
        mc.getProfiler().popPush("stars");
        RenderSystem.enableBlend();
        float starBrightness = (1.0f - sunBrightness) * rainStrength;
        if (starBrightness > 0.0F) {
            float starsRightAscension = this.dimension.getStarsRightAscension();
            RenderSystem.disableTexture();
            //Pushed the matrix to draw the background stars
            matrices.pushPose();
            Blending.ADDITIVE_ALPHA.apply();
            matrices.mulPose(SKY_PRE_TRANSFORM);
            matrices.mulPose(latitudeTransform);
            matrices.mulPose(Vector3f.XP.rotationDegrees(360.0f * starsRightAscension + 180));
            RenderSystem.color4f(starBrightness, starBrightness, starBrightness, starBrightness);
            this.starVBO.bind();
            this.skyVertexFormat.setupBufferState(0L);
            this.starVBO.draw(matrices.last().pose(), GL11.GL_QUADS);
            VertexBuffer.unbind();
            this.skyVertexFormat.clearBufferState();
            matrices.popPose();
            //Popped the matrix to draw the background stars
        }
        if (EvolutionConfig.CLIENT.showPlanets.get()) {
            //Render planets
            mc.getProfiler().popPush("planets");
            float planetStarBrightness = (1.0f - sunBrightness * sunBrightness) * rainStrength;
            if (planetStarBrightness > 0.0F) {
                RenderSystem.disableFog();
                RenderSystem.disableTexture();
                //Pushed the matrix to draw the planets
                Blending.ADDITIVE_ALPHA.apply();
                matrices.pushPose();
                matrices.mulPose(SKY_PRE_TRANSFORM);
                matrices.mulPose(latitudeTransform);
                matrices.translate(PlanetsHelper.getDecOff1Mercury(), 0, 0);
                matrices.mulPose(Vector3f.XP.rotationDegrees(PlanetsHelper.getHa1Mercury() + 180));
                RenderSystem.color4f(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getDist1Mercury(), PlanetsHelper.getAngSize1Mercury() * 5);
                matrices.popPose();
                matrices.pushPose();
                matrices.mulPose(SKY_PRE_TRANSFORM);
                matrices.mulPose(latitudeTransform);
                matrices.translate(PlanetsHelper.getDecOff2Venus(), 0, 0);
                matrices.mulPose(Vector3f.XP.rotationDegrees(PlanetsHelper.getHa2Venus() + 180));
                RenderSystem.color4f(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getDist2Venus(), PlanetsHelper.getAngSize2Venus() * 5);
                matrices.popPose();
                matrices.pushPose();
                matrices.mulPose(SKY_PRE_TRANSFORM);
                matrices.mulPose(latitudeTransform);
                matrices.translate(PlanetsHelper.getDecOff4Mars(), 0, 0);
                matrices.mulPose(Vector3f.XP.rotationDegrees(PlanetsHelper.getHa4Mars() + 180));
                RenderSystem.color4f(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getDist4Mars(), PlanetsHelper.getAngSize4Mars() * 5);
                matrices.popPose();
                matrices.pushPose();
                matrices.mulPose(SKY_PRE_TRANSFORM);
                matrices.mulPose(latitudeTransform);
                matrices.translate(PlanetsHelper.getDecOff5Jupiter(), 0, 0);
                matrices.mulPose(Vector3f.XP.rotationDegrees(PlanetsHelper.getHa5Jupiter() + 180));
                RenderSystem.color4f(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getDist5Jupiter(), PlanetsHelper.getAngSize5Jupiter() * 5);
                matrices.popPose();
                matrices.pushPose();
                matrices.mulPose(SKY_PRE_TRANSFORM);
                matrices.mulPose(latitudeTransform);
                matrices.translate(PlanetsHelper.getDecOff6Saturn(), 0, 0);
                matrices.mulPose(Vector3f.XP.rotationDegrees(PlanetsHelper.getHa6Saturn() + 180));
                RenderSystem.color4f(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getDist6Saturn(), PlanetsHelper.getAngSize6Saturn() * 5);
                matrices.popPose();
                //Popped the matrix to draw the planets
            }
        }
        //Render moon shadow
        mc.getProfiler().popPush("moonShadow");
        Quaternion moonTransform = null;
        if (starBrightness > 0 && !this.dimension.isInSolarEclipse()) {
            RenderSystem.enableFog();
            RenderSystem.enableBlend();
            //Pushed the matrix to draw moon shadow
            matrices.pushPose();
            matrices.mulPose(SKY_PRE_TRANSFORM);
            //Translate the moon in the sky based on monthly amplitude.
            matrices.mulPose(latitudeTransform);
            matrices.translate(moonDeclinationOffset, 0, 0);
            moonTransform = Vector3f.XP.rotationDegrees(360.0f * moonRightAscension + 180);
            matrices.mulPose(moonTransform);
            RenderSystem.enableTexture();
            //Draw the moon shadow
            this.drawMoonShadow(mc, builder, matrices.last().pose(), moonCelestialRadius, skyColor);
            //Finish drawing moon shadow
            matrices.popPose();
            //Popped the matrix to draw moon shadow
        }
        //Render dusk and dawn
        mc.getProfiler().popPush("duskDawn");
        RenderSystem.disableAlphaTest();
        RenderSystem.disableFog();
        float[] duskDawnColors = this.dimension.getDuskDawnColors();
        if (duskDawnColors != null) {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                           GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                           GlStateManager.SourceFactor.ONE,
                                           GlStateManager.DestFactor.ZERO);
            RenderSystem.disableTexture();
            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            //Pushed matrix to draw dusk and dawn
            matrices.pushPose();
            matrices.mulPose(SKY_DAWN_DUSK_TRANSFORM);
            matrices.mulPose(Vector3f.ZP.rotationDegrees(this.dimension.getSunAzimuth()));
            Matrix4f duskDawnMatrix = matrices.last().pose();
            builder.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            builder.vertex(duskDawnMatrix, 0, EarthHelper.CELESTIAL_SPHERE_RADIUS, 0)
                   .color(duskDawnColors[0], duskDawnColors[1], duskDawnColors[2], duskDawnColors[3])
                   .endVertex();
            for (int j = 0; j <= 16; j++) {
                float f6 = j * MathHelper.TAU / 16.0F;
                float f7 = MathHelper.sin(f6);
                float f8 = MathHelper.cos(f6);
                builder.vertex(duskDawnMatrix, f7 * 120.0F, f8 * 120.0F, -f8 * 80.0F * duskDawnColors[3])
                       .color(duskDawnColors[0], duskDawnColors[1], duskDawnColors[2], 0.0F)
                       .endVertex();
            }
            builder.end();
            WorldVertexBufferUploader.end(builder);
            matrices.popPose();
            //Popped matrix of dusk and dawn
            RenderSystem.shadeModel(GL11.GL_FLAT);
        }
        //Render the sun
        mc.getProfiler().popPush("sun");
        RenderSystem.enableTexture();
        Blending.ADDITIVE_ALPHA.apply();
        //Pushed matrix to draw the sun
        matrices.pushPose();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, rainStrength);
        matrices.mulPose(SKY_PRE_TRANSFORM);
        //Translate the sun in the sky based on season.
        matrices.mulPose(latitudeTransform);
        matrices.translate(sunDeclinationOffset, 0, 0);
        matrices.mulPose(Vector3f.XP.rotationDegrees(360.0f * sunRightAscension + 180));
        //Draw the sun
        if (this.dimension.isInSolarEclipse()) {
            drawSolarEclipse(mc, world, partialTicks, matrices.last().pose(), builder, sunCelestialRadius, this.dimension);
        }
        else {
            drawSun(mc, matrices.last().pose(), builder, sunCelestialRadius);
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.enableFog();
        matrices.popPose();
        //Popped matrix of the sun
        mc.getProfiler().popPush("moon");
        RenderSystem.enableBlend();
        //Pushed the matrix to draw the moon
        matrices.pushPose();
        matrices.mulPose(SKY_PRE_TRANSFORM);
        //Translate the moon in the sky based on monthly amplitude.
        matrices.mulPose(latitudeTransform);
        matrices.translate(moonDeclinationOffset, 0, 0);
        if (moonTransform == null) {
            moonTransform = Vector3f.XP.rotationDegrees(360.0f * moonRightAscension + 180);
        }
        matrices.mulPose(moonTransform);
        RenderSystem.enableTexture();
        //Draw the moon
        if (this.dimension.isInLunarEclipse()) {
            this.drawLunarEclipse(mc, world, partialTicks, matrices.last().pose(), builder, moonCelestialRadius);
        }
        else {
            this.drawMoon(mc, builder, matrices.last().pose(), rainStrength, moonCelestialRadius, partialTicks);
        }
        //Finish drawing moon
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableFog();
        matrices.popPose();
        //Popped the matrix to draw the moon
        //Render debug
        mc.getProfiler().popPush("debug");
        boolean forceAll = EvolutionConfig.CLIENT.celestialForceAll.get();
        boolean equator = forceAll || EvolutionConfig.CLIENT.celestialEquator.get();
        boolean poles = forceAll || EvolutionConfig.CLIENT.celestialPoles.get();
        boolean ecliptic = forceAll || EvolutionConfig.CLIENT.ecliptic.get();
        boolean planets = EvolutionConfig.CLIENT.showPlanets.get() && (forceAll || EvolutionConfig.CLIENT.planets.get());
        if (equator || poles) {
            RenderSystem.disableTexture();
            //Pushed matrix to draw celestial equator and poles
            matrices.pushPose();
            matrices.mulPose(SKY_PRE_TRANSFORM);
            //Translate the sun in the sky based on season.
            matrices.mulPose(latitudeTransform);
            //Draw the celestial equator
            if (equator) {
                RenderSystem.color4f(1.0F, 0.0f, 0.0F, 1.0f);
                drawLine(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            }
            if (poles) {
                RenderSystem.color4f(0.0f, 0.0f, 1.0f, 1.0f);
                drawPole(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
                matrices.mulPose(Vector3f.YP.rotationDegrees(180));
                RenderSystem.color4f(1.0f, 0.0f, 0.0f, 1.0f);
                drawPole(matrices.last().pose(), builder, 99);
            }
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.enableFog();
            matrices.popPose();
            //Popped matrix of celestial equator and poles
        }
        if (ecliptic) {
            RenderSystem.disableTexture();
            //Pushed matrix to draw the ecliptic
            matrices.pushPose();
            matrices.mulPose(SKY_PRE_TRANSFORM);
            //Translate the sun in the sky based on season.
            matrices.mulPose(latitudeTransform);
            matrices.translate(sunDeclinationOffset, 0, 0);
            //Draw the ecliptic
            RenderSystem.color4f(0.0F, 1.0f, 0.0F, 1.0f);
            drawLine(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.enableFog();
            matrices.popPose();
            //Popped matrix of the ecliptic
        }
        if (planets) {
            RenderSystem.disableTexture();
            matrices.pushPose();
            matrices.mulPose(SKY_PRE_TRANSFORM);
            matrices.mulPose(latitudeTransform);
            matrices.translate(PlanetsHelper.getDecOff1Mercury(), 0, 0);
            matrices.mulPose(Vector3f.XP.rotationDegrees(PlanetsHelper.getHa1Mercury() + 180));
            RenderSystem.color4f(1.0F, 0.0f, 0.0F, 1.0f);
            drawSquare(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(SKY_PRE_TRANSFORM);
            matrices.mulPose(latitudeTransform);
            matrices.translate(PlanetsHelper.getDecOff2Venus(), 0, 0);
            matrices.mulPose(Vector3f.XP.rotationDegrees(PlanetsHelper.getHa2Venus() + 180));
            RenderSystem.color4f(1.0F, 0.5f, 0.0F, 1.0f);
            drawSquare(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(SKY_PRE_TRANSFORM);
            matrices.mulPose(latitudeTransform);
            matrices.translate(PlanetsHelper.getDecOff4Mars(), 0, 0);
            matrices.mulPose(Vector3f.XP.rotationDegrees(PlanetsHelper.getHa4Mars() + 180));
            RenderSystem.color4f(1.0F, 1.0f, 0.0F, 1.0f);
            drawSquare(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(SKY_PRE_TRANSFORM);
            matrices.mulPose(latitudeTransform);
            matrices.translate(PlanetsHelper.getDecOff5Jupiter(), 0, 0);
            matrices.mulPose(Vector3f.XP.rotationDegrees(PlanetsHelper.getHa5Jupiter() + 180));
            RenderSystem.color4f(0.0F, 1.0f, 0.0F, 1.0f);
            drawSquare(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(SKY_PRE_TRANSFORM);
            matrices.mulPose(latitudeTransform);
            matrices.translate(PlanetsHelper.getDecOff6Saturn(), 0, 0);
            matrices.mulPose(Vector3f.XP.rotationDegrees(PlanetsHelper.getHa6Saturn() + 180));
            RenderSystem.color4f(0.0F, 1.0f, 1.0F, 1.0f);
            drawSquare(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            matrices.popPose();
        }
        mc.getProfiler().popPush("void");
        RenderSystem.disableTexture();
        RenderSystem.color3f(0.0F, 0.0F, 0.0F);
        double distanceAboveTheHorizon = mc.player.getEyePosition(partialTicks).y - world.getLevelData().getHorizonHeight();
        if (distanceAboveTheHorizon < 0.0) {
            //Pushed matrix to draw the dark void
            matrices.pushPose();
            matrices.translate(0, 12, 0);
            this.sky2VBO.bind();
            this.skyVertexFormat.setupBufferState(0L);
            this.sky2VBO.draw(matrices.last().pose(), GL11.GL_QUADS);
            VertexBuffer.unbind();
            this.skyVertexFormat.clearBufferState();
            matrices.popPose();
            //Popped matrix of the dark void
        }
        if (world.effects().hasGround()) {
            RenderSystem.color3f(skyColor.x * 0.2F + 0.04F, skyColor.y * 0.2F + 0.04F, skyColor.z * 0.6F + 0.1F);
        }
        else {
            RenderSystem.color3f(skyColor.x, skyColor.y, skyColor.z);
        }
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        RenderSystem.disableFog();
        mc.getProfiler().pop();
    }
}
