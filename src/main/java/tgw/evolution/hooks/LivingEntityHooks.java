package tgw.evolution.hooks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
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
        if (entity.getMainHandItem().getItem() == Items.CROSSBOW) {
            return CrossbowItem.isCharged(entity.getMainHandItem());
        }
        if (entity.getOffhandItem().getItem() == Items.CROSSBOW) {
            return CrossbowItem.isCharged(entity.getOffhandItem());
        }
        return ((ILivingEntityPatch) entity).shouldRenderSpecialAttack();
    }
}
