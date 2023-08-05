package tgw.evolution.patches;

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

    boolean isCrawling();

    boolean isMoving();

    void setCrawling(boolean crawling);
}
