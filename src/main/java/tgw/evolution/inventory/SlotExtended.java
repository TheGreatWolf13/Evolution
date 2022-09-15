package tgw.evolution.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import tgw.evolution.capabilities.inventory.IInventory;
import tgw.evolution.init.EvolutionResources;

public class SlotExtended extends SlotItemHandler {

    private final Player player;
    private final int slot;

    public SlotExtended(Player player, IInventory handler, int index, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.player = player;
        this.slot = index;
        this.setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_EXTENDED[this.slot]);
    }

    protected boolean isBlocked() {
        switch (this.slot) {
            case EvolutionResources.HAT -> {
                return !this.player.getInventory().armor.get(EvolutionResources.HELMET).isEmpty();
            }
            case EvolutionResources.BODY -> {
                if (!this.player.getInventory().armor.get(EvolutionResources.CHESTPLATE).isEmpty()) {
                    return true;
                }
                return !this.getItemHandler().getStackInSlot(EvolutionResources.CLOAK).isEmpty();
            }
            case EvolutionResources.LEGS -> {
                return !this.player.getInventory().armor.get(EvolutionResources.LEGGINGS).isEmpty();
            }
            case EvolutionResources.FEET -> {
                return !this.player.getInventory().armor.get(EvolutionResources.BOOTS).isEmpty();
            }
        }
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        if (this.isBlocked()) {
            return false;
        }
        return super.mayPickup(player);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (this.isBlocked()) {
            return false;
        }
        return ((IInventory) this.getItemHandler()).isItemValidForSlot(this.slot, stack, this.player);
    }
}
