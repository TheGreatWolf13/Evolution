package tgw.evolution.util.hitbox.hrs;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.entities.misc.ISittableEntity;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.hitbox.hms.HMPlayer;

public interface HRPlayer<T extends Player> extends HRLivingEntity<T, HMPlayer<T>> {

    private static ArmPose getArmPose(Player player, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);
        if (handStack.isEmpty()) {
            return ArmPose.EMPTY;
        }
        if (player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0) {
            UseAnim useanim = handStack.getUseAnimation();
            if (useanim == UseAnim.BLOCK) {
                return ArmPose.BLOCK;
            }
            if (useanim == UseAnim.BOW) {
                return ArmPose.BOW_AND_ARROW;
            }
            if (useanim == UseAnim.SPEAR) {
                return ArmPose.THROW_SPEAR;
            }
            if (useanim == UseAnim.CROSSBOW && hand == player.getUsedItemHand()) {
                return ArmPose.CROSSBOW_CHARGE;
            }
            if (useanim == UseAnim.SPYGLASS) {
                return ArmPose.SPYGLASS;
            }
        }
        else if (!player.swinging && handStack.is(Items.CROSSBOW) && CrossbowItem.isCharged(handStack)) {
            return ArmPose.CROSSBOW_HOLD;
        }
        return ArmPose.ITEM;
    }

    default void modelProperties(T player) {
        HMPlayer<T> model = this.model();
        if (player.isSpectator()) {
            model.setAllVisible(false);
            model.head().setVisible(true);
            model.hat().setVisible(true);
        }
        else {
            model.setAllVisible(true);
            model.hat().setVisible(player.isModelPartShown(PlayerModelPart.HAT));
            model.clothesBody().setVisible(player.isModelPartShown(PlayerModelPart.JACKET));
            model.clothesLegL().setVisible(player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG));
            model.clothesLegR().setVisible(player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG));
            model.clothesArmL().setVisible(player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
            model.clothesArmR().setVisible(player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
            model.setCrouching(player.isCrouching());
            ArmPose mainArmPose = getArmPose(player, InteractionHand.MAIN_HAND);
            ArmPose offArmPose = getArmPose(player, InteractionHand.OFF_HAND);
            if (mainArmPose.isTwoHanded()) {
                offArmPose = player.getOffhandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
            }
            if (player.getMainArm() == HumanoidArm.RIGHT) {
                model.setRightArmPose(mainArmPose);
                model.setLeftArmPose(offArmPose);
            }
            else {
                model.setRightArmPose(offArmPose);
                model.setLeftArmPose(mainArmPose);
            }
        }
    }

    @Override
    default Vec3 renderOffset(T entity, float partialTicks) {
        return HRLivingEntity.super.renderOffset(entity, partialTicks);
    }

    @Override
    default void rotations(T entity, HR hr, float ageInTicks, float rotationYaw, float partialTicks) {
        float swimAmount = entity.getSwimAmount(partialTicks);
        if (entity.isFallFlying()) {
            HRLivingEntity.super.rotations(entity, hr, ageInTicks, rotationYaw, partialTicks);
            float f1 = entity.getFallFlyingTicks() + partialTicks;
            float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!entity.isAutoSpinAttack()) {
                hr.rotateXHR(f2 * (-90.0F - entity.getXRot()));
            }
            Vec3 viewVec = entity.getViewVector(partialTicks);
            Vec3 motion = entity.getDeltaMovement();
            double horizMotionSqr = motion.horizontalDistanceSqr();
            double horizViewSqr = viewVec.horizontalDistanceSqr();
            if (horizMotionSqr > 0 && horizViewSqr > 0) {
                double d2 = (motion.x * viewVec.x + motion.z * viewVec.z) / Math.sqrt(horizMotionSqr * horizViewSqr);
                double d3 = motion.x * viewVec.z - motion.z * viewVec.x;
                hr.rotateYHR(Mth.RAD_TO_DEG * (float) (Math.signum(d3) * Math.acos(d2)));
            }
        }
        else if (swimAmount > 0.0F) {
            HRLivingEntity.super.rotations(entity, hr, ageInTicks, rotationYaw, partialTicks);
            float desiredXRot = entity.isInWater() ? -90.0F - entity.getXRot() : -90.0F;
            float interpXRot = Mth.lerp(swimAmount, 0.0F, desiredXRot);
            if (entity.isVisuallySwimming()) {
                if (!entity.isInWater()) {
                    //Crawling pose
                    hr.rotateXHR(interpXRot);
                    hr.translateHR(0, -1, 0.385f);
                }
                else {
                    //Swimming pose
                    hr.translateHR(0, 0.4f, 0);
                    hr.rotateXHR(interpXRot);
                    hr.translateHR(0, -1.4f, -0.25f);
                }
            }
            else {
                hr.rotateXHR(interpXRot);
                hr.translateHR(0, -1.3f, 0.2f);
            }
        }
        else {
            HRLivingEntity.super.rotations(entity, hr, ageInTicks, rotationYaw, partialTicks);
            if (entity.isPassenger()) {
                Entity vehicle = entity.getVehicle();
                if (vehicle != null && vehicle.shouldRiderSit() && vehicle instanceof ISittableEntity sittable) {
                    hr.translateHR(0, 0, sittable.getZOffset());
                }
            }
        }
    }

    @Override
    default void setScale(T entity, HR hr, float partialTicks) {
        hr.scaleHR(0.937_5f, 0.937_5f, 0.937_5f);
        switch (entity.getPose()) {
            case STANDING, CROUCHING -> hr.translateHR(0, 0, 1 / 16.0f);
            case SWIMMING -> {
                if (!entity.isInWater()) {
                    hr.translateHR(0, -9 / 16.0f, -0.5f / 16.0f);
                }
            }
        }
    }
}
