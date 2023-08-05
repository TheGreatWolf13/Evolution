package tgw.evolution.patches;

import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;

public interface PatchServerPlayerGameMode {

    default void destroyAndAck_(long pos, ServerboundPlayerActionPacket.Action action) {
        throw new AbstractMethodError();
    }

    default boolean destroyBlock_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void handleBlockBreakAction_(long pos,
                                         ServerboundPlayerActionPacket.Action action,
                                         Direction direction,
                                         double hitX,
                                         double hitY,
                                         double hitZ,
                                         int buildHeight) {
        throw new AbstractMethodError();
    }
}
