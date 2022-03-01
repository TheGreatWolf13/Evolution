package tgw.evolution.hooks;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import tgw.evolution.client.util.LungeAttackInfo;
import tgw.evolution.client.util.LungeChargeInfo;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.math.MathHelper;

public final class PlayerRenderHooks {

    private PlayerRenderHooks() {
    }

    public static <T extends LivingEntity> void eatingAnimationLeftHand(HumanoidModel<T> model,
                                                                        InteractionHand hand,
                                                                        LivingEntity entity,
                                                                        float ageInTicks) {
        if (entity.getUsedItemHand() == hand) {
            ItemStack stack = entity.getItemInHand(hand);
            boolean eatingOrDrinking = stack.getUseAnimation() == UseAnim.EAT || stack.getUseAnimation() == UseAnim.DRINK;
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

    public static <T extends LivingEntity> void eatingAnimationRightHand(HumanoidModel<T> model,
                                                                         InteractionHand hand,
                                                                         LivingEntity entity,
                                                                         float ageInTicks) {
        if (entity.getUsedItemHand() == hand) {
            ItemStack stack = entity.getItemInHand(hand);
            boolean eatingOrDrinking = stack.getUseAnimation() == UseAnim.EAT || stack.getUseAnimation() == UseAnim.DRINK;
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
    public static HumanoidModel.ArmPose func_217766_a(AbstractClientPlayer player,
                                                      ItemStack mainhandStack,
                                                      ItemStack offhandStack,
                                                      InteractionHand hand) {
        HumanoidModel.ArmPose armPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack stack = hand == InteractionHand.MAIN_HAND ? mainhandStack : offhandStack;
        if (!stack.isEmpty()) {
            armPose = HumanoidModel.ArmPose.ITEM;
            if (player.getTicksUsingItem() > 0 && hand == player.getUsedItemHand()) {
                UseAnim useaction = stack.getUseAnimation();
                switch (useaction) {
                    case BLOCK -> {
                        return HumanoidModel.ArmPose.BLOCK;
                    }
                    case BOW -> {
                        return HumanoidModel.ArmPose.BOW_AND_ARROW;
                    }
                    case SPEAR -> {
                        return HumanoidModel.ArmPose.THROW_SPEAR;
                    }
                    case CROSSBOW -> {
                        return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                    }
                }
            }
            else {
                boolean mainhandHasCrossbow = mainhandStack.getItem() == Items.CROSSBOW;
                boolean mainhandIsCharged = CrossbowItem.isCharged(mainhandStack);
                boolean offhandHasCrossbow = offhandStack.getItem() == Items.CROSSBOW;
                boolean offhandIsCharged = CrossbowItem.isCharged(offhandStack);
                if (mainhandHasCrossbow && mainhandIsCharged) {
                    armPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
                if (offhandHasCrossbow && offhandIsCharged && mainhandStack.getItem().getUseAnimation(mainhandStack) == UseAnim.NONE) {
                    return HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }
        return armPose;
    }

    public static <T extends LivingEntity> void lungeAnimationLeftHand(HumanoidModel<T> model,
                                                                       T entity,
                                                                       InteractionHand hand,
                                                                       LungeAttackInfo lunge) {
        if (lunge.isLungeInProgress(hand)) {
            model.leftArm.xRot = -0.75f * lunge.getLungeMult(hand);
        }
    }

    public static <T extends LivingEntity> void lungeAnimationRightHand(HumanoidModel<T> model,
                                                                        T entity,
                                                                        InteractionHand hand,
                                                                        LungeAttackInfo lunge) {
        if (lunge.isLungeInProgress(hand)) {
            model.rightArm.xRot = -0.75f * lunge.getLungeMult(hand);
        }
    }

    /**
     * Hooks from {@link BipedModel#setRotationAngles(LivingEntity, float, float, float, float, float, float)} func_212844_a_
     */
    @EvolutionHook
    public static <T extends LivingEntity> void setRotationAngles(HumanoidModel<T> model, T entity, float ageInTicks) {
        LungeChargeInfo lungeCharge = ClientEvents.ABOUT_TO_LUNGE_PLAYERS.get(entity.getId());
        if (lungeCharge != null) {
            if (entity.getMainArm() == HumanoidArm.RIGHT) {
                startLungeAnimationRightHand(model, entity, InteractionHand.MAIN_HAND, lungeCharge);
                startLungeAnimationLeftHand(model, entity, InteractionHand.OFF_HAND, lungeCharge);
            }
            else {
                startLungeAnimationRightHand(model, entity, InteractionHand.OFF_HAND, lungeCharge);
                startLungeAnimationLeftHand(model, entity, InteractionHand.MAIN_HAND, lungeCharge);
            }
        }
        LungeAttackInfo lungeAttack = ClientEvents.LUNGING_PLAYERS.get(entity.getId());
        if (lungeAttack != null) {
            if (entity.getMainArm() == HumanoidArm.RIGHT) {
                lungeAnimationRightHand(model, entity, InteractionHand.MAIN_HAND, lungeAttack);
                lungeAnimationLeftHand(model, entity, InteractionHand.OFF_HAND, lungeAttack);
            }
            else {
                lungeAnimationRightHand(model, entity, InteractionHand.OFF_HAND, lungeAttack);
                lungeAnimationLeftHand(model, entity, InteractionHand.MAIN_HAND, lungeAttack);
            }
        }
        if (entity.getMainArm() == HumanoidArm.RIGHT) {
            eatingAnimationRightHand(model, InteractionHand.MAIN_HAND, entity, ageInTicks);
            eatingAnimationLeftHand(model, InteractionHand.OFF_HAND, entity, ageInTicks);
        }
        else {
            eatingAnimationRightHand(model, InteractionHand.OFF_HAND, entity, ageInTicks);
            eatingAnimationLeftHand(model, InteractionHand.MAIN_HAND, entity, ageInTicks);
        }
//        int tick = ClientEvents.getInstance().getTickCount();
//        int tickForAnim = tick % 30;
//        model.bipedRightArm.rotateAngleX = MathHelper.degToRad(-120.0f * MathHelper.sinDeg(tickForAnim / 30.0f * 90));
//        model.bipedRightArm.rotateAngleZ = MathHelper.degToRad(90.0f * MathHelper.sinDeg(tickForAnim / 30.0f * 90));
    }

    public static <T extends LivingEntity> void startLungeAnimationLeftHand(HumanoidModel<T> model,
                                                                            T entity,
                                                                            InteractionHand hand,
                                                                            LungeChargeInfo lunge) {
        lunge.checkItem(hand, entity.getItemInHand(hand));
        if (lunge.isLungeInProgress(hand)) {
            model.leftArm.xRot = 0.75f * lunge.getLungeMult(hand);
        }
    }

    public static <T extends LivingEntity> void startLungeAnimationRightHand(HumanoidModel<T> model,
                                                                             T entity,
                                                                             InteractionHand hand,
                                                                             LungeChargeInfo lunge) {
        lunge.checkItem(hand, entity.getItemInHand(hand));
        if (lunge.isLungeInProgress(hand)) {
            model.rightArm.xRot = 0.75f * lunge.getLungeMult(hand);
        }
    }
}
