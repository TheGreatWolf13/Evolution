package tgw.evolution.util.earth;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.dimension.DimensionType;
import tgw.evolution.entities.projectiles.IAerodynamicEntity;
import tgw.evolution.util.math.Units;

public final class Gravity {

    /**
     * The average gravity acceleration at the surface of Earth, in m/s^2.
     */
    private static final double EARTH_GRAVITY = Units.toMSUAcceleration(9.8);
    /**
     * The average air density in the atmosphere, in kg/m^3.
     */
    private static final double AIR_DENSITY = 1.225;
    /**
     * The density of water, in kg/m^3.
     */
    private static final double WATER_DENSITY = 997;

    private Gravity() {
    }

    public static double coefOfDrag(Entity entity) {
        if (entity instanceof IAerodynamicEntity) {
            return 0.04;
        }
        return 1.05;
    }

    /**
     * Returns the gravitational acceleration of the given dimension in metres / tick^2.
     */
    public static double gravity(DimensionType dimension) {
        return EARTH_GRAVITY;
    }

    /**
     * Returns the horizontal drag due to air resistance in the given dimension, based on the area of the body.
     */
    public static double horizontalDrag(Entity entity) {
        if (entity.level.dimensionType().natural()) {
            return 0.5 * AIR_DENSITY * entity.getBbWidth() * entity.getBbHeight() * coefOfDrag(entity);
        }
        return 0;
    }

    public static double horizontalWaterDrag(Entity entity) {
        return 0.5 * WATER_DENSITY * entity.getBbWidth() * entity.getBbHeight() * coefOfDrag(entity);
    }

    /**
     * Returns the vertical drag due to air resistance in the given dimension, based on the area of the body.
     */
    public static double verticalDrag(Entity entity) {
        if (entity.level.dimensionType().natural()) {
            return 0.5 * AIR_DENSITY * entity.getBbWidth() * entity.getBbWidth() * coefOfDrag(entity);
        }
        return 0;
    }

    public static double verticalWaterDrag(Entity entity) {
        return 0.5 * WATER_DENSITY * entity.getBbWidth() * entity.getBbWidth() * coefOfDrag(entity);
    }
}
