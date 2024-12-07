package tgw.evolution.patches;

import net.minecraft.stats.Stat;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface PatchPlayer {

    default boolean blockActionRestricted_(Level level, int x, int y, int z, GameType gameType) {
        throw new AbstractMethodError();
    }

    float getDestroySpeed(BlockState state, int x, int y, int z);

    double getMotionX();

    double getMotionY();

    double getMotionZ();

    long getStat(Stat<?> stat);

    boolean isCrawling();

    boolean isMoving();

    void setCrawling(boolean crawling);

    void setStat(Stat<?> stat, long value);
}
