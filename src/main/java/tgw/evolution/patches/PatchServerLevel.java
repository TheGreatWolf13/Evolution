package tgw.evolution.patches;

import net.minecraft.world.entity.player.Player;

public interface PatchServerLevel {

    default boolean mayInteract_(Player player, int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
