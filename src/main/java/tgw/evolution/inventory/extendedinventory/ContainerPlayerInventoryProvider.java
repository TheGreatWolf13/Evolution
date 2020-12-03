package tgw.evolution.inventory.extendedinventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ContainerPlayerInventoryProvider implements INamedContainerProvider {

    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new ContainerPlayerInventory(id, inventory);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("");
    }

}
