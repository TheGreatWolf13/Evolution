package tgw.evolution.world.dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.Evolution;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.util.*;

import javax.annotation.Nullable;

public class DimensionOverworld {

    private static final float[] DUSK_DAWN_COLORS = new float[4];
    private final Vec3f lastFogColor = new Vec3f(0, 0, 0);
    private float[] duskDawnColors;
    private MoonPhase eclipsePhase = MoonPhase.FULL_MOON;
    private Vector3d fogColor = Vector3d.ZERO;
    private boolean isInLunarEclipse;
    private boolean isInSolarEclipse;
    private float latitude;
    private float[] lightBrightnessTable;
    private float lunarEclipseDDeclination;
    private float lunarEclipseDRightAscension;
    private float moonAltitude;
    private float moonCelestialRadius;
    private float moonDeclinationOffset;
    private MoonPhase moonPhase = MoonPhase.NEW_MOON;
    private float moonRightAscension;
    private float solarEclipseDDeclination;
    private float solarEclipseDRightAscension;
    private float starsRightAscension;
    private float sunAltitude;
    private float sunAzimuth;
    private float sunCelestialRadius;
    private float sunDeclinationOffset;
    private float sunRightAscension;
    private ClientWorld world;

    public DimensionOverworld() {
        this.generateLightBrightnessTable();
    }

    private Vector3d calculateFogColor() {
        float sunAngle = 1.0f;
        if (this.sunAltitude > 80) {
            sunAngle = -this.sunAltitude * this.sunAltitude / 784.0f + 10.0f * this.sunAltitude / 49.0f - 7.163_265f;
            sunAngle = MathHelper.clamp(sunAngle, 0.0F, 1.0F);
        }
        if (this.isInSolarEclipse) {
            float intensity = 1.0F - this.getSolarEclipseIntensity();
            if (intensity < sunAngle) {
                sunAngle = intensity;
            }
        }
        float r = 0.752_941_2F;
        r *= sunAngle;
        float g = 0.847_058_83F;
        g *= sunAngle;
        float b = 1.0F;
        b *= sunAngle;
        return new Vector3d(r, g, b);
    }

    private void generateLightBrightnessTable() {
        this.lightBrightnessTable = new float[16];
        for (int lightLevel = 0; lightLevel <= 15; ++lightLevel) {
            float f1 = 1.0F - lightLevel / 15.0F;
            this.lightBrightnessTable[lightLevel] = (1.0F - f1) / (f1 * 3.0F + 1.0F);
        }
    }

    public float getAmbientLight(int light) {
        return this.lightBrightnessTable[light];
    }

    public Vector3d getBrightnessDependentFogColor(Vector3d biomeFogColor, float multiplier) {
        return biomeFogColor.multiply(multiplier * 0.97 + 0.03, multiplier * 0.97 + 0.03, multiplier * 0.97 + 0.03);
    }

    public float[] getDuskDawnColors() {
        return this.duskDawnColors;
    }

    public MoonPhase getEclipsePhase() {
        return this.eclipsePhase;
    }

    public Vector3d getFogColor() {
        return this.fogColor;
    }

    public Vec3f getLastFogColor() {
        return this.lastFogColor;
    }

    @OnlyIn(Dist.CLIENT)
    public float getLatitude() {
        return this.latitude;
    }

    public int getLunarEclipseDeclinationIndex() {
        return Math.round(this.lunarEclipseDDeclination);
    }

    public float getLunarEclipseIntensity() {
        float angleMod = 9.0F - Math.abs(this.lunarEclipseDRightAscension);
        float amplitudeMod = 9.0F - Math.abs(this.lunarEclipseDDeclination);
        return angleMod * amplitudeMod / 81.0F;
    }

    public int getLunarEclipseRightAscensionIndex() {
        return Math.round(this.lunarEclipseDRightAscension);
    }

    @OnlyIn(Dist.CLIENT)
    public float getMoonAltitude() {
        return this.moonAltitude;
    }

    public float getMoonCelestialRadius() {
        return this.moonCelestialRadius;
    }

    public float getMoonDeclinationOffset() {
        return this.moonDeclinationOffset;
    }

    public MoonPhase getMoonPhase() {
        return this.moonPhase;
    }

    public float getMoonRightAscension() {
        return this.moonRightAscension;
    }

    public float getSkyBrightness(float partialTicks) {
        float moonlightMult = this.moonlightMult();
        float moonlightMin = 1 - moonlightMult;
        return this.getSunBrightness(partialTicks) * moonlightMult + moonlightMin;
    }

    public Vector3d getSkyColor(BlockPos pos, float partialTick) {
        Vec3f skyColor = EarthHelper.getSkyColor(this.world, pos, partialTick, this);
        return new Vector3d(skyColor.x, skyColor.y, skyColor.z);
    }

    public int getSolarEclipseDeclinationIndex() {
        return Math.round(this.solarEclipseDDeclination);
    }

    public float getSolarEclipseIntensity() {
        float angleMod = 9.0F - Math.abs(this.solarEclipseDRightAscension);
        float amplitudeMod = 9.0F - Math.abs(this.solarEclipseDDeclination);
        return angleMod * amplitudeMod / 81.0F;
    }

    public int getSolarEclipseRightAscensionIndex() {
        return Math.round(this.solarEclipseDRightAscension);
    }

    public float getStarsRightAscension() {
        return this.starsRightAscension;
    }

    /**
     * @return The angle the Sun makes with the Zenith, being 0º at the Zenith, 90º at the Horizon and 180º at Nadir
     */
    @OnlyIn(Dist.CLIENT)
    public float getSunAltitude() {
        return this.sunAltitude;
    }

    public float getSunAzimuth() {
        return this.sunAzimuth;
    }

    public float getSunBrightness(float partialTicks) {
        float skyBrightness = 1.0f - (MathHelper.cosDeg(this.sunAltitude) * 2.0f + 0.62f);
        skyBrightness = MathHelper.clamp(skyBrightness, 0, 1);
        if (this.isInSolarEclipse) {
            float intensity = MathHelper.clampMax(this.getSolarEclipseIntensity(), 0.9F);
            if (skyBrightness < intensity) {
                skyBrightness = intensity;
            }
        }
        skyBrightness = 1.0f - skyBrightness;
        if (this.world != null) {
            skyBrightness *= 1.0f - this.world.getRainLevel(partialTicks) * 0.312_5f;
            skyBrightness *= 1.0f - this.world.getThunderLevel(partialTicks) * 0.312_5f;
        }
        return skyBrightness;
    }

    public float getSunCelestialRadius() {
        return this.sunCelestialRadius;
    }

    public float getSunDeclinationOffset() {
        return this.sunDeclinationOffset;
    }

    public float getSunRightAscension() {
        return this.sunRightAscension;
    }

    public boolean isInLunarEclipse() {
        return this.isInLunarEclipse;
    }

    public boolean isInSolarEclipse() {
        return this.isInSolarEclipse;
    }

    public float moonlightMult() {
        if (this.moonAltitude > 96) {
            return 0.97F;
        }
        float moonLight = this.isInLunarEclipse ? (1.0f - this.getLunarEclipseIntensity()) * 0.27f + 0.03f : this.moonPhase.getMoonLight();
        if (this.moonAltitude < 90) {
            return 1.0f - moonLight;
        }
        float mult = 1.0f - (this.moonAltitude - 90) / 5.0f;
        return 0.97f - (moonLight - 0.03f) * mult;
    }

    public void setFogColor(float red, float green, float blue) {
        this.lastFogColor.x = red;
        this.lastFogColor.y = green;
        this.lastFogColor.z = blue;
    }

    public void setWorld(ClientWorld world) {
        if (this.world != world) {
            this.world = world;
            this.tick();
        }
    }

    @Nullable
    private float[] sunsetColors() {
        if (this.sunAltitude >= 66.0F && this.sunAltitude <= 107.5F) {
            float cosSunAlt = MathHelper.cosDeg(this.sunAltitude);
            float mult = this.sunAltitude > 90 ? 1.5f : 1.1f;
            float f3 = cosSunAlt * mult + 0.5F;
            float alpha = 1.0F - (1.0F - MathHelper.sin(f3 * MathHelper.PI)) * 0.99F;
            alpha *= alpha;
            DUSK_DAWN_COLORS[0] = f3 * 0.3F + 0.7F;
            DUSK_DAWN_COLORS[1] = f3 * f3 * alpha * 0.7F + 0.2F;
            DUSK_DAWN_COLORS[2] = 0.2F * alpha;
            DUSK_DAWN_COLORS[3] = alpha;
            return DUSK_DAWN_COLORS;
        }
        return null;
    }

    public void tick() {
        if (this.world == null) {
            return;
        }
        Minecraft.getInstance().getProfiler().push("init");
        long dayTime = this.world.getDayTime();
        this.latitude = EarthHelper.calculateLatitude(Evolution.PROXY.getClientPlayer().getZ());
        float sinLatitude = MathHelper.sinDeg(this.latitude);
        float cosLatitude = MathHelper.cosDeg(this.latitude);
        Minecraft.getInstance().getProfiler().popPush("stars");
        this.starsRightAscension = EarthHelper.calculateStarsRightAscension(dayTime);
        Minecraft.getInstance().getProfiler().popPush("sun");
        this.sunRightAscension = EarthHelper.calculateSunRightAscension(dayTime);
        float seasonDeclination = EarthHelper.sunSeasonalDeclination(dayTime);
        this.sunCelestialRadius = EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.cosDeg(seasonDeclination);
        this.sunDeclinationOffset = -EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.sinDeg(seasonDeclination);
        this.sunAltitude = EarthHelper.getSunAltitude(sinLatitude,
                                                      cosLatitude,
                                                      this.sunRightAscension * 360,
                                                      this.sunCelestialRadius,
                                                      this.sunDeclinationOffset);
        Minecraft.getInstance().getProfiler().popPush("moon");
        this.moonRightAscension = EarthHelper.calculateMoonRightAscension(dayTime);
        float monthlyDeclination = EarthHelper.lunarMonthlyDeclination(dayTime);
        this.moonCelestialRadius = EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.cosDeg(monthlyDeclination);
        this.moonDeclinationOffset = -EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.sinDeg(monthlyDeclination);
        this.moonPhase = MoonPhase.byAngles(this.sunRightAscension * 360, this.moonRightAscension * 360);
        this.moonAltitude = EarthHelper.getMoonAltitude(sinLatitude,
                                                        cosLatitude,
                                                        this.moonRightAscension * 360,
                                                        this.moonCelestialRadius,
                                                        this.moonDeclinationOffset);
        Minecraft.getInstance().getProfiler().popPush("eclipse");
        float dRightAscension = MathHelper.wrapDegrees(360 * (this.sunRightAscension - this.moonRightAscension));
        this.isInSolarEclipse = false;
        this.isInLunarEclipse = false;
        if (Math.abs(dRightAscension) <= 3.0f) {
            float dDeclination = MathHelper.wrapDegrees(seasonDeclination - monthlyDeclination);
            if (Math.abs(dDeclination) <= 7.0f) {
                this.isInSolarEclipse = true;
                this.solarEclipseDRightAscension = EarthHelper.getEclipseAmount(dRightAscension * 7.0f / 3.0f);
                this.solarEclipseDDeclination = EarthHelper.getEclipseAmount(dDeclination);
            }
        }
        else if (177.0f <= Math.abs(dRightAscension) || Math.abs(dRightAscension) <= -177.0f) {
            float dDeclination = MathHelper.wrapDegrees(seasonDeclination - monthlyDeclination);
            if (Math.abs(dDeclination) <= 14.0f) {
                if (dRightAscension > 0) {
                    dRightAscension -= 180;
                }
                else {
                    dRightAscension += 180;
                }
                dRightAscension = -dRightAscension;
                this.isInLunarEclipse = true;
                this.lunarEclipseDRightAscension = EarthHelper.getEclipseAmount(Math.signum(dRightAscension) *
                                                                                dRightAscension *
                                                                                dRightAscension *
                                                                                7.0f / 9.0f);
                this.lunarEclipseDDeclination = EarthHelper.getEclipseAmount(dDeclination * dDeclination * dDeclination / 392.0f);
                this.eclipsePhase = EarthHelper.phaseByEclipseIntensity(this.getLunarEclipseRightAscensionIndex(),
                                                                        this.getLunarEclipseDeclinationIndex());
            }
        }
        Minecraft.getInstance().getProfiler().popPush("effects");
        this.fogColor = this.calculateFogColor();
        this.duskDawnColors = this.sunsetColors();
        //noinspection VariableNotUsedInsideIf
        if (this.duskDawnColors != null) {
            this.sunAzimuth = MathHelper.radToDeg((float) MathHelper.atan2(EarthHelper.sunX, EarthHelper.sunZ)) + 180;
        }
        if (EvolutionConfig.CLIENT.showPlanets.get()) {
            Minecraft.getInstance().getProfiler().popPush("planets");
            PlanetsHelper.preCalculations(dayTime);
            PlanetsHelper.calculateOrbit1Mercury(dayTime);
            PlanetsHelper.calculateOrbit2Venus(dayTime);
            PlanetsHelper.calculateOrbit4Mars(dayTime);
            PlanetsHelper.calculateOrbit5Jupiter(dayTime);
            PlanetsHelper.calculateOrbit6Saturn(dayTime);
        }
        Minecraft.getInstance().getProfiler().pop();
    }
}
