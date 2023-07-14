package tgw.evolution.patches;

import net.minecraft.world.entity.player.Player;

public interface PatchAbstractContainerMenu {

    default void setPlayer(Player player) {
        throw new AbstractMethodError();
    }
}
