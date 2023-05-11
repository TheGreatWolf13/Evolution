package tgw.evolution.world.dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.math.Vec3f;
import tgw.evolution.util.physics.EarthHelper;
import tgw.evolution.util.physics.MoonPhase;
import tgw.evolution.util.physics.PlanetsHelper;

public class DimensionOverworld {

    private static final float[] DUSK_DAWN_COLORS = new float[4];
    private final Vec3f lastFogColor = new Vec3f(0, 0, 0);
    private float @Nullable [] duskDawnColors;
    private MoonPhase eclipsePhase = MoonPhase.FULL_MOON;
    private boolean isCloseToLunarEclipse;
    private boolean isCloseToSolarEclipse;
    private float latitude;
    private @Nullable ClientLevel level;
    private float[] lightBrightnessTable;
    private float lunarEclipseDDeclination;
    private float lunarEclipseDRightAscension;
    private float moonAltitude;
    private float moonDeclinationOffset;
    private MoonPhase moonPhase = MoonPhase.NEW_MOON;
    private float moonRightAscension;
    private float solarEclipseDDeclination;
    private float solarEclipseDRightAscension;
    private float starsRightAscension;
    private float sunAltitude;
    private float sunAzimuth;
    private float sunDeclinationOffset;
    private float sunRightAscension;

    public DimensionOverworld() {
        this.generateLightBrightnessTable();
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

    public Vec3d getBrightnessDependentFogColor(Vec3d biomeFogColor, float multiplier) {
        return biomeFogColor.scaleMutable(multiplier * 0.97 + 0.03);
    }

    public float @Nullable [] getDuskDawnColors() {
        return this.duskDawnColors;
    }

    public MoonPhase getEclipsePhase() {
        return this.eclipsePhase;
    }

    public Vec3f getLastFogColor() {
        return this.lastFogColor;
    }

    public float getLatitude() {
        return this.latitude;
    }

    public float getLunarEclipseDRightAscension() {
        return this.lunarEclipseDRightAscension;
    }

    public int getLunarEclipseDeclinationIndex() {
        return Math.round(this.lunarEclipseDDeclination);
    }

    public float getLunarEclipseIntensity() {
        float angleMod = 8.0F - Math.abs(this.lunarEclipseDRightAscension);
        angleMod = MathHelper.relativize(angleMod, 4, 7.75f);
        float amplitudeMod = 8.0F - Math.abs(this.lunarEclipseDDeclination);
        amplitudeMod = MathHelper.relativize(amplitudeMod, 4, 6.75f);
        return angleMod * amplitudeMod;
    }

    public int getLunarEclipseRightAscensionIndex() {
        return Math.round(this.lunarEclipseDRightAscension);
    }

    public float getMoonAltitude() {
        return this.moonAltitude;
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

    public float getSolarEclipseIntensity() {
        float angleMod = 8.0F - Math.abs(this.solarEclipseDRightAscension);
        angleMod = MathHelper.relativize(angleMod, 4.5f, 7.75f);
        float amplitudeMod = 8.0F - Math.abs(this.solarEclipseDDeclination);
        amplitudeMod = MathHelper.relativize(amplitudeMod, 4.5f, 7.75f);
        return angleMod * amplitudeMod;
    }

    public float getStarsRightAscension() {
        return this.starsRightAscension;
    }

    /**
     * @return The angle the Sun makes with the Zenith, being 0º at the Zenith, 90º at the Horizon and 180º at Nadir
     */
    public float getSunAltitude() {
        return this.sunAltitude;
    }

    public float getSunAzimuth() {
        return this.sunAzimuth;
    }

    public float getSunBrightness(float partialTicks) {
        float skyBrightness = 1.0f - (MathHelper.cosDeg(this.sunAltitude) * 2.0f + 0.62f);
        skyBrightness = MathHelper.clamp(skyBrightness, 0, 1);
        if (this.isCloseToSolarEclipse) {
            float intensity = Math.max(this.getSolarEclipseIntensity(), 0.2f);
            intensity -= 0.2f;
            if (skyBrightness < intensity) {
                skyBrightness = intensity;
            }
        }
        skyBrightness = 1.0f - skyBrightness;
        if (this.level != null) {
            skyBrightness *= 1.0f - this.level.getRainLevel(partialTicks) * 0.312_5f;
            skyBrightness *= 1.0f - this.level.getThunderLevel(partialTicks) * 0.312_5f;
        }
        return skyBrightness;
    }

    public float getSunDeclinationOffset() {
        return this.sunDeclinationOffset;
    }

    public float getSunRightAscension() {
        return this.sunRightAscension;
    }

    public boolean isCloseToLunarEclipse() {
        return this.isCloseToLunarEclipse;
    }

    public boolean isCloseToSolarEclipse() {
        return this.isCloseToSolarEclipse;
    }

    public float moonlightMult() {
        if (this.moonAltitude > 96) {
            return 0.97F;
        }
        float moonLight = this.isCloseToLunarEclipse ? (1.0f - this.getLunarEclipseIntensity()) * 0.27f + 0.03f : this.moonPhase.getMoonLight();
        if (this.moonAltitude < 90) {
            return 1.0f - moonLight;
        }
        float mult = 1.0f - (this.moonAltitude - 90) / 6.0f;
        return 0.97f - (moonLight - 0.03f) * mult;
    }

    public void setFogColor(float red, float green, float blue) {
        this.lastFogColor.x = red;
        this.lastFogColor.y = green;
        this.lastFogColor.z = blue;
    }

    public void setLevel(ClientLevel level) {
        if (this.level != level) {
            this.level = level;
            this.tick();
        }
    }

    private float @Nullable [] sunsetColors() {
        if (this.sunAltitude >= 66.0F && this.sunAltitude <= 107.5F) {
            float cosSunAlt = MathHelper.cosDeg(this.sunAltitude);
            float mult = this.sunAltitude > 90 ? 1.5f : 1.1f;
            float f3 = cosSunAlt * mult + 0.5F;
            float alpha = 1.0F - (1.0F - Mth.sin(f3 * Mth.PI)) * 0.99F;
            if (this.sunAltitude > 90) {
                alpha *= alpha;
            }
            else {
                alpha *= alpha * alpha;
                alpha /= 1 + (this.sunAltitude - 90) / -24;
            }
            DUSK_DAWN_COLORS[0] = f3 * 0.3F + 0.7F;
            DUSK_DAWN_COLORS[1] = f3 * f3 * alpha * 0.7F + 0.2F;
            DUSK_DAWN_COLORS[2] = 0;
            DUSK_DAWN_COLORS[3] = alpha;
            return DUSK_DAWN_COLORS;
        }
        return null;
    }

    public void tick() {
        if (this.level == null) {
            return;
        }
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("init");
        long dayTime = this.level.getDayTime();
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        this.latitude = -EarthHelper.calculateLatitude(cameraEntity == null ? 0 : cameraEntity.getZ());
        float sinLatitude = MathHelper.sinDeg(this.latitude);
        float cosLatitude = MathHelper.cosDeg(this.latitude);
        profiler.popPush("stars");
        this.starsRightAscension = EarthHelper.calculateStarsRightAscension(dayTime);
        profiler.popPush("sun");
        this.sunRightAscension = EarthHelper.calculateSunRightAscension(dayTime);
        float seasonDeclination = EarthHelper.sunSeasonalDeclination(dayTime);
        this.sunDeclinationOffset = -EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.tanDeg(seasonDeclination);
        this.sunAltitude = EarthHelper.getSunAltitude(sinLatitude, cosLatitude, this.sunRightAscension, EarthHelper.CELESTIAL_SPHERE_RADIUS,
                                                      this.sunDeclinationOffset);
        profiler.popPush("moon");
        this.moonRightAscension = EarthHelper.calculateMoonRightAscension(dayTime);
        float monthlyDeclination = EarthHelper.lunarMonthlyDeclination(dayTime);
        this.moonDeclinationOffset = -EarthHelper.CELESTIAL_SPHERE_RADIUS * MathHelper.tanDeg(monthlyDeclination);
        this.moonPhase = MoonPhase.byAngles(this.sunRightAscension, this.moonRightAscension);
        this.moonAltitude = EarthHelper.getMoonAltitude(sinLatitude, cosLatitude, this.moonRightAscension, EarthHelper.CELESTIAL_SPHERE_RADIUS,
                                                        this.moonDeclinationOffset);
        profiler.popPush("eclipse");
        float dRightAscension = Mth.wrapDegrees(this.sunRightAscension - this.moonRightAscension);
        this.isCloseToSolarEclipse = false;
        this.isCloseToLunarEclipse = false;
        if (Math.abs(dRightAscension) <= 8) {
            float dDeclination = Mth.wrapDegrees(seasonDeclination - monthlyDeclination);
            if (Math.abs(dDeclination) <= 8) {
                this.isCloseToSolarEclipse = true;
                this.solarEclipseDRightAscension = dRightAscension;
                this.solarEclipseDDeclination = dDeclination;
            }
        }
        else if (176 <= Math.abs(dRightAscension)) {
            float dDeclination = Mth.wrapDegrees(seasonDeclination + monthlyDeclination);
            if (Math.abs(dDeclination) <= 8) {
                if (dRightAscension > 0) {
                    dRightAscension -= 180;
                }
                else {
                    dRightAscension += 180;
                }
                dRightAscension = -dRightAscension;
                this.isCloseToLunarEclipse = true;
                this.lunarEclipseDRightAscension = dRightAscension;
                this.lunarEclipseDDeclination = dDeclination;
                this.eclipsePhase = EarthHelper.phaseByEclipseIntensity(this.getLunarEclipseIntensity());
            }
        }
        profiler.popPush("effects");
        this.duskDawnColors = this.sunsetColors();
        //noinspection VariableNotUsedInsideIf
        if (this.duskDawnColors != null) {
            this.sunAzimuth = (float) MathHelper.atan2Deg(EarthHelper.sunX, EarthHelper.sunZ) + 180;
        }
        if (EvolutionConfig.CLIENT.showPlanets.get()) {
            profiler.popPush("planets");
            PlanetsHelper.preCalculations(dayTime);
            PlanetsHelper.calculateOrbit1Mercury(dayTime);
            PlanetsHelper.calculateOrbit2Venus(dayTime);
            PlanetsHelper.calculateOrbit4Mars(dayTime);
            PlanetsHelper.calculateOrbit5Jupiter(dayTime);
            PlanetsHelper.calculateOrbit6Saturn(dayTime);
        }
        profiler.pop();
    }
}
