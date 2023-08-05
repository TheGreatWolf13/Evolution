package tgw.evolution.patches;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public interface PatchMinecraftServer {

    default boolean isMultiplayerPaused() {
        throw new AbstractMethodError();
    }

    default boolean isUnderSpawnProtection_(ServerLevel level, int x, int y, int z, Player player) {
        throw new AbstractMethodError();
    }

    default void setMultiplayerPaused(boolean paused) {
        throw new AbstractMethodError();
    }
}
