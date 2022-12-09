package tgw.evolution.client.models.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.client.models.CubeListBuilderEv;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.collection.RList;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMHumanoid;

import java.util.function.Function;

public class ModelHumanoid<T extends LivingEntity> extends ModelAgeableList<T> implements ArmedModel, HeadedModel, HMHumanoid<T> {

    public final ModelPart armL;
    public final ModelPart armR;
    public final ModelPart body;
    public final ModelPart forearmL;
    public final ModelPart forearmR;
    public final ModelPart forelegL;
    public final ModelPart forelegR;
    public final ModelPart hat;
    public final ModelPart head;
    public final ModelPart itemL;
    public final ModelPart itemR;
    public final ModelPart legL;
    public final ModelPart legR;
    protected final RList<ModelPart> bodyParts = new RArrayList<>();
    protected final RList<ModelPart> headParts = new RArrayList<>();
    public boolean crouching;
    public ArmPose leftArmPose = ArmPose.EMPTY;
    public ArmPose rightArmPose = ArmPose.EMPTY;
    public float swimAmount;
    protected boolean shouldCancelLeft;
    protected boolean shouldCancelRight;

    public ModelHumanoid(ModelPart root) {
        this(root, RenderHelper.RENDER_TYPE_ENTITY_CUTOUT_NO_CULL);
    }

    public ModelHumanoid(ModelPart root, Function<ResourceLocation, RenderType> renderType) {
        super(renderType, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
        this.head = root.getChild("head");
        this.hat = root.getChild("hat");
        this.body = root.getChild("body");
        this.armR = this.body.getChild("arm_r");
        this.forearmR = this.armR.getChild("forearm_r");
        this.itemR = this.forearmR.getChild("item_r");
        this.armL = this.body.getChild("arm_l");
        this.forearmL = this.armL.getChild("forearm_l");
        this.itemL = this.forearmL.getChild("item_l");
        this.legR = this.body.getChild("leg_r");
        this.forelegR = this.legR.getChild("foreleg_r");
        this.legL = this.body.getChild("leg_l");
        this.forelegL = this.legL.getChild("foreleg_l");
        this.headParts.add(this.head);
        this.headParts.add(this.hat);
        this.headParts.trimCollection();
        this.bodyParts.add(this.body);
        this.bodyParts.trimCollection();
        this.itemR.visible = false;
        this.itemL.visible = false;
    }

    public static MeshDefinition createMesh(CubeDeformation def, float offset) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose neckPoint = PartPose.offset(0, 24 + offset, 0);
        root.addOrReplaceChild("head", CubeListBuilderEv.create().requestFix().texOffs(0, 0).addBox(-4, 0, -4, 8, 8, 8, def), neckPoint);
        root.addOrReplaceChild("hat",
                               CubeListBuilderEv.create()
                                                .requestFix()
                                                .texOffs(32, 0)
                                                .addBox(-4, 0, -4, 8, 8, 8, def.extend(0.5F)), neckPoint);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilderEv.create()
                                                                              .requestFix()
                                                                              .texOffs(16, 16)
                                                                              .addBox(-4, -12, -2, 8, 12, 4, def), neckPoint);
        PartDefinition armR = body.addOrReplaceChild("arm_r", CubeListBuilderEv.create()
                                                                               .texOffs(40, 16)
                                                                               .addBoxBend(-1, -4, -2, 4, 6, 4, def, true),
                                                     PartPose.offset(5, -2 + offset, 0));
        PartDefinition forearmR = armR.addOrReplaceChild("forearm_r",
                                                         CubeListBuilderEv.create().texOffs(40, 22).addBoxBend(-2, -6, -2, 4, 6, 4, def, false),
                                                         PartPose.offset(1, -4, 0));
        forearmR.addOrReplaceChild("item_r", CubeListBuilderEv.create().texOffs(8, 8).addBox(-0.5f, -0.5f, -0.5f, 1, 1, 1, def),
                                   PartPose.offset(0, 0, 0));
        PartDefinition armL = body.addOrReplaceChild("arm_l", CubeListBuilderEv.create()
                                                                               .texOffs(40, 16)
                                                                               .mirror()
                                                                               .addBoxBend(-3, -4, -2, 4, 6, 4, def, true),
                                                     PartPose.offset(-5, -2 + offset, 0));
        PartDefinition forearmL = armL.addOrReplaceChild("forearm_l", CubeListBuilderEv.create()
                                                                                       .texOffs(40, 22)
                                                                                       .mirror()
                                                                                       .addBoxBend(-2, -6, -2, 4, 6, 4, def, false),
                                                         PartPose.offset(-1, -4, 0));
        forearmL.addOrReplaceChild("item_l", CubeListBuilderEv.create()
                                                              .texOffs(0, 0)
                                                              .addBox(0, -4, 0, 0, 0, 0, def), PartPose.offset(0, 0, 0));
        PartPose kneePoint = PartPose.offset(0, -6, 0);
        PartDefinition legR = body.addOrReplaceChild("leg_r", CubeListBuilderEv.create()
                                                                               .texOffs(0, 16)
                                                                               .addBoxBend(-2, -6, -2, 4, 6, 4, def, true),
                                                     PartPose.offset(1.9F, -12 + offset, 0));
        legR.addOrReplaceChild("foreleg_r", CubeListBuilderEv.create().texOffs(0, 22).addBoxBend(-2, -6, -2, 4, 6, 4, def, false), kneePoint);
        PartDefinition legL = body.addOrReplaceChild("leg_l", CubeListBuilderEv.create()
                                                                               .texOffs(0, 16)
                                                                               .mirror()
                                                                               .addBoxBend(-2, -6, -2, 4, 6, 4, def, true),
                                                     PartPose.offset(-1.9F, -12 + offset, 0));
        legL.addOrReplaceChild("foreleg_l", CubeListBuilderEv.create().texOffs(0, 22).mirror().addBoxBend(-2, -6, -2, 4, 6, 4, def, false),
                               kneePoint);
        return mesh;
    }

    @Override
    public HM armL() {
        return (HM) (Object) this.armL;
    }

    @Override
    public HM armR() {
        return (HM) (Object) this.armR;
    }

    @Override
    public float attackTime() {
        return this.attackTime;
    }

    @Override
    public HM body() {
        return (HM) (Object) this.body;
    }

    @Override
    protected RList<ModelPart> bodyParts() {
        return this.bodyParts;
    }

    public void copyPropertiesTo(ModelHumanoid<T> model) {
        super.copyPropertiesTo(model);
        model.leftArmPose = this.leftArmPose;
        model.rightArmPose = this.rightArmPose;
        model.crouching = this.crouching;
        model.head.copyFrom(this.head);
        model.hat.copyFrom(this.hat);
        model.body.copyFrom(this.body);
        model.armR.copyFrom(this.armR);
        model.armL.copyFrom(this.armL);
        model.legR.copyFrom(this.legR);
        model.legL.copyFrom(this.legL);
        this.itemR.visible = false;
        this.itemL.visible = false;
    }

    @Override
    public boolean crouching() {
        return this.crouching;
    }

    @Override
    public HM forearmL() {
        return (HM) (Object) this.forearmL;
    }

    @Override
    public HM forearmR() {
        return (HM) (Object) this.forearmR;
    }

    @Override
    public HM forelegL() {
        return (HM) (Object) this.forelegL;
    }

    @Override
    public HM forelegR() {
        return (HM) (Object) this.forelegR;
    }

    protected ModelPart getArm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.armL : this.armR;
    }

    protected ModelPart getForearm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.forearmL : this.forearmR;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    protected ModelPart getItem(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.itemL : this.itemR;
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
    protected RList<ModelPart> headParts() {
        return this.headParts;
    }

    @Override
    public HM itemL() {
        return (HM) (Object) this.itemL;
    }

    @Override
    public HM itemR() {
        return (HM) (Object) this.itemR;
    }

    @Override
    public ArmPose leftArmPose() {
        return this.leftArmPose;
    }

    @Override
    public HM legL() {
        return (HM) (Object) this.legL;
    }

    @Override
    public HM legR() {
        return (HM) (Object) this.legR;
    }

    @Override
    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {
        this.prepare(entity, limbSwing, limbSwingAmount, partialTicks);
    }

    @Override
    public boolean riding() {
        return this.riding;
    }

    @Override
    public ArmPose rightArmPose() {
        return this.rightArmPose;
    }

    public void setAllVisible(boolean visible) {
        this.head.visible = visible;
        this.hat.visible = visible;
        this.body.visible = visible;
        this.armR.visible = visible;
        this.armL.visible = visible;
        this.legR.visible = visible;
        this.legL.visible = visible;
        this.itemR.visible = false;
        this.itemL.visible = false;
    }

    @Override
    public void setAttackTime(float attackTime) {
        this.attackTime = attackTime;
    }

    @Override
    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    @Override
    public void setLeftArmPose(ArmPose leftArmPose) {
        this.leftArmPose = leftArmPose;
    }

    @Override
    public void setRiding(boolean riding) {
        this.riding = riding;
    }

    @Override
    public void setRightArmPose(ArmPose rightArmPose) {
        this.rightArmPose = rightArmPose;
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

    @Override
    public void setYoung(boolean young) {
        this.young = young;
    }

    @Override
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

    @Override
    public void translateToHand(HumanoidArm side, PoseStack matrices) {
        this.getArm(side).translateAndRotate(matrices);
    }

    @Override
    public boolean young() {
        return this.young;
    }
}
