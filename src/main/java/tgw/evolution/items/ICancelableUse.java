package tgw.evolution.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ICancelableUse {

    Component getCancelMessage(Component key);

    default boolean isCancelable(ItemStack stack, LivingEntity entity) {
        return true;
    }
}
