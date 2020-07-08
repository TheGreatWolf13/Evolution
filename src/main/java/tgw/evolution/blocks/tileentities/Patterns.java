package tgw.evolution.blocks.tileentities;

import tgw.evolution.util.MathHelper;

public abstract class Patterns {

    public static final boolean[] FALSE_ARRAY = {false, false, false, false, false};
    public static final boolean[] TRUE_ARRAY = {true, true, true, true, true};
    public static final boolean[][] FALSE_MATRIX = {FALSE_ARRAY, FALSE_ARRAY, FALSE_ARRAY, FALSE_ARRAY, FALSE_ARRAY};
    public static final boolean[][][] FALSE_TENSOR = {FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX};
    public static final boolean[][] AXE_TRUE = {{false, true, true, true, false},
                                                TRUE_ARRAY,
                                                {false, true, true, true, false},
                                                {false, true, true, true, false},
                                                {false, false, true, false, false}};
    public static final boolean[][] AXE_FALSE = MathHelper.invertMatrix(AXE_TRUE);
    public static final boolean[][] JAVELIN_TRUE = {{true, true, true, false, false},
                                                    {true, true, true, true, false},
                                                    TRUE_ARRAY,
                                                    {false, true, true, true, false},
                                                    {false, false, true, false, false}};
    public static final boolean[][] JAVELIN_FALSE = MathHelper.invertMatrix(JAVELIN_TRUE);
    public static final boolean[][] SHOVEL_TRUE = {FALSE_ARRAY,
                                                   {true, true, true, true, false},
                                                   TRUE_ARRAY,
                                                   {true, true, true, true, false},
                                                   FALSE_ARRAY};
    public static final boolean[][] SHOVEL_FALSE = MathHelper.invertMatrix(SHOVEL_TRUE);
    public static final boolean[][] HAMMER_TRUE = {{false, true, true, false, false},
                                                   {false, true, true, false, false},
                                                   {false, true, true, true, false},
                                                   {false, true, true, false, false},
                                                   {false, true, true, false, false}};
    public static final boolean[][] HAMMER_FALSE = MathHelper.invertMatrix(HAMMER_TRUE);
    public static final boolean[][] HOE_TRUE = {{false, true, false, false, false},
                                                {false, true, false, false, false},
                                                {false, true, false, false, false},
                                                {false, true, true, false, false},
                                                {false, true, true, false, false}};
    public static final boolean[][] HOE_FALSE = MathHelper.invertMatrix(HOE_TRUE);
    public static final boolean[][] KNIFE_TRUE = {TRUE_ARRAY,
                                                  {false, true, true, true, true},
                                                  FALSE_ARRAY,
                                                  TRUE_ARRAY,
                                                  {false, true, true, true, true}};
    public static final boolean[][] KNIFE_FALSE = MathHelper.invertMatrix(KNIFE_TRUE);
    public static final boolean[][] TRUE_MATRIX = {TRUE_ARRAY, TRUE_ARRAY, TRUE_ARRAY, TRUE_ARRAY, TRUE_ARRAY};
    public static final boolean[][] RING = {{false, true, true, true, false},
                                            {true, false, false, false, true},
                                            {true, false, false, false, true},
                                            {true, false, false, false, true},
                                            {false, true, true, true, false}};
    public static final boolean[][] FULL_RING = {TRUE_ARRAY,
                                                 {true, false, false, false, true},
                                                 {true, false, false, false, true},
                                                 {true, false, false, false, true},
                                                 TRUE_ARRAY};
}
