package tgw.evolution.patches;

import net.minecraft.server.level.ServerLevel;

public interface PatchLocationPredicate {

    default boolean matches_(ServerLevel level, int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
