package tgw.evolution.patches;

public interface IBakedQuadPatch {

    /**
     * @return The integer-encoded color of the vertex at index {@param idx}
     */
    int getColor(int idx);

    /**
     * @return The texture x-coordinate for the vertex at index {@param idx}
     */
    float getTexU(int idx);

    /**
     * @return The texture y-coordinate for the vertex at index {@param idx}
     */
    float getTexV(int idx);

    /**
     * @return The x-position of the vertex at index {@param idx}
     */
    float getX(int idx);

    /**
     * @return The y-position of the vertex at index {@param idx}
     */
    float getY(int idx);

    /**
     * @return The z-position of the vertex at index {@param idx}
     */
    float getZ(int idx);
}
