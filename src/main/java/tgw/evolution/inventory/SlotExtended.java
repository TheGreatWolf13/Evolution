package tgw.evolution.inventory;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.inventory.extendedinventory.IExtendedItemHandler;

public class SlotExtended extends SlotItemHandler {

    private final PlayerEntity player;
    private final int slot;

    public SlotExtended(PlayerEntity player, IExtendedItemHandler handler, int index, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.player = player;
        this.slot = index;
        this.setBackground(AtlasTexture.LOCATION_BLOCKS, EvolutionResources.SLOT_EXTENDED[this.slot]);
    }

    protected boolean isBlocked() {
        switch (this.slot) {
            case EvolutionResources.HAT: {
                return !this.player.inventory.armor.get(EvolutionResources.HELMET).isEmpty();
            }
            case EvolutionResources.BODY: {
                if (!this.player.inventory.armor.get(EvolutionResources.CHESTPLATE).isEmpty()) {
                    return true;
                }
                return !this.getItemHandler().getStackInSlot(EvolutionResources.CLOAK).isEmpty();
            }
            case EvolutionResources.LEGS: {
                return !this.player.inventory.armor.get(EvolutionResources.LEGGINGS).isEmpty();
            }
            case EvolutionResources.FEET: {
                return !this.player.inventory.armor.get(EvolutionResources.BOOTS).isEmpty();
            }
        }
        return false;
    }

    @Override
    public boolean mayPickup(PlayerEntity player) {
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
        return ((IExtendedItemHandler) this.getItemHandler()).isItemValidForSlot(this.slot, stack, this.player);
    }
}
