package tgw.evolution.util.physics;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.renderer.DimensionOverworld;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3f;
import tgw.evolution.util.time.Time;

public final class EarthHelper {

    private static final int BITS_BLOCK = 9;
    private static final int BITS_CHUNK = BITS_BLOCK - 4;
    private static final int MAX_CHUNK = (1 << BITS_CHUNK - 1) - 1;
    private static final int MIN_CHUNK = -(1 << BITS_CHUNK - 1);
    private static final int MASK_CHUNK = (1 << BITS_CHUNK) - 1;
    public static final float CELESTIAL_SPHERE_RADIUS = 100.0f;
    public static final float ECLIPTIC_INCLINATION = 23.5f; //º
    private static final int MASK_BLOCK = (1 << BITS_BLOCK) - 1;
    private static final int MAX_BLOCK = (1 << BITS_BLOCK - 1) - 1;
    private static final int MIN_BLOCK = -(1 << BITS_BLOCK - 1);
    private static final Vec3f MOON = new Vec3f(0, 0, 0);
    public static final int POLE = 100_000;
    public static final int POLAR_CIRCLE = (int) -calculateZFromLatitude(90 - ECLIPTIC_INCLINATION);
    private static final Vec3f SKY_COLOR = new Vec3f(0, 0, 0);
    private static final Vec3f SUN = new Vec3f(0, 0, 0);
    public static final int TROPIC = (int) -calculateLatitude(ECLIPTIC_INCLINATION);
    public static final int WORLD_SIZE = 1 << BITS_BLOCK;
    private static final Vec3f ZENITH = new Vec3f(0, CELESTIAL_SPHERE_RADIUS, 0);
    public static float sunX;
    public static float sunZ;

    private EarthHelper() {
    }

    public static double absDeltaBlockCoordinate(double d0, double d1) {
        double d = Math.abs(d0 - d1);
        if (d > MAX_BLOCK + 1) {
            return -d + 2 * (MAX_BLOCK + 1);
        }
        return d;
    }

    public static int absDeltaChunkCoordinate(int d0, int d1) {
        int d = Math.abs(d0 - d1);
        if (d > MAX_CHUNK + 1) {
            return -d + 2 * (MAX_CHUNK + 1);
        }
        return d;
    }

    /**
     * Calculates the current latitude of the position, given in degrees.
     *
     * @param posZ The target Z position.
     * @return A {@code float} representing the latitude angle in degrees.
     */
    public static float calculateLatitude(double posZ) {
        return PlanetsHelper.calculateLatitude(POLE, posZ);
    }

    public static float calculateMoonRightAscension(long worldTime) {
        worldTime += (long) (19.8 * Time.TICKS_PER_HOUR);
        long dayTime = worldTime % (long) (1.05f * Time.TICKS_PER_DAY);
        return Mth.wrapDegrees(360.0f / (long) (1.05f * Time.TICKS_PER_DAY) * dayTime);
    }

    public static float calculateStarsRightAscension(long worldTime) {
        worldTime += Time.TICKS_PER_DAY;
        //In theory all these modulus shouldn't be necessary, but when the worldTime exceeds a few years, floating point errors start to make these
        // numbers jittery.
        long dayTime = worldTime % Time.TICKS_PER_DAY;
        long yearTime = worldTime % Time.TICKS_PER_YEAR;
        return Mth.wrapDegrees(360.0f / Time.TICKS_PER_DAY * dayTime + 360.0f / Time.TICKS_PER_YEAR * yearTime);
    }

    public static float calculateSunRightAscension(long worldTime) {
        worldTime += 6L * Time.TICKS_PER_HOUR;
        long dayTime = worldTime % Time.TICKS_PER_DAY;
        return Mth.wrapDegrees(360.0f / Time.TICKS_PER_DAY * dayTime);
    }

    public static double calculateZFromLatitude(float latitude) {
        return PlanetsHelper.calculateZFromLatitude(POLE, latitude);
    }

    public static double deltaBlockCoordinate(double d0, double d1) {
        double d = d0 - d1;
        double absD = Math.abs(d);
        if (absD > MAX_BLOCK + 1) {
            return d - Math.signum(d) * (2 * (MAX_BLOCK + 1));
        }
        return d;
    }

    public static int deltaChunkCoordinate(int d0, int d1) {
        int d = d0 - d1;
        int absD = Math.abs(d);
        if (absD > MAX_CHUNK + 1) {
            return d - Mth.sign(d) * 2 * (MAX_CHUNK + 1);
        }
        return d;
    }

    /**
     * The current intensity of the eclipse, where negative means it's going to happen, 0 is full, and positive means it's already happened.
     *
     * @param dRightAsc The difference of the celestial objects' right ascension.
     * @return A float, from -9 to 9.
     */
    public static float getEclipseAmount(float dRightAsc) {
        return 9.0f / 7.0F * dRightAsc;
    }

    public static float getMoonAltitude(float sinLatitude, float cosLatitude, float rightAscension, float celestialRadius, float declination) {
        rightAscension -= 90;
        float sinRightAsc = MathHelper.sinDeg(rightAscension);
        MOON.x = celestialRadius * MathHelper.cosDeg(rightAscension);
        float yt = celestialRadius * sinRightAsc;
        MOON.y = yt * cosLatitude + declination * sinLatitude;
        MOON.z = declination * cosLatitude - celestialRadius * sinRightAsc * sinLatitude;
        return MathHelper.arcCosDeg(MOON.dotProduct(ZENITH) * MOON.inverseLength() * ZENITH.inverseLength());
    }

    public static Vec3f getMoonDir() {
        return MOON;
    }

    public static Vec3f getSkyColor(ClientLevel level, BlockPos pos, float partialTick, @Nullable DimensionOverworld dimension) {
        float sunAngle = getSunAngle(dimension);
        int color = level.getBiome_(pos.getX(), pos.getY(), pos.getZ()).value().getSkyColor();
        float r = (color >> 16 & 255) / 255.0F;
        r *= sunAngle;
        float g = (color >> 8 & 255) / 255.0F;
        g *= sunAngle;
        float b = (color & 255) / 255.0F;
        b *= sunAngle;
        float rainStrength = level.getRainLevel(partialTick);
        if (rainStrength > 0.0F) {
            float colorMod = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.6F;
            float rainMod = 1.0F - rainStrength * 0.75F;
            r = r * rainMod + colorMod * (1.0F - rainMod);
            g = g * rainMod + colorMod * (1.0F - rainMod);
            b = b * rainMod + colorMod * (1.0F - rainMod);
        }
        float thunderStrength = level.getThunderLevel(partialTick);
        if (thunderStrength > 0.0F) {
            float colorMod = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
            float thunderMod = 1.0F - thunderStrength * 0.75F;
            r = r * thunderMod + colorMod * (1.0F - thunderMod);
            g = g * thunderMod + colorMod * (1.0F - thunderMod);
            b = b * thunderMod + colorMod * (1.0F - thunderMod);
        }
        if (level.getSkyFlashTime() > 0) {
            float lastLightningBolt = level.getSkyFlashTime() - partialTick;
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

    public static float getSunAltitude(float sinLatitude, float cosLatitude, float rightAscension, float celestialRadius, float declination) {
        rightAscension -= 90;
        sunX = celestialRadius * MathHelper.cosDeg(rightAscension);
        float sinRightAsc = MathHelper.sinDeg(rightAscension);
        SUN.x = sunX;
        float yt = celestialRadius * sinRightAsc;
        SUN.y = yt * cosLatitude + declination * sinLatitude;
        sunZ = declination * cosLatitude - celestialRadius * sinRightAsc * sinLatitude;
        SUN.z = sunZ;
        return MathHelper.arcCosDeg(SUN.dotProduct(ZENITH) * SUN.inverseLength() * ZENITH.inverseLength());
    }

    private static float getSunAngle(@Nullable DimensionOverworld dimension) {
        float sunAngle = 1.0f;
        float elevationAngle = dimension == null ? 0 : dimension.getSunAltitude();
        if (elevationAngle > 80) {
            sunAngle = -elevationAngle * elevationAngle / 784.0f + 10.0f * elevationAngle / 49.0f - 7.163_265f;
            sunAngle = MathHelper.clamp(sunAngle, 0.0F, 1.0F);
        }
        if (dimension != null && dimension.isCloseToSolarEclipse()) {
            float intensity = Math.max(0.2f, dimension.getSolarEclipseIntensity());
            intensity -= 0.2f;
            intensity = 1.0f - intensity;
            if (intensity < sunAngle) {
                return intensity;
            }
        }
        return sunAngle;
    }

    public static Vec3f getSunDir() {
        return SUN;
    }

    public static boolean isChunkOutsideMapping(ChunkPos pos) {
        return isChunkOutsideMapping(pos.x, pos.z);
    }

    public static boolean isChunkOutsideMapping(int x, int z) {
        if (x > MAX_CHUNK || x < MIN_CHUNK) {
            return true;
        }
        return z > MAX_CHUNK || z < MIN_CHUNK;
    }

    public static boolean isChunkOutsideMapping(long pos) {
        return isChunkOutsideMapping(ChunkPos.getX(pos), ChunkPos.getZ(pos));
    }

    /**
     * Computes whether the point at (x, z) is in the oriented area defined by (x0, z0) and (x1, z1), in a wrapped space.
     */
    public static boolean isInWrappedArea(int x0, int z0, int x1, int z1, int x, int z) {
        if (x0 > x1) {
            if (x > x1 && x < x0) {
                return false;
            }
        }
        else {
            if (x0 > x || x > x1) {
                return false;
            }
        }
        if (z0 > z1) {
            return z <= z1 || z >= z0;
        }
        return z0 <= z && z <= z1;
    }

    /**
     * Calculates the declination of the Moon in the skies. This phenomenon is cyclic and repeats monthly.
     * The maximum declination depends on the lunar standstill (which varies from -5.1 degrees to +5.1 degrees)
     * and the tilt of the Earth's orbit (23.5 degrees).
     * <p>
     * Obs.: The {@code worldTime} is increased by 66.2 hours in order to make the first solar eclipse a total one instead of a partial.
     *
     * @param worldTime The time of the world, in ticks.
     * @return A value in degrees representing the declination of the Moon in the skies from -28.6 to +28.6.
     */
    public static float lunarMonthlyDeclination(long worldTime) {
        long monthTime = (worldTime + (long) (16.2 * Time.TICKS_PER_HOUR)) % Time.TICKS_PER_MONTH;
        float amplitude = -23.5f + 5.1f * Mth.sin(Mth.TWO_PI / (long) Time.TICKS_PER_MONTH * monthTime);
        long yearTime = worldTime % (long) (Time.TICKS_PER_YEAR / 13.0);
        return amplitude * Mth.sin(Mth.TWO_PI / (long) (Time.TICKS_PER_YEAR / 13.0) * yearTime);
    }

    public static MoonPhase phaseByEclipseIntensity(float intensity) {
        intensity *= intensity;
        if (intensity == 0) {
            return MoonPhase.FULL_MOON;
        }
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
        if (intensity <= 1.0f) {
            return MoonPhase.WAXING_CRESCENT_1;
        }
        return MoonPhase.NEW_MOON;
    }

    /**
     * The current declination of the Sun from the Celestial Equator, based on the seasons, given in degrees.
     *
     * @param worldTime The time of the world, in ticks.
     * @return A {@code float} value representing the Sun declination angle in degrees.
     */
    public static float sunSeasonalDeclination(long worldTime) {
        worldTime += Time.TICKS_PER_DAY;
        long yearTime = worldTime % Time.TICKS_PER_YEAR;
        return ECLIPTIC_INCLINATION * Mth.sin(Mth.TWO_PI / Time.TICKS_PER_YEAR * yearTime);
    }

    public static int wrapBlockCoordinate(int value) {
        value &= MASK_BLOCK;
        value <<= Integer.SIZE - BITS_BLOCK;
        return value >> Integer.SIZE - BITS_BLOCK;
    }

    public static double wrapBlockCoordinate(double value) {
        if (value < MIN_BLOCK || value >= MAX_BLOCK + 1) {
            int wholePart = (int) Math.floor(value);
            return wrapBlockCoordinate(wholePart) + value - wholePart;
        }
        return value;
    }

    public static int wrapChunkCoordinate(int value) {
        value &= MASK_CHUNK;
        value <<= Integer.SIZE - BITS_CHUNK;
        return value >> Integer.SIZE - BITS_CHUNK;
    }
}
