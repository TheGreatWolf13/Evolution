package tgw.evolution.util.physics;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import tgw.evolution.Evolution;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.time.Time;

import static tgw.evolution.util.physics.OrbitalConstants.*;

public final class PlanetsHelper {

    public static final float ARGUMENT_PERIHELION_0SUN = 270.0F * SI.DEGREE;
    public static final float ARGUMENT_PERIHELION_1MERCURY = 16.183_7F * SI.DEGREE;
    public static final float ARGUMENT_PERIHELION_2VENUS = 41.950_6f * SI.DEGREE;
    public static final float ARGUMENT_PERIHELION_4MARS = 273.561_2f * SI.DEGREE;
    public static final float ARGUMENT_PERIHELION_5JUPITER = 260.937_3f * SI.DEGREE;
    public static final float ARGUMENT_PERIHELION_6SATURN = 326.453_5f * SI.DEGREE;
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
    private static float ha1Mercury;
    private static float ha2Venus;
    private static float ha4Mars;
    private static float ha5Jupiter;
    private static float ha6Saturn;
    private static float ySun;
    private static float xSun;
    private static float localTime;
    private static float sunRA;
    private static boolean is1MercuryTransiting;
    private static boolean is2VenusTransiting;

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
        worldTime += 3L * Time.TICKS_PER_MONTH + Time.TICKS_PER_DAY;
        long yearTime = worldTime % Time.TICKS_PER_YEAR;
        return MathHelper.wrapRadians(Mth.TWO_PI / Time.TICKS_PER_YEAR * yearTime);
    }

    private static float calculateMeanAnomaly1Mercury(long worldTime) {
        long yearTime = worldTime % Time.MERCURIAN_YEAR;
        return MathHelper.wrapRadians(168.656_2f * SI.DEGREE + Mth.TWO_PI / Time.MERCURIAN_YEAR * yearTime);
    }

    /**
     * The mean anomaly is an angle that represents how far, on average, the planet is in its orbit (perihelion marks 0º).
     * If the eccentricity of the orbit is low, it approaches the value of the true anomaly, which accounts for the eccentricity.
     */
    private static float calculateMeanAnomaly2Venus(long worldTime) {
        long yearTime = worldTime % Time.VENUSIAN_YEAR;
        return MathHelper.wrapRadians(48.005_2f * SI.DEGREE + Mth.TWO_PI / Time.VENUSIAN_YEAR * yearTime);
    }

    private static float calculateMeanAnomaly4Mars(long worldTime) {
        long yearTime = worldTime % Time.MARTIAN_YEAR;
        return MathHelper.wrapRadians(18.602_1f * SI.DEGREE + Mth.TWO_PI / Time.MARTIAN_YEAR * yearTime);
    }

    private static float calculateMeanAnomaly5Jupiter(long worldTime) {
        long yearTime = worldTime % Time.JUPITERIAN_YEAR;
        return MathHelper.wrapRadians(19.895_0f * SI.DEGREE + Mth.TWO_PI / Time.JUPITERIAN_YEAR * yearTime);
    }

    private static float calculateMeanAnomaly6Saturn(long worldTime) {
        long yearTime = worldTime % Time.SATURNIAN_YEAR;
        return MathHelper.wrapRadians(316.967_0f * SI.DEGREE + Mth.TWO_PI / Time.SATURNIAN_YEAR * yearTime);
    }

    public static void calculateOrbit1Mercury(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly1Mercury(worldTime);
        float trueLong = meanAnomaly + ARGUMENT_PERIHELION_1MERCURY;
        float sinLong = Mth.sin(trueLong);
        float cosLong = Mth.cos(trueLong);
        //Heliocentric coordinates
        float xh = SEMI_MAJOR_AXIS_1MERCURY *
                   (COS_LONG_ASC_NODE_1MERCURY * cosLong - SIN_LONG_ASC_NODE_1MERCURY * sinLong * COS_INCLINATION_1MERCURY);
        float yh = SEMI_MAJOR_AXIS_1MERCURY *
                   (SIN_LONG_ASC_NODE_1MERCURY * cosLong + COS_LONG_ASC_NODE_1MERCURY * sinLong * COS_INCLINATION_1MERCURY);
        float zh = SEMI_MAJOR_AXIS_1MERCURY * (sinLong * SIN_INCLINATION_1MERCURY);
        //Geocentric coordinates
        float xg = xh + xSun;
        float yg = yh + ySun;
        // zg = zh
        //Equatorial coordinates
        // xe = xg
        float ye = yg * COS_ECLIPTIC - zh * SIN_ECLIPTIC;
        float ze = yg * SIN_ECLIPTIC + zh * COS_ECLIPTIC;
        //
        float ra = (float) MathHelper.atan2Deg(ye, xg);
        if (Math.abs(Mth.wrapDegrees(ra - sunRA)) > 30) {
            Evolution.warn("Mercury dRA is greater than 30º: " + Math.abs(Mth.wrapDegrees(ra - sunRA)));
        }
        decOff1Mercury = -EarthHelper.CELESTIAL_SPHERE_RADIUS * ze / Mth.sqrt(xg * xg + ye * ye);
        ha1Mercury = Mth.wrapDegrees(localTime - ra + 90);
        float dist = Mth.sqrt(xg * xg + ye * ye + ze * ze);
        is1MercuryTransiting = dist < 1;
        float dist1Mercury = MathHelper.relativize(dist, MIN_DIST_1MERCURY, MAX_DIST_1MERCURY);
        angSize1Mercury = Mth.lerp(dist1Mercury, 0.1f, 0.05f);
    }

    public static void calculateOrbit2Venus(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly2Venus(worldTime);
        float trueLong = meanAnomaly + ARGUMENT_PERIHELION_2VENUS;
        float sinLong = Mth.sin(trueLong);
        float cosLong = Mth.cos(trueLong);
        float xh = SEMI_MAJOR_AXIS_2VENUS * (COS_LONG_ASC_NODE_2VENUS * cosLong - SIN_LONG_ASC_NODE_2VENUS * sinLong * COS_INCLINATION_2VENUS);
        float yh = SEMI_MAJOR_AXIS_2VENUS * (SIN_LONG_ASC_NODE_2VENUS * cosLong + COS_LONG_ASC_NODE_2VENUS * sinLong * COS_INCLINATION_2VENUS);
        float zh = SEMI_MAJOR_AXIS_2VENUS * (sinLong * SIN_INCLINATION_2VENUS);
        float xg = xh + xSun;
        float yg = yh + ySun;
        float ye = yg * COS_ECLIPTIC - zh * SIN_ECLIPTIC;
        float ze = yg * SIN_ECLIPTIC + zh * COS_ECLIPTIC;
        float ra = (float) MathHelper.atan2Deg(ye, xg);
        if (Math.abs(Mth.wrapDegrees(ra - sunRA)) > 51) {
            Evolution.warn("Venus dRA is greater than 51º: " + Math.abs(Mth.wrapDegrees(ra - sunRA)));
        }
        decOff2Venus = -EarthHelper.CELESTIAL_SPHERE_RADIUS * ze / Mth.sqrt(xg * xg + ye * ye);
        ha2Venus = Mth.wrapDegrees(localTime - ra + 90);
        float dist = Mth.sqrt(xg * xg + ye * ye + ze * ze);
        is2VenusTransiting = dist < 1;
        float dist2Venus = MathHelper.relativize(dist, MIN_DIST_2VENUS, MAX_DIST_2VENUS);
        angSize2Venus = Mth.lerp(dist2Venus, 0.23f, 0.05f);
    }

    public static void calculateOrbit4Mars(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly4Mars(worldTime);
        float trueLong = meanAnomaly + ARGUMENT_PERIHELION_4MARS;
        float sinLong = Mth.sin(trueLong);
        float cosLong = Mth.cos(trueLong);
        float xh = SEMI_MAJOR_AXIS_4MARS * (COS_LONG_ASC_NODE_4MARS * cosLong - SIN_LONG_ASC_NODE_4MARS * sinLong * COS_INCLINATION_4MARS);
        float yh = SEMI_MAJOR_AXIS_4MARS * (SIN_LONG_ASC_NODE_4MARS * cosLong + COS_LONG_ASC_NODE_4MARS * sinLong * COS_INCLINATION_4MARS);
        float zh = SEMI_MAJOR_AXIS_4MARS * (sinLong * SIN_INCLINATION_4MARS);
        float xg = xh + xSun;
        float yg = yh + ySun;
        float ye = yg * COS_ECLIPTIC - zh * SIN_ECLIPTIC;
        float ze = yg * SIN_ECLIPTIC + zh * COS_ECLIPTIC;
        float ra = (float) MathHelper.atan2Deg(ye, xg);
        decOff4Mars = -EarthHelper.CELESTIAL_SPHERE_RADIUS * ze / Mth.sqrt(xg * xg + ye * ye);
        ha4Mars = Mth.wrapDegrees(localTime - ra + 90);
        float dist4Mars = MathHelper.relativize(Mth.sqrt(xg * xg + ye * ye + ze * ze), MIN_DIST_4MARS, MAX_DIST_4MARS);
        angSize4Mars = Mth.lerp(dist4Mars, 0.1f, 0.05f);
    }

    public static void calculateOrbit5Jupiter(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly5Jupiter(worldTime);
        float trueLong = meanAnomaly + ARGUMENT_PERIHELION_5JUPITER;
        float sinLong = Mth.sin(trueLong);
        float cosLong = Mth.cos(trueLong);
        float xh = SEMI_MAJOR_AXIS_5JUPITER *
                   (COS_LONG_ASC_NODE_5JUPITER * cosLong - SIN_LONG_ASC_NODE_5JUPITER * sinLong * COS_INCLINATION_5JUPITER);
        float yh = SEMI_MAJOR_AXIS_5JUPITER *
                   (SIN_LONG_ASC_NODE_5JUPITER * cosLong + COS_LONG_ASC_NODE_5JUPITER * sinLong * COS_INCLINATION_5JUPITER);
        float zh = SEMI_MAJOR_AXIS_5JUPITER * (sinLong * SIN_INCLINATION_5JUPITER);
        float xg = xh + xSun;
        float yg = yh + ySun;
        float ye = yg * COS_ECLIPTIC - zh * SIN_ECLIPTIC;
        float ze = yg * SIN_ECLIPTIC + zh * COS_ECLIPTIC;
        float ra = (float) MathHelper.atan2Deg(ye, xg);
        decOff5Jupiter = -EarthHelper.CELESTIAL_SPHERE_RADIUS * ze / Mth.sqrt(xg * xg + ye * ye);
        ha5Jupiter = Mth.wrapDegrees(localTime - ra + 90);
        float dist5Jupiter = MathHelper.relativize(Mth.sqrt(xg * xg + ye * ye + ze * ze), MIN_DIST_5JUPITER, MAX_DIST_5JUPITER);
        angSize5Jupiter = Mth.lerp(dist5Jupiter, 0.17f, 0.10f);
    }

    public static void calculateOrbit6Saturn(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly6Saturn(worldTime);
        float trueLong = meanAnomaly + ARGUMENT_PERIHELION_6SATURN;
        float sinLong = Mth.sin(trueLong);
        float cosLong = Mth.cos(trueLong);
        float xh = SEMI_MAJOR_AXIS_6SATURN * (COS_LONG_ASC_NODE_6SATURN * cosLong - SIN_LONG_ASC_NODE_6SATURN * sinLong * COS_INCLINATION_6SATURN);
        float yh = SEMI_MAJOR_AXIS_6SATURN * (SIN_LONG_ASC_NODE_6SATURN * cosLong + COS_LONG_ASC_NODE_6SATURN * sinLong * COS_INCLINATION_6SATURN);
        float zh = SEMI_MAJOR_AXIS_6SATURN * (sinLong * SIN_INCLINATION_6SATURN);
        float xg = xh + xSun;
        float yg = yh + ySun;
        float ye = yg * COS_ECLIPTIC - zh * SIN_ECLIPTIC;
        float ze = yg * SIN_ECLIPTIC + zh * COS_ECLIPTIC;
        float ra = (float) MathHelper.atan2Deg(ye, xg);
        decOff6Saturn = -EarthHelper.CELESTIAL_SPHERE_RADIUS * ze / Mth.sqrt(xg * xg + ye * ye);
        ha6Saturn = Mth.wrapDegrees(localTime - ra + 90);
        float dist6Saturn = MathHelper.relativize(Mth.sqrt(xg * xg + ye * ye + ze * ze), MIN_DIST_6SATURN, MAX_DIST_6SATURN);
        angSize6Saturn = Mth.lerp(dist6Saturn, 0.1f, 0.05f);
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

    public static float getLocalTime() {
        return localTime;
    }

    public static int getQuarterCircunference(Level level) {
        return EarthHelper.POLE;
    }

    public static boolean is1MercuryTransiting() {
        return is1MercuryTransiting;
    }

    public static boolean is2VenusTransiting() {
        return is2VenusTransiting;
    }

    public static void preCalculations(long worldTime) {
        float meanAnomaly = calculateMeanAnomaly0Sun(worldTime);
        float lonSun = meanAnomaly + ARGUMENT_PERIHELION_0SUN;
        //Geocentric
        xSun = Mth.cos(lonSun);
        ySun = Mth.sin(lonSun);
        float xe = xSun;
        float ye = ySun * COS_ECLIPTIC;
        sunRA = (float) MathHelper.atan2Deg(ye, xe);
        localTime = EarthHelper.calculateStarsRightAscension(worldTime);
    }
}
