package tgw.evolution.blocks.tileentities;

import tgw.evolution.Evolution;

public abstract class KnappingPatterns {

    public static final boolean[][] NULL = {{false, false, false, false, false},
                                            {false, false, false, false, false},
                                            {false, false, false, false, false},
                                            {false, false, false, false, false},
                                            {false, false, false, false, false}};

    public static final boolean[][] AXE = {{false, true, true, true, false},
                                           {true, true, true, true, true},
                                           {false, true, true, true, false},
                                           {false, true, true, true, false},
                                           {false, false, true, false, false}};

    public static final boolean[][] JAVELIN = {{true, true, true, false, false},
                                               {true, true, true, true, false},
                                               {true, true, true, true, true},
                                               {false, true, true, true, false},
                                               {false, false, true, false, false}};

    public static final boolean[][] SHOVEL = {{false, false, false, false, false},
                                              {true, true, true, true, false},
                                              {true, true, true, true, true},
                                              {true, true, true, true, false},
                                              {false, false, false, false, false}};

    public static final boolean[][] HAMMER = {{false, true, true, false, false},
                                              {false, true, true, false, false},
                                              {false, true, true, true, false},
                                              {false, true, true, false, false},
                                              {false, true, true, false, false}};

    public static final boolean[][] HOE = {{false, true, false, false, false},
                                           {false, true, false, false, false},
                                           {false, true, false, false, false},
                                           {false, true, true, false, false},
                                           {false, true, true, false, false}};

    public static final boolean[][] KNIFE = {{true, true, true, true, true},
                                             {false, true, true, true, true},
                                             {false, false, false, false, false},
                                             {true, true, true, true, true},
                                             {false, true, true, true, true}};

    public static void load() {
        Evolution.LOGGER.info("Loaded Knapping Patterns");
    }
}
