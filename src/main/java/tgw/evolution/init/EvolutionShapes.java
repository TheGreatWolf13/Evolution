package tgw.evolution.init;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.MathHelper;

public final class EvolutionShapes {

    //Simple shapes
    public static final VoxelShape SLAB_2_D;
    public static final VoxelShape[] SLAB_4_D;
    public static final VoxelShape[] SLAB_8_D;
    public static final VoxelShape[] SLAB_16_D;
    public static final VoxelShape SLAB_16_U;
    public static final VoxelShape SLAB_16_N;
    public static final VoxelShape SLAB_16_S;
    public static final VoxelShape SLAB_16_E;
    public static final VoxelShape SLAB_16_W;
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
    //Mold
    public static final VoxelShape MOLD_1 = Block.box(0, 0, 0, 16, 3, 16);
    public static final VoxelShape MOLD_2 = Block.box(0, 0, 0, 16, 6, 16);
    public static final VoxelShape MOLD_3 = Block.box(0, 0, 0, 16, 9, 16);
    public static final VoxelShape MOLD_4 = Block.box(0, 0, 0, 16, 12, 16);
    public static final VoxelShape MOLD_5 = Block.box(0.5, 0, 0.5, 15.5, 15, 15.5);
    public static final VoxelShape MOLD_TOTAL_BASE;
    //Groups
    public static final VoxelShape[] LOG_PILE;
    public static final VoxelShape[] MOLD_CLAY;
    private static final VoxelShape PICKAXE1_THICK = Block.box(3.5, 0.5, 3.5, 12.5, 3, 6.5);
    private static final VoxelShape PICKAXE2_THICK = Block.box(0.5, 0.5, 6.5, 3.5, 3, 9.5);
    private static final VoxelShape PICKAXE3_THICK = Block.box(12.5, 0.5, 6.5, 15.5, 3, 9.5);
    public static final VoxelShape PICKAXE_THICK = Shapes.or(PICKAXE1_THICK, PICKAXE2_THICK, PICKAXE3_THICK);
    private static final VoxelShape AXE1_THICK = Block.box(0.5, 0.5, 3.5, 12.5, 3, 12.5);
    private static final VoxelShape AXE2_THICK = Block.box(12.5, 0.5, 6.5, 15.5, 3, 9.5);
    private static final VoxelShape AXE3_THICK = Block.box(3.5, 0.5, 0.5, 6.5, 3, 15.5);
    public static final VoxelShape AXE_THICK = Shapes.or(AXE1_THICK, AXE2_THICK, AXE3_THICK);

    static {
        SLAB_2_D = Block.box(0, 0, 0, 16, 8, 16);
        SLAB_4_D = new VoxelShape[]{Block.box(0, 0, 0, 16, 4, 16),
                                    SLAB_2_D,
                                    Block.box(0, 0, 0, 16, 12, 16),
                                    Shapes.block()};
        SLAB_8_D = new VoxelShape[]{Block.box(0, 0, 0, 16, 2, 16),
                                    SLAB_4_D[0],
                                    Block.box(0, 0, 0, 16, 6, 16),
                                    SLAB_4_D[1],
                                    };
        SLAB_16_D = new VoxelShape[]{Block.box(0, 0, 0, 16, 1, 16),
                                     SLAB_8_D[0],
                                     Block.box(0, 0, 0, 16, 3, 16),
                                     SLAB_8_D[1],
                                     Block.box(0, 0, 0, 16, 5, 16),
                                     SLAB_8_D[2],
                                     Block.box(0, 0, 0, 16, 7, 16),
                                     SLAB_8_D[3]};
        SLAB_16_U = Block.box(0, 15, 0, 16, 16, 16);
        SLAB_16_N = Block.box(0, 0, 0, 16, 16, 1);
        SLAB_16_S = Block.box(0, 0, 15, 16, 16, 16);
        SLAB_16_E = Block.box(15, 0, 0, 16, 16, 16);
        SLAB_16_W = Block.box(0, 0, 0, 1, 16, 16);
        LOG_PILE = new VoxelShape[]{Block.box(0, 0, 0, 4, 4, 16), Block.box(0, 0, 0, 8, 4, 16), Block.box(0, 0, 0, 12, 4, 16), SLAB_4_D[0],
                                    Block.box(0, 0, 0, 4, 8, 16), Block.box(0, 0, 0, 8, 8, 16), Block.box(0, 0, 0, 12, 8, 16), SLAB_4_D[1],
                                    Block.box(0, 0, 0, 4, 12, 16), Block.box(0, 0, 0, 8, 12, 16), Block.box(0, 0, 0, 12, 12, 16), SLAB_4_D[2],
                                    Block.box(0, 0, 0, 4, 16, 16), Block.box(0, 0, 0, 8, 16, 16), Block.box(0, 0, 0, 12, 16, 16)};
        MOLD_CLAY = new VoxelShape[]{MOLD_1, MOLD_2, MOLD_3, MOLD_4, MOLD_5};
        VoxelShape moldBase = Shapes.box(0, 0, 0, 1, 0.5 / 16, 1);
        VoxelShape moldBaseN = Shapes.box(0, 0, 0, 1, 3 / 16.0, 0.5 / 16);
        VoxelShape moldBaseS = Shapes.box(0, 0, 15.5 / 16, 1, 3 / 16.0, 1);
        VoxelShape moldBaseW = Shapes.box(0, 0, 0, 0.5 / 16, 3 / 16.0, 1);
        VoxelShape moldBaseE = Shapes.box(15.5 / 16, 0, 0, 1, 3 / 16.0, 1);
        MOLD_TOTAL_BASE = Shapes.or(moldBase, moldBaseN, moldBaseS, moldBaseW, moldBaseE);
    }

    private EvolutionShapes() {
    }

    public static double collide(Direction.Axis axis, AABB bb, OList<VoxelShape> list, double value) {
        for (int i = 0, len = list.size(); i < len; ++i) {
            if (Math.abs(value) < 1.0E-7) {
                return 0;
            }
            value = list.get(i).collide(axis, bb, value);
        }
        return value;
    }
}
