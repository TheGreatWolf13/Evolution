package tgw.evolution.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotEquip extends Slot {

    protected final LivingEntity entity;

    public SlotEquip(Container container, int slotId, int x, int y, LivingEntity entity) {
        super(container, slotId, x, y);
        this.entity = entity;
    }

    @Override
    public void set(ItemStack stack) {
        if (!ItemStack.isSame(stack, this.getItem())) {
            this.entity.equipEventAndSound(stack);
        }
        super.set(stack);
    }
}
