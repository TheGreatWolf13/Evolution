package tgw.evolution.init;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.util.math.MathHelper;

public final class EvolutionHitBoxes {

    //Simple shapes
    //2 Sections
    public static final VoxelShape SLAB_LOWER = Block.box(0, 0, 0, 16, 8, 16);
    //4 Sections
    public static final VoxelShape QUARTER_SLAB_LOWER_1 = Block.box(0, 0, 0, 16, 4, 16);
    public static final VoxelShape QUARTER_SLAB_LOWER_2 = SLAB_LOWER;
    public static final VoxelShape QUARTER_SLAB_LOWER_3 = Block.box(0, 0, 0, 16, 12, 16);
    //8 Sections
    public static final VoxelShape OCTAVE_SLAB_LOWER_1 = Block.box(0, 0, 0, 16, 2, 16);
    public static final VoxelShape OCTAVE_SLAB_LOWER_2 = QUARTER_SLAB_LOWER_1;
    public static final VoxelShape OCTAVE_SLAB_LOWER_3 = Block.box(0, 0, 0, 16, 6, 16);
    public static final VoxelShape OCTAVE_SLAB_LOWER_4 = QUARTER_SLAB_LOWER_2;
    //16 Sections
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_1 = Block.box(0, 0, 0, 16, 1, 16);
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_2 = OCTAVE_SLAB_LOWER_1;
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_3 = Block.box(0, 0, 0, 16, 3, 16);
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_4 = OCTAVE_SLAB_LOWER_2;
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_5 = Block.box(0, 0, 0, 16, 5, 16);
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_6 = OCTAVE_SLAB_LOWER_3;
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_7 = Block.box(0, 0, 0, 16, 7, 16);
    public static final VoxelShape SIXTEENTH_SLAB_LOWER_8 = OCTAVE_SLAB_LOWER_4;

    public static final VoxelShape SIXTEENTH_SLAB_UPPER_1 = Block.box(0, 15, 0, 16, 16, 16);
    public static final VoxelShape SIXTEENTH_SLAB_WEST_1 = Block.box(0, 0, 0, 1, 16, 16);
    public static final VoxelShape SIXTEENTH_SLAB_EAST_1 = Block.box(15, 0, 0, 16, 16, 16);
    public static final VoxelShape SIXTEENTH_SLAB_NORTH_1 = Block.box(0, 0, 0, 16, 16, 1);
    public static final VoxelShape SIXTEENTH_SLAB_SOUTH_1 = Block.box(0, 0, 15, 16, 16, 16);
    //Knapping
    public static final VoxelShape KNAPPING_PART = Block.box(0, 0, 0, 2, 1, 2);
    //Molding
    public static final VoxelShape MOLDING_PART = Block.box(0, 0, 0, 2, 2, 2);
    //General Blocks
    public static final VoxelShape GRASS = Block.box(2, 0, 2, 14, 13, 14);
    public static final VoxelShape GROUND_ITEM = Block.box(5, 0, 5, 11, 0.5, 11);
    public static final VoxelShape GROUND_ROCK = Block.box(4.5, 0, 4.5, 11.5, 2, 11.5);
    public static final VoxelShape HOOK_NORTH = Block.box(4, 0, 5, 12, 6, 16);
    public static final VoxelShape HOOK_SOUTH = MathHelper.rotateShape(Direction.NORTH, Direction.SOUTH, HOOK_NORTH);
    public static final VoxelShape HOOK_EAST = MathHelper.rotateShape(Direction.NORTH, Direction.EAST, HOOK_NORTH);
    public static final VoxelShape HOOK_WEST = MathHelper.rotateShape(Direction.NORTH, Direction.WEST, HOOK_NORTH);
    public static final VoxelShape LOG_SINGLE_1 = Block.box(0, 0, 0, 4, 4, 16);
    public static final VoxelShape LOG_SINGLE_2 = Block.box(0, 0, 0, 4, 8, 16);
    public static final VoxelShape LOG_SINGLE_3 = Block.box(0, 0, 0, 4, 12, 16);
    public static final VoxelShape LOG_SINGLE_4 = Block.box(0, 0, 0, 4, 16, 16);
    public static final VoxelShape LOG_DOUBLE_1 = Block.box(0, 0, 0, 8, 4, 16);
    public static final VoxelShape LOG_DOUBLE_2 = Block.box(0, 0, 0, 8, 8, 16);
    public static final VoxelShape LOG_DOUBLE_3 = Block.box(0, 0, 0, 8, 12, 16);
    public static final VoxelShape LOG_DOUBLE_4 = Block.box(0, 0, 0, 8, 16, 16);
    public static final VoxelShape LOG_TRIPLE_1 = Block.box(0, 0, 0, 12, 4, 16);
    public static final VoxelShape LOG_TRIPLE_2 = Block.box(0, 0, 0, 12, 8, 16);
    public static final VoxelShape LOG_TRIPLE_3 = Block.box(0, 0, 0, 12, 12, 16);
    public static final VoxelShape LOG_TRIPLE_4 = Block.box(0, 0, 0, 12, 16, 16);
    public static final VoxelShape[] LOG_PILE = {Shapes.empty(), LOG_SINGLE_1, LOG_DOUBLE_1, LOG_TRIPLE_1,
                                                 QUARTER_SLAB_LOWER_1, LOG_SINGLE_2, LOG_DOUBLE_2, LOG_TRIPLE_2,
                                                 SLAB_LOWER, LOG_SINGLE_3, LOG_DOUBLE_3, LOG_TRIPLE_3,
                                                 QUARTER_SLAB_LOWER_3, LOG_SINGLE_4, LOG_DOUBLE_4, LOG_TRIPLE_4};
    public static final VoxelShape[] PEAT = {Shapes.empty(),
                                             QUARTER_SLAB_LOWER_1,
                                             QUARTER_SLAB_LOWER_2,
                                             QUARTER_SLAB_LOWER_3,
                                             Shapes.block()};
    public static final VoxelShape[] PIT_KILN = {SIXTEENTH_SLAB_LOWER_1,
                                                 SIXTEENTH_SLAB_LOWER_1,
                                                 SIXTEENTH_SLAB_LOWER_2,
                                                 SIXTEENTH_SLAB_LOWER_3,
                                                 SIXTEENTH_SLAB_LOWER_4,
                                                 SIXTEENTH_SLAB_LOWER_5,
                                                 SIXTEENTH_SLAB_LOWER_6,
                                                 SIXTEENTH_SLAB_LOWER_7,
                                                 SIXTEENTH_SLAB_LOWER_8,
                                                 MathHelper.union(LOG_SINGLE_3, SIXTEENTH_SLAB_LOWER_8),
                                                 MathHelper.union(LOG_DOUBLE_3, SIXTEENTH_SLAB_LOWER_8),
                                                 MathHelper.union(LOG_TRIPLE_3, SIXTEENTH_SLAB_LOWER_8),
                                                 QUARTER_SLAB_LOWER_3,
                                                 MathHelper.union(LOG_SINGLE_4, QUARTER_SLAB_LOWER_3),
                                                 MathHelper.union(LOG_DOUBLE_4, QUARTER_SLAB_LOWER_3),
                                                 MathHelper.union(LOG_TRIPLE_4, QUARTER_SLAB_LOWER_3),
                                                 Shapes.block()};
    public static final VoxelShape ROPE_GROUND_X = Block.box(0, 0, 6, 16, 2, 10);
    public static final VoxelShape ROPE_GROUND_Z = MathHelper.rotateShape(Direction.EAST, Direction.NORTH, ROPE_GROUND_X);
    public static final VoxelShape ROPE_WALL_NORTH = Block.box(6, 0, 0, 10, 16, 2);
    public static final VoxelShape ROPE_WALL_SOUTH = MathHelper.rotateShape(Direction.NORTH, Direction.SOUTH, ROPE_WALL_NORTH);
    public static final VoxelShape ROPE_WALL_EAST = MathHelper.rotateShape(Direction.NORTH, Direction.EAST, ROPE_WALL_NORTH);
    public static final VoxelShape ROPE_WALL_WEST = MathHelper.rotateShape(Direction.NORTH, Direction.WEST, ROPE_WALL_NORTH);
    public static final VoxelShape SAPLING = Block.box(2, 0, 2, 14, 12, 14);
    public static final VoxelShape TORCH = Block.box(6, 0, 6, 10, 10, 10);
    public static final VoxelShape TORCH_NORTH = Block.box(5.5, 3, 11, 10.5, 13, 16);
    public static final VoxelShape TORCH_SOUTH = Block.box(5.5, 3, 0, 10.5, 13, 5);
    public static final VoxelShape TORCH_EAST = Block.box(0, 3, 5.5, 5, 13, 10.5);
    public static final VoxelShape TORCH_WEST = Block.box(11, 3, 5.5, 16, 13, 10.5);

    public static final VoxelShape MOLD_1 = Block.box(0, 0, 0, 16, 3, 16);
    public static final VoxelShape MOLD_2 = Block.box(0, 0, 0, 16, 6, 16);
    public static final VoxelShape MOLD_3 = Block.box(0, 0, 0, 16, 9, 16);
    public static final VoxelShape MOLD_4 = Block.box(0, 0, 0, 16, 12, 16);
    public static final VoxelShape MOLD_5 = Block.box(0.5, 0, 0.5, 15.5, 15, 15.5);






    public static final VoxelShape[] MOLD_CLAY = {MOLD_1, MOLD_2, MOLD_3, MOLD_4, MOLD_5};

    private static final VoxelShape MOLD_BASE = Shapes.box(0, 0, 0, 1, 0.5 / 16, 1);
    private static final VoxelShape MOLD_BASE_N = Shapes.box(0, 0, 0, 1, 3 / 16.0, 0.5 / 16);
    private static final VoxelShape MOLD_BASE_S = Shapes.box(0, 0, 15.5 / 16, 1, 3 / 16.0, 1);
    private static final VoxelShape MOLD_BASE_W = Shapes.box(0, 0, 0, 0.5 / 16, 3 / 16.0, 1);
    private static final VoxelShape MOLD_BASE_E = Shapes.box(15.5 / 16, 0, 0, 1, 3 / 16.0, 1);
    public static final VoxelShape MOLD_TOTAL_BASE = Shapes.or(MOLD_BASE, MOLD_BASE_N, MOLD_BASE_S, MOLD_BASE_W, MOLD_BASE_E);
    private static final VoxelShape PICKAXE1_THICK = Block.box(3.5, 0.5, 3.5, 12.5, 3, 6.5);
    private static final VoxelShape PICKAXE2_THICK = Block.box(0.5, 0.5, 6.5, 3.5, 3, 9.5);
    private static final VoxelShape PICKAXE3_THICK = Block.box(12.5, 0.5, 6.5, 15.5, 3, 9.5);
    public static final VoxelShape PICKAXE_THICK = Shapes.or(PICKAXE1_THICK, PICKAXE2_THICK, PICKAXE3_THICK);
    private static final VoxelShape AXE1_THICK = Block.box(0.5, 0.5, 3.5, 12.5, 3, 12.5);
    private static final VoxelShape AXE2_THICK = Block.box(12.5, 0.5, 6.5, 15.5, 3, 9.5);
    private static final VoxelShape AXE3_THICK = Block.box(3.5, 0.5, 0.5, 6.5, 3, 15.5);
    public static final VoxelShape AXE_THICK = Shapes.or(AXE1_THICK, AXE2_THICK, AXE3_THICK);

    private EvolutionHitBoxes() {
    }
}
