package tgw.evolution.blocks.tileentities;

import tgw.evolution.Evolution;
import tgw.evolution.util.MathHelper;

public abstract class MoldingPatterns {

    private static final boolean[][] FULL = {{true, true, true, true, true},
                                             {true, true, true, true, true},
                                             {true, true, true, true, true},
                                             {true, true, true, true, true},
                                             {true, true, true, true, true}};
    private static final boolean[][] RING = {{false, true, true, true, false},
                                             {true, false, false, false, true},
                                             {true, false, false, false, true},
                                             {true, false, false, false, true},
                                             {false, true, true, true, false}};
    private static final boolean[][] FULL_RING = {{true, true, true, true, true},
                                                  {true, false, false, false, true},
                                                  {true, false, false, false, true},
                                                  {true, false, false, false, true},
                                                  {true, true, true, true, true}};
    private static final boolean[][] AXE1 = {{true, false, true, true, true},
                                             {false, false, false, false, true},
                                             {false, false, false, false, false},
                                             {false, false, false, false, true},
                                             {true, false, true, true, true}};
    private static final boolean[][] AXE2 = MathHelper.rotateClockWise(AXE1);
    private static final boolean[][] AXE3 = MathHelper.rotateClockWise(AXE2);
    private static final boolean[][] AXE4 = MathHelper.rotateClockWise(AXE3);
    public static final boolean[][][] AXE = {AXE1, AXE2, AXE3, AXE4};
    private static final boolean[][] SHOVEL1 = {{true, false, false, false, true},
                                                {true, false, false, false, true},
                                                {true, false, false, false, true},
                                                {true, false, false, false, true},
                                                {true, true, false, true, true}};
    private static final boolean[][] SHOVEL2 = MathHelper.rotateClockWise(SHOVEL1);
    private static final boolean[][] SHOVEL3 = MathHelper.rotateClockWise(SHOVEL2);
    private static final boolean[][] SHOVEL4 = MathHelper.rotateClockWise(SHOVEL3);
    public static final boolean[][][] SHOVEL = {SHOVEL1, SHOVEL2, SHOVEL3, SHOVEL4};
    private static final boolean[][] PICKAXE1 = {{true, true, true, true, true},
                                                 {true, false, false, false, true},
                                                 {false, true, true, true, false},
                                                 {true, true, true, true, true},
                                                 {true, true, true, true, true}};
    private static final boolean[][] PICKAXE2 = MathHelper.rotateClockWise(PICKAXE1);
    private static final boolean[][] PICKAXE3 = MathHelper.rotateClockWise(PICKAXE2);
    private static final boolean[][] PICKAXE4 = MathHelper.rotateClockWise(PICKAXE3);
    public static final boolean[][][] PICKAXE = {PICKAXE1, PICKAXE2, PICKAXE3, PICKAXE4};
    private static final boolean[][] HAMMER1 = {{true, true, true, true, true},
                                                {false, false, false, false, false},
                                                {false, false, false, false, false},
                                                {true, true, false, true, true},
                                                {true, true, true, true, true}};
    private static final boolean[][] HAMMER2 = MathHelper.rotateClockWise(HAMMER1);
    private static final boolean[][] HAMMER3 = MathHelper.rotateClockWise(HAMMER2);
    private static final boolean[][] HAMMER4 = MathHelper.rotateClockWise(HAMMER3);
    public static final boolean[][][] HAMMER = {HAMMER1, HAMMER2, HAMMER3, HAMMER4};
    private static final boolean[][] HOE1 = {{true, true, true, true, true},
                                             {false, false, false, false, false},
                                             {false, false, true, true, true},
                                             {true, true, true, true, true},
                                             {true, true, true, true, true}};
    private static final boolean[][] HOE2 = MathHelper.rotateClockWise(HOE1);
    private static final boolean[][] HOE3 = MathHelper.rotateClockWise(HOE2);
    private static final boolean[][] HOE4 = MathHelper.rotateClockWise(HOE3);
    public static final boolean[][][] HOE = {HOE1, HOE2, HOE3, HOE4};
    private static final boolean[][] SPEAR1 = {{false, false, false, true, true},
                                               {false, false, false, false, true},
                                               {false, false, false, false, false},
                                               {true, false, false, false, true},
                                               {true, true, false, true, true}};
    private static final boolean[][] SPEAR2 = MathHelper.rotateClockWise(SPEAR1);
    private static final boolean[][] SPEAR3 = MathHelper.rotateClockWise(SPEAR2);
    private static final boolean[][] SPEAR4 = MathHelper.rotateClockWise(SPEAR3);
    public static final boolean[][][] SPEAR = {SPEAR1, SPEAR2, SPEAR3, SPEAR4};
    private static final boolean[][] KNIFE1 = {{true, true, false, true, true},
                                               {true, false, false, true, true},
                                               {true, false, false, true, true},
                                               {true, false, false, true, true},
                                               {true, false, false, true, true}};
    private static final boolean[][] KNIFE2 = MathHelper.rotateClockWise(KNIFE1);
    private static final boolean[][] KNIFE3 = MathHelper.rotateClockWise(KNIFE2);
    private static final boolean[][] KNIFE4 = MathHelper.rotateClockWise(KNIFE3);
    public static final boolean[][][] KNIFE = {KNIFE1, KNIFE2, KNIFE3, KNIFE4};
    private static final boolean[][] PROSPECTING1 = {{true, true, true, true, true},
                                                     {true, false, false, false, false},
                                                     {false, true, true, true, false},
                                                     {true, true, true, true, false},
                                                     {true, true, true, true, true}};
    private static final boolean[][] PROSPECTING2 = MathHelper.rotateClockWise(PROSPECTING1);
    private static final boolean[][] PROSPECTING3 = MathHelper.rotateClockWise(PROSPECTING2);
    private static final boolean[][] PROSPECTING4 = MathHelper.rotateClockWise(PROSPECTING3);
    public static final boolean[][][] PROSPECTING = {PROSPECTING1, PROSPECTING2, PROSPECTING3, PROSPECTING4};
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
    public static final boolean[][] PLATE = KnappingPatterns.NULL;
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
    public static final boolean[][][] CRUCIBLE = {FULL, FULL_RING, FULL_RING, FULL_RING, FULL_RING};

    public static void load() {
        Evolution.LOGGER.info("Molding patterns loaded");
    }

    public static boolean comparePatternsOneLayer(boolean[][] matrix, boolean[][][] pattern) {
        for (boolean[][] booleans : pattern) {
            if (MathHelper.matricesEqual(matrix, booleans)) {
                return true;
            }
        }
        return false;
    }

    public static boolean comparePatternsTwoLayer(boolean[][][] matrix, boolean[][][][] pattern) {
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
