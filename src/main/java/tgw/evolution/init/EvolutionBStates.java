package tgw.evolution.init;

import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import tgw.evolution.blocks.tileentities.SchematicMode;

public final class EvolutionBStates {

    public static final IntegerProperty LAYERS_0_16 = IntegerProperty.create("layers", 0, 16);
    public static final IntegerProperty LAYERS_1_4 = IntegerProperty.create("layers", 1, 4);
    public static final IntegerProperty LAYERS_1_5 = IntegerProperty.create("layers", 1, 5);
    public static final BooleanProperty TREE = BooleanProperty.create("tree");
    public static final IntegerProperty LOG_COUNT = IntegerProperty.create("log_count", 1, 16);
    public static final IntegerProperty OXIDATION = IntegerProperty.create("oxidation", 0, 8);
    public static final BooleanProperty HIT = BooleanProperty.create("hit");
    public static final IntegerProperty LEVEL_1_8 = IntegerProperty.create("level", 1, 8);
    public static final BooleanProperty FULL = BooleanProperty.create("full");
    public static final BooleanProperty FLUIDLOGGED = BooleanProperty.create("fluidlogged");
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final DirectionProperty DIRECTION_EXCEPT_UP = BlockStateProperties.FACING_EXCEPT_UP;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty AGE_0_15 = BlockStateProperties.AGE_0_15;
    public static final IntegerProperty DISTANCE_0_7 = BlockStateProperties.DISTANCE_0_7;
    public static final DirectionProperty DIRECTION_HORIZONTAL = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;
    public static final IntegerProperty STAGE_0_1 = BlockStateProperties.STAGE_0_1;
    public static final EnumProperty<SchematicMode> SCHEMATIC_MODE = EnumProperty.create("mode", SchematicMode.class);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    private EvolutionBStates() {
    }

    public static BooleanProperty directionToProperty(Direction direction) {
        switch (direction) {
            case UP:
                return UP;
            case DOWN:
                return DOWN;
            case EAST:
                return EAST;
            case WEST:
                return WEST;
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
        }
        throw new IllegalStateException("Unknown direction: " + direction);
    }
}
