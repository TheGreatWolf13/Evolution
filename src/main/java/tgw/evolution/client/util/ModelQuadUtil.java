package tgw.evolution.client.util;

import net.minecraft.core.Direction;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.Norm3b;

/**
 * Provides some utilities and constants for interacting with vanilla's model quad vertex format.
 * <p>
 * This is the current vertex format used by Minecraft for chunk meshes and model quads. Internally, it uses integer
 * arrays for store baked quad data, and as such the following table provides both the byte and int indices.
 * <p>
 * Byte Index    Integer Index             Name                 Format                 Fields
 * 0 ..11        0..2                      Position             3 floats               x, y, z
 * 12..15        3                         Color                4 unsigned bytes       a, r, g, b
 * 16..23        4..5                      Block Texture        2 floats               u, v
 * 24..27        6                         Light Texture        2 shorts               u, v
 * 28..30        7                         Normal               3 unsigned bytes       x, y, z
 * 31                                      Padding              1 byte
 */
public final class ModelQuadUtil {
    // Integer indices for vertex attributes, useful for accessing baked quad data
    public static final int POSITION_INDEX = 0;
    public static final int COLOR_INDEX = 3;
    public static final int TEXTURE_INDEX = 4;
    public static final int LIGHT_INDEX = 6;
    public static final int NORMAL_INDEX = 7;

    // Size of vertex format in 4-byte integers
    public static final int VERTEX_SIZE = 8;

    // Cached array of normals for every facing to avoid expensive computation
    static final int[] NORMALS = new int[DirectionUtil.ALL.length];

    static {
        for (int i = 0; i < NORMALS.length; i++) {
            NORMALS[i] = Norm3b.pack(DirectionUtil.ALL[i].getNormal());
        }
    }

    private ModelQuadUtil() {
    }

    /**
     * Returns the normal vector for a model quad with the given {@param direction}.
     */
    public static int getFacingNormal(Direction direction) {
        return NORMALS[direction.ordinal()];
    }

    /**
     * @param vertexIndex The index of the vertex to access
     * @return The starting offset of the vertex's attributes
     */
    public static int vertexOffset(int vertexIndex) {
        return vertexIndex * VERTEX_SIZE;
    }
}
