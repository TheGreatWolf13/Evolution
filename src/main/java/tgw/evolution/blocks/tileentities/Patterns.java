package tgw.evolution.blocks.tileentities;

public final class Patterns {

    //2D
    public static final long MATRIX_TRUE = 0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_11111111L;
    public static final long MATRIX_FALSE = ~MATRIX_TRUE;
    public static final long AXE_TRUE = 0b00011000_00111100_01111100_11111100_11111110_01111111_00001111_00000110L;
    public static final long AXE_FALSE = ~AXE_TRUE;
    public static final long HAMMER_TRUE = 0b00110000_01111000_11111100_01111110_00111111_00011111_00001110_00000100L;
    public static final long HAMMER_FALSE = ~HAMMER_TRUE;
    public static final long HOE_TRUE = 0b00000000_01100000_01110000_01111000_00111100_00011110_00001111_00000110L;
    public static final long HOE_FALSE = ~HOE_TRUE;
    public static final long JAVELIN_TRUE = 0b11100000_11111000_11111100_01111110_01111110_00111100_00011000_00000000L;
    public static final long JAVELIN_FALSE = ~JAVELIN_TRUE;
    public static final long KNIFE_TRUE = 0b11000000_11100000_11110000_11111000_01111100_00111110_00011100_00001000L;
    public static final long KNIFE_FALSE = ~KNIFE_TRUE;
    public static final long RING_CORNERLESS = 0b01111110_10000001_10000001_10000001_10000001_10000001_10000001_01111110L;
    public static final long RING_FULL = 0b11111111_10000001_10000001_10000001_10000001_10000001_10000001_11111111L;
    public static final long SHOVEL_TRUE = 0b00001000_00011100_00111110_01111111_11111110_01111100_01111000_00010000L;
    public static final long SHOVEL_FALSE = ~SHOVEL_TRUE;
    //3D
    public static final long[] FALSE_TENSOR = {0, 0, 0, 0, 0, 0, 0, 0};

    private Patterns() {
    }
}
