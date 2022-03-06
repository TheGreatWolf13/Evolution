package tgw.evolution.client.renderer.ambient;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ISkyRenderHandler;
import tgw.evolution.client.util.Blending;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.constants.CommonRotations;
import tgw.evolution.util.earth.EarthHelper;
import tgw.evolution.util.earth.MoonPhase;
import tgw.evolution.util.earth.PlanetsHelper;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3f;
import tgw.evolution.world.dimension.DimensionOverworld;

import java.util.Random;
import java.util.random.RandomGenerator;

@OnlyIn(Dist.CLIENT)
public class SkyRenderer implements ISkyRenderHandler {

    private static final float SCALE_OF_CELESTIAL = 20.0f;
    private static final Quaternion LATITUDE_TRANSFORM = Vector3f.ZP.rotationDegrees(0);
    private static final Quaternion MOON_TRANSFORM = Vector3f.XP.rotationDegrees(180);
    private static float oldLatitude;
    private static float oldMoonRA;
    private final DimensionOverworld dimension;
    private VertexBuffer darkBuffer;
    private VertexBuffer skyBuffer;
    private VertexBuffer starBuffer;

    public SkyRenderer(DimensionOverworld dimension) {
        this.dimension = dimension;
        this.createStars();
        this.createLightSky();
        this.createDarkSky();
    }

    private static void buildSkyDisc(BufferBuilder builder, float y) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        builder.vertex(0, y, 0).endVertex();
        float f = Math.signum(y) * 512.0F;
        for (int i = -180; i <= 180; i += 45) {
            builder.vertex(f * MathHelper.cosDeg(i), y, 512.0F * MathHelper.sinDeg(i)).endVertex();
        }
        builder.end();
    }

    private static void drawLine(Matrix4f matrix, BufferBuilder builder, float celestialRadius) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int i = 0; i < 45; i++) {
            builder.vertex(matrix, -0.5f, celestialRadius * MathHelper.cosDeg(8 * i), celestialRadius * MathHelper.sinDeg(8 * i)).endVertex();
            builder.vertex(matrix, 0.5f, celestialRadius * MathHelper.cosDeg(8 * i), celestialRadius * MathHelper.sinDeg(8 * i)).endVertex();
            builder.vertex(matrix, 0.5f, celestialRadius * MathHelper.cosDeg(8 * (i + 1)), celestialRadius * MathHelper.sinDeg(8 * (i + 1)))
                   .endVertex();
            builder.vertex(matrix, -0.5f, celestialRadius * MathHelper.cosDeg(8 * (i + 1)), celestialRadius * MathHelper.sinDeg(8 * (i + 1)))
                   .endVertex();
        }
        builder.end();
        BufferUploader.end(builder);
    }

    private static void drawMoonShadow(BufferBuilder builder,
                                       Matrix4f moonMatrix,
                                       float moonCelestialRadius,
                                       Vec3f skyColor,
                                       float starBrightness,
                                       float mult) {
        float r = skyColor.x;
        float g = skyColor.y;
        float b = skyColor.z;
        Blending.DEFAULT.apply();
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(moonMatrix, 0, moonCelestialRadius, 0).color(r, g, b, Math.min(1.0f, 3 * starBrightness)).endVertex();
        float closeScale = SCALE_OF_CELESTIAL * mult;
        float closeA = 0.0f;
        builder.vertex(moonMatrix, -closeScale / 3, moonCelestialRadius, closeScale).color(r, g, b, closeA).endVertex();
        builder.vertex(moonMatrix, -closeScale, moonCelestialRadius, closeScale / 3).color(r, g, b, closeA).endVertex();
        builder.vertex(moonMatrix, -closeScale, moonCelestialRadius, -closeScale / 3).color(r, g, b, closeA).endVertex();
        builder.vertex(moonMatrix, -closeScale / 3, moonCelestialRadius, -closeScale).color(r, g, b, closeA).endVertex();
        builder.vertex(moonMatrix, closeScale / 3, moonCelestialRadius, -closeScale).color(r, g, b, closeA).endVertex();
        builder.vertex(moonMatrix, closeScale, moonCelestialRadius, -closeScale / 3).color(r, g, b, closeA).endVertex();
        builder.vertex(moonMatrix, closeScale, moonCelestialRadius, closeScale / 3).color(r, g, b, closeA).endVertex();
        builder.vertex(moonMatrix, closeScale / 3, moonCelestialRadius, closeScale).color(r, g, b, closeA).endVertex();
        builder.vertex(moonMatrix, -closeScale / 3, moonCelestialRadius, closeScale).color(r, g, b, closeA).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    private static void drawMoonlight(BufferBuilder builder,
                                      Matrix4f moonMatrix,
                                      float starBrightness,
                                      float moonCelestialRadius,
                                      float x0,
                                      float y0,
                                      float x1,
                                      float y1) {
        Blending.DEFAULT.apply();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, starBrightness);
        RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_MOONLIGHT);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).uv(x0, y0).endVertex();
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).uv(x1, y0).endVertex();
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).uv(x1, y1).endVertex();
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).uv(x0, y1).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    private static void drawPlanet(BufferBuilder builder, Matrix4f matrix, float radius, float angularSize) {
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        builder.vertex(matrix, -angularSize / 3, radius, angularSize).endVertex();
        builder.vertex(matrix, -angularSize, radius, angularSize / 3).endVertex();
        builder.vertex(matrix, -angularSize, radius, -angularSize / 3).endVertex();
        builder.vertex(matrix, -angularSize / 3, radius, -angularSize).endVertex();
        builder.vertex(matrix, angularSize / 3, radius, -angularSize).endVertex();
        builder.vertex(matrix, angularSize, radius, -angularSize / 3).endVertex();
        builder.vertex(matrix, angularSize, radius, angularSize / 3).endVertex();
        builder.vertex(matrix, angularSize / 3, radius, angularSize).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    private static void drawPole(Matrix4f matrix, BufferBuilder builder, float celestialRadius) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        builder.vertex(matrix, celestialRadius, 0, 0).endVertex();
        for (int i = 0; i < 8; i++) {
            builder.vertex(matrix, celestialRadius, MathHelper.sin(i), MathHelper.cos(i)).endVertex();
        }
        builder.end();
        BufferUploader.end(builder);
    }

    private static void drawSolarEclipse(ClientLevel level,
                                         float partialTicks,
                                         Matrix4f sunMatrix,
                                         BufferBuilder builder,
                                         float radius,
                                         DimensionOverworld dimension) {
        float intensity = dimension.getSolarEclipseIntensity();
        if (intensity <= 1.0F / 81.0F) {
            drawSun(sunMatrix, builder, radius);
            float rainStrength = 1.0F - level.getRainLevel(partialTicks);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainStrength * intensity * 81.0F);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_SOLAR_ECLIPSE);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
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
        BufferUploader.end(builder);
    }

    private static void drawSquare(PoseStack matrices, BufferBuilder builder, float celestialRadius) {
        Matrix4f pose = matrices.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        //First quad
        builder.vertex(pose, -2.5f - 0.5f, celestialRadius, 2.5f - 0.5f).endVertex();
        builder.vertex(pose, -2.5f + 0.5f, celestialRadius, 2.5f - 0.5f).endVertex();
        builder.vertex(pose, -2.5f + 0.5f, celestialRadius, 2.5f + 0.5f).endVertex();
        builder.vertex(pose, -2.5f - 0.5f, celestialRadius, 2.5f + 0.5f).endVertex();
        //Second quad
        builder.vertex(pose, -2.5f - 0.5f, celestialRadius, -2.5f - 0.5f).endVertex();
        builder.vertex(pose, -2.5f + 0.5f, celestialRadius, -2.5f - 0.5f).endVertex();
        builder.vertex(pose, -2.5f + 0.5f, celestialRadius, -2.5f + 0.5f).endVertex();
        builder.vertex(pose, -2.5f - 0.5f, celestialRadius, -2.5f + 0.5f).endVertex();
        //Third quad
        builder.vertex(pose, 2.5f - 0.5f, celestialRadius, -2.5f - 0.5f).endVertex();
        builder.vertex(pose, 2.5f + 0.5f, celestialRadius, -2.5f - 0.5f).endVertex();
        builder.vertex(pose, 2.5f + 0.5f, celestialRadius, -2.5f + 0.5f).endVertex();
        builder.vertex(pose, 2.5f - 0.5f, celestialRadius, -2.5f + 0.5f).endVertex();
        //Fourth quad
        builder.vertex(pose, 2.5f - 0.5f, celestialRadius, 2.5f - 0.5f).endVertex();
        builder.vertex(pose, 2.5f + 0.5f, celestialRadius, 2.5f - 0.5f).endVertex();
        builder.vertex(pose, 2.5f + 0.5f, celestialRadius, 2.5f + 0.5f).endVertex();
        builder.vertex(pose, 2.5f - 0.5f, celestialRadius, 2.5f + 0.5f).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    private static void drawStars(BufferBuilder builder) {
        RandomGenerator random = new Random(10_842L);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
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
                double x = dx * EarthHelper.CELESTIAL_SPHERE_RADIUS;
                double y = dy * EarthHelper.CELESTIAL_SPHERE_RADIUS;
                double z = dz * EarthHelper.CELESTIAL_SPHERE_RADIUS;
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

    private static void drawSun(Matrix4f sunMatrix, BufferBuilder builder, float sunCelestialRadius) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_SUN);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(sunMatrix, -SCALE_OF_CELESTIAL, sunCelestialRadius, -SCALE_OF_CELESTIAL).uv(0, 0).endVertex();
        builder.vertex(sunMatrix, SCALE_OF_CELESTIAL, sunCelestialRadius, -SCALE_OF_CELESTIAL).uv(1, 0).endVertex();
        builder.vertex(sunMatrix, SCALE_OF_CELESTIAL, sunCelestialRadius, SCALE_OF_CELESTIAL).uv(1, 1).endVertex();
        builder.vertex(sunMatrix, -SCALE_OF_CELESTIAL, sunCelestialRadius, SCALE_OF_CELESTIAL).uv(0, 1).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    private void createDarkSky() {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        if (this.darkBuffer != null) {
            this.darkBuffer.close();
        }
        this.darkBuffer = new VertexBuffer();
        buildSkyDisc(builder, -16.0F);
        this.darkBuffer.upload(builder);
    }

    private void createLightSky() {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
        }
        this.skyBuffer = new VertexBuffer();
        buildSkyDisc(builder, 16.0F);
        this.skyBuffer.upload(builder);
    }

    private void createStars() {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        if (this.starBuffer != null) {
            this.starBuffer.close();
        }
        this.starBuffer = new VertexBuffer();
        drawStars(builder);
        builder.end();
        this.starBuffer.upload(builder);
    }

    private void drawLunarEclipse(ClientLevel level, float partialTicks, Matrix4f moonMatrix, BufferBuilder builder, float radius) {
        MoonPhase phase = this.dimension.getEclipsePhase();
        float textureX0 = phase.getTextureX();
        float textureY0 = phase.getTextureY();
        float textureX1 = textureX0 + 0.2f;
        float textureY1 = textureY0 + 0.25f;
        float rainStrength = 1.0f - level.getRainLevel(partialTicks);
        drawMoonlight(builder, moonMatrix, 1.0F - rainStrength * this.dimension.getSunBrightness(partialTicks), radius, textureX0, textureY0,
                      textureX1, textureY1);
        Blending.DEFAULT.apply();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainStrength);
        RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_LUNAR_ECLIPSE);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
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
        BufferUploader.end(builder);
    }

    private void drawMoon(BufferBuilder builder, Matrix4f moonMatrix, float rainStrength, float moonCelestialRadius, float partialTicks) {
        MoonPhase phase = this.dimension.getMoonPhase();
        float textureX0 = phase.getTextureX();
        float textureY0 = phase.getTextureY();
        float textureX1 = textureX0 + 0.2f;
        float textureY1 = textureY0 + 0.25f;
        drawMoonlight(builder, moonMatrix, 1 - this.dimension.getSunBrightness(partialTicks) * rainStrength, moonCelestialRadius, textureX0,
                      textureY0, textureX1, textureY1);
        Blending.DEFAULT.apply();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainStrength);
        RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_MOON);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).uv(textureX0, textureY0).endVertex();
        builder.vertex(moonMatrix, SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).uv(textureX1, textureY0).endVertex();
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, moonCelestialRadius, SCALE_OF_CELESTIAL).uv(textureX1, textureY1).endVertex();
        builder.vertex(moonMatrix, -SCALE_OF_CELESTIAL, moonCelestialRadius, -SCALE_OF_CELESTIAL).uv(textureX0, textureY1).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    @Override
    public void render(int ticks, float partialTicks, PoseStack matrices, ClientLevel level, Minecraft mc) {
        mc.getProfiler().push("init");
        this.dimension.setLevel(level);
        RenderSystem.disableTexture();
        float latitude = this.dimension.getLatitude();
        if (oldLatitude != latitude) {
            oldLatitude = latitude;
            MathHelper.getExtendedQuaternion(LATITUDE_TRANSFORM).set(Vector3f.ZP, latitude, true);
        }
        float sunRightAscension = this.dimension.getSunRightAscension();
        float sunCelestialRadius = this.dimension.getSunCelestialRadius();
        float sunDeclinationOffset = this.dimension.getSunDeclinationOffset();
        float rainStrength = 1.0F - level.getRainLevel(partialTicks);
        Vec3f skyColor = EarthHelper.getSkyColor(level, mc.gameRenderer.getMainCamera().getBlockPosition(), partialTicks, this.dimension);
        float moonDeclinationOffset = this.dimension.getMoonDeclinationOffset();
        float moonRightAscension = this.dimension.getMoonRightAscension();
        if (oldMoonRA != moonRightAscension) {
            oldMoonRA = moonRightAscension;
            MathHelper.getExtendedQuaternion(MOON_TRANSFORM).set(Vector3f.XP, 360.0f * moonRightAscension + 180, true);
        }
        float moonCelestialRadius = this.dimension.getMoonCelestialRadius();
        float sunBrightness = this.dimension.getSunBrightness(partialTicks);
        mc.getProfiler().popPush("dome");
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(skyColor.x, skyColor.y, skyColor.z, 1.0f);
        ShaderInstance shader = RenderSystem.getShader();
        this.skyBuffer.drawWithShader(matrices.last().pose(), RenderSystem.getProjectionMatrix(), shader);
        RenderSystem.enableBlend();
        //Render background stars
        mc.getProfiler().popPush("stars");
        float starBrightness = (1.0f - sunBrightness) * rainStrength;
        if (starBrightness > 0.0F) {
            float starsRightAscension = this.dimension.getStarsRightAscension();
            RenderSystem.disableTexture();
            //Pushed the matrix to draw the background stars
            matrices.pushPose();
            Blending.ADDITIVE_ALPHA.apply();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            MathHelper.getExtendedMatrix(matrices).mulPoseX(360.0f * starsRightAscension + 180);
            RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness, starBrightness);
            this.starBuffer.drawWithShader(matrices.last().pose(), RenderSystem.getProjectionMatrix(), GameRenderer.getPositionShader());
            matrices.popPose();
            //Popped the matrix to draw the background stars
        }
        if (EvolutionConfig.CLIENT.showPlanets.get()) {
            //Render planets
            mc.getProfiler().popPush("planets");
            float planetStarBrightness = 1.25f - sunBrightness * sunBrightness;
            if (sunBrightness >= 0.95f) {
                planetStarBrightness -= (1.0f - (1.0f - sunBrightness) / 0.05f) * 0.25f;
            }
            planetStarBrightness *= rainStrength;
            if (planetStarBrightness > 0.0F) {
                FogRenderer.setupNoFog();
                RenderSystem.disableTexture();
                //Pushed the matrix to draw the planets
                Blending.ADDITIVE_ALPHA.apply();
                matrices.pushPose();
                matrices.mulPose(CommonRotations.YN90);
                matrices.mulPose(LATITUDE_TRANSFORM);
                matrices.translate(PlanetsHelper.getDecOff1Mercury(), 0, 0);
                MathHelper.getExtendedMatrix(matrices).mulPoseX(PlanetsHelper.getHa1Mercury() + 180);
                RenderSystem.setShaderColor(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getDist1Mercury(), PlanetsHelper.getAngSize1Mercury() * 5);
                matrices.popPose();
                matrices.pushPose();
                matrices.mulPose(CommonRotations.YN90);
                matrices.mulPose(LATITUDE_TRANSFORM);
                matrices.translate(PlanetsHelper.getDecOff2Venus(), 0, 0);
                MathHelper.getExtendedMatrix(matrices).mulPoseX(PlanetsHelper.getHa2Venus() + 180);
                RenderSystem.setShaderColor(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getDist2Venus(), PlanetsHelper.getAngSize2Venus() * 5);
                matrices.popPose();
                matrices.pushPose();
                matrices.mulPose(CommonRotations.YN90);
                matrices.mulPose(LATITUDE_TRANSFORM);
                matrices.translate(PlanetsHelper.getDecOff4Mars(), 0, 0);
                MathHelper.getExtendedMatrix(matrices).mulPoseX(PlanetsHelper.getHa4Mars() + 180);
                RenderSystem.setShaderColor(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getDist4Mars(), PlanetsHelper.getAngSize4Mars() * 5);
                matrices.popPose();
                matrices.pushPose();
                matrices.mulPose(CommonRotations.YN90);
                matrices.mulPose(LATITUDE_TRANSFORM);
                matrices.translate(PlanetsHelper.getDecOff5Jupiter(), 0, 0);
                MathHelper.getExtendedMatrix(matrices).mulPoseX(PlanetsHelper.getHa5Jupiter() + 180);
                RenderSystem.setShaderColor(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getDist5Jupiter(), PlanetsHelper.getAngSize5Jupiter() * 5);
                matrices.popPose();
                matrices.pushPose();
                matrices.mulPose(CommonRotations.YN90);
                matrices.mulPose(LATITUDE_TRANSFORM);
                matrices.translate(PlanetsHelper.getDecOff6Saturn(), 0, 0);
                MathHelper.getExtendedMatrix(matrices).mulPoseX(PlanetsHelper.getHa6Saturn() + 180);
                RenderSystem.setShaderColor(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getDist6Saturn(), PlanetsHelper.getAngSize6Saturn() * 5);
                matrices.popPose();
                //Popped the matrix to draw the planets
            }
        }
        //Render moon shadow
        mc.getProfiler().popPush("moonShadow");
        if (starBrightness > 0) {
            //Pushed the matrix to draw moon shadow
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            //Translate the moon in the sky based on monthly amplitude.
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(moonDeclinationOffset, 0, 0);
            matrices.mulPose(MOON_TRANSFORM);
            //Draw the moon shadow
            drawMoonShadow(builder, matrices.last().pose(), moonCelestialRadius, skyColor, starBrightness, 3.0f);
            //Finish drawing moon shadow
            matrices.popPose();
            //Popped the matrix to draw moon shadow
        }
        //Render dusk and dawn
        mc.getProfiler().popPush("duskDawn");
        FogRenderer.setupNoFog();
        float[] duskDawnColors = this.dimension.getDuskDawnColors();
        if (duskDawnColors != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            Blending.ADDITIVE_ALPHA.apply();
            //Pushed matrix to draw dusk and dawn
            matrices.pushPose();
            matrices.mulPose(CommonRotations.XN90);
            MathHelper.getExtendedMatrix(matrices).mulPoseZ(this.dimension.getSunAzimuth());
            Matrix4f duskDawnMatrix = matrices.last().pose();
            builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            builder.vertex(duskDawnMatrix, 0, EarthHelper.CELESTIAL_SPHERE_RADIUS, 0)
                   .color(duskDawnColors[0], duskDawnColors[1], duskDawnColors[2], duskDawnColors[3])
                   .endVertex();
            for (int j = 0; j <= 16; j++) {
                float f6 = j * MathHelper.TAU / 16.0F;
                float f7 = MathHelper.sin(f6);
                float f8 = MathHelper.cos(f6);
                builder.vertex(duskDawnMatrix, -f7 * 120.0F, f8 * 120.0F, f8 * 120.0F * duskDawnColors[3])
                       .color(duskDawnColors[0], duskDawnColors[1], duskDawnColors[2], 0.0F)
                       .endVertex();
            }
            builder.end();
            BufferUploader.end(builder);
            matrices.popPose();
            //Popped matrix of dusk and dawn
        }
        //Render the sun
        mc.getProfiler().popPush("sun");
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        Blending.ADDITIVE_ALPHA.apply();
        //Pushed matrix to draw the sun
        matrices.pushPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainStrength);
        matrices.mulPose(CommonRotations.YN90);
        //Translate the sun in the sky based on season.
        matrices.mulPose(LATITUDE_TRANSFORM);
        matrices.translate(sunDeclinationOffset, 0, 0);
        MathHelper.getExtendedMatrix(matrices).mulPoseX(360.0f * sunRightAscension + 180);
        //Draw the sun
        if (this.dimension.isInSolarEclipse()) {
            drawSolarEclipse(level, partialTicks, matrices.last().pose(), builder, sunCelestialRadius, this.dimension);
        }
        else {
            drawSun(matrices.last().pose(), builder, sunCelestialRadius);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        matrices.popPose();
        //Popped matrix of the sun
        mc.getProfiler().popPush("moon");
        RenderSystem.enableBlend();
        //Pushed the matrix to draw the moon
        matrices.pushPose();
        matrices.mulPose(CommonRotations.YN90);
        //Translate the moon in the sky based on monthly amplitude.
        matrices.mulPose(LATITUDE_TRANSFORM);
        matrices.translate(moonDeclinationOffset, 0, 0);
        matrices.mulPose(MOON_TRANSFORM);
        RenderSystem.enableTexture();
        //Draw the moon
        if (this.dimension.isInLunarEclipse()) {
            this.drawLunarEclipse(level, partialTicks, matrices.last().pose(), builder, moonCelestialRadius);
        }
        else {
            this.drawMoon(builder, matrices.last().pose(), rainStrength, moonCelestialRadius, partialTicks);
        }
        //Finish drawing moon
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
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
            matrices.mulPose(CommonRotations.YN90);
            //Translate the sun in the sky based on season.
            matrices.mulPose(LATITUDE_TRANSFORM);
            //Draw the celestial equator
            if (equator) {
                RenderSystem.setShaderColor(1.0F, 0.0f, 0.0F, 1.0f);
                drawLine(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            }
            if (poles) {
                RenderSystem.setShaderColor(0.0f, 0.0f, 1.0f, 1.0f);
                drawPole(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
                MathHelper.getExtendedMatrix(matrices).mulPoseY(180);
                RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, 1.0f);
                drawPole(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            matrices.popPose();
            //Popped matrix of celestial equator and poles
        }
        if (ecliptic) {
            RenderSystem.disableTexture();
            //Pushed matrix to draw the ecliptic
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            //Translate the sun in the sky based on season.
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(sunDeclinationOffset, 0, 0);
            //Draw the ecliptic
            RenderSystem.setShaderColor(0.0F, 1.0f, 0.0F, 1.0f);
            drawLine(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            matrices.popPose();
            //Popped matrix of the ecliptic
        }
        if (planets) {
            RenderSystem.disableTexture();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(PlanetsHelper.getDecOff1Mercury(), 0, 0);
            MathHelper.getExtendedMatrix(matrices).mulPoseX(PlanetsHelper.getHa1Mercury() + 180);
            RenderSystem.setShaderColor(1.0F, 0.0f, 0.0F, 1.0f);
            drawSquare(matrices, builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(PlanetsHelper.getDecOff2Venus(), 0, 0);
            MathHelper.getExtendedMatrix(matrices).mulPoseX(PlanetsHelper.getHa2Venus() + 180);
            RenderSystem.setShaderColor(1.0F, 0.5f, 0.0F, 1.0f);
            drawSquare(matrices, builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(PlanetsHelper.getDecOff4Mars(), 0, 0);
            MathHelper.getExtendedMatrix(matrices).mulPoseX(PlanetsHelper.getHa4Mars() + 180);
            RenderSystem.setShaderColor(1.0F, 1.0f, 0.0F, 1.0f);
            drawSquare(matrices, builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(PlanetsHelper.getDecOff5Jupiter(), 0, 0);
            MathHelper.getExtendedMatrix(matrices).mulPoseX(PlanetsHelper.getHa5Jupiter() + 180);
            RenderSystem.setShaderColor(0.0F, 1.0f, 0.0F, 1.0f);
            drawSquare(matrices, builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(PlanetsHelper.getDecOff6Saturn(), 0, 0);
            MathHelper.getExtendedMatrix(matrices).mulPoseX(PlanetsHelper.getHa6Saturn() + 180);
            RenderSystem.setShaderColor(0.0F, 1.0f, 1.0F, 1.0f);
            drawSquare(matrices, builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            matrices.popPose();
        }
        mc.getProfiler().popPush("void");
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0f);
        double distanceAboveTheHorizon = mc.player.getEyePosition(partialTicks).y - level.getLevelData().getHorizonHeight(level);
        if (distanceAboveTheHorizon < 0.0) {
            //Pushed matrix to draw the dark void
            matrices.pushPose();
            matrices.translate(0, 12, 0);
            this.darkBuffer.drawWithShader(matrices.last().pose(), RenderSystem.getProjectionMatrix(), shader);
            matrices.popPose();
            //Popped matrix of the dark void
        }
        if (level.effects().hasGround()) {
            RenderSystem.setShaderColor(skyColor.x * 0.2F + 0.04F, skyColor.y * 0.2F + 0.04F, skyColor.z * 0.6F + 0.1F, 1.0f);
        }
        else {
            RenderSystem.setShaderColor(skyColor.x, skyColor.y, skyColor.z, 1.0f);
        }
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        mc.getProfiler().pop();
    }
}
