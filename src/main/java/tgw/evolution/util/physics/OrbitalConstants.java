package tgw.evolution.util.physics;

import net.minecraft.util.Mth;
import tgw.evolution.util.math.MathHelper;

public final class OrbitalConstants {

    public static final float COS_ECLIPTIC = MathHelper.cosDeg(EarthHelper.ECLIPTIC_INCLINATION);
    public static final float SIN_ECLIPTIC = MathHelper.sinDeg(EarthHelper.ECLIPTIC_INCLINATION);

    public static final float COS_INCLINATION_1MERCURY = Mth.cos(PlanetsHelper.INCLINATION_1MERCURY);
    public static final float COS_INCLINATION_2VENUS = Mth.cos(PlanetsHelper.INCLINATION_2VENUS);
    public static final float COS_INCLINATION_4MARS = Mth.cos(PlanetsHelper.INCLINATION_4MARS);
    public static final float COS_INCLINATION_5JUPITER = Mth.cos(PlanetsHelper.INCLINATION_5JUPITER);
    public static final float COS_INCLINATION_6SATURN = Mth.cos(PlanetsHelper.INCLINATION_6SATURN);

    public static final float COS_LONG_ASC_NODE_1MERCURY = Mth.cos(PlanetsHelper.LONG_ASC_NODE_1MERCURY);
    public static final float COS_LONG_ASC_NODE_2VENUS = Mth.cos(PlanetsHelper.LONG_ASC_NODE_2VENUS);
    public static final float COS_LONG_ASC_NODE_4MARS = Mth.cos(PlanetsHelper.LONG_ASC_NODE_4MARS);
    public static final float COS_LONG_ASC_NODE_5JUPITER = Mth.cos(PlanetsHelper.LONG_ASC_NODE_5JUPITER);
    public static final float COS_LONG_ASC_NODE_6SATURN = Mth.cos(PlanetsHelper.LONG_ASC_NODE_6SATURN);

    public static final float SIN_INCLINATION_1MERCURY = Mth.sin(PlanetsHelper.INCLINATION_1MERCURY);
    public static final float SIN_INCLINATION_2VENUS = Mth.sin(PlanetsHelper.INCLINATION_2VENUS);
    public static final float SIN_INCLINATION_4MARS = Mth.sin(PlanetsHelper.INCLINATION_4MARS);
    public static final float SIN_INCLINATION_5JUPITER = Mth.sin(PlanetsHelper.INCLINATION_5JUPITER);
    public static final float SIN_INCLINATION_6SATURN = Mth.sin(PlanetsHelper.INCLINATION_6SATURN);

    public static final float SIN_LONG_ASC_NODE_1MERCURY = Mth.sin(PlanetsHelper.LONG_ASC_NODE_1MERCURY);
    public static final float SIN_LONG_ASC_NODE_2VENUS = Mth.sin(PlanetsHelper.LONG_ASC_NODE_2VENUS);
    public static final float SIN_LONG_ASC_NODE_4MARS = Mth.sin(PlanetsHelper.LONG_ASC_NODE_4MARS);
    public static final float SIN_LONG_ASC_NODE_5JUPITER = Mth.sin(PlanetsHelper.LONG_ASC_NODE_5JUPITER);
    public static final float SIN_LONG_ASC_NODE_6SATURN = Mth.sin(PlanetsHelper.LONG_ASC_NODE_6SATURN);

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
