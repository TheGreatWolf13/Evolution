package tgw.evolution.inventory.corpse;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import tgw.evolution.entities.misc.EntityPlayerCorpse;

import javax.annotation.Nullable;

public class ContainerCorpseProvider implements INamedContainerProvider {

    private final EntityPlayerCorpse corpse;

    public ContainerCorpseProvider(EntityPlayerCorpse corpse) {
        this.corpse = corpse;
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new ContainerCorpse(id, this.corpse, inventory);
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.corpse.getName();
    }

}
