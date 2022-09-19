package tgw.evolution.inventory.extendedinventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import tgw.evolution.init.EvolutionTexts;

public class ContainerInventoryProvider implements MenuProvider {

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerInventory(id, inventory);
    }

    @Override
    public Component getDisplayName() {
        return EvolutionTexts.EMPTY;
    }
}
