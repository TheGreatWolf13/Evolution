package tgw.evolution.mixin;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.hitbox.MixinTempHelper;
import tgw.evolution.util.math.MathHelper;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> {

    @Final
    @Shadow
    public ModelPart body;
    @Shadow
    public boolean crouching;
    @Final
    @Shadow
    public ModelPart hat;
    @Final
    @Shadow
    public ModelPart head;
    @Final
    @Shadow
    public ModelPart leftArm;
    @Shadow
    public HumanoidModel.ArmPose leftArmPose;
    @Final
    @Shadow
    public ModelPart leftLeg;
    @Final
    @Shadow
    public ModelPart rightArm;
    @Shadow
    public HumanoidModel.ArmPose rightArmPose;
    @Final
    @Shadow
    public ModelPart rightLeg;
    @Shadow
    public float swimAmount;

    @Shadow
    protected abstract ModelPart getArm(HumanoidArm p_187074_1_);

    @Shadow
    protected abstract HumanoidArm getAttackArm(T p_217147_1_);

    /**
     * @author MGSchultz
     */
    @Overwrite
    private void poseLeftArm(T entity) {
        switch (this.leftArmPose) {
            case EMPTY -> this.leftArm.yRot = 0.0F;
            case BLOCK -> {
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.942_477_9F;
                this.leftArm.yRot = MathHelper.PI / 6.0F;
            }
            case ITEM -> {
                if (entity.isUsingItem()) {
                    if (this.shouldPoseArm(entity, HumanoidArm.LEFT)) {
                        ItemStack stack = entity.getUseItem();
                        Item useItem = stack.getItem();
                        UseAnim action = useItem.getUseAnimation(stack);
                        if (action == UseAnim.EAT || action == UseAnim.DRINK) {
                            this.leftArm.xRot = -Mth.lerp(-1.0f * (entity.getXRot() - 90.0f) / 180.0f, 1.0f, 2.0f) +
                                                MathHelper.sin(entity.tickCount * 1.5f) * 0.1f;
                            this.leftArm.yRot = 0.3f;
                            this.leftArm.zRot = -0.3f;
                            break;
                        }
                    }
                }
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - MathHelper.PI / 10.0F;
                this.leftArm.yRot = 0.0F;
            }
            case THROW_SPEAR -> {
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - (MathHelper.PI - MathHelper.degToRad(entity.getXRot()));
                this.leftArm.yRot = 0.0F;
            }
            case BOW_AND_ARROW -> {
                this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
                this.leftArm.yRot = 0.1F + this.head.yRot;
                this.rightArm.xRot = -MathHelper.PI_OVER_2 + this.head.xRot;
                this.leftArm.xRot = -MathHelper.PI_OVER_2 + this.head.xRot;
            }
            case CROSSBOW_CHARGE -> AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, entity, false);
            case CROSSBOW_HOLD -> AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
            case SPYGLASS -> {
                this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.919_862_2F - (entity.isCrouching() ? 0.261_799_4F : 0.0F), -2.4F, 3.3F);
                this.leftArm.yRot = this.head.yRot + 0.261_799_4F;
            }
        }
    }

    /**
     * @author MGSchultz
     */
    @Overwrite
    private void poseRightArm(T entity) {
        switch (this.rightArmPose) {
            case EMPTY -> this.rightArm.yRot = 0.0F;
            case BLOCK -> {
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.942_477_9F;
                this.rightArm.yRot = -MathHelper.PI / 6.0F;
            }
            case ITEM -> {
                if (entity.isUsingItem()) {
                    if (this.shouldPoseArm(entity, HumanoidArm.RIGHT)) {
                        ItemStack stack = entity.getUseItem();
                        Item useItem = stack.getItem();
                        UseAnim action = useItem.getUseAnimation(stack);
                        if (action == UseAnim.EAT || action == UseAnim.DRINK) {
                            this.rightArm.xRot = -Mth.lerp(-1.0f * (entity.getXRot() - 90.0f) / 180.0f, 1.0f, 2.0f) +
                                                 MathHelper.sin(entity.tickCount * 1.5f) * 0.1f;
                            this.rightArm.yRot = -0.3f;
                            this.rightArm.zRot = 0.3f;
                            break;
                        }
                    }
                }
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - MathHelper.PI / 10.0F;
                this.rightArm.yRot = 0.0F;
            }
            case THROW_SPEAR -> {
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - (MathHelper.PI - MathHelper.degToRad(entity.getXRot()));
                this.rightArm.yRot = 0.0F;
            }
            case BOW_AND_ARROW -> {
                this.rightArm.yRot = -0.1F + this.head.yRot;
                this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
                this.rightArm.xRot = -MathHelper.PI_OVER_2 + this.head.xRot;
                this.leftArm.xRot = -MathHelper.PI_OVER_2 + this.head.xRot;
            }
            case CROSSBOW_CHARGE -> AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, entity, true);
            case CROSSBOW_HOLD -> AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
            case SPYGLASS -> {
                this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.919_862_2F - (entity.isCrouching() ? 0.261_799_4F : 0.0F), -3.3F, 3.3F);
                this.rightArm.yRot = this.head.yRot - 0.261_799_4F;
            }
        }
    }

    @Shadow
    protected abstract float quadraticArmUpdate(float p_203068_1_);

    @Shadow
    protected abstract float rotlerpRad(float p_205060_1_, float p_205060_2_, float p_205060_3_);

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to improve first person camera.
     */
    @Override
    @Overwrite
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean isElytraFlying = entity.getFallFlyingTicks() > 4;
        boolean isVisuallySwimming = entity.isVisuallySwimming();
        this.head.yRot = MathHelper.degToRad(netHeadYaw);
        this.head.zRot = 0; //Reset model
        if (isElytraFlying) {
            this.head.xRot = -MathHelper.PI / 4.0F;
        }
        else if (this.swimAmount > 0.0F) {
            if (isVisuallySwimming) {
                if (!entity.isInWater()) {
                    //Crawling pose
                    this.head.xRot = MathHelper.degToRad(headPitch - 90);
                }
                else {
                    this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, -MathHelper.PI_OVER_2);
                }
                this.head.yRot = 0; //Improve head rotation
                this.head.zRot = -MathHelper.degToRad(netHeadYaw); //Improve head rotation
            }
            else {
                this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, MathHelper.degToRad(headPitch));
            }
        }
        else {
            this.head.xRot = MathHelper.degToRad(headPitch);
        }
        this.body.yRot = 0.0F;
        this.rightArm.z = 0.0F;
        this.rightArm.x = -5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        float f = 1.0F;
        if (isElytraFlying) {
            f = (float) entity.getDeltaMovement().lengthSqr();
            f /= 0.2F;
            f = f * f * f;
        }
        if (f < 1.0F) {
            f = 1.0F;
        }
        this.rightArm.xRot = MathHelper.cos(limbSwing * 0.666_2F + MathHelper.PI) * 2.0F * limbSwingAmount * 0.5F / f;
        this.leftArm.xRot = MathHelper.cos(limbSwing * 0.666_2F) * 2.0F * limbSwingAmount * 0.5F / f;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = MathHelper.cos(limbSwing * 0.666_2F) * 1.4F * limbSwingAmount / f;
        this.leftLeg.xRot = MathHelper.cos(limbSwing * 0.666_2F + MathHelper.PI) * 1.4F * limbSwingAmount / f;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        this.rightLeg.zRot = 0.0F;
        this.leftLeg.zRot = 0.0F;
        if (this.riding) {
            this.rightArm.xRot += -MathHelper.PI / 5.0F; //-36º
            this.leftArm.xRot += -MathHelper.PI / 5.0F;  //-36º
            this.rightLeg.xRot = -1.413_716_7F;
            this.rightLeg.yRot = MathHelper.PI / 10.0F; //18º
            this.rightLeg.zRot = 0.078_539_82F;
            this.leftLeg.xRot = -1.413_716_7F;
            this.leftLeg.yRot = -MathHelper.PI / 10.0F; //-18º
            this.leftLeg.zRot = -0.078_539_82F;
        }
        this.rightArm.yRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        boolean isRightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        boolean isPoseTwoHanded = isRightHanded ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
        if (isRightHanded != isPoseTwoHanded) {
            this.poseLeftArm(entity);
            this.poseRightArm(entity);
        }
        else {
            this.poseRightArm(entity);
            this.poseLeftArm(entity);
        }
        this.setupAttackAnimation(entity, ageInTicks);
        if (this.crouching) {
            this.body.xRot = 0.5F;
            this.rightArm.xRot += 0.4F;
            this.leftArm.xRot += 0.4F;
            this.rightLeg.z = 4.0F;
            this.leftLeg.z = 4.0F;
            this.rightLeg.y = 12.2F;
            this.leftLeg.y = 12.2F;
            this.head.y = 4.2F;
            this.body.y = 3.2F;
            this.leftArm.y = 5.2F;
            this.rightArm.y = 5.2F;
        }
        else {
            this.body.xRot = 0.0F;
            this.rightLeg.z = 0.1F;
            this.leftLeg.z = 0.1F;
            this.rightLeg.y = 12.0F;
            this.leftLeg.y = 12.0F;
            this.head.y = 0.0F;
            this.body.y = 0.0F;
            this.leftArm.y = 2.0F;
            this.rightArm.y = 2.0F;
        }
        if (this.rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.rightArm, ageInTicks, 1.0F);
        }
        if (this.leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.leftArm, ageInTicks, -1.0F);
        }
        if (this.swimAmount > 0.0F) {
            float f1 = limbSwing % 26.0F;
            HumanoidArm attackArm = this.getAttackArm(entity);
            float f2 = attackArm == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            float f3 = attackArm == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            if (f1 < 14.0F) {
                this.leftArm.xRot = this.rotlerpRad(f3, this.leftArm.xRot, 0.0F);
                this.rightArm.xRot = Mth.lerp(f2, this.rightArm.xRot, 0.0F);
                this.leftArm.yRot = this.rotlerpRad(f3, this.leftArm.yRot, MathHelper.PI);
                this.rightArm.yRot = Mth.lerp(f2, this.rightArm.yRot, MathHelper.PI);
                this.leftArm.zRot = this.rotlerpRad(f3, this.leftArm.zRot,
                                                    MathHelper.PI + 1.870_796_4F * this.quadraticArmUpdate(f1) / this.quadraticArmUpdate(14.0F));
                this.rightArm.zRot = Mth.lerp(f2, this.rightArm.zRot,
                                              MathHelper.PI - 1.870_796_4F * this.quadraticArmUpdate(f1) / this.quadraticArmUpdate(14.0F));
            }
            else if (f1 >= 14.0F && f1 < 22.0F) {
                float f6 = (f1 - 14.0F) / 8.0F;
                this.leftArm.xRot = this.rotlerpRad(f3, this.leftArm.xRot, MathHelper.PI_OVER_2 * f6);
                this.rightArm.xRot = Mth.lerp(f2, this.rightArm.xRot, MathHelper.PI_OVER_2 * f6);
                this.leftArm.yRot = this.rotlerpRad(f3, this.leftArm.yRot, MathHelper.PI);
                this.rightArm.yRot = Mth.lerp(f2, this.rightArm.yRot, MathHelper.PI);
                this.leftArm.zRot = this.rotlerpRad(f3, this.leftArm.zRot, 5.012_389F - 1.870_796_4F * f6);
                this.rightArm.zRot = Mth.lerp(f2, this.rightArm.zRot, 1.270_796_3F + 1.870_796_4F * f6);
            }
            else if (f1 >= 22.0F && f1 < 26.0F) {
                float f4 = (f1 - 22.0F) / 4.0F;
                this.leftArm.xRot = this.rotlerpRad(f3, this.leftArm.xRot, MathHelper.PI_OVER_2 - MathHelper.PI_OVER_2 * f4);
                this.rightArm.xRot = Mth.lerp(f2, this.rightArm.xRot, MathHelper.PI_OVER_2 - MathHelper.PI_OVER_2 * f4);
                this.leftArm.yRot = this.rotlerpRad(f3, this.leftArm.yRot, MathHelper.PI);
                this.rightArm.yRot = Mth.lerp(f2, this.rightArm.yRot, MathHelper.PI);
                this.leftArm.zRot = this.rotlerpRad(f3, this.leftArm.zRot, MathHelper.PI);
                this.rightArm.zRot = Mth.lerp(f2, this.rightArm.zRot, MathHelper.PI);
            }
            this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * MathHelper.cos(limbSwing * 0.333_333_34F + MathHelper.PI));
            this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * MathHelper.cos(limbSwing * 0.333_333_34F));
        }
        this.hat.copyFrom(this.head);
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to implement Evolution's custom attack animations
     */
    @Overwrite
    protected void setupAttackAnimation(T entity, float ageInTicks) {
        if (this.attackTime > 0.0F) {
            HumanoidArm attackingSide = this.getAttackArm(entity);
            if (!(((ILivingEntityPatch) entity).renderMainhandSpecialAttack() && entity.getMainArm() == this.getAttackArm(entity))) {
                ModelPart attackingArm = this.getArm(attackingSide);
                float sqrtAttackTime = this.attackTime;
                this.body.yRot = MathHelper.sin(MathHelper.sqrt(sqrtAttackTime) * MathHelper.TAU) * 0.2F;
                ModelPart var10000;
                if (attackingSide == HumanoidArm.LEFT) {
                    var10000 = this.body;
                    var10000.yRot *= -1.0F;
                }
                this.rightArm.z = MathHelper.sin(this.body.yRot) * 5.0F;
                this.rightArm.x = -MathHelper.cos(this.body.yRot) * 5.0F;
                this.leftArm.z = -MathHelper.sin(this.body.yRot) * 5.0F;
                this.leftArm.x = MathHelper.cos(this.body.yRot) * 5.0F;
                var10000 = this.rightArm;
                var10000.yRot += this.body.yRot;
                var10000 = this.leftArm;
                var10000.yRot += this.body.yRot;
                var10000 = this.leftArm;
                var10000.xRot += this.body.yRot;
                sqrtAttackTime = 1.0F - this.attackTime;
                sqrtAttackTime *= sqrtAttackTime;
                sqrtAttackTime *= sqrtAttackTime;
                sqrtAttackTime = 1.0F - sqrtAttackTime;
                float f1 = MathHelper.sin(sqrtAttackTime * MathHelper.PI);
                float f2 = MathHelper.sin(this.attackTime * MathHelper.PI) * -(this.head.xRot - 0.7F) * 0.75F;
                attackingArm.xRot -= f1 * 1.2 + f2;
                attackingArm.yRot += this.body.yRot * 2.0F;
                attackingArm.zRot += MathHelper.sin(this.attackTime * MathHelper.PI) * -0.4F;
            }
        }
        if (((ILivingEntityPatch) entity).renderMainhandSpecialAttack()) {
            MixinTempHelper.setup((HumanoidModel) (Object) this, entity);
        }
    }

    private boolean shouldPoseArm(T entity, HumanoidArm side) {
        if (entity.getMainArm() == side) {
            return entity.getUsedItemHand() == InteractionHand.MAIN_HAND;
        }
        return entity.getUsedItemHand() == InteractionHand.OFF_HAND;
    }
}