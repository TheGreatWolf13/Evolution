package tgw.evolution.util;

public final class OrbitalConstants {

    public static final float COS_ECLIPTIC = MathHelper.cosDeg(EarthHelper.ECLIPTIC_INCLINATION);
    public static final float SIN_ECLIPTIC = MathHelper.sinDeg(EarthHelper.ECLIPTIC_INCLINATION);

    public static final float COS_INCLINATION_1MERCURY = MathHelper.cosDeg(PlanetsHelper.INCLINATION_1MERCURY);
    public static final float COS_INCLINATION_2VENUS = MathHelper.cosDeg(PlanetsHelper.INCLINATION_2VENUS);
    public static final float COS_INCLINATION_4MARS = MathHelper.cosDeg(PlanetsHelper.INCLINATION_4MARS);
    public static final float COS_INCLINATION_5JUPITER = MathHelper.cosDeg(PlanetsHelper.INCLINATION_5JUPITER);
    public static final float COS_INCLINATION_6SATURN = MathHelper.cosDeg(PlanetsHelper.INCLINATION_6SATURN);

    public static final float COS_LONG_ASC_NODE_1MERCURY = MathHelper.cosDeg(PlanetsHelper.LONG_ASC_NODE_1MERCURY);
    public static final float COS_LONG_ASC_NODE_2VENUS = MathHelper.cosDeg(PlanetsHelper.LONG_ASC_NODE_2VENUS);
    public static final float COS_LONG_ASC_NODE_4MARS = MathHelper.cosDeg(PlanetsHelper.LONG_ASC_NODE_4MARS);
    public static final float COS_LONG_ASC_NODE_5JUPITER = MathHelper.cosDeg(PlanetsHelper.LONG_ASC_NODE_5JUPITER);
    public static final float COS_LONG_ASC_NODE_6SATURN = MathHelper.cosDeg(PlanetsHelper.LONG_ASC_NODE_6SATURN);

    public static final float SIN_INCLINATION_1MERCURY = MathHelper.sinDeg(PlanetsHelper.INCLINATION_1MERCURY);
    public static final float SIN_INCLINATION_2VENUS = MathHelper.sinDeg(PlanetsHelper.INCLINATION_2VENUS);
    public static final float SIN_INCLINATION_4MARS = MathHelper.sinDeg(PlanetsHelper.INCLINATION_4MARS);
    public static final float SIN_INCLINATION_5JUPITER = MathHelper.sinDeg(PlanetsHelper.INCLINATION_5JUPITER);
    public static final float SIN_INCLINATION_6SATURN = MathHelper.sinDeg(PlanetsHelper.INCLINATION_6SATURN);

    public static final float SIN_LONG_ASC_NODE_1MERCURY = MathHelper.sinDeg(PlanetsHelper.LONG_ASC_NODE_1MERCURY);
    public static final float SIN_LONG_ASC_NODE_2VENUS = MathHelper.sinDeg(PlanetsHelper.LONG_ASC_NODE_2VENUS);
    public static final float SIN_LONG_ASC_NODE_4MARS = MathHelper.sinDeg(PlanetsHelper.LONG_ASC_NODE_4MARS);
    public static final float SIN_LONG_ASC_NODE_5JUPITER = MathHelper.sinDeg(PlanetsHelper.LONG_ASC_NODE_5JUPITER);
    public static final float SIN_LONG_ASC_NODE_6SATURN = MathHelper.sinDeg(PlanetsHelper.LONG_ASC_NODE_6SATURN);

    public static final float MIN_DIST_1MERCURY = 1 - PlanetsHelper.SEMI_MAJOR_AXIS_1MERCURY;
    public static final float MIN_DIST_2VENUS = 1 - PlanetsHelper.SEMI_MAJOR_AXIS_2VENUS;
    public static final float MIN_DIST_4MARS = 1 - PlanetsHelper.SEMI_MAJOR_AXIS_4MARS;
    public static final float MIN_DIST_5JUPITER = 1 - PlanetsHelper.SEMI_MAJOR_AXIS_5JUPITER;
    public static final float MIN_DIST_6SATURN = 1 - PlanetsHelper.SEMI_MAJOR_AXIS_6SATURN;

    public static final float MAX_DIST_1MERCURY = 1 + PlanetsHelper.SEMI_MAJOR_AXIS_1MERCURY;
    public static final float MAX_DIST_2VENUS = 1 + PlanetsHelper.SEMI_MAJOR_AXIS_2VENUS;
    public static final float MAX_DIST_4MARS = 1 + PlanetsHelper.SEMI_MAJOR_AXIS_4MARS;
    public static final float MAX_DIST_5JUPITER = 1 + PlanetsHelper.SEMI_MAJOR_AXIS_5JUPITER;
    public static final float MAX_DIST_6SATURN = 1 + PlanetsHelper.SEMI_MAJOR_AXIS_6SATURN;

    private OrbitalConstants() {
    }
}
