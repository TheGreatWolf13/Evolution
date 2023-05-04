package tgw.evolution.inventory;

import net.minecraft.world.entity.player.Player;

public interface IContainerCheckable {

    void onClose(Player player);

    void onOpen(Player player);
}
