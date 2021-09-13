package tgw.evolution.util;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.world.dimension.DimensionOverworld;

public final class EarthHelper {

    public static final long NORTH_POLE = 100_000L;
    private static final float RADIUS = 100.0f;
    private static final Vec3f ZENITH = new Vec3f(0, RADIUS, 0);
    private static final Vec3f SUN = new Vec3f(0, 0, 0);
    private static final Vec3f MOON = new Vec3f(0, 0, 0);
    private static final Vec3f SKY_COLOR = new Vec3f(0, 0, 0);
    private static final Vec3f NEXT_COLOR = new Vec3f(0, 0, 0);
    private static final Vec3f CURRENT_COLOR = new Vec3f(0, 0, 0);
    public static float moonX;
    public static float moonZ;
    public static float sunX;
    public static float sunZ;
    private static int tick;

    private EarthHelper() {
    }

    /**
     * Calculates the current latitude of the position, given in degrees.
     *
     * @param posZ The target Z position.
     * @return A {@code float} representing the latitude angle in degrees.
     */
    public static float calculateLatitude(double posZ) {
        posZ = MathHelper.clamp(posZ, -NORTH_POLE, NORTH_POLE);
        double side = Math.sqrt(NORTH_POLE * NORTH_POLE - posZ * posZ);
        return 90 - MathHelper.radToDeg((float) MathHelper.atan2(side, posZ));
    }

    public static float calculateMoonAngle(long worldTime) {
        worldTime += 19.8 * Time.HOUR_IN_TICKS;
        worldTime %= 1.05 * Time.DAY_IN_TICKS;
        return (float) (worldTime / (1.05 * Time.DAY_IN_TICKS));
    }

    public static float calculateSunAngle(long worldTime) {
        worldTime += 6 * Time.HOUR_IN_TICKS;
        worldTime %= Time.DAY_IN_TICKS;
        return (float) worldTime / Time.DAY_IN_TICKS;
    }

    /**
     * The current intensity of the eclipse, where negative means it's going to happen, 0 is full, and positive means it's already happened.
     *
     * @param angle The angle of the eclipse.
     * @return A float, from -9 to 9.
     */
    public static float getEclipseAmount(float angle) {
        return angle * 9.0f / 7.0F;
    }

    public static float getMoonElevation(float sinLatitude, float cosLatitude, float celestialAngle, float celestialRadius, float monthlyOffset) {
        celestialAngle -= 90;
        moonX = celestialRadius * MathHelper.cosDeg(celestialAngle);
        float sinCelestialAngle = MathHelper.sinDeg(celestialAngle);
        MOON.x = moonX;
        float yt = celestialRadius * sinCelestialAngle;
        MOON.y = yt * cosLatitude + monthlyOffset * sinLatitude;
        moonZ = monthlyOffset * cosLatitude - celestialRadius * sinCelestialAngle * sinLatitude;
        MOON.z = moonZ;
        return MathHelper.arcCosDeg(MOON.dotProduct(ZENITH) * MOON.inverseLength() * ZENITH.inverseLength());
    }

    public static Vec3f getSkyColor(ClientWorld world, BlockPos pos, float partialTick, DimensionOverworld dimension) {
        if (EvolutionConfig.CLIENT.crazyMode.get()) {
            int partial = tick % 20;
            if (partial == 0) {
                CURRENT_COLOR.x = NEXT_COLOR.x;
                CURRENT_COLOR.y = NEXT_COLOR.y;
                CURRENT_COLOR.z = NEXT_COLOR.z;
                NEXT_COLOR.x = MathHelper.RANDOM.nextFloat();
                NEXT_COLOR.y = MathHelper.RANDOM.nextFloat();
                NEXT_COLOR.z = MathHelper.RANDOM.nextFloat();
            }
            float interp = partial / 20.0f;
            SKY_COLOR.x = MathHelper.lerp(interp, CURRENT_COLOR.x, NEXT_COLOR.x);
            SKY_COLOR.y = MathHelper.lerp(interp, CURRENT_COLOR.y, NEXT_COLOR.y);
            SKY_COLOR.z = MathHelper.lerp(interp, CURRENT_COLOR.z, NEXT_COLOR.z);
            tick++;
            return SKY_COLOR;
        }
        float sunAngle = 1.0f;
        float elevationAngle = dimension == null ? 0 : dimension.getSunElevationAngle();
        if (elevationAngle > 80) {
            sunAngle = -elevationAngle * elevationAngle / 784.0f + 10.0f * elevationAngle / 49.0f - 7.163_265f;
            sunAngle = MathHelper.clamp(sunAngle, 0.0F, 1.0F);
        }
        if (dimension != null && dimension.isInSolarEclipse()) {
            float intensity = 1.0F - dimension.getSolarEclipseIntensity();
            if (intensity < sunAngle) {
                sunAngle = intensity;
            }
        }
        Biome biome = world.getBiome(pos);
        int color = biome.getSkyColor();
        float r = (color >> 16 & 255) / 255.0F;
        r *= sunAngle;
        float g = (color >> 8 & 255) / 255.0F;
        g *= sunAngle;
        float b = (color & 255) / 255.0F;
        b *= sunAngle;
        float rainStrength = world.getRainLevel(partialTick);
        if (rainStrength > 0.0F) {
            float colorMod = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.6F;
            float rainMod = 1.0F - rainStrength * 0.75F;
            r = r * rainMod + colorMod * (1.0F - rainMod);
            g = g * rainMod + colorMod * (1.0F - rainMod);
            b = b * rainMod + colorMod * (1.0F - rainMod);
        }
        float thunderStrength = world.getThunderLevel(partialTick);
        if (thunderStrength > 0.0F) {
            float colorMod = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
            float thunderMod = 1.0F - thunderStrength * 0.75F;
            r = r * thunderMod + colorMod * (1.0F - thunderMod);
            g = g * thunderMod + colorMod * (1.0F - thunderMod);
            b = b * thunderMod + colorMod * (1.0F - thunderMod);
        }
        if (world.getSkyFlashTime() > 0) {
            float lastLightningBolt = world.getSkyFlashTime() - partialTick;
            if (lastLightningBolt > 1.0F) {
                lastLightningBolt = 1.0F;
            }
            lastLightningBolt *= 0.45F;
            r = r * (1.0F - lastLightningBolt) + 0.8F * lastLightningBolt;
            g = g * (1.0F - lastLightningBolt) + 0.8F * lastLightningBolt;
            b = b * (1.0F - lastLightningBolt) + lastLightningBolt;
        }
        SKY_COLOR.x = r;
        SKY_COLOR.y = g;
        SKY_COLOR.z = b;
        return SKY_COLOR;
    }

    public static float getSunElevation(float sinLatitude, float cosLatitude, float celestialAngle, float celestialRadius, float seasonOffset) {
        celestialAngle -= 90;
        sunX = celestialRadius * MathHelper.cosDeg(celestialAngle);
        float sinCelestialAngle = MathHelper.sinDeg(celestialAngle);
        SUN.x = sunX;
        float yt = celestialRadius * sinCelestialAngle;
        SUN.y = yt * cosLatitude + seasonOffset * sinLatitude;
        sunZ = seasonOffset * cosLatitude - celestialRadius * sinCelestialAngle * sinLatitude;
        SUN.z = sunZ;
        return MathHelper.arcCosDeg(SUN.dotProduct(ZENITH) * SUN.inverseLength() * ZENITH.inverseLength());
    }

    /**
     * Calculates the visual amplitude of the moon in the skies. This phenomena is cylic and repeats monthly.
     * The maximum amplitude depends on the lunar standstill (which varies from -5.1 degrees to +5.1 degrees)
     * and the tilt of the Earth's orbit (23.5 degrees).
     *
     * @param worldTime The time of the world, in ticks.
     * @return A value in degrees representing the visual lunar amplitude in the skies.
     */
    public static float lunarMonthlyAmpl(long worldTime) {
        float amplitude = lunarStandStillAmpl(worldTime) + 23.5f;
        return amplitude * MathHelper.sin(MathHelper.TAU * worldTime / Time.MONTH_IN_TICKS);
    }

    /**
     * Calculates the amplitude of the inclination of the lunar orbit. This phenomena is cyclic and repeats every 18.6 years,
     * with maximum amplitude of 5.1 degrees.
     *
     * @param worldTime The time of the world, in ticks.
     * @return A value in degrees representing the lunar orbit amplitude.
     */
    public static float lunarStandStillAmpl(long worldTime) {
        return 5.1f * MathHelper.cos(MathHelper.TAU * worldTime / (Time.YEAR_IN_TICKS * 18.6f));
    }

    public static MoonPhase phaseByEclipseIntensity(int angle, int amplitude) {
        int angleMod = 9 - Math.abs(angle);
        int amplitudeMod = 9 - Math.abs(amplitude);
        float intensity = angleMod * amplitudeMod / 81.0F;
        if (intensity < 0.111_111f) {
            return MoonPhase.WAXING_GIBBOUS_4;
        }
        if (intensity < 0.222_222f) {
            return MoonPhase.WAXING_GIBBOUS_3;
        }
        if (intensity < 0.333_333f) {
            return MoonPhase.WAXING_GIBBOUS_2;
        }
        if (intensity < 0.444_444f) {
            return MoonPhase.WAXING_GIBBOUS_1;
        }
        if (intensity < 0.555_556f) {
            return MoonPhase.FIRST_QUARTER;
        }
        if (intensity < 0.666_667f) {
            return MoonPhase.WAXING_CRESCENT_4;
        }
        if (intensity < 0.777_778f) {
            return MoonPhase.WAXING_CRESCENT_3;
        }
        if (intensity < 0.888_889f) {
            return MoonPhase.WAXING_CRESCENT_2;
        }
        if (intensity < 1.0f) {
            return MoonPhase.WAXING_CRESCENT_1;
        }
        return MoonPhase.NEW_MOON;
    }

    /**
     * The current visual inclination of the Sun based on the seasons, given in degrees.
     *
     * @param worldTime The time of the world, in ticks.
     * @return A {@code float} value representing the Sun inclination angle in degrees.
     */
    public static float sunSeasonalInclination(long worldTime) {
        worldTime = worldTime / Time.DAY_IN_TICKS + Date.DAYS_SINCE_MARCH_EQUINOX;
        return 23.5f * MathHelper.sin(MathHelper.TAU * worldTime / Time.DAYS_IN_A_YEAR);
    }
}
