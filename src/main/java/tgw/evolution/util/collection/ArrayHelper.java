package tgw.evolution.util.collection;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.maps.R2OMap;
import tgw.evolution.util.math.DirectionUtil;

public final class ArrayHelper {

    public static final GenerationStep.Carving[] CARVINGS = GenerationStep.Carving.values();
    public static final ChatFormatting[] CHAT_FORMATTINGS = ChatFormatting.values();
    public static final GameType[] GAME_TYPES = GameType.values();
    public static final InteractionHand[] HANDS_MAIN_PRIORITY = {InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND};
    public static final InteractionHand[] HANDS_OFF_PRIORITY = {InteractionHand.OFF_HAND, InteractionHand.MAIN_HAND};
    public static final Heightmap.Types[] HEIGHTMAP = Heightmap.Types.values();
    public static final SupportType[] SUPPORT_TYPE = SupportType.values();
    private static final R2OMap<Class<? extends Enum<?>>, Enum[]> ARRAY_CACHE = new R2OHashMap<>();

    static {
        ARRAY_CACHE.put(Direction.class, DirectionUtil.ALL);
        ARRAY_CACHE.put(SupportType.class, SUPPORT_TYPE);
        ARRAY_CACHE.put(Heightmap.Types.class, HEIGHTMAP);
        ARRAY_CACHE.put(Direction.Axis.class, DirectionUtil.AXIS);
    }

    private ArrayHelper() {
    }

    public static <E extends Enum<E>> E[] getOrCacheArray(Class<E> clazz) {
        Enum[] enums = ARRAY_CACHE.get(clazz);
        if (enums == null) {
            enums = clazz.getEnumConstants();
            ARRAY_CACHE.put(clazz, enums);
        }
        return (E[]) enums;
    }
}
