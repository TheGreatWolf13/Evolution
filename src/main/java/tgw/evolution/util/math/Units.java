package tgw.evolution.util.math;

/**
 * This class contains utility methods to convert from SI to Minecraft System of Units (MSU)
 * <p>
 * The main difference between the two is that while SI uses seconds as its unit of time, MSU uses ticks
 */
public final class Units {

    private static final int TICKS_PER_SECOND = 20;
    public static final double METER_PER_SECOND = 1.0 / TICKS_PER_SECOND;
    public static final double METER_PER_SECOND_PER_SECOND = 1.0 / TICKS_PER_SECOND / TICKS_PER_SECOND;
    public static final double NEWTON = 1.0 / TICKS_PER_SECOND / TICKS_PER_SECOND;
    public static final double METER_PER_TICK = TICKS_PER_SECOND;

    private Units() {
    }
}
