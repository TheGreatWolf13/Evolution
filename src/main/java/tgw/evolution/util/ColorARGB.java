package tgw.evolution.util;

public class ColorARGB implements IColor {

    /**
     * Packs the specified color components into big-endian format for consumption by OpenGL.
     *
     * @param r The red component of the color
     * @param g The green component of the color
     * @param b The blue component of the color
     * @param a The alpha component of the color
     */
    public static int pack(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    /**
     * Re-packs the ARGB color into a aBGR color with the specified alpha component.
     */
    public static int toABGR(int color, int alpha) {
        return Integer.reverseBytes(color << 8 | alpha);
    }

    public static int toABGR(int color) {
        return Integer.reverseBytes(color << 8);
    }

    /**
     * @param color The packed 32-bit ARGB color to unpack
     * @return The red color component in the range of 0..255
     */
    public static int unpackAlpha(int color) {
        return color >> 24 & 0xFF;
    }

    /**
     * @param color The packed 32-bit ARGB color to unpack
     * @return The blue color component in the range of 0..255
     */
    public static int unpackBlue(int color) {
        return color & 0xFF;
    }

    /**
     * @param color The packed 32-bit ARGB color to unpack
     * @return The green color component in the range of 0..255
     */
    public static int unpackGreen(int color) {
        return color >> 8 & 0xFF;
    }

    /**
     * @param color The packed 32-bit ARGB color to unpack
     * @return The red color component in the range of 0..255
     */
    public static int unpackRed(int color) {
        return color >> 16 & 0xFF;
    }
}
