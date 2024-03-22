package tgw.evolution.patches;

import net.minecraft.world.level.WorldGenLevel;

public interface PatchBlockPredicate {

    default boolean test_(WorldGenLevel level, int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
