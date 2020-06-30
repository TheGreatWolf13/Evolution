package tgw.evolution.util;

import net.minecraft.world.dimension.Dimension;

public class Gravity {

    private static final double EARTH_GRAVITY = 0.0245;
    private static final double EARTH_DRAG = 0.0090444;
    private static final double MINE_GRAVITY = 0.08;
    private static final double MINE_DRAG = 0.02;

    /**
     * Returns the gravitational acceleration of the given dimension in metres / tick^2.
     */
    public static double gravity(Dimension dimension) {
        if (dimension.isSurfaceWorld()) {
            return EARTH_GRAVITY;
        }
        return MINE_GRAVITY;
    }

    /**
     * Returns the vertical drag due to air resistance in the given dimension, based on the area of the body.
     */
    public static double verticalDrag(Dimension dimension, double width) {
        if (dimension.isSurfaceWorld()) {
            return 1 - EARTH_DRAG * width * width;
        }
        return 1 - MINE_DRAG;
    }

    /**
     * Returns the horizontal drag due to air resistance in the given dimension, based on the area of the body.
     */
    public static double horizontalDrag(Dimension dimension, double width, double height) {
        if (dimension.isSurfaceWorld()) {
            return 1 - EARTH_DRAG * width * height;
        }
        return 1 - MINE_DRAG;
    }
}
