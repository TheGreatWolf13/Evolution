package tgw.evolution.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.dimension.Dimension;

public class Gravity {

    private static final double EARTH_GRAVITY = 0.0245;
    private static final double AIR_DENSITY = 1.225;
    private static final double WATER_DENSITY = 997;

    /**
     * Returns the gravitational acceleration of the given dimension in metres / tick^2.
     */
    public static double gravity(Dimension dimension) {
        return EARTH_GRAVITY;
    }

    /**
     * Returns the vertical drag due to air resistance in the given dimension, based on the area of the body.
     */
    public static double verticalDrag(Entity entity) {
        if (entity.getEntityWorld().getDimension().isSurfaceWorld()) {
            return 0.5 * AIR_DENSITY * entity.getWidth() * entity.getWidth() * coefOfDrag(entity);
        }
        return 0;
    }

    public static double coefOfDrag(Entity entity) {
        return 1.05;
    }

    /**
     * Returns the horizontal drag due to air resistance in the given dimension, based on the area of the body.
     */
    public static double horizontalDrag(Entity entity) {
        if (entity.getEntityWorld().getDimension().isSurfaceWorld()) {
            return 0.5 * AIR_DENSITY * entity.getWidth() * entity.getHeight() * coefOfDrag(entity);
        }
        return 0;
    }

    public static double horizontalWaterDrag(Entity entity) {
        return 0.5 * WATER_DENSITY * entity.getWidth() * entity.getHeight() * coefOfDrag(entity);
    }

    public static double verticalWaterDrag(Entity entity) {
        return 0.5 * WATER_DENSITY * entity.getWidth() * entity.getWidth() * coefOfDrag(entity);
    }
}
