package tgw.evolution.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.extendedinventory.IExtendedItemHandler;

import javax.annotation.Nullable;

public class SlotExtended extends SlotItemHandler {

    private final PlayerEntity player;
    private final int slot;

    public SlotExtended(PlayerEntity player, IExtendedItemHandler handler, int index, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.player = player;
        this.slot = index;
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        if (this.isBlocked()) {
            return false;
        }
        return super.canTakeStack(player);
    }

    @Nullable
    @Override
    public String getSlotTexture() {
        return EvolutionResources.SLOT_EXTENDED[this.slot];
    }

    private boolean isBlocked() {
        switch (this.slot) {
            case EvolutionResources.HAT:
                return !this.player.inventory.armorInventory.get(EvolutionResources.HELMET).isEmpty();
            case EvolutionResources.BODY:
                if (!this.player.inventory.armorInventory.get(EvolutionResources.CHESTPLATE).isEmpty()) {
                    return true;
                }
                return !this.getItemHandler().getStackInSlot(EvolutionResources.CLOAK).isEmpty();
            case EvolutionResources.LEGS:
                return !this.player.inventory.armorInventory.get(EvolutionResources.LEGGINGS).isEmpty();
            case EvolutionResources.FEET:
                return !this.player.inventory.armorInventory.get(EvolutionResources.BOOTS).isEmpty();
        }
        return false;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (this.isBlocked()) {
            return false;
        }
        return ((IExtendedItemHandler) this.getItemHandler()).isItemValidForSlot(this.slot, stack, this.player);
    }
}
