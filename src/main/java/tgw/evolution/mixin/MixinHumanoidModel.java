package tgw.evolution.mixin;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.models.CubeListBuilderEv;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.ArmPoseConverter;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMHumanoid;

@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel<T extends LivingEntity> extends AgeableListModel<T> implements HMHumanoid<T> {

    @Shadow @Final public ModelPart body;
    @Shadow public boolean crouching;
    @Shadow @Final public ModelPart hat;
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart leftArm;
    @Shadow public HumanoidModel.ArmPose leftArmPose;
    @Shadow @Final public ModelPart leftLeg;
    @Shadow @Final public ModelPart rightArm;
    @Shadow public HumanoidModel.ArmPose rightArmPose;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow public float swimAmount;
    @Unique private boolean shouldCancelLeft;
    @Unique private boolean shouldCancelRight;

    /**
     * @author TheGreatWolf
     * @reason Fix for HMs
     */
    @Overwrite
    public static MeshDefinition createMesh(CubeDeformation deformation, float offset) {
        MeshDefinition meshDef = new MeshDefinition();
        PartDefinition partDef = meshDef.getRoot();
        partDef.addOrReplaceChild("head",
                                  CubeListBuilderEv.create().requestFix().texOffs(0, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 8.0F, 8.0F, deformation),
                                  PartPose.offset(0.0F, 24.0F + offset, 0.0F));
        partDef.addOrReplaceChild("hat",
                                  CubeListBuilderEv.create()
                                                   .requestFix()
                                                   .texOffs(32, 0)
                                                   .addBox(-4.0F, 0.0f, -4.0F, 8.0F, 8.0F, 8.0F, deformation.extend(0.5F)),
                                  PartPose.offset(0.0F, 24.0F + offset, 0.0F));
        partDef.addOrReplaceChild("body", CubeListBuilderEv.create()
                                                           .requestFix()
                                                           .texOffs(16, 16)
                                                           .addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(0.0F, 24.0F + offset, 0.0F));
        partDef.addOrReplaceChild("right_arm",
                                  CubeListBuilderEv.create()
                                                   .requestFix()
                                                   .texOffs(40, 16)
                                                   .addBox(-1.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(5.0F, 22.0F + offset, 0.0F));
        partDef.addOrReplaceChild("left_arm", CubeListBuilderEv.create().requestFix()
                                                               .texOffs(40, 16)
                                                               .mirror()
                                                               .addBox(-3.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(-5.0F, 22.0F + offset, 0.0F));
        partDef.addOrReplaceChild("right_leg",
                                  CubeListBuilderEv.create().requestFix().texOffs(0, 16).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(1.9F, 12.0F + offset, 0.0F));
        partDef.addOrReplaceChild("left_leg",
                                  CubeListBuilderEv.create()
                                                   .requestFix()
                                                   .texOffs(0, 16)
                                                   .mirror()
                                                   .addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(-1.9F, 12.0F + offset, 0.0F));
        return meshDef;
    }

    @Override
    public HM armL() {
        return (HM) (Object) this.leftArm;
    }

    @Override
    public HM armR() {
        return (HM) (Object) this.rightArm;
    }

    @Override
    public HM body() {
        return (HM) (Object) this.body;
    }

    @Override
    public boolean crouching() {
        return this.crouching;
    }

    @Override
    public HM hat() {
        return (HM) (Object) this.hat;
    }

    @Override
    public HM head() {
        return (HM) (Object) this.head;
    }

    @Override
    public ArmPose leftArmPose() {
        return ArmPoseConverter.fromVanilla(this.leftArmPose);
    }

    @Override
    public HM legL() {
        return (HM) (Object) this.leftLeg;
    }

    @Override
    public HM legR() {
        return (HM) (Object) this.rightLeg;
    }

    /**
     * @author TheGreatWolf
     * @reason Use HMs
     */
    @Override
    @Overwrite
    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {
        this.prepare(entity, limbSwing, limbSwingAmount, partialTicks);
    }

    @Override
    public ArmPose rightArmPose() {
        return ArmPoseConverter.fromVanilla(this.rightArmPose);
    }

    @Override
    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    @Override
    public void setLeftArmPose(ArmPose leftArmPose) {
        this.leftArmPose = ArmPoseConverter.toVanilla(leftArmPose);
    }

    @Override
    public void setRightArmPose(ArmPose rightArmPose) {
        this.rightArmPose = ArmPoseConverter.toVanilla(rightArmPose);
    }

    @Override
    public void setShouldCancelLeft(boolean shouldCancel) {
        this.shouldCancelLeft = shouldCancel;
    }

    @Override
    public void setShouldCancelRight(boolean shouldCancel) {
        this.shouldCancelRight = shouldCancel;
    }

    @Override
    public void setSwimAmount(float swimAmount) {
        this.swimAmount = swimAmount;
    }

    /**
     * @author TheGreatWolf
     * @reason Use HMs
     */
    @Override
    @Overwrite
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.setup(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public boolean shouldCancelLeft() {
        return this.shouldCancelLeft;
    }

    @Override
    public boolean shouldCancelRight() {
        return this.shouldCancelRight;
    }

    @Override
    public float swimAmount() {
        return this.swimAmount;
    }
}
