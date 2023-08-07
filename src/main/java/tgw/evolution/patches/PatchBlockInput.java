package tgw.evolution.patches;

import net.minecraft.server.level.ServerLevel;
import tgw.evolution.util.constants.BlockFlags;

public interface PatchBlockInput {

    default boolean place_(ServerLevel level, int x, int y, int z, @BlockFlags int flags) {
        throw new AbstractMethodError();
    }
}
