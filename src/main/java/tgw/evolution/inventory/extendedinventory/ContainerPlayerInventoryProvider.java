package tgw.evolution.inventory.extendedinventory;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ContainerPlayerInventoryProvider implements MenuProvider {

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerPlayerInventory(id, inventory);
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("");
    }
}
