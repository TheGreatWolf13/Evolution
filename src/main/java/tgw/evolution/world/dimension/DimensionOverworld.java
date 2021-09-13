package tgw.evolution.world.dimension;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.Evolution;
import tgw.evolution.util.EarthHelper;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.MoonPhase;
import tgw.evolution.util.Vec3f;

import javax.annotation.Nullable;

public class DimensionOverworld {

    private static final float[] SUNSET_COLORS = new float[4];
    private MoonPhase eclipsePhase = MoonPhase.FULL_MOON;
    private Vector3d fogColor = Vector3d.ZERO;
    private boolean isInLunarEclipse;
    private boolean isInSolarEclipse;
    private float latitude;
    private float[] lightBrightnessTable;
    private float lunarEclipseAmplitude;
    private float lunarEclipseAngle;
    private float moonAngle;
    private float moonCelestialRadius;
    private float moonElevationAngle;
    private float moonMonthlyOffset;
    private MoonPhase moonPhase = MoonPhase.NEW_MOON;
    private float solarEclipseAmplitude;
    private float solarEclipseAngle;
    private float sunAngle;
    private float sunCelestialRadius;
    private float sunElevationAngle;
    private float sunSeasonalOffset;
    private float[] sunsetColors;
    private ClientWorld world;

    public DimensionOverworld() {
        this.generateLightBrightnessTable();
    }

    public Vector3d biomeColorModifier(Vector3d biomeFogColor, float multiplier) {
        return biomeFogColor.multiply(multiplier * 0.97 + 0.03, multiplier * 0.97 + 0.03, multiplier * 0.97 + 0.03);
    }

    private Vector3d calculateFogColor() {
        float sunAngle = 1.0f;
        if (this.sunElevationAngle > 80) {
            sunAngle = -this.sunElevationAngle * this.sunElevationAngle / 784.0f + 10.0f * this.sunElevationAngle / 49.0f - 7.163_265f;
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

    public MoonPhase getEclipsePhase() {
        return this.eclipsePhase;
    }

    public Vector3d getFogColor() {
        return this.fogColor;
    }

    @OnlyIn(Dist.CLIENT)
    public float getLatitude() {
        return this.latitude;
    }

    public int getLunarEclipseAmplitudeIndex() {
        return Math.round(this.lunarEclipseAmplitude);
    }

    public int getLunarEclipseAngleIndex() {
        return Math.round(this.lunarEclipseAngle);
    }

    public float getLunarEclipseIntensity() {
        float angleMod = 9.0F - Math.abs(this.lunarEclipseAngle);
        float amplitudeMod = 9.0F - Math.abs(this.lunarEclipseAmplitude);
        return angleMod * amplitudeMod / 81.0F;
    }

    public float getMoonAngle() {
        return this.moonAngle;
    }

    public float getMoonCelestialRadius() {
        return this.moonCelestialRadius;
    }

    @OnlyIn(Dist.CLIENT)
    public float getMoonElevationAngle() {
        return this.moonElevationAngle;
    }

    public float getMoonMonthlyOffset() {
        return this.moonMonthlyOffset;
    }

    public MoonPhase getMoonPhase() {
        return this.moonPhase;
    }

    public Vector3d getSkyColor(BlockPos pos, float partialTick) {
        Vec3f skyColor = EarthHelper.getSkyColor(this.world, pos, partialTick, this);
        return new Vector3d(skyColor.x, skyColor.y, skyColor.z);
    }

    public int getSolarEclipseAmplitudeIndex() {
        return Math.round(this.solarEclipseAmplitude);
    }

    public int getSolarEclipseAngleIndex() {
        return Math.round(this.solarEclipseAngle);
    }

    public float getSolarEclipseIntensity() {
        float angleMod = 9.0F - Math.abs(this.solarEclipseAngle);
        float amplitudeMod = 9.0F - Math.abs(this.solarEclipseAmplitude);
        return angleMod * amplitudeMod / 81.0F;
    }

    public float getStarBrightness() {
        float f1 = 1.0F - (MathHelper.cosDeg(this.sunElevationAngle) * 2.0F + 0.25F);
        f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
        return f1 * f1 * 0.5F;
    }

    public float getSunAngle() {
        return this.sunAngle;
    }

    public float getSunBrightness(float partialTicks) {
        float moonlightMult = this.moonlightMult();
        float moonlightMin = 1 - moonlightMult;
        return this.getSunBrightnessPure(partialTicks) * moonlightMult + moonlightMin;
    }

    public float getSunBrightnessPure(float partialTicks) {
        float skyBrightness = 1.0F - (MathHelper.cosDeg(this.sunElevationAngle) * 2.0F + 0.62F);
        skyBrightness = MathHelper.clamp(skyBrightness, 0.0F, 1.0F);
        if (this.isInSolarEclipse) {
            float intensity = MathHelper.clampMax(this.getSolarEclipseIntensity(), 0.9F);
            if (skyBrightness < intensity) {
                skyBrightness = intensity;
            }
        }
        skyBrightness = 1.0F - skyBrightness;
        if (this.world != null) {
            skyBrightness *= 1.0f - this.world.getRainLevel(partialTicks) * 0.312_5f;
            skyBrightness *= 1.0f - this.world.getThunderLevel(partialTicks) * 0.312_5f;
        }
        return skyBrightness;
    }

    public float getSunCelestialRadius() {
        return this.sunCelestialRadius;
    }

    /**
     * @return The angle the Sun makes with the Zenith, being 0º at the Zenith, 90º at the Horizon and 180º at Nadir
     */
    @OnlyIn(Dist.CLIENT)
    public float getSunElevationAngle() {
        return this.sunElevationAngle;
    }

    public float getSunSeasonalOffset() {
        return this.sunSeasonalOffset;
    }

    public float[] getSunriseColors() {
        return this.sunsetColors;
    }

    public boolean isInLunarEclipse() {
        return this.isInLunarEclipse;
    }

    public boolean isInSolarEclipse() {
        return this.isInSolarEclipse;
    }

    public float moonlightMult() {
        if (this.moonElevationAngle > 96) {
            return 0.97F;
        }
        float moonLight = this.isInLunarEclipse ? (1.0f - this.getLunarEclipseIntensity()) * 0.27f + 0.03f : this.moonPhase.getMoonLight();
        if (this.moonElevationAngle < 90) {
            return 1.0f - moonLight;
        }
        float mult = 1.0f - (this.moonElevationAngle - 90) / 5.0f;
        return 0.97f - (moonLight - 0.03f) * mult;
    }

    public void setWorld(ClientWorld world) {
        if (this.world != world) {
            this.world = world;
            this.tick();
        }
    }

    @Nullable
    private float[] sunsetColors() {
        if (this.sunElevationAngle >= 66.0F && this.sunElevationAngle <= 107.5F) {
            float cosElevation = MathHelper.cosDeg(this.sunElevationAngle);
            float mult = this.sunElevationAngle > 90 ? 1.5f : 1.1f;
            float f3 = cosElevation * mult + 0.5F;
            float alpha = 1.0F - (1.0F - MathHelper.sin(f3 * MathHelper.PI)) * 0.99F;
            alpha *= alpha;
            SUNSET_COLORS[0] = f3 * 0.3F + 0.7F;
            SUNSET_COLORS[1] = f3 * f3 * alpha * 0.7F + 0.2F;
            SUNSET_COLORS[2] = 0.2F * alpha;
            SUNSET_COLORS[3] = alpha;
            return SUNSET_COLORS;
        }
        return null;
    }

    public void tick() {
        if (this.world == null) {
            return;
        }
        this.sunAngle = EarthHelper.calculateSunAngle(this.world.getDayTime());
        this.moonAngle = EarthHelper.calculateMoonAngle(this.world.getDayTime());
        float seasonAngle = EarthHelper.sunSeasonalInclination(this.world.getDayTime());
        float monthlyAngle = EarthHelper.lunarMonthlyAmpl(this.world.getDayTime());
        float eclipseAngle = MathHelper.wrapDegrees(360 * (this.sunAngle - this.moonAngle));
        this.isInSolarEclipse = false;
        this.isInLunarEclipse = false;
        if (Math.abs(eclipseAngle) <= 3.0f) {
            float eclipseAmplitude = MathHelper.wrapDegrees(seasonAngle - monthlyAngle);
            if (Math.abs(eclipseAmplitude) <= 7.0f) {
                this.isInSolarEclipse = true;
                this.solarEclipseAngle = EarthHelper.getEclipseAmount(eclipseAngle * 7.0f / 3.0f);
                this.solarEclipseAmplitude = EarthHelper.getEclipseAmount(eclipseAmplitude);
            }
        }
        else if (177.0f <= Math.abs(eclipseAngle) || Math.abs(eclipseAngle) <= -177.0f) {
            float eclipseAmplitude = MathHelper.wrapDegrees(seasonAngle - monthlyAngle);
            if (Math.abs(eclipseAmplitude) <= 14.0f) {
                if (eclipseAngle > 0) {
                    eclipseAngle -= 180;
                }
                else {
                    eclipseAngle += 180;
                }
                eclipseAngle = -eclipseAngle;
                this.isInLunarEclipse = true;
                this.lunarEclipseAngle = EarthHelper.getEclipseAmount(Math.signum(eclipseAngle) * eclipseAngle * eclipseAngle * 7.0f / 9.0f);
                this.lunarEclipseAmplitude = EarthHelper.getEclipseAmount(eclipseAmplitude * eclipseAmplitude * eclipseAmplitude / 392.0f);
                this.eclipsePhase = EarthHelper.phaseByEclipseIntensity(this.getLunarEclipseAngleIndex(), this.getLunarEclipseAmplitudeIndex());
            }
        }
        this.latitude = EarthHelper.calculateLatitude(Evolution.PROXY.getClientPlayer().getZ());
        float sinLatitude = MathHelper.sinDeg(this.latitude);
        float cosLatitude = MathHelper.cosDeg(this.latitude);
        this.sunCelestialRadius = 100.0f * MathHelper.cosDeg(seasonAngle);
        this.moonCelestialRadius = 100.0f * MathHelper.cosDeg(monthlyAngle);
        this.sunSeasonalOffset = -100.0f * MathHelper.sinDeg(seasonAngle);
        this.moonMonthlyOffset = -100.0f * MathHelper.sinDeg(monthlyAngle);
        this.sunElevationAngle = EarthHelper.getSunElevation(sinLatitude,
                                                             cosLatitude,
                                                             this.sunAngle * 360,
                                                             this.sunCelestialRadius,
                                                             this.sunSeasonalOffset);
        this.moonElevationAngle = EarthHelper.getMoonElevation(sinLatitude,
                                                               cosLatitude,
                                                               this.moonAngle * 360,
                                                               this.moonCelestialRadius,
                                                               this.moonMonthlyOffset);
        this.fogColor = this.calculateFogColor();
        this.sunsetColors = this.sunsetColors();
        this.moonPhase = MoonPhase.byAngles(this.sunAngle * 360, this.moonAngle * 360);
    }
}
