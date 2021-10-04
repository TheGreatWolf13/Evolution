package tgw.evolution.util;

public class ColorABGR implements IColor {

    /**
     * Multiplies the RGB components of the packed ABGR color using the given scale factors.
     *
     * @param color The ABGR packed color to be multiplied
     * @param rw    The red component scale factor
     * @param gw    The green component scale factor
     * @param bw    The blue component scale factor
     */
    public static int mul(int color, float rw, float gw, float bw) {
        float r = unpackRed(color) * rw;
        float g = unpackGreen(color) * gw;
        float b = unpackBlue(color) * bw;
        return pack((int) r, (int) g, (int) b, 0xFF);
    }

    public static int mul(int color, float w) {
        return mul(color, w, w, w);
    }

    /**
     * Packs the specified color components into ABGR format.
     *
     * @param r The red component of the color
     * @param g The green component of the color
     * @param b The blue component of the color
     * @param a The alpha component of the color
     */
    public static int pack(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (b & 0xFF) << 16 | (g & 0xFF) << 8 | r & 0xFF;
    }

    /**
     * @see ColorABGR#pack(int, int, int, int)
     */
    public static int pack(float r, float g, float b, float a) {
        return pack((int) (r * COMPONENT_RANGE), (int) (g * COMPONENT_RANGE), (int) (b * COMPONENT_RANGE), (int) (a * COMPONENT_RANGE));
    }

    public static int pack(float r, float g, float b) {
        return pack(r, g, b, 255);
    }

    /**
     * @param color The packed 32-bit ABGR color to unpack
     * @return The red color component in the range of 0..255
     */
    public static int unpackAlpha(int color) {
        return color >> 24 & 0xFF;
    }

    /**
     * @param color The packed 32-bit ABGR color to unpack
     * @return The blue color component in the range of 0..255
     */
    public static int unpackBlue(int color) {
        return color >> 16 & 0xFF;
    }

    /**
     * @param color The packed 32-bit ABGR color to unpack
     * @return The green color component in the range of 0..255
     */
    public static int unpackGreen(int color) {
        return color >> 8 & 0xFF;
    }

    /**
     * @param color The packed 32-bit ABGR color to unpack
     * @return The red color component in the range of 0..255
     */
    public static int unpackRed(int color) {
        return color & 0xFF;
    }
}
