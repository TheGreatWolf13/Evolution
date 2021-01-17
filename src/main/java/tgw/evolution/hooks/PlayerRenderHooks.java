package tgw.evolution.hooks;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import tgw.evolution.client.LungeAttackInfo;
import tgw.evolution.client.LungeChargeInfo;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.MathHelper;

public final class PlayerRenderHooks {

    private PlayerRenderHooks() {
    }

    public static <T extends LivingEntity> void eatingAnimationLeftHand(PlayerModel<T> model, Hand hand, LivingEntity entity, float ageInTicks) {
        if (entity.getActiveHand() == hand) {
            ItemStack stack = entity.getHeldItem(hand);
            boolean eatingOrDrinking = stack.getUseAction() == UseAction.EAT || stack.getUseAction() == UseAction.DRINK;
            if (entity.getItemInUseCount() > 0 && eatingOrDrinking) {
                model.bipedLeftArm.rotateAngleY = 0.5F;
                model.bipedLeftArm.rotateAngleX = -1.3F;
                model.bipedLeftArm.rotateAngleZ = MathHelper.cos(ageInTicks) * 0.1F;
                model.bipedLeftArmwear.copyModelAngles(model.bipedLeftArm);
                model.bipedHead.rotateAngleX = MathHelper.cos(ageInTicks) * 0.2F;
                model.bipedHead.rotateAngleY = 0.0F;
                model.bipedHeadwear.copyModelAngles(model.bipedHead);
            }
        }
    }

    public static <T extends LivingEntity> void eatingAnimationRightHand(PlayerModel<T> model, Hand hand, LivingEntity entity, float ageInTicks) {
        if (entity.getActiveHand() == hand) {
            ItemStack stack = entity.getHeldItem(hand);
            boolean eatingOrDrinking = stack.getUseAction() == UseAction.EAT || stack.getUseAction() == UseAction.DRINK;
            if (entity.getItemInUseCount() > 0 && eatingOrDrinking) {
                model.bipedRightArm.rotateAngleY = -0.5F;
                model.bipedRightArm.rotateAngleX = -1.3F;
                model.bipedRightArm.rotateAngleZ = MathHelper.cos(ageInTicks) * 0.1F;
                model.bipedRightArmwear.copyModelAngles(model.bipedRightArm);
                model.bipedHead.rotateAngleX = MathHelper.cos(ageInTicks) * 0.2F;
                model.bipedHead.rotateAngleY = 0.0F;
                model.bipedHeadwear.copyModelAngles(model.bipedHead);
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
            if (player.getItemInUseCount() > 0 && hand == player.getActiveHand()) {
                UseAction useaction = stack.getUseAction();
                if (useaction == UseAction.BLOCK) {
                    return BipedModel.ArmPose.BLOCK;
                }
                if (useaction == UseAction.BOW) {
                    return BipedModel.ArmPose.BOW_AND_ARROW;
                }
                if (useaction == UseAction.SPEAR) {
                    return BipedModel.ArmPose.THROW_SPEAR;
                }
                if (useaction == UseAction.CROSSBOW) {
                    return BipedModel.ArmPose.CROSSBOW_CHARGE;
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
                if (offhandHasCrossbow && offhandIsCharged && mainhandStack.getItem().getUseAction(mainhandStack) == UseAction.NONE) {
                    return BipedModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }
        return armPose;
    }

    public static <T extends LivingEntity> void lungeAnimationLeftHand(PlayerModel<T> model, T entity, Hand hand, LungeAttackInfo lunge) {
        if (lunge.isLungeInProgress(hand)) {
            model.bipedLeftArm.rotateAngleX = -0.75f * lunge.getLungeMult(hand);
            model.bipedLeftArmwear.copyModelAngles(model.bipedLeftArm);
        }
    }

    public static <T extends LivingEntity> void lungeAnimationRightHand(PlayerModel<T> model, T entity, Hand hand, LungeAttackInfo lunge) {
        if (lunge.isLungeInProgress(hand)) {
            model.bipedRightArm.rotateAngleX = -0.75f * lunge.getLungeMult(hand);
            model.bipedRightArmwear.copyModelAngles(model.bipedRightArm);
        }
    }

    /**
     * Hooks from {@link PlayerModel#setRotationAngles(LivingEntity, float, float, float, float, float, float)} func_212844_a_
     */
    @EvolutionHook
    public static <T extends LivingEntity> void setRotationAngles(PlayerModel<T> model, T entity, float ageInTicks) {
        LungeChargeInfo lungeCharge = ClientEvents.ABOUT_TO_LUNGE_PLAYERS.get(entity.getEntityId());
        if (lungeCharge != null) {
            if (entity.getPrimaryHand() == HandSide.RIGHT) {
                startLungeAnimationRightHand(model, entity, Hand.MAIN_HAND, lungeCharge);
                startLungeAnimationLeftHand(model, entity, Hand.OFF_HAND, lungeCharge);
            }
            else {
                startLungeAnimationRightHand(model, entity, Hand.OFF_HAND, lungeCharge);
                startLungeAnimationLeftHand(model, entity, Hand.MAIN_HAND, lungeCharge);
            }
        }
        LungeAttackInfo lungeAttack = ClientEvents.LUNGING_PLAYERS.get(entity.getEntityId());
        if (lungeAttack != null) {
            if (entity.getPrimaryHand() == HandSide.RIGHT) {
                lungeAnimationRightHand(model, entity, Hand.MAIN_HAND, lungeAttack);
                lungeAnimationLeftHand(model, entity, Hand.OFF_HAND, lungeAttack);
            }
            else {
                lungeAnimationRightHand(model, entity, Hand.OFF_HAND, lungeAttack);
                lungeAnimationLeftHand(model, entity, Hand.MAIN_HAND, lungeAttack);
            }
        }
        if (entity.getPrimaryHand() == HandSide.RIGHT) {
            eatingAnimationRightHand(model, Hand.MAIN_HAND, entity, ageInTicks);
            eatingAnimationLeftHand(model, Hand.OFF_HAND, entity, ageInTicks);
        }
        else {
            eatingAnimationRightHand(model, Hand.OFF_HAND, entity, ageInTicks);
            eatingAnimationLeftHand(model, Hand.MAIN_HAND, entity, ageInTicks);
        }
    }

    public static <T extends LivingEntity> void startLungeAnimationLeftHand(PlayerModel<T> model, T entity, Hand hand, LungeChargeInfo lunge) {
        lunge.checkItem(hand, entity.getHeldItem(hand));
        if (lunge.isLungeInProgress(hand)) {
            model.bipedLeftArm.rotateAngleX = 0.75f * lunge.getLungeMult(hand);
            model.bipedLeftArmwear.copyModelAngles(model.bipedLeftArm);
        }
    }

    public static <T extends LivingEntity> void startLungeAnimationRightHand(PlayerModel<T> model, T entity, Hand hand, LungeChargeInfo lunge) {
        lunge.checkItem(hand, entity.getHeldItem(hand));
        if (lunge.isLungeInProgress(hand)) {
            model.bipedRightArm.rotateAngleX = 0.75f * lunge.getLungeMult(hand);
            model.bipedRightArmwear.copyModelAngles(model.bipedRightArm);
        }
    }
}
