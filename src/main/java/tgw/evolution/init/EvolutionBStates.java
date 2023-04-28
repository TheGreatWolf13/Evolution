package tgw.evolution.init;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.*;
import tgw.evolution.blocks.tileentities.SchematicMode;
import tgw.evolution.blocks.util.IntProperty;

public final class EvolutionBStates {

    //Boolean
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty FALL = BooleanProperty.create("fall");
    public static final BooleanProperty FULL = BooleanProperty.create("full");
    public static final BooleanProperty HIT = BooleanProperty.create("hit");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
    public static final BooleanProperty TREE = BooleanProperty.create("tree");
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    //Int
    public static final IntProperty AGE_0_15 = IntProperty.create("age", 0, 15);
    public static final IntProperty ATM = IntProperty.create("atm", 0, 31);
    public static final IntProperty DISTANCE_0_7 = IntProperty.create("distance", 0, 7);
    public static final IntProperty FIREWOOD_COUNT = IntProperty.create("log_count", 1, 16);
    public static final IntProperty LAYERS_0_16 = IntProperty.create("layers", 0, 16);
    public static final IntProperty LAYERS_1_4 = IntProperty.create("layers", 1, 4);
    public static final IntProperty LAYERS_1_5 = IntProperty.create("layers", 1, 5);
    public static final IntProperty LEVEL_1_8 = IntProperty.create("level", 1, 8);
    public static final IntProperty STAGE_0_1 = IntProperty.create("stage", 0, 1);
    //Direction
    public static final DirectionProperty DIRECTION_EXCEPT_UP = BlockStateProperties.FACING_HOPPER;
    public static final DirectionProperty DIRECTION_HORIZONTAL = BlockStateProperties.HORIZONTAL_FACING;
    //Enum
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<SchematicMode> SCHEMATIC_MODE = EnumProperty.create("mode", SchematicMode.class);

    private EvolutionBStates() {
    }

    public static BooleanProperty directionToProperty(Direction direction) {
        return switch (direction) {
            case UP -> UP;
            case DOWN -> DOWN;
            case EAST -> EAST;
            case WEST -> WEST;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
        };
    }
}
