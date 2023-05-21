package tgw.evolution.util.physics;

/**
 * This class contains utility fields to convert from SI to Minecraft System of SI (MSU)
 * <p>
 * The main difference between the two is that while SI uses seconds as its unit of time, MSU uses ticks.
 * <br>
 * <br>
 * Base Units: <br>
 * <br>
 * Length: Meter <br>
 * Time: Second <br>
 * Mass: Kilogram <br>
 */
public final class SI {

    public static final double METER = 1.0;
    public static final double CUBIC_METER = METER * METER * METER;
    public static final double CENTIMETER = 1e-2;
    public static final double CUBIC_CENTIMETER = CENTIMETER * CENTIMETER * CENTIMETER;
    public static final double KILOGRAM = 1.0;
    public static final float RADIAN = 1.0f;
    public static final float DEGREE = (float) (Math.PI / 180.0);
    private static final int TICKS_PER_SECOND = 20;
    public static final double SECOND = TICKS_PER_SECOND;
    public static final double NEWTON = KILOGRAM * METER / (SECOND * SECOND);
    public static final double PASCAL = NEWTON / METER / METER;

    private SI() {
    }
}
