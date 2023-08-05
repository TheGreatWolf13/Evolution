package tgw.evolution.util.collection;

import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ArrayUtils {

    public static final Heightmap.Types[] HEIGHTMAP = Heightmap.Types.values();
    public static final SupportType[] SUPPORT_TYPE = SupportType.values();

    private ArrayUtils() {
    }
}
