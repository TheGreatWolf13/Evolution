package tgw.evolution.util.physics;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.time.Time;

import static tgw.evolution.util.physics.OrbitalConstants.*;

public final class PlanetsHelper {

    public static final float ARGUMENT_PERIHELION_0SUN = 282.940_4F * SI.DEGREE;
    public static final float ARGUMENT_PERIHELION_1MERCURY = 29.124_1F * SI.DEGREE;
    public static final float ARGUMENT_PERIHELION_2VENUS = 54.891_0f * SI.DEGREE;
    public static final float ARGUMENT_PERIHELION_4MARS = 286.501_6f * SI.DEGREE;
    public static final float ARGUMENT_PERIHELION_5JUPITER = 273.877_7f * SI.DEGREE;
    public static final float ARGUMENT_PERIHELION_6SATURN = 339.393_9f * SI.DEGREE;
    public static final float INCLINATION_1MERCURY = 7.004_7f * SI.DEGREE;
    public static final float INCLINATION_2VENUS = 3.394_6f * SI.DEGREE;
    public static final float INCLINATION_4MARS = 1.849_7f * SI.DEGREE;
    public static final float INCLINATION_5JUPITER = 1.303_0f * SI.DEGREE;
    public static final float INCLINATION_6SATURN = 2.488_6f * SI.DEGREE;
    public static final float LONG_ASC_NODE_1MERCURY = 48.331_3f * SI.DEGREE;
    public static final float LONG_ASC_NODE_2VENUS = 76.679_9f * SI.DEGREE;
    public static final float LONG_ASC_NODE_4MARS = 49.557_4f * SI.DEGREE;
    public static final float LONG_ASC_NODE_5JUPITER = 100.454_2f * SI.DEGREE;
    public static final float LONG_ASC_NODE_6SATURN = 113.663_4f * SI.DEGREE;
    public static final float SEMI_MAJOR_AXIS_1MERCURY = 0.387_098f; //AU
    public static final float SEMI_MAJOR_AXIS_2VENUS = 0.723_330f; //AU
    public static final float SEMI_MAJOR_AXIS_4MARS = 1.523_688f; //AU
    public static final float SEMI_MAJOR_AXIS_5JUPITER = 5.202_56f; //AU
    public static final float SEMI_MAJOR_AXIS_6SATURN = 9.554_75f; //AU
    private static float angSize1Mercury;
    private static float angSize2Venus;
    private static float angSize4Mars;
    private static float angSize5Jupiter;
    private static float angSize6Saturn;
    private static float decOff1Mercury;
    private static float decOff2Venus;
    private static float decOff4Mars;
    private static float decOff5Jupiter;
    private static float decOff6Saturn;
    private static float dist1Mercury;
    private static float dist2Venus;
    private static float dist4Mars;
    private static float dist5Jupiter;
    private static float dist6Saturn;
    private static float ha1Mercury;
    private static float ha2Venus;
    private static float ha4Mars;
    private static float ha5Jupiter;
    private static float ha6Saturn;
    private static float sinLongSun;
    private static float cosLongSun;
    private static float localTime;

    private PlanetsHelper() {
    }

    public static float calculateLatitude(Level level, double posZ) {
        return calculateLatitude(getQuarterCircunference(level), posZ);
    }

    public static float calculateLatitude(int quarterCircunference, double posZ) {
        posZ = MathHelper.clamp(posZ, -quarterCircunference, quarterCircunference);
        return -90.0f * (float) posZ / quarterCircunference;
    }

    private static float calculateMeanAnomaly0Sun(long worldTime) {
        return MathHelper.wrapRadians(356.047_0f * SI.DEGREE + Mth.TWO_PI / Time.TICKS_PER_YEAR * (worldTime % Time.TICKS_PER_YEAR));
    }

    private static float calculateMeanAnomaly1Mercury(long worldTime) {
        return MathHelper.wrapRadians(168.656_2f * SI.DEGREE + Mth.TWO_PI / Time.MERCURIAN_YEAR * (worldTime % Time.MERCURIAN_YEAR));
    }

    /**
     * The mean anomaly is an angle that represents how far, on average, the planet is in its orbit (perihelion marks 0º).
     * If the eccentricity of the orbit is low, it approaches the value of the true anomaly, which accounts for the eccentricity.
     */
    private static float calculateMeanAnomaly2Venus(long worldTime) {
        return MathHelper.wrapRadians(48.005_2f * SI.DEGREE + Mth.TWO_PI / Time.VENUSIAN_YEAR * (worldTime % Time.VENUSIAN_YEAR));
    }

    private static float calculateMeanAnomaly4Mars(long worldTime) {
        return MathHelper.wrapRadians(18.602_1f * SI.DEGREE + Mth.TWO_PI / Time.MARTIAN_YEAR * (worldTime % Time.MARTIAN_YEAR));
    }

    private static float calculateMeanAnomaly5Jupiter(long worldTime) {
        return MathHelper.wrapRadians(19.895_0f * SI.DEGREE + Mth.TWO_PI / Time.JUPITERIAN_YEAR * (worldTime % Time.JUPITERIAN_YEAR));
    }

    private static float calculateMeanAnomaly6Saturn(long worldTime) {
        return MathHelper.wrapRadians(316.967_0f * SI.DEGREE + Mth.TWO_PI / Time.SATURNIAN_YEAR * (worldTime % Time.SATURNIAN_YEAR));
    }

    public static void calculateOrbit1Mercury(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly1Mercury(worldTime);
        float trueLong = meanAnomaly + ARGUMENT_PERIHELION_1MERCURY;
        float sinLong = Mth.sin(trueLong);
        float cosLong = Mth.cos(trueLong);
        double xh = SEMI_MAJOR_AXIS_1MERCURY *
                    (COS_LONG_ASC_NODE_1MERCURY * cosLong - SIN_LONG_ASC_NODE_1MERCURY * sinLong * COS_INCLINATION_1MERCURY);
        double yh = SEMI_MAJOR_AXIS_1MERCURY *
                    (SIN_LONG_ASC_NODE_1MERCURY * cosLong + COS_LONG_ASC_NODE_1MERCURY * sinLong * COS_INCLINATION_1MERCURY);
        double zh = SEMI_MAJOR_AXIS_1MERCURY * (sinLong * SIN_INCLINATION_1MERCURY);
        double xg = xh + cosLongSun;
        double yg = yh + sinLongSun;
        double ye = yg * COS_ECLIPTIC - zh * SIN_ECLIPTIC;
        double ze = yg * SIN_ECLIPTIC + zh * COS_ECLIPTIC;
        double ra = MathHelper.atan2Deg(ye, xg);
        decOff1Mercury = (float) (EarthHelper.CELESTIAL_SPHERE_RADIUS * (ze / Math.sqrt(xg * xg + ye * ye)));
        ha1Mercury = (float) Mth.wrapDegrees(localTime - ra + 90);
        dist1Mercury = (float) MathHelper.relativize(Math.sqrt(xg * xg + ye * ye + ze * ze), MIN_DIST_1MERCURY, MAX_DIST_1MERCURY);
        angSize1Mercury = Mth.lerp(dist1Mercury, 0.02f, 0.05f);
        dist1Mercury = 1.1f * EarthHelper.CELESTIAL_SPHERE_RADIUS;
    }

    public static void calculateOrbit2Venus(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly2Venus(worldTime);
        float trueLong = meanAnomaly + ARGUMENT_PERIHELION_2VENUS;
        float sinLong = Mth.sin(trueLong);
        float cosLong = Mth.cos(trueLong);
        double xh = SEMI_MAJOR_AXIS_2VENUS * (COS_LONG_ASC_NODE_2VENUS * cosLong - SIN_LONG_ASC_NODE_2VENUS * sinLong * COS_INCLINATION_2VENUS);
        double yh = SEMI_MAJOR_AXIS_2VENUS * (SIN_LONG_ASC_NODE_2VENUS * cosLong + COS_LONG_ASC_NODE_2VENUS * sinLong * COS_INCLINATION_2VENUS);
        double zh = SEMI_MAJOR_AXIS_2VENUS * (sinLong * SIN_INCLINATION_2VENUS);
        double xg = xh + cosLongSun;
        double yg = yh + sinLongSun;
        double ye = yg * COS_ECLIPTIC - zh * SIN_ECLIPTIC;
        double ze = yg * SIN_ECLIPTIC + zh * COS_ECLIPTIC;
        double ra = MathHelper.atan2Deg(ye, xg);
        decOff2Venus = (float) (EarthHelper.CELESTIAL_SPHERE_RADIUS * (ze / Math.sqrt(xg * xg + ye * ye)));
        ha2Venus = (float) Mth.wrapDegrees(localTime - ra + 90);
        dist2Venus = (float) MathHelper.relativize(Math.sqrt(xg * xg + ye * ye + ze * ze), MIN_DIST_2VENUS, MAX_DIST_2VENUS);
        angSize2Venus = Mth.lerp(dist2Venus, 0.03f, 0.23f);
        dist2Venus = 1.1f * EarthHelper.CELESTIAL_SPHERE_RADIUS;
    }

    public static void calculateOrbit4Mars(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly4Mars(worldTime);
        float trueLong = meanAnomaly + ARGUMENT_PERIHELION_4MARS;
        float sinLong = Mth.sin(trueLong);
        float cosLong = Mth.cos(trueLong);
        double xh = SEMI_MAJOR_AXIS_4MARS * (COS_LONG_ASC_NODE_4MARS * cosLong - SIN_LONG_ASC_NODE_4MARS * sinLong * COS_INCLINATION_4MARS);
        double yh = SEMI_MAJOR_AXIS_4MARS * (SIN_LONG_ASC_NODE_4MARS * cosLong + COS_LONG_ASC_NODE_4MARS * sinLong * COS_INCLINATION_4MARS);
        double zh = SEMI_MAJOR_AXIS_4MARS * (sinLong * SIN_INCLINATION_4MARS);
        double xg = xh + cosLongSun;
        double yg = yh + sinLongSun;
        double ye = yg * COS_ECLIPTIC - zh * SIN_ECLIPTIC;
        double ze = yg * SIN_ECLIPTIC + zh * COS_ECLIPTIC;
        double ra = MathHelper.atan2Deg(ye, xg);
        decOff4Mars = (float) (EarthHelper.CELESTIAL_SPHERE_RADIUS * (ze / Math.sqrt(xg * xg + ye * ye)));
        ha4Mars = (float) Mth.wrapDegrees(localTime - ra + 90);
        dist4Mars = (float) MathHelper.relativize(Math.sqrt(xg * xg + ye * ye + ze * ze), MIN_DIST_4MARS, MAX_DIST_4MARS);
        angSize4Mars = Mth.lerp(dist4Mars, 0.01f, 0.09f);
        dist4Mars = 1.1f * EarthHelper.CELESTIAL_SPHERE_RADIUS;
    }

    public static void calculateOrbit5Jupiter(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly5Jupiter(worldTime);
        float trueLong = meanAnomaly + ARGUMENT_PERIHELION_5JUPITER;
        float sinLong = Mth.sin(trueLong);
        float cosLong = Mth.cos(trueLong);
        double xh = SEMI_MAJOR_AXIS_5JUPITER *
                    (COS_LONG_ASC_NODE_5JUPITER * cosLong - SIN_LONG_ASC_NODE_5JUPITER * sinLong * COS_INCLINATION_5JUPITER);
        double yh = SEMI_MAJOR_AXIS_5JUPITER *
                    (SIN_LONG_ASC_NODE_5JUPITER * cosLong + COS_LONG_ASC_NODE_5JUPITER * sinLong * COS_INCLINATION_5JUPITER);
        double zh = SEMI_MAJOR_AXIS_5JUPITER * (sinLong * SIN_INCLINATION_5JUPITER);
        double xg = xh + cosLongSun;
        double yg = yh + sinLongSun;
        double ye = yg * COS_ECLIPTIC - zh * SIN_ECLIPTIC;
        double ze = yg * SIN_ECLIPTIC + zh * COS_ECLIPTIC;
        double ra = MathHelper.atan2Deg(ye, xg);
        decOff5Jupiter = (float) (EarthHelper.CELESTIAL_SPHERE_RADIUS * (ze / Math.sqrt(xg * xg + ye * ye)));
        ha5Jupiter = (float) Mth.wrapDegrees(localTime - ra + 90);
        dist5Jupiter = (float) MathHelper.relativize(Math.sqrt(xg * xg + ye * ye + ze * ze), MIN_DIST_5JUPITER, MAX_DIST_5JUPITER);
        angSize5Jupiter = Mth.lerp(dist5Jupiter, 0.10f, 0.17f);
        dist5Jupiter = 1.1f * EarthHelper.CELESTIAL_SPHERE_RADIUS;
    }

    public static void calculateOrbit6Saturn(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly6Saturn(worldTime);
        float trueLong = meanAnomaly + ARGUMENT_PERIHELION_6SATURN;
        float sinLong = Mth.sin(trueLong);
        float cosLong = Mth.cos(trueLong);
        double xh = SEMI_MAJOR_AXIS_6SATURN * (COS_LONG_ASC_NODE_6SATURN * cosLong - SIN_LONG_ASC_NODE_6SATURN * sinLong * COS_INCLINATION_6SATURN);
        double yh = SEMI_MAJOR_AXIS_6SATURN * (SIN_LONG_ASC_NODE_6SATURN * cosLong + COS_LONG_ASC_NODE_6SATURN * sinLong * COS_INCLINATION_6SATURN);
        double zh = SEMI_MAJOR_AXIS_6SATURN * (sinLong * SIN_INCLINATION_6SATURN);
        double xg = xh + cosLongSun;
        double yg = yh + sinLongSun;
        double ye = yg * COS_ECLIPTIC - zh * SIN_ECLIPTIC;
        double ze = yg * SIN_ECLIPTIC + zh * COS_ECLIPTIC;
        double ra = MathHelper.atan2Deg(ye, xg);
        decOff6Saturn = (float) (EarthHelper.CELESTIAL_SPHERE_RADIUS * (ze / Math.sqrt(xg * xg + ye * ye)));
        ha6Saturn = (float) Mth.wrapDegrees(localTime - ra + 90);
        dist6Saturn = (float) MathHelper.relativize(Math.sqrt(xg * xg + ye * ye + ze * ze), MIN_DIST_6SATURN, MAX_DIST_6SATURN);
        angSize6Saturn = Mth.lerp(dist6Saturn, 0.05f, 0.07f);
        dist6Saturn = 1.1f * EarthHelper.CELESTIAL_SPHERE_RADIUS;
    }

    public static float calculateZFromLatitude(Level level, float latitude) {
        return calculateZFromLatitude(getQuarterCircunference(level), latitude);
    }

    public static float calculateZFromLatitude(int quarterCircunference, float latitude) {
        return -quarterCircunference * MathHelper.sinDeg(latitude);
    }

    public static float getAngSize1Mercury() {
        return angSize1Mercury;
    }

    public static float getAngSize2Venus() {
        return angSize2Venus;
    }

    public static float getAngSize4Mars() {
        return angSize4Mars;
    }

    public static float getAngSize5Jupiter() {
        return angSize5Jupiter;
    }

    public static float getAngSize6Saturn() {
        return angSize6Saturn;
    }

    public static float getDecOff1Mercury() {
        return decOff1Mercury;
    }

    public static float getDecOff2Venus() {
        return decOff2Venus;
    }

    public static float getDecOff4Mars() {
        return decOff4Mars;
    }

    public static float getDecOff5Jupiter() {
        return decOff5Jupiter;
    }

    public static float getDecOff6Saturn() {
        return decOff6Saturn;
    }

    public static float getDist1Mercury() {
        return dist1Mercury;
    }

    public static float getDist2Venus() {
        return dist2Venus;
    }

    public static float getDist4Mars() {
        return dist4Mars;
    }

    public static float getDist5Jupiter() {
        return dist5Jupiter;
    }

    public static float getDist6Saturn() {
        return dist6Saturn;
    }

    public static float getHa1Mercury() {
        return ha1Mercury;
    }

    public static float getHa2Venus() {
        return ha2Venus;
    }

    public static float getHa4Mars() {
        return ha4Mars;
    }

    public static float getHa5Jupiter() {
        return ha5Jupiter;
    }

    public static float getHa6Saturn() {
        return ha6Saturn;
    }

    public static int getQuarterCircunference(Level level) {
        return EarthHelper.POLE;
    }

    public static void preCalculations(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly0Sun(worldTime);
        float lonSun = meanAnomaly + ARGUMENT_PERIHELION_0SUN;
        sinLongSun = Mth.sin(lonSun);
        cosLongSun = Mth.cos(lonSun);
        localTime = EarthHelper.calculateStarsRightAscension(worldTime) * 360;
    }
}
