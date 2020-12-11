package tgw.evolution.init;

import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;

public final class EvolutionBlockStateProperties {

    public static final IntegerProperty LAYERS_0_16 = IntegerProperty.create("layers", 0, 16);
    public static final IntegerProperty LAYERS_1_4 = IntegerProperty.create("layers", 1, 4);
    public static final IntegerProperty LAYERS_1_5 = IntegerProperty.create("layers", 1, 5);
    public static final BooleanProperty TREE = BooleanProperty.create("tree");
    public static final IntegerProperty LOG_COUNT = IntegerProperty.create("log_count", 1, 16);
    public static final IntegerProperty OXIDATION = IntegerProperty.create("oxidation", 0, 8);
    public static final BooleanProperty HIT = BooleanProperty.create("hit");
    public static final IntegerProperty LEVEL_1_8 = IntegerProperty.create("level", 1, 8);
    public static final BooleanProperty FULL = BooleanProperty.create("full");

    private EvolutionBlockStateProperties() {
    }
}
