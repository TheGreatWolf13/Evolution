package tgw.evolution.hooks;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import tgw.evolution.client.util.LungeAttackInfo;
import tgw.evolution.client.util.LungeChargeInfo;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.MathHelper;

public final class PlayerRenderHooks {

    private PlayerRenderHooks() {
    }

    public static <T extends LivingEntity> void eatingAnimationLeftHand(BipedModel<T> model, Hand hand, LivingEntity entity, float ageInTicks) {
        if (entity.getUsedItemHand() == hand) {
            ItemStack stack = entity.getItemInHand(hand);
            boolean eatingOrDrinking = stack.getUseAnimation() == UseAction.EAT || stack.getUseAnimation() == UseAction.DRINK;
            if (entity.getTicksUsingItem() > 0 && eatingOrDrinking) {
                model.leftArm.yRot = 0.5F;
                model.leftArm.xRot = -1.3F;
                model.leftArm.zRot = MathHelper.cos(ageInTicks) * 0.1F;
                model.head.xRot = MathHelper.cos(ageInTicks) * 0.2F;
                model.head.yRot = 0.0F;
                model.hat.copyFrom(model.head);
            }
        }
    }

    public static <T extends LivingEntity> void eatingAnimationRightHand(BipedModel<T> model, Hand hand, LivingEntity entity, float ageInTicks) {
        if (entity.getUsedItemHand() == hand) {
            ItemStack stack = entity.getItemInHand(hand);
            boolean eatingOrDrinking = stack.getUseAnimation() == UseAction.EAT || stack.getUseAnimation() == UseAction.DRINK;
            if (entity.getTicksUsingItem() > 0 && eatingOrDrinking) {
                model.rightArm.yRot = -0.5F;
                model.rightArm.xRot = -1.3F;
                model.rightArm.zRot = MathHelper.cos(ageInTicks) * 0.1F;
                model.head.xRot = MathHelper.cos(ageInTicks) * 0.2F;
                model.head.yRot = 0.0F;
                model.hat.copyFrom(model.head);
            }
        }
    }

    /**
     * Hooks from {@link net.minecraft.client.renderer.entity.PlayerRenderer#func_217766_a(AbstractClientPlayerEntity, ItemStack, ItemStack, Hand)}
     */
    @EvolutionHook
    public static BipedModel.ArmPose func_217766_a(AbstractClientPlayerEntity player, ItemStack mainhandStack, ItemStack offhandStack, Hand hand) {
        BipedModel.ArmPose armPose = BipedModel.ArmPose.EMPTY;
        ItemStack stack = hand == Hand.MAIN_HAND ? mainhandStack : offhandStack;
        if (!stack.isEmpty()) {
            armPose = BipedModel.ArmPose.ITEM;
            if (player.getTicksUsingItem() > 0 && hand == player.getUsedItemHand()) {
                UseAction useaction = stack.getUseAnimation();
                switch (useaction) {
                    case BLOCK: {
                        return BipedModel.ArmPose.BLOCK;
                    }
                    case BOW: {
                        return BipedModel.ArmPose.BOW_AND_ARROW;
                    }
                    case SPEAR: {
                        return BipedModel.ArmPose.THROW_SPEAR;
                    }
                    case CROSSBOW: {
                        return BipedModel.ArmPose.CROSSBOW_CHARGE;
                    }
                }
            }
            else {
                boolean mainhandHasCrossbow = mainhandStack.getItem() == Items.CROSSBOW;
                boolean mainhandIsCharged = CrossbowItem.isCharged(mainhandStack);
                boolean offhandHasCrossbow = offhandStack.getItem() == Items.CROSSBOW;
                boolean offhandIsCharged = CrossbowItem.isCharged(offhandStack);
                if (mainhandHasCrossbow && mainhandIsCharged) {
                    armPose = BipedModel.ArmPose.CROSSBOW_HOLD;
                }
                if (offhandHasCrossbow && offhandIsCharged && mainhandStack.getItem().getUseAnimation(mainhandStack) == UseAction.NONE) {
                    return BipedModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }
        return armPose;
    }

    public static <T extends LivingEntity> void lungeAnimationLeftHand(BipedModel<T> model, T entity, Hand hand, LungeAttackInfo lunge) {
        if (lunge.isLungeInProgress(hand)) {
            model.leftArm.xRot = -0.75f * lunge.getLungeMult(hand);
        }
    }

    public static <T extends LivingEntity> void lungeAnimationRightHand(BipedModel<T> model, T entity, Hand hand, LungeAttackInfo lunge) {
        if (lunge.isLungeInProgress(hand)) {
            model.rightArm.xRot = -0.75f * lunge.getLungeMult(hand);
        }
    }

    /**
     * Hooks from {@link BipedModel#setRotationAngles(LivingEntity, float, float, float, float, float, float)} func_212844_a_
     */
    @EvolutionHook
    public static <T extends LivingEntity> void setRotationAngles(BipedModel<T> model, T entity, float ageInTicks) {
        LungeChargeInfo lungeCharge = ClientEvents.ABOUT_TO_LUNGE_PLAYERS.get(entity.getId());
        if (lungeCharge != null) {
            if (entity.getMainArm() == HandSide.RIGHT) {
                startLungeAnimationRightHand(model, entity, Hand.MAIN_HAND, lungeCharge);
                startLungeAnimationLeftHand(model, entity, Hand.OFF_HAND, lungeCharge);
            }
            else {
                startLungeAnimationRightHand(model, entity, Hand.OFF_HAND, lungeCharge);
                startLungeAnimationLeftHand(model, entity, Hand.MAIN_HAND, lungeCharge);
            }
        }
        LungeAttackInfo lungeAttack = ClientEvents.LUNGING_PLAYERS.get(entity.getId());
        if (lungeAttack != null) {
            if (entity.getMainArm() == HandSide.RIGHT) {
                lungeAnimationRightHand(model, entity, Hand.MAIN_HAND, lungeAttack);
                lungeAnimationLeftHand(model, entity, Hand.OFF_HAND, lungeAttack);
            }
            else {
                lungeAnimationRightHand(model, entity, Hand.OFF_HAND, lungeAttack);
                lungeAnimationLeftHand(model, entity, Hand.MAIN_HAND, lungeAttack);
            }
        }
        if (entity.getMainArm() == HandSide.RIGHT) {
            eatingAnimationRightHand(model, Hand.MAIN_HAND, entity, ageInTicks);
            eatingAnimationLeftHand(model, Hand.OFF_HAND, entity, ageInTicks);
        }
        else {
            eatingAnimationRightHand(model, Hand.OFF_HAND, entity, ageInTicks);
            eatingAnimationLeftHand(model, Hand.MAIN_HAND, entity, ageInTicks);
        }
//        int tick = ClientEvents.getInstance().getTickCount();
//        int tickForAnim = tick % 30;
//        model.bipedRightArm.rotateAngleX = MathHelper.degToRad(-120.0f * MathHelper.sinDeg(tickForAnim / 30.0f * 90));
//        model.bipedRightArm.rotateAngleZ = MathHelper.degToRad(90.0f * MathHelper.sinDeg(tickForAnim / 30.0f * 90));
    }

    public static <T extends LivingEntity> void startLungeAnimationLeftHand(BipedModel<T> model, T entity, Hand hand, LungeChargeInfo lunge) {
        lunge.checkItem(hand, entity.getItemInHand(hand));
        if (lunge.isLungeInProgress(hand)) {
            model.leftArm.xRot = 0.75f * lunge.getLungeMult(hand);
        }
    }

    public static <T extends LivingEntity> void startLungeAnimationRightHand(BipedModel<T> model, T entity, Hand hand, LungeChargeInfo lunge) {
        lunge.checkItem(hand, entity.getItemInHand(hand));
        if (lunge.isLungeInProgress(hand)) {
            model.rightArm.xRot = 0.75f * lunge.getLungeMult(hand);
        }
    }
}
