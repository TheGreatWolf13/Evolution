package tgw.evolution.patches;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface PatchMultiPlayerGameMode {

    default boolean continueDestroyBlock_(int x, int y, int z, Direction face, BlockHitResult hitResult) {
        throw new AbstractMethodError();
    }

    default boolean destroyBlock_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void handleBlockBreakAck_(ClientLevel level,
                                      long pos,
                                      BlockState state,
                                      ServerboundPlayerActionPacket.Action action,
                                      boolean allGood) {
        throw new AbstractMethodError();
    }

    default boolean startDestroyBlock_(BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    default boolean wasLastInteractionUsedOnBlock() {
        throw new AbstractMethodError();
    }
}
