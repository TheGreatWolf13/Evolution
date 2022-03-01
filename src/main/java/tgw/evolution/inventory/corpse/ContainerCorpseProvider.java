package tgw.evolution.inventory.corpse;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import tgw.evolution.entities.misc.EntityPlayerCorpse;

import javax.annotation.Nullable;

public class ContainerCorpseProvider implements MenuProvider {

    private final EntityPlayerCorpse corpse;

    public ContainerCorpseProvider(EntityPlayerCorpse corpse) {
        this.corpse = corpse;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerCorpse(id, this.corpse, inventory);
    }

    @Override
    public Component getDisplayName() {
        return this.corpse.getName();
    }

}
