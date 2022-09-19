package tgw.evolution.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionResources;

public class SlotArmor extends Slot {

    private final Entity entity;
    private final EquipmentSlot equip;

    public SlotArmor(Container container, int slotId, int x, int y, EquipmentSlot equip, Entity entity) {
        super(container, slotId, x, y);
        this.equip = equip;
        this.entity = entity;
        this.setBackground(InventoryMenu.BLOCK_ATLAS, EvolutionResources.SLOT_ARMOR[equip.getIndex()]);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.canEquip(this.equip, this.entity);
    }
}
