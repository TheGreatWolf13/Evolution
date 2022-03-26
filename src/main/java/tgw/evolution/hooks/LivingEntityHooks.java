package tgw.evolution.hooks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import tgw.evolution.patches.ILivingEntityPatch;

public final class LivingEntityHooks {

    private LivingEntityHooks() {
    }

    public static boolean shouldFixRotation(LivingEntity entity) {
        if (entity.getVehicle() != null) {
            return false;
        }
        if (entity.isUsingItem() && !entity.getUseItem().isEmpty()) {
            ItemStack activeItem = entity.getUseItem();
            Item item = activeItem.getItem();
            UseAnim action = item.getUseAnimation(activeItem);
            if (action == UseAnim.BLOCK || action == UseAnim.SPEAR || action == UseAnim.EAT || action == UseAnim.DRINK || action == UseAnim.BOW) {
                return item.getUseDuration(activeItem) > 0;
            }
            return false;
        }
        return ((ILivingEntityPatch) entity).isMainhandInSpecialAttack() || ((ILivingEntityPatch) entity).isOffhandInSpecialAttack();
    }
}
