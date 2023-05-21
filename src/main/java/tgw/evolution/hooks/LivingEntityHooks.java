package tgw.evolution.hooks;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
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
        if (entity.getFluidHeight(FluidTags.WATER) >= entity.getBbHeight() * 0.5) {
            return true;
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

    public static float xDelta(Entity entity, float partialTicks) {
        if (entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit()) {
            return 0;
        }
        float swimAmount = 0;
        if (entity instanceof LivingEntity living) {
            swimAmount = living.getSwimAmount(partialTicks);
        }
        boolean inWater = entity.isInWater();
        if (!inWater && swimAmount > 0) {
            if (swimAmount == 1) {
                return -90;
            }
            if (0.5 <= swimAmount) {
                return (swimAmount - 0.5f) * -180;
            }
            return 0;
        }
        if (entity.isCrouching()) {
            return -30;
        }
        if (inWater && swimAmount == 1) {
            return 0;
        }
        //Standing
        return 0;
    }
}
