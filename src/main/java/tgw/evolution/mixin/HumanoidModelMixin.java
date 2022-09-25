package tgw.evolution.mixin;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.ArmPoseConverter;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMHumanoid;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements HMHumanoid<T> {

    @Shadow
    @Final
    public ModelPart body;

    @Shadow
    public boolean crouching;

    @Shadow
    @Final
    public ModelPart hat;

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    public HumanoidModel.ArmPose leftArmPose;

    @Shadow
    @Final
    public ModelPart leftLeg;

    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    public HumanoidModel.ArmPose rightArmPose;

    @Shadow
    @Final
    public ModelPart rightLeg;

    @Shadow
    public float swimAmount;

    /**
     * @author TheGreatWolf
     * @reason Fix for HMs
     */
    @Overwrite
    public static MeshDefinition createMesh(CubeDeformation deformation, float offset) {
        MeshDefinition meshDef = new MeshDefinition();
        PartDefinition partDef = meshDef.getRoot();
        partDef.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 8.0F, 8.0F, deformation),
                                  PartPose.offset(0.0F, 24.0F + offset, 0.0F));
        partDef.addOrReplaceChild("hat",
                                  CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, 0.0f, -4.0F, 8.0F, 8.0F, 8.0F, deformation.extend(0.5F)),
                                  PartPose.offset(0.0F, 24.0F + offset, 0.0F));
        partDef.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(0.0F, 24.0F + offset, 0.0F));
        partDef.addOrReplaceChild("right_arm",
                                  CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(5.0F, 22.0F + offset, 0.0F));
        partDef.addOrReplaceChild("left_arm", CubeListBuilder.create()
                                                             .texOffs(40, 16)
                                                             .mirror()
                                                             .addBox(-3.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(-5.0F, 22.0F + offset, 0.0F));
        partDef.addOrReplaceChild("right_leg",
                                  CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(1.9F, 12.0F + offset, 0.0F));
        partDef.addOrReplaceChild("left_leg",
                                  CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(-1.9F, 12.0F + offset, 0.0F));
        return meshDef;
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
    public HM leftArm() {
        return (HM) (Object) this.leftArm;
    }

    @Override
    public ArmPose leftArmPose() {
        return ArmPoseConverter.fromVanilla(this.leftArmPose);
    }

    @Override
    public HM leftLeg() {
        return (HM) (Object) this.leftLeg;
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
    public HM rightArm() {
        return (HM) (Object) this.rightArm;
    }

    @Override
    public ArmPose rightArmPose() {
        return ArmPoseConverter.fromVanilla(this.rightArmPose);
    }

    @Override
    public HM rightLeg() {
        return (HM) (Object) this.rightLeg;
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
    public float swimAmount() {
        return this.swimAmount;
    }
}
