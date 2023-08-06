package tgw.evolution.util;

import net.minecraft.world.level.LevelReader;

public interface BlockInWorldPredicate {

    boolean test(LevelReader level, int x, int y, int z, boolean loadChunks);
}
