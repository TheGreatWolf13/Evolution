package tgw.evolution.blocks.tileentities;

import tgw.evolution.Evolution;

import static tgw.evolution.blocks.tileentities.Patterns.*;

public final class KnappingPatterns {

    public static final boolean[][] NULL = FALSE_MATRIX;
    public static final boolean[][] AXE = AXE_TRUE;
    public static final boolean[][] JAVELIN = JAVELIN_TRUE;
    public static final boolean[][] SHOVEL = SHOVEL_TRUE;
    public static final boolean[][] HAMMER = HAMMER_TRUE;
    public static final boolean[][] HOE = HOE_TRUE;
    public static final boolean[][] KNIFE = KNIFE_TRUE;

    private KnappingPatterns() {
    }

    public static void load() {
        Evolution.LOGGER.info("Loaded Knapping Patterns");
    }
}
