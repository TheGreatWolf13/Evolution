package tgw.evolution.mixin;

import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.MathHelper;

@Mixin(BipedModel.class)
public abstract class BipedModelMixin<T extends LivingEntity> extends AgeableModel<T> {

    @Shadow
    public ModelRenderer body;
    @Shadow
    public boolean crouching;
    @Shadow
    public ModelRenderer hat;
    @Shadow
    public ModelRenderer head;
    @Shadow
    public ModelRenderer leftArm;
    @Shadow
    public BipedModel.ArmPose leftArmPose;
    @Shadow
    public ModelRenderer leftLeg;
    @Shadow
    public ModelRenderer rightArm;
    @Shadow
    public BipedModel.ArmPose rightArmPose;
    @Shadow
    public ModelRenderer rightLeg;
    @Shadow
    public float swimAmount;

    @Shadow
    protected abstract HandSide getAttackArm(T p_217147_1_);

    @Shadow
    protected abstract void poseLeftArm(T p_241655_1_);

    @Shadow
    protected abstract void poseRightArm(T p_241654_1_);

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
        boolean isRightHanded = entity.getMainArm() == HandSide.RIGHT;
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
        ModelHelper.bobArms(this.rightArm, this.leftArm, ageInTicks);
        if (this.swimAmount > 0.0F) {
            float f1 = limbSwing % 26.0F;
            HandSide attackArm = this.getAttackArm(entity);
            float f2 = attackArm == HandSide.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            float f3 = attackArm == HandSide.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            if (f1 < 14.0F) {
                this.leftArm.xRot = this.rotlerpRad(f3, this.leftArm.xRot, 0.0F);
                this.rightArm.xRot = MathHelper.lerp(f2, this.rightArm.xRot, 0.0F);
                this.leftArm.yRot = this.rotlerpRad(f3, this.leftArm.yRot, MathHelper.PI);
                this.rightArm.yRot = MathHelper.lerp(f2, this.rightArm.yRot, MathHelper.PI);
                this.leftArm.zRot = this.rotlerpRad(f3,
                                                    this.leftArm.zRot,
                                                    MathHelper.PI + 1.870_796_4F * this.quadraticArmUpdate(f1) / this.quadraticArmUpdate(14.0F));
                this.rightArm.zRot = MathHelper.lerp(f2,
                                                     this.rightArm.zRot,
                                                     MathHelper.PI - 1.870_796_4F * this.quadraticArmUpdate(f1) / this.quadraticArmUpdate(14.0F));
            }
            else if (f1 >= 14.0F && f1 < 22.0F) {
                float f6 = (f1 - 14.0F) / 8.0F;
                this.leftArm.xRot = this.rotlerpRad(f3, this.leftArm.xRot, MathHelper.PI_OVER_2 * f6);
                this.rightArm.xRot = MathHelper.lerp(f2, this.rightArm.xRot, MathHelper.PI_OVER_2 * f6);
                this.leftArm.yRot = this.rotlerpRad(f3, this.leftArm.yRot, MathHelper.PI);
                this.rightArm.yRot = MathHelper.lerp(f2, this.rightArm.yRot, MathHelper.PI);
                this.leftArm.zRot = this.rotlerpRad(f3, this.leftArm.zRot, 5.012_389F - 1.870_796_4F * f6);
                this.rightArm.zRot = MathHelper.lerp(f2, this.rightArm.zRot, 1.270_796_3F + 1.870_796_4F * f6);
            }
            else if (f1 >= 22.0F && f1 < 26.0F) {
                float f4 = (f1 - 22.0F) / 4.0F;
                this.leftArm.xRot = this.rotlerpRad(f3, this.leftArm.xRot, MathHelper.PI_OVER_2 - MathHelper.PI_OVER_2 * f4);
                this.rightArm.xRot = MathHelper.lerp(f2, this.rightArm.xRot, MathHelper.PI_OVER_2 - MathHelper.PI_OVER_2 * f4);
                this.leftArm.yRot = this.rotlerpRad(f3, this.leftArm.yRot, MathHelper.PI);
                this.rightArm.yRot = MathHelper.lerp(f2, this.rightArm.yRot, MathHelper.PI);
                this.leftArm.zRot = this.rotlerpRad(f3, this.leftArm.zRot, MathHelper.PI);
                this.rightArm.zRot = MathHelper.lerp(f2, this.rightArm.zRot, MathHelper.PI);
            }
            this.leftLeg.xRot = MathHelper.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * MathHelper.cos(limbSwing * 0.333_333_34F + MathHelper.PI));
            this.rightLeg.xRot = MathHelper.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * MathHelper.cos(limbSwing * 0.333_333_34F));
        }
        this.hat.copyFrom(this.head);
    }

    @Shadow
    protected abstract void setupAttackAnimation(T p_230486_1_, float p_230486_2_);
}
