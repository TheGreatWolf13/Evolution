package tgw.evolution.util.math;

/**
 * Represents a half-precision floating point value.
 * <p>The format is laid out as follows:
 * <pre>
 * 1   111111   111111111
 * ^   --^---   ----^----
 * sign  |          |_______ mantissa (9 bits)
 *       |
 *       -- exponent (6 bits)
 * </pre>
 */
public final class HalfFloat {

    /**
     * Smallest negative value a half-precision float may have.
     */
    public static final short LOWEST_VALUE = (short) 0b1__11_1110__1_1111_1111;
    /**
     * Maximum positive finite value a half-precision float may have.
     */
    public static final short MAX_VALUE = 0b0__11_1110__1_1111_1111;
    /**
     * Smallest positive normal value a half-precision float may have.
     */
    public static final short MIN_NORMAL = 0b0__00_0001__0_0000_0000;
    /**
     * Smallest positive non-zero value a half-precision float may have.
     */
    public static final short MIN_VALUE = 0b0__00_0000__0_0000_0001;
    /**
     * A Not-a-Number representation of a half-precision float.
     */
    public static final short NaN = 0b0__11_1111__1_0000_0000;
    /**
     * Negative infinity of type half-precision float.
     */
    public static final short NEGATIVE_INFINITY = (short) 0b1__11_1111__0_0000_0000;
    /**
     * Negative 0 of type half-precision float.
     */
    public static final short NEGATIVE_ZERO = (short) 0b1__00_0000__0_0000_0000;
    /**
     * Positive infinity of type half-precision float.
     */
    public static final short POSITIVE_INFINITY = 0b0__11_1111__0_0000_0000;
    /**
     * Positive 0 of type half-precision float.
     */
    public static final short POSITIVE_ZERO = 0b0__00_0000__0_0000_0000;
    /**
     * The offset to shift by to obtain the sign bit.
     */
    public static final int SIGN_SHIFT = 15;
    /**
     * The offset to shift by to obtain the exponent bits.
     */
    public static final int EXPONENT_SHIFT = 9;
    /**
     * The bitmask to AND a number with to obtain the sign bit.
     */
    public static final int SIGN_MASK = 0b1__00_0000__0_0000_0000;
    /**
     * The bitmask to AND a number shifted by {@link #EXPONENT_SHIFT} right, to obtain exponent bits.
     */
    public static final int SHIFTED_EXPONENT_MASK = 0b11_1111;
    /**
     * The bitmask to AND a number with to obtain mantissa bits.
     */
    public static final int MANTISSA_MASK = 0b1_1111_1111;
    /**
     * The bitmask to AND with to obtain exponent and mantissa bits.
     **/
    public static final int EXPONENT_MANTISSA_MASK = 0b11_1111__1_1111_1111;
    /**
     * The bitmask to AND with to obtain the full number bits.
     */
    public static final int FULL_MASK = 0b1__11_1111__1_1111_1111;
    public static final int QNAN_MASK = 0b1_0000_0000;
    public static final int LOST_MANTISSA_MASK = 0b11_1111_1111_1111;
    /**
     * The offset of the exponent from the actual value.
     */
    public static final int EXPONENT_BIAS = 31;

    private static final int FP32_SIGN_SHIFT = 31;
    private static final int FP32_EXPONENT_SHIFT = 23;
    private static final int FP32_SHIFTED_EXPONENT_MASK = 255;
    private static final int FP32_MANTISSA_MASK = 0b111_1111_1111_1111_1111_1111;
    private static final int FP32_EXPONENT_BIAS = 127;
    private static final int FP32_QNAN_MASK = 0b100_0000_0000_0000_0000_0000;

    private HalfFloat() {
    }

    /**
     * Returns true if the specified half-precision float value represents
     * infinity, false otherwise.
     *
     * @param value A half-precision float value
     * @return True if the value is positive infinity or negative infinity,
     * false otherwise
     */
    public static boolean isInfinite(short value) {
        return (value & EXPONENT_MANTISSA_MASK) == POSITIVE_INFINITY;
    }

    /**
     * Returns true if the specified half-precision float value represents
     * a Not-a-Number, false otherwise.
     *
     * @param value A half-precision float value
     * @return True if the value is a NaN, false otherwise
     */
    public static boolean isNaN(short value) {
        return (value & EXPONENT_MANTISSA_MASK) > POSITIVE_INFINITY;
    }

    /**
     * <p>Converts the specified half-precision float value into a
     * single-precision float value. The following special cases are handled:</p>
     * <ul>
     * <li>If the input is {@link #NaN}, the returned value is {@link Float#NaN}</li>
     * <li>If the input is {@link #POSITIVE_INFINITY} or
     * {@link #NEGATIVE_INFINITY}, the returned value is respectively
     * {@link Float#POSITIVE_INFINITY} or {@link Float#NEGATIVE_INFINITY}</li>
     * <li>If the input is 0 (positive or negative), the returned value is +/-0.0f</li>
     * <li>Otherwise, the returned value is a normalized single-precision float value</li>
     * </ul>
     *
     * @param value The half-precision float value to convert to single-precision
     * @return A normalized single-precision float value
     * @hide
     */
    public static float toFloat(short value) {
        int bits = value & FULL_MASK;
        int s = bits & SIGN_MASK;
        int e = bits >>> EXPONENT_SHIFT & SHIFTED_EXPONENT_MASK;
        int m = bits & MANTISSA_MASK;
        int outE = 0;
        int outM = 0;
        if (e == 0) { // Subnormal or 0
            if (m != 0) {
                outM = m << 1;
                outE = FP32_EXPONENT_BIAS - EXPONENT_BIAS;
                while ((outM & 1 << EXPONENT_SHIFT) == 0) {
                    outM <<= 1;
                    outE--;
                }
                outM &= MANTISSA_MASK;
                return Float.intBitsToFloat(
                        s << FP32_SIGN_SHIFT - SIGN_SHIFT | outE << FP32_EXPONENT_SHIFT | outM << FP32_EXPONENT_SHIFT - EXPONENT_SHIFT);
            }
        }
        else {
            outM = m << FP32_EXPONENT_SHIFT - EXPONENT_SHIFT;
            if (e == SHIFTED_EXPONENT_MASK) { // Infinite or NaN
                outE = FP32_SHIFTED_EXPONENT_MASK;
                if (outM != 0) { // SNaNs are quieted
                    outM |= FP32_QNAN_MASK;
                }
            }
            else {
                outE = e - EXPONENT_BIAS + FP32_EXPONENT_BIAS;
            }
        }
        int out = s << FP32_SIGN_SHIFT - SIGN_SHIFT | outE << FP32_EXPONENT_SHIFT | outM;
        return Float.intBitsToFloat(out);
    }

    /**
     * <p>Converts the specified single-precision float value into a
     * half-precision float value. The following special cases are handled:</p>
     * <ul>
     * <li>If the input is NaN (see {@link Float#isNaN(float)}), the returned
     * value is {@link #NaN}</li>
     * <li>If the input is {@link Float#POSITIVE_INFINITY} or
     * {@link Float#NEGATIVE_INFINITY}, the returned value is respectively
     * {@link #POSITIVE_INFINITY} or {@link #NEGATIVE_INFINITY}</li>
     * <li>If the input is 0 (positive or negative), the returned value is
     * {@link #POSITIVE_ZERO} or {@link #NEGATIVE_ZERO}</li>
     * <li>If the input is a less than {@link #MIN_VALUE}, the returned value
     * is flushed to {@link #POSITIVE_ZERO} or {@link #NEGATIVE_ZERO}</li>
     * <li>If the input is a less than {@link #MIN_NORMAL}, the returned value
     * is a subnormal half-precision float</li>
     * <li>Otherwise, the returned value is rounded to the nearest
     * representable half-precision float value</li>
     * </ul>
     *
     * @param value The single-precision float value to convert to half-precision
     * @return A half-precision float value
     */
    public static short toHalf(float value) {
        int bits = Float.floatToRawIntBits(value);
        int s = bits >>> FP32_SIGN_SHIFT;
        int e = bits >>> FP32_EXPONENT_SHIFT & FP32_SHIFTED_EXPONENT_MASK;
        int m = bits & FP32_MANTISSA_MASK;
        int outE = 0;
        int outM = 0;
        if (e == FP32_SHIFTED_EXPONENT_MASK) { // Infinite or NaN
            outE = SHIFTED_EXPONENT_MASK;
            outM = m != 0 ? QNAN_MASK : 0;
            return (short) (s << SIGN_SHIFT | (outE << EXPONENT_SHIFT) + outM);
        }
        e = e - FP32_EXPONENT_BIAS + EXPONENT_BIAS;
        if (e >= SHIFTED_EXPONENT_MASK) { // Overflow
            outE = SHIFTED_EXPONENT_MASK;
        }
        else if (e <= 0) { // Underflow
            if (e < -8) {//This value is the precision of MIN_VALUE
                // The absolute fp32 value is less than MIN_VALUE, flush to +/-0
                return (short) (s << SIGN_SHIFT);
            }
            // The fp32 value is a normalized float less than MIN_NORMAL,
            // we convert to a subnormal fp16
            m |= 1 << FP32_EXPONENT_SHIFT;
            int shift = 15 - e;//This value is FP32_EXPONENT_SHIFT minus the precision of MIN_VALUE
            outM = m >> shift;
            int lowm = m & (1 << shift) - 1;
            int hway = 1 << shift - 1;
            // if above halfway or exactly halfway and outM is odd
            if (lowm + (outM & 1) > hway) {
                // Round to nearest even
                // Can overflow into exponent bit, which surprisingly is OK.
                // This increment relies on the +outM in the return statement below
                outM++;
            }
        }
        else {
            outE = e;
            outM = m >> FP32_EXPONENT_SHIFT - EXPONENT_SHIFT;
            // if above halfway or exactly halfway and outM is odd
            if ((m & LOST_MANTISSA_MASK) + (outM & 1) > 1 << FP32_EXPONENT_SHIFT - EXPONENT_SHIFT - 1) {
                // Round to nearest even
                // Can overflow into exponent bit, which surprisingly is OK.
                // This increment relies on the +outM in the return statement below
                outM++;
            }
        }
        // The outM is added here as the +1 increments for outM above can
        // cause an overflow in the exponent bit which is OK.
        return (short) (s << SIGN_SHIFT | (outE << EXPONENT_SHIFT) + outM);
    }
}
