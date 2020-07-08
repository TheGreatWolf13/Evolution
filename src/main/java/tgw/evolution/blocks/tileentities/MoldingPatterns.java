package tgw.evolution.blocks.tileentities;

import tgw.evolution.Evolution;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nonnull;

import static tgw.evolution.blocks.tileentities.Patterns.*;

public abstract class MoldingPatterns {

    public static final boolean[][][] NULL = FALSE_TENSOR;
    public static final boolean[][][] AXE = {AXE_FALSE, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX};
    public static final boolean[][][] CRUCIBLE = {TRUE_MATRIX, FULL_RING, FULL_RING, FULL_RING, FULL_RING};
    public static final boolean[][][] SHOVEL = {SHOVEL_FALSE, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX};
    public static final boolean[][][] HAMMER = {HAMMER_FALSE, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX};
    public static final boolean[][][] HOE = {HOE_FALSE, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX};
    public static final boolean[][][] PICKAXE = {{{true, true, false, true, true},
                                                  {true, false, true, true, true},
                                                  {true, false, true, true, true},
                                                  {true, false, true, true, true},
                                                  {true, true, false, true, true}}, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX};
    public static final boolean[][][] SPEAR = {JAVELIN_FALSE, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX};
    public static final boolean[][][] KNIFE = {KNIFE_FALSE, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX};
    public static final boolean[][][] PROSPECTING = {{{true, true, false, true, true},
                                                      {true, true, true, false, true},
                                                      {true, true, true, false, true},
                                                      {true, true, true, false, true},
                                                      {true, false, false, false, true}}, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX, FALSE_MATRIX};

    private static final boolean[][] SAW1 = {{true, true, true, false, false},
                                             {true, true, false, false, false},
                                             {true, false, false, false, true},
                                             {false, false, false, false, true},
                                             {false, false, true, true, true}};
    private static final boolean[][] SAW2 = MathHelper.rotateClockWise(SAW1);
    private static final boolean[][] SAW3 = MathHelper.rotateClockWise(SAW2);
    private static final boolean[][] SAW4 = MathHelper.rotateClockWise(SAW3);
    public static final boolean[][][] SAW = {SAW1, SAW2, SAW3, SAW4};
    private static final boolean[][] SWORD1 = {{false, false, true, true, true},
                                               {false, false, false, true, true},
                                               {true, false, false, false, true},
                                               {true, true, false, false, false},
                                               {true, true, true, false, true}};
    private static final boolean[][] SWORD2 = MathHelper.rotateClockWise(SWORD1);
    private static final boolean[][] SWORD3 = MathHelper.rotateClockWise(SWORD2);
    private static final boolean[][] SWORD4 = MathHelper.rotateClockWise(SWORD3);
    public static final boolean[][][] SWORD = {SWORD1, SWORD2, SWORD3, SWORD4};
    private static final boolean[][] GUARD1 = {{true, true, true, true, true},
                                               {true, true, true, true, true},
                                               {false, false, true, false, false},
                                               {true, false, false, false, true},
                                               {true, true, true, true, true}};
    private static final boolean[][] GUARD2 = MathHelper.rotateClockWise(GUARD1);
    private static final boolean[][] GUARD3 = MathHelper.rotateClockWise(GUARD2);
    private static final boolean[][] GUARD4 = MathHelper.rotateClockWise(GUARD3);
    public static final boolean[][][] GUARD = {GUARD1, GUARD2, GUARD3, GUARD4};
    private static final boolean[][] INGOT1 = {{true, true, true, true, true},
                                               {false, false, false, false, false},
                                               {false, false, false, false, false},
                                               {false, false, false, false, false},
                                               {true, true, true, true, true}};
    private static final boolean[][] INGOT2 = MathHelper.rotateClockWise(INGOT1);
    public static final boolean[][][] INGOT = {INGOT1, INGOT2};
    private static final boolean[][] BRICK_LAYER1 = {{true, true, true, true, false},
                                                     {true, true, true, true, false},
                                                     {false, false, false, false, false},
                                                     {true, true, true, true, false},
                                                     {true, true, true, true, false}};
    private static final boolean[][] BRICK_LAYER2 = MathHelper.rotateClockWise(BRICK_LAYER1);
    private static final boolean[][] BRICK_LAYER3 = MathHelper.rotateClockWise(BRICK_LAYER2);
    private static final boolean[][] BRICK_LAYER4 = MathHelper.rotateClockWise(BRICK_LAYER3);
    private static final boolean[][][] BRICK1 = {BRICK_LAYER1, BRICK_LAYER1};
    private static final boolean[][][] BRICK2 = {BRICK_LAYER2, BRICK_LAYER2};
    private static final boolean[][][] BRICK3 = {BRICK_LAYER3, BRICK_LAYER3};
    private static final boolean[][][] BRICK4 = {BRICK_LAYER4, BRICK_LAYER4};
    public static final boolean[][][][] BRICK = {BRICK1, BRICK2, BRICK3, BRICK4};

    public static void load() {
        Evolution.LOGGER.info("Molding patterns loaded");
    }

    public static boolean comparePatternsOneLayer(@Nonnull boolean[][] matrix, @Nonnull boolean[][][] pattern) {
        for (boolean[][] booleans : pattern) {
            if (MathHelper.matricesEqual(matrix, booleans)) {
                return true;
            }
        }
        return false;
    }

    public static boolean comparePatternsTwoLayer(@Nonnull boolean[][][] matrix, @Nonnull boolean[][][][] pattern) {
        for (boolean[][][] booleans : pattern) {
            if (MathHelper.matricesEqual(matrix[0], booleans[0])) {
                if (MathHelper.matricesEqual(matrix[1], booleans[1])) {
                    return true;
                }
            }
        }
        return false;
    }
}
