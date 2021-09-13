package tgw.evolution.blocks.tileentities;

import tgw.evolution.Evolution;

import static tgw.evolution.blocks.tileentities.Patterns.*;

public final class KnappingPatterns {

    public static final long NULL = MATRIX_FALSE;
    public static final long AXE = AXE_TRUE;
    public static final long JAVELIN = JAVELIN_TRUE;
    public static final long SHOVEL = SHOVEL_TRUE;
    public static final long HAMMER = HAMMER_TRUE;
    public static final long HOE = HOE_TRUE;
    public static final long KNIFE = KNIFE_TRUE;

    private KnappingPatterns() {
    }

    public static void load() {
        Evolution.LOGGER.info("Loaded Knapping Patterns");
    }
}
