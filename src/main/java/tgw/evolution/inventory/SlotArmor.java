package tgw.evolution.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionResources;

public class SlotArmor extends SlotEquip {

    private final EquipmentSlot equip;

    public SlotArmor(Container container, int slotId, int x, int y, EquipmentSlot equip, LivingEntity entity) {
        super(container, slotId, x, y, entity);
        this.equip = equip;
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
