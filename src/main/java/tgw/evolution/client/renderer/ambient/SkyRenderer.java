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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.lwjgl.opengl.GL11;
import tgw.evolution.client.renderer.DimensionOverworld;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.renderer.chunk.SkyFogSetup;
import tgw.evolution.client.util.Blending;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.mixin.AccessorRenderSystem;
import tgw.evolution.util.constants.CommonRotations;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3f;
import tgw.evolution.util.physics.EarthHelper;
import tgw.evolution.util.physics.MoonPhase;
import tgw.evolution.util.physics.PlanetsHelper;

import java.util.Random;
import java.util.random.RandomGenerator;

public class SkyRenderer {

    private static final float SCALE_OF_CELESTIAL = 10.0f;
    private static final Quaternion LATITUDE_TRANSFORM = Vector3f.ZP.rotationDegrees(0);
    private static final Quaternion MOON_TRANSFORM = Vector3f.XP.rotationDegrees(180);
    private static float oldLatitude;
    private static float oldMoonRA;
    private @Nullable VertexBuffer darkBuffer;
    private final DimensionOverworld dimension;
    private boolean fogEnabled;
    private SkyFogSetup fogSetup;
    private @Nullable VertexBuffer skyBuffer;
    private @Nullable VertexBuffer starBuffer;

    public SkyRenderer(DimensionOverworld dimension) {
        this.dimension = dimension;
        this.createStars();
        this.createLightSky();
        this.createDarkSky();
    }

    private static void buildSkyDisc(BufferBuilder builder, float y) {
        RenderSystem.setShader(RenderHelper.SHADER_POSITION);
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        builder.vertex(0, y, 0).endVertex();
        float f = Math.signum(y) * 512.0F;
        for (int i = -180; i <= 180; i += 45) {
            builder.vertex(f * MathHelper.cosDeg(i), y, 512.0F * MathHelper.sinDeg(i)).endVertex();
        }
        builder.end();
    }

    private static void drawCelestial(Matrix4f matrix, BufferBuilder builder, float x0, float y0, float x1, float y1) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, SCALE_OF_CELESTIAL, EarthHelper.CELESTIAL_SPHERE_RADIUS, -SCALE_OF_CELESTIAL).uv(x0, y0).endVertex();
        builder.vertex(matrix, SCALE_OF_CELESTIAL, EarthHelper.CELESTIAL_SPHERE_RADIUS, SCALE_OF_CELESTIAL).uv(x1, y0).endVertex();
        builder.vertex(matrix, -SCALE_OF_CELESTIAL, EarthHelper.CELESTIAL_SPHERE_RADIUS, SCALE_OF_CELESTIAL).uv(x1, y1).endVertex();
        builder.vertex(matrix, -SCALE_OF_CELESTIAL, EarthHelper.CELESTIAL_SPHERE_RADIUS, -SCALE_OF_CELESTIAL).uv(x0, y1).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    private static void drawCelestial(Matrix4f matrix, BufferBuilder builder) {
        drawCelestial(matrix, builder, 0, 0, 1, 1);
    }

    private static void drawLine(Matrix4f matrix, BufferBuilder builder, float celestialRadius) {
        AccessorRenderSystem.setShader(GameRenderer.getPositionShader());
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int i = 0; i < 45; i++) {
            builder.vertex(matrix, -0.5f, celestialRadius * MathHelper.cosDeg(8 * i), celestialRadius * MathHelper.sinDeg(8 * i)).endVertex();
            builder.vertex(matrix, 0.5f, celestialRadius * MathHelper.cosDeg(8 * i), celestialRadius * MathHelper.sinDeg(8 * i)).endVertex();
            builder.vertex(matrix, 0.5f, celestialRadius * MathHelper.cosDeg(8 * (i + 1)), celestialRadius * MathHelper.sinDeg(8 * (i + 1))).endVertex();
            builder.vertex(matrix, -0.5f, celestialRadius * MathHelper.cosDeg(8 * (i + 1)), celestialRadius * MathHelper.sinDeg(8 * (i + 1))).endVertex();
        }
        builder.end();
        BufferUploader.end(builder);
    }

    private static void drawMoonlight(Matrix4f moonMatrix, BufferBuilder builder, float x0, float y0, float x1, float y1) {
        Blending.DEFAULT.apply();
        RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_MOONLIGHT);
        drawCelestial(moonMatrix, builder, x0, y0, x1, y1);
    }

    private static void drawPlanet(BufferBuilder builder, Matrix4f matrix, float angularSize, @Range(from = 1, to = 6) int planet) {
        planet -= 1;
        float x0 = (planet % 3) / 3.0f;
        float x1 = x0 + 1 / 3.0f;
        float y0 = planet < 3 ? 0 : 0.5f;
        float y1 = y0 + 0.5f;
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, angularSize, EarthHelper.CELESTIAL_SPHERE_RADIUS, -angularSize).uv(x0, y0).endVertex();
        builder.vertex(matrix, angularSize, EarthHelper.CELESTIAL_SPHERE_RADIUS, angularSize).uv(x1, y0).endVertex();
        builder.vertex(matrix, -angularSize, EarthHelper.CELESTIAL_SPHERE_RADIUS, angularSize).uv(x1, y1).endVertex();
        builder.vertex(matrix, -angularSize, EarthHelper.CELESTIAL_SPHERE_RADIUS, -angularSize).uv(x0, y1).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    private static void drawPole(Matrix4f matrix, BufferBuilder builder) {
        AccessorRenderSystem.setShader(GameRenderer.getPositionShader());
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        builder.vertex(matrix, EarthHelper.CELESTIAL_SPHERE_RADIUS, 0, 0).endVertex();
        for (int i = 0; i <= 8; i++) {
            builder.vertex(matrix, EarthHelper.CELESTIAL_SPHERE_RADIUS, MathHelper.sinDeg(45 * i), MathHelper.cosDeg(45 * i)).endVertex();
        }
        builder.end();
        BufferUploader.end(builder);
    }

    private static void drawSquare(PoseStack matrices, BufferBuilder builder) {
        Matrix4f pose = matrices.last().pose();
        AccessorRenderSystem.setShader(GameRenderer.getPositionShader());
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        //First quad
        builder.vertex(pose, -2.5f - 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, 2.5f - 0.5f).endVertex();
        builder.vertex(pose, -2.5f + 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, 2.5f - 0.5f).endVertex();
        builder.vertex(pose, -2.5f + 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, 2.5f + 0.5f).endVertex();
        builder.vertex(pose, -2.5f - 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, 2.5f + 0.5f).endVertex();
        //Second quad
        builder.vertex(pose, -2.5f - 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, -2.5f - 0.5f).endVertex();
        builder.vertex(pose, -2.5f + 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, -2.5f - 0.5f).endVertex();
        builder.vertex(pose, -2.5f + 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, -2.5f + 0.5f).endVertex();
        builder.vertex(pose, -2.5f - 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, -2.5f + 0.5f).endVertex();
        //Third quad
        builder.vertex(pose, 2.5f - 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, -2.5f - 0.5f).endVertex();
        builder.vertex(pose, 2.5f + 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, -2.5f - 0.5f).endVertex();
        builder.vertex(pose, 2.5f + 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, -2.5f + 0.5f).endVertex();
        builder.vertex(pose, 2.5f - 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, -2.5f + 0.5f).endVertex();
        //Fourth quad
        builder.vertex(pose, 2.5f - 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, 2.5f - 0.5f).endVertex();
        builder.vertex(pose, 2.5f + 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, 2.5f - 0.5f).endVertex();
        builder.vertex(pose, 2.5f + 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, 2.5f + 0.5f).endVertex();
        builder.vertex(pose, 2.5f - 0.5f, EarthHelper.CELESTIAL_SPHERE_RADIUS, 2.5f + 0.5f).endVertex();
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
                lengthSquared = Mth.fastInvSqrt(lengthSquared);
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

    public void clearMemory() {
        if (this.darkBuffer != null) {
            this.darkBuffer.close();
        }
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
        }
        if (this.starBuffer != null) {
            this.starBuffer.close();
        }
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
        RenderSystem.setShader(RenderHelper.SHADER_POSITION);
        if (this.starBuffer != null) {
            this.starBuffer.close();
        }
        this.starBuffer = new VertexBuffer();
        drawStars(builder);
        builder.end();
        this.starBuffer.upload(builder);
    }

    private void drawMoon(Matrix4f matrix, BufferBuilder builder, MoonPhase phase, boolean trueMoon, float rainStrength, float partialTicks) {
        float x0 = phase.getTextureX();
        float y0 = phase.getTextureY();
        float x1 = x0 + 0.2f;
        float y1 = y0 + 0.25f;
        AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
        float starBrightness = 1 - this.dimension.getSunBrightness(partialTicks);
        boolean stencil = false;
        if (trueMoon) {
            float eclipseIntensity = this.dimension.isCloseToLunarEclipse() ? this.dimension.getLunarEclipseIntensity() : 0;
            Blending.DEFAULT.apply();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainStrength);
            RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_MOON);
            if (eclipseIntensity == 0) {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, starBrightness * rainStrength);
                drawMoonlight(matrix, builder, x0, y0, x1, y1);
            }
            else {
                float color = 1 - this.dimension.getLunarEclipseIntensity();
                color = Mth.sqrt(color);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, color * starBrightness * rainStrength);
                drawMoonlight(matrix, builder, x0, y0, x1, y1);
                GL11.glEnable(GL11.GL_STENCIL_TEST);
                RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
                RenderSystem.stencilFunc(GL11.GL_EQUAL, 0, 16);
                stencil = true;
            }
        }
        Blending.DEFAULT.apply();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainStrength);
        RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_MOON);
        drawCelestial(matrix, builder, x0, y0, x1, y1);
        if (stencil) {
            RenderSystem.stencilFunc(GL11.GL_EQUAL, 16, 16);
            float color = Math.min(1 - this.dimension.getLunarEclipseIntensity(), 0.5f);
            if (starBrightness < 1) {
                color = Math.max(color, 1 - starBrightness);
            }
            if (color < 0.1) {
                float red = color < 0.05 ? 0.3f - 6 * color : 0;
                RenderSystem.setShaderColor(0.1f + red, 0.1f, 0.1f, (0.1f - color) * 6 * rainStrength);
            }
            else {
                RenderSystem.setShaderColor(color, color, color, starBrightness * color * rainStrength);
            }
            drawCelestial(matrix, builder, x0, y0, x1, y1);
            GL11.glDisable(GL11.GL_STENCIL_TEST);
        }
    }

    private void drawSun(Matrix4f matrix, BufferBuilder builder, float rainStrength) {
        AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
        RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_SUNLIGHT);
        float eclipseIntensity = this.dimension.isCloseToSolarEclipse() ? this.dimension.getSolarEclipseIntensity() : 0;
        if (eclipseIntensity <= 0.2) {
            drawCelestial(matrix, builder);
        }
        else {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
            RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 4 | 1, 1);
            drawCelestial(matrix, builder);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (1.2f - eclipseIntensity) * rainStrength);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            RenderSystem.stencilFunc(GL11.GL_EQUAL, 4, 4);
            drawCelestial(matrix, builder);
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, rainStrength);
        }
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 0, 1 | 8);
        RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_SUN);
        drawCelestial(matrix, builder);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    private void fog(boolean fog) {
        if (fog != this.fogEnabled) {
            this.fogEnabled = fog;
            if (fog) {
                this.fogSetup.setup();
            }
            else {
                FogRenderer.setupNoFog();
            }
        }
    }

    public void render(float partialTick, PoseStack matrices, ClientLevel level, Minecraft mc, SkyFogSetup fogSetup) {
        mc.getProfiler().push("init");
        this.fogSetup = fogSetup;
        this.fogEnabled = true;
        this.dimension.setLevel(level);
        RenderSystem.disableTexture();
        float latitude = this.dimension.getLatitude();
        if (oldLatitude != latitude) {
            oldLatitude = latitude;
            LATITUDE_TRANSFORM.set(Vector3f.ZP, latitude, true);
        }
        float sunRightAscension = this.dimension.getSunHA();
        float sunDeclinationOffset = this.dimension.getSunDeclinationOffset();
        Vec3f skyColor = EarthHelper.getSkyColor(level, mc.gameRenderer.getMainCamera().getBlockPosition(), partialTick, this.dimension);
        float moonDeclinationOffset = this.dimension.getMoonDeclinationOffset();
        float moonRightAscension = this.dimension.getMoonHA();
        if (oldMoonRA != moonRightAscension) {
            oldMoonRA = moonRightAscension;
            MOON_TRANSFORM.set(Vector3f.XP, moonRightAscension + 180, true);
        }
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        ShaderInstance shader = RenderSystem.getShader();
        assert shader != null;
        RenderSystem.depthMask(false);
        float rainStrength = 1.0F - level.getRainLevel(partialTick);
        float sunBrightness = this.dimension.getSunBrightness(partialTick);
        float starBrightness = (1.0f - sunBrightness) * rainStrength;
        float planetStarBrightness = 1.25f - sunBrightness * sunBrightness;
        if (sunBrightness >= 0.95f) {
            planetStarBrightness -= (1.0f - (1.0f - sunBrightness) / 0.05f) * 0.25f;
        }
        planetStarBrightness *= rainStrength;
        if (planetStarBrightness > 1) {
            planetStarBrightness = 1;
        }
        //Stencil Buffer (8 bits)
        //Bit 0: Moon
        //Bit 1: Sun
        //Bit 2: Moon on Sunlight (for Solar Eclipses)
        //Bit 3: Mercury and Venus (for transits)
        //Bit 4: Earth Shadow (for Lunar Eclipses)
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
        mc.getProfiler().popPush("stencil");
        if (planetStarBrightness > 0 || starBrightness > 0 || this.dimension.isCloseToSolarEclipse()) {
            //Pushed the matrix to draw moon shadow
            matrices.pushPose();
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            //Translate the moon in the sky based on monthly amplitude.
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(moonDeclinationOffset, 0, 0);
            matrices.mulPose(MOON_TRANSFORM);
            //Setup Stencil with value 1
            RenderSystem.colorMask(false, false, false, false);
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 1);
            //Draw the moon shadow
            this.drawMoon(matrices.last().pose(), builder, MoonPhase.FULL_MOON, false, 1.0f, partialTick);
            //Finish drawing moon shadow
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            RenderSystem.colorMask(true, true, true, true);
            matrices.popPose();
            //Popped the matrix to draw moon shadow
        }
        if (starBrightness > 0 || planetStarBrightness > 0) {
            //Pushed the matrix to draw sun shadow
            matrices.pushPose();
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            //Translate the moon in the sky based on monthly amplitude.
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(sunDeclinationOffset, 0, 0);
            matrices.mulPoseX(sunRightAscension + 180);
            //Setup Stencil with value 2
            RenderSystem.colorMask(false, false, false, false);
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilMask(2);
            RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
            RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 2, 2);
            //Draw the moon shadow
            AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
            RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_SUN);
            drawCelestial(matrices.last().pose(), builder);
            //Finish drawing moon shadow
            RenderSystem.stencilMask(255);
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            RenderSystem.colorMask(true, true, true, true);
            matrices.popPose();
            //Popped the matrix to draw sun shadow
        }
        mc.getProfiler().popPush("dome");
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(skyColor.x, skyColor.y, skyColor.z, 1.0f);
        assert this.skyBuffer != null;
        this.skyBuffer.drawWithShader(matrices.last().pose(), RenderSystem.getProjectionMatrix(), shader);
        RenderSystem.enableBlend();
        //Render background stars
        mc.getProfiler().popPush("stars");
        if (starBrightness > 0) {
            float starsRightAscension = this.dimension.getLocalTime();
            RenderSystem.disableTexture();
            this.fog(false);
            //Pushed the matrix to draw the background stars
            matrices.pushPose();
            Blending.ADDITIVE_ALPHA.apply();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.mulPoseX(starsRightAscension);
            RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness, starBrightness);
            assert this.starBuffer != null;
            assert GameRenderer.getPositionShader() != null;
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            RenderSystem.stencilFunc(GL11.GL_EQUAL, 0, 1 | 2);
            this.starBuffer.drawWithShader(matrices.last().pose(), RenderSystem.getProjectionMatrix(), GameRenderer.getPositionShader());
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            matrices.popPose();
            //Popped the matrix to draw the background stars
        }
        if (EvolutionConfig.SHOW_PLANETS.get()) {
            //Render planets
            mc.getProfiler().popPush("planets");
            boolean transit = false;
            boolean started = false;
            //Mercury
            if (planetStarBrightness > 0 || PlanetsHelper.is1MercuryTransiting()) {
                this.fog(false);
                RenderSystem.disableTexture();
                GL11.glEnable(GL11.GL_STENCIL_TEST);
                Blending.ADDITIVE_ALPHA.apply();
                AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
                RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_PLANETS);
                started = true;
                if (PlanetsHelper.is1MercuryTransiting()) {
                    RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
                    RenderSystem.stencilFunc(GL11.GL_EQUAL, 8, 1);
                    float dRA = Mth.abs(Mth.wrapDegrees(sunRightAscension - PlanetsHelper.getHa1Mercury()));
                    float color = 1.0f;
                    if (dRA < 90) {
                        color = 0.1f + 0.9f / 90 * dRA;
                    }
                    RenderSystem.setShaderColor(color, color, color, planetStarBrightness);
                    transit = true;
                }
                else {
                    RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
                    RenderSystem.stencilFunc(GL11.GL_EQUAL, 0, 1 | 2);
                    RenderSystem.setShaderColor(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                }
                matrices.pushPose();
                matrices.mulPose(CommonRotations.YN90);
                matrices.mulPose(LATITUDE_TRANSFORM);
                matrices.translate(PlanetsHelper.getDecOff1Mercury(), 0, 0);
                matrices.mulPoseX(PlanetsHelper.getHa1Mercury() + 180);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getAngSize1Mercury() * 10, 1);
                matrices.popPose();
            }
            if (planetStarBrightness > 0 || PlanetsHelper.is2VenusTransiting()) {
                if (!started) {
                    this.fog(false);
                    RenderSystem.disableTexture();
                    GL11.glEnable(GL11.GL_STENCIL_TEST);
                    Blending.ADDITIVE_ALPHA.apply();
                    AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
                    RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_PLANETS);
                    started = true;
                }
                //Venus
                if (PlanetsHelper.is2VenusTransiting()) {
                    if (!transit) {
                        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
                        RenderSystem.stencilFunc(GL11.GL_EQUAL, 8, 1);
                        float dRA = Mth.abs(Mth.wrapDegrees(sunRightAscension - PlanetsHelper.getHa2Venus()));
                        float color = 1.0f;
                        if (dRA < 90) {
                            color = 0.1f + 0.9f / 90 * dRA;
                        }
                        RenderSystem.setShaderColor(color, color, color, planetStarBrightness);
                        transit = true;
                    }
                }
                else {
                    if (transit) {
                        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
                        RenderSystem.stencilFunc(GL11.GL_EQUAL, 0, 1 | 2);
                        RenderSystem.setShaderColor(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                        transit = false;
                    }
                }
                matrices.pushPose();
                matrices.mulPose(CommonRotations.YN90);
                matrices.mulPose(LATITUDE_TRANSFORM);
                matrices.translate(PlanetsHelper.getDecOff2Venus(), 0, 0);
                matrices.mulPoseX(PlanetsHelper.getHa2Venus() + 180);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getAngSize2Venus() * 10, 2);
                matrices.popPose();
            }
            if (planetStarBrightness > 0.0F) {
                //Mars
                if (transit) {
                    RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
                    RenderSystem.stencilFunc(GL11.GL_EQUAL, 0, 1 | 2);
                    RenderSystem.setShaderColor(planetStarBrightness, planetStarBrightness, planetStarBrightness, planetStarBrightness);
                }
                matrices.pushPose();
                matrices.mulPose(CommonRotations.YN90);
                matrices.mulPose(LATITUDE_TRANSFORM);
                matrices.translate(PlanetsHelper.getDecOff4Mars(), 0, 0);
                matrices.mulPoseX(PlanetsHelper.getHa4Mars() + 180);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getAngSize4Mars() * 10, 4);
                matrices.popPose();
                //Jupiter
                matrices.pushPose();
                matrices.mulPose(CommonRotations.YN90);
                matrices.mulPose(LATITUDE_TRANSFORM);
                matrices.translate(PlanetsHelper.getDecOff5Jupiter(), 0, 0);
                matrices.mulPoseX(PlanetsHelper.getHa5Jupiter() + 180);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getAngSize5Jupiter() * 10, 5);
                matrices.popPose();
                //Saturn
                matrices.pushPose();
                matrices.mulPose(CommonRotations.YN90);
                matrices.mulPose(LATITUDE_TRANSFORM);
                matrices.translate(PlanetsHelper.getDecOff6Saturn(), 0, 0);
                matrices.mulPoseX(PlanetsHelper.getHa6Saturn() + 180);
                drawPlanet(builder, matrices.last().pose(), PlanetsHelper.getAngSize6Saturn() * 10, 6);
                matrices.popPose();
                //Popped the matrix to draw the planets
            }
            if (started) {
                GL11.glDisable(GL11.GL_STENCIL_TEST);
            }
        }
        //Render the sun
        mc.getProfiler().popPush("sun");
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        this.fog(true);
        Blending.ADDITIVE_ALPHA.apply();
        //Pushed matrix to draw the sun
        matrices.pushPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainStrength);
        matrices.mulPose(CommonRotations.YN90);
        //Translate the sun in the sky based on season.
        matrices.mulPose(LATITUDE_TRANSFORM);
        matrices.translate(sunDeclinationOffset, 0, 0);
        matrices.mulPoseX(sunRightAscension + 180);
        //Draw the sun
        this.drawSun(matrices.last().pose(), builder, rainStrength);
        //Earth Shadow
        if (this.dimension.isCloseToLunarEclipse()) {
            Blending.DEFAULT.apply();
            RenderSystem.setShaderTexture(0, EvolutionResources.ENVIRONMENT_PLANETS);
            float declinationOffset = Math.abs(moonDeclinationOffset + sunDeclinationOffset);
            if (declinationOffset <= 0.75) {
                matrices.translate(moonDeclinationOffset - sunDeclinationOffset, 0, 0);
            }
            else if (declinationOffset >= 5) {
                matrices.translate(-2 * sunDeclinationOffset, 0, 0);
            }
            else {
                float rel = MathHelper.relativize(declinationOffset, 0.75f, 5.0f);
                rel = 1 - rel;
                rel = Mth.sqrt(rel);
                matrices.translate(rel * moonDeclinationOffset - (2 - rel) * sunDeclinationOffset, 0, 0);
            }
            matrices.mulPoseX(180);
            RenderSystem.colorMask(false, false, false, false);
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 16, 0);
            RenderSystem.stencilMask(16);
            drawCelestial(matrices.last().pose(), builder, 2 / 3.0f, 0, 1, 0.5f);
            RenderSystem.stencilMask(255);
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            RenderSystem.colorMask(true, true, true, true);
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
        this.drawMoon(matrices.last().pose(), builder, this.dimension.getMoonPhase(), true, rainStrength, partialTick);
        //Finish drawing moon
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        matrices.popPose();
        //Popped the matrix to draw the moon
        //Render dusk and dawn
        mc.getProfiler().popPush("duskDawn");
        float[] duskDawnColors = this.dimension.getDuskDawnColors();
        if (duskDawnColors != null) {
            AccessorRenderSystem.setShader(GameRenderer.getPositionColorShader());
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            Blending.ADDITIVE_ALPHA.apply();
            this.fog(false);
            //Pushed matrix to draw dusk and dawn
            matrices.pushPose();
            matrices.mulPoseX(-90);
            matrices.mulPoseZ(this.dimension.getSunAzimuth());
            Matrix4f duskDawnMatrix = matrices.last().pose();
            builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            builder.vertex(duskDawnMatrix, 0, EarthHelper.CELESTIAL_SPHERE_RADIUS, 0)
                   .color(duskDawnColors[0], duskDawnColors[1], duskDawnColors[2], duskDawnColors[3])
                   .endVertex();
            for (int i = 0; i <= 16; i++) {
                float angle = Mth.TWO_PI / 16.0F * i;
                float sin = Mth.sin(angle);
                float cos = Mth.cos(angle);
                builder.vertex(duskDawnMatrix, -sin * 120, cos * 120, cos * 120 * duskDawnColors[3])
                       .color(duskDawnColors[0], duskDawnColors[1], duskDawnColors[2], 0.0F)
                       .endVertex();
            }
            builder.end();
            BufferUploader.end(builder);
            RenderSystem.disableBlend();
            matrices.popPose();
            //Popped matrix of dusk and dawn
        }
        //Render debug
        mc.getProfiler().popPush("debug");
        boolean forceAll = EvolutionConfig.CELESTIAL_FORCE_ALL.get();
        boolean equator = forceAll || EvolutionConfig.CELESTIAL_EQUATOR.get();
        boolean poles = forceAll || EvolutionConfig.CELESTIAL_POLES.get();
        boolean ecliptic = forceAll || EvolutionConfig.ECLIPTIC.get();
        boolean sunPath = forceAll || EvolutionConfig.SUN_PATH.get();
        boolean planets = EvolutionConfig.SHOW_PLANETS.get() && (forceAll || EvolutionConfig.PLANETS.get());
        if (equator || poles) {
            RenderSystem.disableTexture();
            this.fog(false);
            //Pushed matrix to draw celestial equator and poles
            matrices.pushPose();
            matrices.mulPoseY(-90);
            //Translate the sun in the sky based on season.
            matrices.mulPose(LATITUDE_TRANSFORM);
            //Draw the celestial equator
            if (equator) {
                RenderSystem.setShaderColor(1.0F, 0.0f, 0.0F, 1.0f);
                drawLine(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            }
            if (poles) {
                RenderSystem.setShaderColor(0.0f, 0.0f, 1.0f, 1.0f);
                drawPole(matrices.last().pose(), builder);
                matrices.mulPoseY(180);
                RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, 1.0f);
                drawPole(matrices.last().pose(), builder);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            matrices.popPose();
            //Popped matrix of celestial equator and poles
        }
        if (sunPath) {
            RenderSystem.disableTexture();
            this.fog(false);
            //Pushed matrix to draw the sun path
            matrices.pushPose();
            matrices.mulPoseY(-90);
            //Translate the sun in the sky based on season.
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(sunDeclinationOffset, 0, 0);
            //Draw the sun path
            RenderSystem.setShaderColor(0.0F, 1.0f, 0.0F, 1.0f);
            drawLine(matrices.last().pose(), builder, EarthHelper.CELESTIAL_SPHERE_RADIUS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            matrices.popPose();
            //Popped matrix of the sun path
        }
        if (ecliptic) {
            RenderSystem.disableTexture();
            this.fog(false);
            matrices.pushPose();
            matrices.mulPoseX(-latitude);
            matrices.mulPoseZ(this.dimension.getLocalTime() + 180);
            matrices.mulPoseX(-23.5f);
            RenderSystem.setShaderColor(1.0f, 0.0f, 1.0f, 1.0f);
            Matrix4f matrix = matrices.last().pose();
            AccessorRenderSystem.setShader(GameRenderer.getPositionShader());
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            for (int i = 0; i < 45; i++) {
                builder.vertex(matrix, EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.cosDeg(8 * i), EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.sinDeg(8 * i), -0.5f).endVertex();
                builder.vertex(matrix, EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.cosDeg(8 * i), EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.sinDeg(8 * i), 0.5f).endVertex();
                builder.vertex(matrix, EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.cosDeg(8 * (i + 1)), EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.sinDeg(8 * (i + 1)), 0.5f).endVertex();
                builder.vertex(matrix, EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.cosDeg(8 * (i + 1)), EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.sinDeg(8 * (i + 1)), -0.5f).endVertex();
            }
            builder.end();
            BufferUploader.end(builder);
            //Autumn Equinox
            RenderSystem.setShaderColor(1.0f, 0.5f, 0.0f, 1.0f);
            drawPole(matrix, builder);
            //Summer Solstice
            RenderSystem.setShaderColor(1.0f, 1.0f, 0.0f, 1.0f);
            matrices.mulPose(CommonRotations.ZP90);
            drawPole(matrix, builder);
            //Spring Equinox
            RenderSystem.setShaderColor(0.5f, 1.0f, 0.0f, 1.0f);
            matrices.mulPose(CommonRotations.ZP90);
            drawPole(matrix, builder);
            //Winter Solstice
            RenderSystem.setShaderColor(0.8f, 0.8f, 0.8f, 1.0f);
            matrices.mulPose(CommonRotations.ZP90);
            drawPole(matrix, builder);
            matrices.popPose();
        }
        if (planets) {
            this.fog(false);
            RenderSystem.disableTexture();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(moonDeclinationOffset, 0, 0);
            matrices.mulPose(MOON_TRANSFORM);
            RenderSystem.setShaderColor(0.75F, 0.75F, 0.75F, 1.0f);
            drawSquare(matrices, builder);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(PlanetsHelper.getDecOff1Mercury(), 0, 0);
            matrices.mulPoseX(PlanetsHelper.getHa1Mercury() + 180);
            RenderSystem.setShaderColor(1.0F, 0.0f, 0.0F, 1.0f);
            drawSquare(matrices, builder);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(PlanetsHelper.getDecOff2Venus(), 0, 0);
            matrices.mulPoseX(PlanetsHelper.getHa2Venus() + 180);
            RenderSystem.setShaderColor(1.0F, 0.5f, 0.0F, 1.0f);
            drawSquare(matrices, builder);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(PlanetsHelper.getDecOff4Mars(), 0, 0);
            matrices.mulPoseX(PlanetsHelper.getHa4Mars() + 180);
            RenderSystem.setShaderColor(1.0F, 1.0f, 0.0F, 1.0f);
            drawSquare(matrices, builder);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(PlanetsHelper.getDecOff5Jupiter(), 0, 0);
            matrices.mulPoseX(PlanetsHelper.getHa5Jupiter() + 180);
            RenderSystem.setShaderColor(0.0F, 1.0f, 0.0F, 1.0f);
            drawSquare(matrices, builder);
            matrices.popPose();
            matrices.pushPose();
            matrices.mulPose(CommonRotations.YN90);
            matrices.mulPose(LATITUDE_TRANSFORM);
            matrices.translate(PlanetsHelper.getDecOff6Saturn(), 0, 0);
            matrices.mulPoseX(PlanetsHelper.getHa6Saturn() + 180);
            RenderSystem.setShaderColor(0.0F, 1.0f, 1.0F, 1.0f);
            drawSquare(matrices, builder);
            matrices.popPose();
        }
        mc.getProfiler().popPush("void");
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0f);
        Entity cameraEntity = mc.getCameraEntity();
        assert cameraEntity != null;
        double distanceAboveTheHorizon = cameraEntity.getEyePosition(partialTick).y - level.getLevelData().getHorizonHeight(level);
        if (distanceAboveTheHorizon < 0.0) {
            this.fog(true);
            //Pushed matrix to draw the dark void
            matrices.pushPose();
            matrices.translate(0, 12, 0);
            assert this.darkBuffer != null;
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
        this.fog(true);
        mc.getProfiler().pop();
    }
}
