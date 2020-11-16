package tgw.evolution.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nullable;

public abstract class EarthHelper {

    public static final long NORTH_POLE = 100_000;
    private static final float RADIUS = 100f;
    private static final Vec3f ZENITH = new Vec3f(0, RADIUS, 0);
    private static final Vec3f SUN = new Vec3f(0, 0, 0);
    private static final Vec3f MOON = new Vec3f(0, 0, 0);
    private static final float[] SUNSET_COLORS = new float[4];
    private static final Vec3f SKY_COLOR = new Vec3f(0, 0, 0);
    public static float sunX;
    public static float sunZ;
    public static float moonX;
    public static float moonZ;

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

    /**
     * The current visual inclination of the Sun based on the seasons, given in degrees.
     *
     * @param worldTime The time of the world, in ticks.
     * @return A {@code float} value representing the Sun inclination angle in degrees.
     */
    public static float sunSeasonalInclination(long worldTime) {
        worldTime = worldTime / Time.DAY_IN_TICKS + Date.DAYS_SINCE_MARCH_EQUINOX;
        return 23.5f * MathHelper.sin(2 * MathHelper.PI * worldTime / Time.DAYS_IN_A_YEAR);
    }

    public static float calculateSunAngle(long worldTime) {
        worldTime += 6 * Time.HOUR_IN_TICKS;
        worldTime %= Time.DAY_IN_TICKS;
        return (float) worldTime / Time.DAY_IN_TICKS;
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

    public static Vec3f getSkyColor(World world, BlockPos pos, float partialTick, float elevationAngle) {
        float sunAngle = 1f;
        if (elevationAngle > 80) {
            sunAngle = -elevationAngle * elevationAngle / 784f + 10f * elevationAngle / 49f - 351f / 49f;
            sunAngle = MathHelper.clamp(sunAngle, 0.0F, 1.0F);
        }
        int i = ForgeHooksClient.getSkyBlendColour(world, pos);
        float f3 = (float) (i >> 16 & 255) / 255.0F;
        float f4 = (float) (i >> 8 & 255) / 255.0F;
        float f5 = (float) (i & 255) / 255.0F;
        f3 *= sunAngle;
        f4 *= sunAngle;
        f5 *= sunAngle;
        float f6 = world.getRainStrength(partialTick);
        if (f6 > 0.0F) {
            float f7 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.6F;
            float f8 = 1.0F - f6 * 0.75F;
            f3 = f3 * f8 + f7 * (1.0F - f8);
            f4 = f4 * f8 + f7 * (1.0F - f8);
            f5 = f5 * f8 + f7 * (1.0F - f8);
        }
        float f10 = world.getThunderStrength(partialTick);
        if (f10 > 0.0F) {
            float f11 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.2F;
            float f9 = 1.0F - f10 * 0.75F;
            f3 = f3 * f9 + f11 * (1.0F - f9);
            f4 = f4 * f9 + f11 * (1.0F - f9);
            f5 = f5 * f9 + f11 * (1.0F - f9);
        }
        if (world.getLastLightningBolt() > 0) {
            float lastLightningBolt = (float) world.getLastLightningBolt() - partialTick;
            if (lastLightningBolt > 1.0F) {
                lastLightningBolt = 1.0F;
            }
            lastLightningBolt *= 0.45F;
            f3 = f3 * (1.0F - lastLightningBolt) + 0.8F * lastLightningBolt;
            f4 = f4 * (1.0F - lastLightningBolt) + 0.8F * lastLightningBolt;
            f5 = f5 * (1.0F - lastLightningBolt) + 1.0F * lastLightningBolt;
        }
        SKY_COLOR.x = f3;
        SKY_COLOR.y = f4;
        SKY_COLOR.z = f5;
        return SKY_COLOR;
    }

    @Nullable
    public static float[] calcSunriseSunsetColors(float elevationAngle) {
        if (elevationAngle >= 66F && elevationAngle <= 98F) {
            float cosElevation = MathHelper.cosDeg(elevationAngle);
            float f3 = cosElevation / 0.4F * 0.5F + 0.5F;
            float f4 = 1.0F - (1.0F - MathHelper.sin(f3 * MathHelper.PI)) * 0.99F;
            f4 *= f4;
            SUNSET_COLORS[0] = f3 * 0.3F + 0.7F;
            SUNSET_COLORS[1] = f3 * f3 * 0.7F + 0.2F;
            SUNSET_COLORS[2] = f3 * f3 * 0.0F + 0.2F;
            SUNSET_COLORS[3] = f4;
            return SUNSET_COLORS;
        }
        return null;
    }

    /**
     * Calculates the visual amplitude of the moon in the skyes. This phenomena is cylic and repeats monthly.
     * The maximum amplitude depends on the lunar standstill (which varies from -5.1 degrees to +5.1 degrees)
     * and the tilt of the Earth's orbit (23.5 degrees).
     *
     * @param worldTime The time of the world, in ticks.
     * @return A value in degrees representing the visual lunar amplitude in the skyes.
     */
    public static float lunarMonthlyAmpl(long worldTime) {
        float amplitude = lunarStandStillAmpl(worldTime) + 23.5f;
        return amplitude * MathHelper.sin(2 * MathHelper.PI * worldTime / Time.MONTH_IN_TICKS);
    }

    /**
     * Calculates the amplitude of the inclination of the lunar orbit. This phenomena is cyclic and repeats every 18.6 years,
     * with maximum amplitude of 5.1 degrees.
     *
     * @param worldTime The time of the world, in ticks.
     * @return A value in degrees representing the lunar orbit amplitude.
     */
    public static float lunarStandStillAmpl(long worldTime) {
        return 5.1f * MathHelper.cos(2 * MathHelper.PI * worldTime / (Time.YEAR_IN_TICKS * 18.6f));
    }

    public static float calculateMoonAngle(long worldTime) {
        worldTime += 20 * Time.HOUR_IN_TICKS;
        worldTime %= 25_200;
        return (float) worldTime / 25_200;
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
}
