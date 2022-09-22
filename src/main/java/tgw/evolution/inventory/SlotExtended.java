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

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return ((IInventory) this.getItemHandler()).isItemValidForSlot(this.slot, stack, this.player);
    }

    @Override
    public void set(ItemStack stack) {
        if (!ItemStack.isSame(stack, this.getItem())) {
            this.player.equipEventAndSound(stack);
        }
        super.set(stack);
    }
}
