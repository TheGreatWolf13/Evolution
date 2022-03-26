package tgw.evolution.util.math;

/**
 * This class contains utility methods to convert from SI to Minecraft System of Units (MSU)
 * <p>
 * The main difference between the two is that while SI uses seconds as its unit of time, MSU uses ticks
 */
public final class Units {

    private static final int TICKS_PER_SECOND = 20;

    private Units() {
    }

    public static double toMSUAcceleration(double siAcceleration) {
        return siAcceleration / (TICKS_PER_SECOND * TICKS_PER_SECOND);
    }

    public static double toMSUForce(double siForce) {
        return siForce / (TICKS_PER_SECOND * TICKS_PER_SECOND);
    }

    public static double toMSUSpeed(double siSpeed) {
        return siSpeed / TICKS_PER_SECOND;
    }

    public static double toSISpeed(double msuSpeed) {
        return msuSpeed * TICKS_PER_SECOND;
    }
}
