package tgw.evolution.util;

import tgw.evolution.capabilities.chunkstorage.EnumStorage;

import java.util.EnumMap;
import java.util.Map;

import static tgw.evolution.capabilities.chunkstorage.EnumStorage.*;

public class NutrientHelper {

    public static final Map<EnumStorage, Integer> GROW_GRASS_BLOCK = new EnumMap<>(EnumStorage.class);
    public static final Map<EnumStorage, Integer> DECAY_GRASS_BLOCK = new EnumMap<>(EnumStorage.class);
    public static final Map<EnumStorage, Integer> GROW_TALL_GRASS = new EnumMap<>(EnumStorage.class);
    public static final Map<EnumStorage, Integer> GROW_TALL_GRASS_2 = new EnumMap<>(EnumStorage.class);
    public static final Map<EnumStorage, Integer> DECAY_TALL_GRASS = new EnumMap<>(EnumStorage.class);

    static {
        GROW_GRASS_BLOCK.put(NITROGEN, 2);
        GROW_GRASS_BLOCK.put(PHOSPHORUS, 1);
        GROW_GRASS_BLOCK.put(POTASSIUM, 1);
        GROW_GRASS_BLOCK.put(CARBON_DIOXIDE, 2);
        GROW_GRASS_BLOCK.put(WATER, 2);
        GROW_GRASS_BLOCK.put(OXYGEN, -2);

        DECAY_GRASS_BLOCK.put(NITROGEN, 2);
        DECAY_GRASS_BLOCK.put(PHOSPHORUS, 1);
        DECAY_GRASS_BLOCK.put(POTASSIUM, 1);
        DECAY_GRASS_BLOCK.put(ORGANIC, 2);

        GROW_TALL_GRASS.put(NITROGEN, 1);
        GROW_TALL_GRASS.put(CARBON_DIOXIDE, 1);
        GROW_TALL_GRASS.put(WATER, 1);
        GROW_TALL_GRASS.put(POTASSIUM, 1);
        GROW_TALL_GRASS.put(PHOSPHORUS, 1);
        GROW_TALL_GRASS.put(OXYGEN, -1);

        GROW_TALL_GRASS_2.put(NITROGEN, 2);
        GROW_TALL_GRASS_2.put(CARBON_DIOXIDE, 2);
        GROW_TALL_GRASS_2.put(WATER, 2);
        GROW_TALL_GRASS_2.put(POTASSIUM, 2);
        GROW_TALL_GRASS_2.put(PHOSPHORUS, 2);
        GROW_TALL_GRASS_2.put(OXYGEN, -2);

        DECAY_TALL_GRASS.put(NITROGEN, 1);
        DECAY_TALL_GRASS.put(PHOSPHORUS, 1);
        DECAY_TALL_GRASS.put(POTASSIUM, 1);
        DECAY_TALL_GRASS.put(ORGANIC, 1);
    }
}
