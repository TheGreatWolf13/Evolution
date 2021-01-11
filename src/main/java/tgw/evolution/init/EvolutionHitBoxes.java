package tgw.evolution.init;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import tgw.evolution.util.MathHelper;

public final class EvolutionHitBoxes {

    public static final VoxelShape TORCH = Block.makeCuboidShape(6, 0, 6, 10, 10, 10);
    public static final VoxelShape SLAB_LOWER = Block.makeCuboidShape(0, 0, 0, 16, 8, 16);
    public static final VoxelShape SINGLE_LOG_1 = Block.makeCuboidShape(0, 0, 0, 4, 4, 16);
    public static final VoxelShape DOUBLE_LOG_1 = Block.makeCuboidShape(0, 0, 0, 8, 4, 16);
    public static final VoxelShape TRIPLE_LOG_1 = Block.makeCuboidShape(0, 0, 0, 12, 4, 16);
    public static final VoxelShape QUARTER_SLAB_LOWER_1 = Block.makeCuboidShape(0, 0, 0, 16, 4, 16);
    public static final VoxelShape QUARTER_SLAB_LOWER_2 = SLAB_LOWER;
    public static final VoxelShape QUARTER_SLAB_LOWER_3 = Block.makeCuboidShape(0, 0, 0, 16, 12, 16);
    public static final VoxelShape SINGLE_LOG_2 = Block.makeCuboidShape(0, 0, 0, 4, 8, 16);
    public static final VoxelShape DOUBLE_LOG_2 = Block.makeCuboidShape(0, 0, 0, 8, 8, 16);
    public static final VoxelShape TRIPLE_LOG_2 = Block.makeCuboidShape(0, 0, 0, 12, 8, 16);
    public static final VoxelShape SINGLE_LOG_3 = Block.makeCuboidShape(0, 0, 0, 4, 12, 16);
    public static final VoxelShape DOUBLE_LOG_3 = Block.makeCuboidShape(0, 0, 0, 8, 12, 16);
    public static final VoxelShape TRIPLE_LOG_3 = Block.makeCuboidShape(0, 0, 0, 12, 12, 16);
    public static final VoxelShape SINGLE_LOG_4 = Block.makeCuboidShape(0, 0, 0, 4, 16, 16);
    public static final VoxelShape DOUBLE_LOG_4 = Block.makeCuboidShape(0, 0, 0, 8, 16, 16);
    public static final VoxelShape TRIPLE_LOG_4 = Block.makeCuboidShape(0, 0, 0, 12, 16, 16);
    public static final VoxelShape MOLD_1 = Block.makeCuboidShape(0, 0, 0, 16, 3, 16);
    public static final VoxelShape MOLD_2 = Block.makeCuboidShape(0, 0, 0, 16, 6, 16);
    public static final VoxelShape MOLD_3 = Block.makeCuboidShape(0, 0, 0, 16, 9, 16);
    public static final VoxelShape MOLD_4 = Block.makeCuboidShape(0, 0, 0, 16, 12, 16);
    public static final VoxelShape MOLD_5 = Block.makeCuboidShape(0.5, 0, 0.5, 15.5, 15, 15.5);
    public static final VoxelShape OCTAVE_SLAB_LOWER_1 = Block.makeCuboidShape(0, 0, 0, 16, 2, 16);
    public static final VoxelShape OCTAVE_SLAB_LOWER_2 = QUARTER_SLAB_LOWER_1;
    public static final VoxelShape OCTAVE_SLAB_LOWER_3 = Block.makeCuboidShape(0, 0, 0, 16, 6, 16);
    public static final VoxelShape OCTAVE_SLAB_LOWER_4 = QUARTER_SLAB_LOWER_2;
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_1 = Block.makeCuboidShape(0, 0, 0, 16, 1, 16);
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_2 = OCTAVE_SLAB_LOWER_1;
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_3 = Block.makeCuboidShape(0, 0, 0, 16, 3, 16);
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_4 = OCTAVE_SLAB_LOWER_2;
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_5 = Block.makeCuboidShape(0, 0, 0, 16, 5, 16);
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_6 = OCTAVE_SLAB_LOWER_3;
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_7 = Block.makeCuboidShape(0, 0, 0, 16, 7, 16);
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_8 = OCTAVE_SLAB_LOWER_4;
    public static final VoxelShape GROUND_ITEM = Block.makeCuboidShape(5, 0, 5, 11, 0.5, 11);
    public static final VoxelShape GROUND_ROCK = Block.makeCuboidShape(4.5, 0, 4.5, 11.5, 2, 11.5);
    public static final VoxelShape SAPLING = Block.makeCuboidShape(2, 0, 2, 14, 12, 14);
    public static final VoxelShape GRASS = Block.makeCuboidShape(2, 0, 2, 14, 13, 14);
    public static final VoxelShape TORCH_EAST = Block.makeCuboidShape(0, 3, 5.5, 5, 13, 10.5);
    public static final VoxelShape TORCH_WEST = Block.makeCuboidShape(11, 3, 5.5, 16, 13, 10.5);
    public static final VoxelShape TORCH_SOUTH = Block.makeCuboidShape(5.5, 3, 0, 10.5, 13, 5);
    public static final VoxelShape TORCH_NORTH = Block.makeCuboidShape(5.5, 3, 11, 10.5, 13, 16);
    public static final VoxelShape SIXTEENTH_SLAB_UPPER_1 = Block.makeCuboidShape(0, 15, 0, 16, 16, 16);
    public static final VoxelShape SIXTEENTH_SLAB_WEST_1 = Block.makeCuboidShape(0, 0, 0, 1, 16, 16);
    public static final VoxelShape SIXTEENTH_SLAB_EAST_1 = Block.makeCuboidShape(15, 0, 0, 16, 16, 16);
    public static final VoxelShape SIXTEENTH_SLAB_NORTH_1 = Block.makeCuboidShape(0, 0, 0, 16, 16, 1);
    public static final VoxelShape SIXTEENTH_SLAB_SOUTH_1 = Block.makeCuboidShape(0, 0, 15, 16, 16, 16);
    public static final VoxelShape ROPE_GROUND_X = Block.makeCuboidShape(0, 0, 6, 16, 2, 10);
    public static final VoxelShape ROPE_GROUND_Z = MathHelper.rotateShape(Direction.EAST, Direction.NORTH, ROPE_GROUND_X);
    public static final VoxelShape ROPE_WALL_NORTH = Block.makeCuboidShape(6, 0, 0, 10, 16, 2);
    public static final VoxelShape ROPE_WALL_EAST = MathHelper.rotateShape(Direction.NORTH, Direction.EAST, ROPE_WALL_NORTH);
    public static final VoxelShape ROPE_WALL_SOUTH = MathHelper.rotateShape(Direction.NORTH, Direction.SOUTH, ROPE_WALL_NORTH);
    public static final VoxelShape ROPE_WALL_WEST = MathHelper.rotateShape(Direction.NORTH, Direction.WEST, ROPE_WALL_NORTH);
    public static final VoxelShape HOOK_SOUTH = Block.makeCuboidShape(4, 0, 0, 12, 6, 11);
    public static final VoxelShape HOOK_NORTH = MathHelper.rotateShape(Direction.SOUTH, Direction.NORTH, HOOK_SOUTH);
    public static final VoxelShape HOOK_EAST = MathHelper.rotateShape(Direction.SOUTH, Direction.EAST, HOOK_SOUTH);
    public static final VoxelShape HOOK_WEST = MathHelper.rotateShape(Direction.SOUTH, Direction.WEST, HOOK_SOUTH);
    public static final VoxelShape KNAPPING_FULL = Block.makeCuboidShape(0.5, 0, 0.5, 15.5, 1, 15.5);
    public static final VoxelShape KNAPPING_PART = Block.makeCuboidShape(0.5, 0, 0.5, 3.5, 1, 3.5);
    public static final VoxelShape[] LOG_PILE = {VoxelShapes.empty(),
                                                 SINGLE_LOG_1,
                                                 DOUBLE_LOG_1,
                                                 TRIPLE_LOG_1,
                                                 QUARTER_SLAB_LOWER_1,
                                                 SINGLE_LOG_2,
                                                 DOUBLE_LOG_2,
                                                 TRIPLE_LOG_2,
                                                 SLAB_LOWER,
                                                 SINGLE_LOG_3,
                                                 DOUBLE_LOG_3,
                                                 TRIPLE_LOG_3,
                                                 QUARTER_SLAB_LOWER_3,
                                                 SINGLE_LOG_4,
                                                 DOUBLE_LOG_4,
                                                 TRIPLE_LOG_4};
    public static final VoxelShape[] MOLD_CLAY = {MOLD_1, MOLD_2, MOLD_3, MOLD_4, MOLD_5};
    public static final VoxelShape MOLD_PART = Block.makeCuboidShape(0.5, 0, 0.5, 3.5, 3, 3.5);
    public static final VoxelShape[] PEAT = {VoxelShapes.empty(),
                                             QUARTER_SLAB_LOWER_1,
                                             QUARTER_SLAB_LOWER_2,
                                             QUARTER_SLAB_LOWER_3,
                                             VoxelShapes.fullCube()};
    public static final VoxelShape[] PIT_KILN = {SIXTEENTH_SLAB_LOWER_1,
                                                 SIXTEENTH_SLAB_LOWER_1,
                                                 SIXTEENTH_SLAB_LOWER_2,
                                                 SIXTEENTH_SLAB_LOWER_3,
                                                 SIXTEENTH_SLAB_LOWER_4,
                                                 SIXTEENTH_SLAB_LOWER_5,
                                                 SIXTEENTH_SLAB_LOWER_6,
                                                 SIXTEENTH_SLAB_LOWER_7,
                                                 SIXTEENTH_SLAB_LOWER_8,
                                                 MathHelper.union(SINGLE_LOG_3, SIXTEENTH_SLAB_LOWER_8),
                                                 MathHelper.union(DOUBLE_LOG_3, SIXTEENTH_SLAB_LOWER_8),
                                                 MathHelper.union(TRIPLE_LOG_3, SIXTEENTH_SLAB_LOWER_8),
                                                 QUARTER_SLAB_LOWER_3,
                                                 MathHelper.union(SINGLE_LOG_4, QUARTER_SLAB_LOWER_3),
                                                 MathHelper.union(DOUBLE_LOG_4, QUARTER_SLAB_LOWER_3),
                                                 MathHelper.union(TRIPLE_LOG_4, QUARTER_SLAB_LOWER_3),
                                                 VoxelShapes.fullCube()};
    private static final VoxelShape MOLD_BASE = VoxelShapes.create(0, 0, 0, 1, 0.5 / 16, 1);
    private static final VoxelShape MOLD_BASE_N = VoxelShapes.create(0, 0, 0, 1, 3 / 16.0, 0.5 / 16);
    private static final VoxelShape MOLD_BASE_S = VoxelShapes.create(0, 0, 15.5 / 16, 1, 3 / 16.0, 1);
    private static final VoxelShape MOLD_BASE_W = VoxelShapes.create(0, 0, 0, 0.5 / 16, 3 / 16.0, 1);
    private static final VoxelShape MOLD_BASE_E = VoxelShapes.create(15.5 / 16, 0, 0, 1, 3 / 16.0, 1);
    public static final VoxelShape MOLD_TOTAL_BASE = VoxelShapes.or(MOLD_BASE, MOLD_BASE_N, MOLD_BASE_S, MOLD_BASE_W, MOLD_BASE_E);
    private static final VoxelShape PICKAXE1_THICK = Block.makeCuboidShape(3.5, 0.5, 3.5, 12.5, 3, 6.5);
    private static final VoxelShape PICKAXE2_THICK = Block.makeCuboidShape(0.5, 0.5, 6.5, 3.5, 3, 9.5);
    private static final VoxelShape PICKAXE3_THICK = Block.makeCuboidShape(12.5, 0.5, 6.5, 15.5, 3, 9.5);
    public static final VoxelShape PICKAXE_THICK = VoxelShapes.or(PICKAXE1_THICK, PICKAXE2_THICK, PICKAXE3_THICK);
    private static final VoxelShape AXE1 = Block.makeCuboidShape(0.5, 0, 3.5, 12.5, 1, 12.5);
    private static final VoxelShape AXE2 = Block.makeCuboidShape(12.5, 0, 6.5, 15.5, 1, 9.5);
    private static final VoxelShape AXE3 = Block.makeCuboidShape(3.5, 0, 0.5, 6.5, 1, 15.5);
    public static final VoxelShape AXE = VoxelShapes.or(AXE1, AXE2, AXE3);
    private static final VoxelShape AXE1_THICK = Block.makeCuboidShape(0.5, 0.5, 3.5, 12.5, 3, 12.5);
    private static final VoxelShape AXE2_THICK = Block.makeCuboidShape(12.5, 0.5, 6.5, 15.5, 3, 9.5);
    private static final VoxelShape AXE3_THICK = Block.makeCuboidShape(3.5, 0.5, 0.5, 6.5, 3, 15.5);
    public static final VoxelShape AXE_THICK = VoxelShapes.or(AXE1_THICK, AXE2_THICK, AXE3_THICK);
    private static final VoxelShape JAVELIN1 = Block.makeCuboidShape(0.5, 0, 0.5, 9.5, 1, 3.5);
    private static final VoxelShape JAVELIN2 = Block.makeCuboidShape(0.5, 0, 3.5, 12.5, 1, 6.5);
    private static final VoxelShape JAVELIN3 = Block.makeCuboidShape(0.5, 0, 6.5, 15.5, 1, 9.5);
    private static final VoxelShape JAVELIN4 = Block.makeCuboidShape(3.5, 0, 9.5, 12.5, 1, 12.5);
    private static final VoxelShape JAVELIN5 = Block.makeCuboidShape(6.5, 0, 12.5, 9.5, 1, 15.5);
    public static final VoxelShape JAVELIN = VoxelShapes.or(JAVELIN1, JAVELIN2, JAVELIN3, JAVELIN4, JAVELIN5);
    private static final VoxelShape SHOVEL1 = Block.makeCuboidShape(3.5, 0, 0.5, 12.5, 1, 12.5);
    private static final VoxelShape SHOVEL2 = Block.makeCuboidShape(6.5, 0, 12.5, 9.5, 1, 15.5);
    public static final VoxelShape SHOVEL = VoxelShapes.or(SHOVEL1, SHOVEL2);
    private static final VoxelShape HAMMER1 = Block.makeCuboidShape(0.5, 0, 3.5, 15.5, 1, 9.5);
    private static final VoxelShape HAMMER2 = Block.makeCuboidShape(6.5, 0, 9.5, 9.5, 1, 12.5);
    public static final VoxelShape HAMMER = VoxelShapes.or(HAMMER1, HAMMER2);
    private static final VoxelShape HOE1 = Block.makeCuboidShape(0.5, 0, 3.5, 15.5, 1, 6.5);
    private static final VoxelShape HOE2 = Block.makeCuboidShape(9.5, 0, 6.5, 15.5, 1, 9.5);
    public static final VoxelShape HOE = VoxelShapes.or(HOE1, HOE2);
    private static final VoxelShape KNIFE1 = Block.makeCuboidShape(0.5, 0, 0.5, 3.5, 1, 15.5);
    private static final VoxelShape KNIFE2 = Block.makeCuboidShape(3.5, 0, 3.5, 6.5, 1, 15.5);
    private static final VoxelShape KNIFE3 = Block.makeCuboidShape(9.5, 0, 0.5, 12.5, 1, 15.5);
    private static final VoxelShape KNIFE4 = Block.makeCuboidShape(12.5, 0, 3.5, 15.5, 1, 15.5);
    public static final VoxelShape KNIFE = VoxelShapes.or(KNIFE1, KNIFE2, KNIFE3, KNIFE4);

    private EvolutionHitBoxes() {
    }
}
