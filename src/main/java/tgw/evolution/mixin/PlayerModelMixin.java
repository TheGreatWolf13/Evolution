package tgw.evolution.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.models.CubeListBuilderEv;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMPlayer;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> implements HMPlayer<T> {

    @Shadow
    @Final
    public ModelPart jacket;
    @Shadow
    @Final
    public ModelPart leftPants;
    @Shadow
    @Final
    public ModelPart leftSleeve;
    @Shadow
    @Final
    public ModelPart rightPants;
    @Shadow
    @Final
    public ModelPart rightSleeve;
    @Shadow
    @Final
    private ModelPart cloak;
    @Shadow
    @Final
    private ModelPart ear;

    public PlayerModelMixin(ModelPart pRoot) {
        super(pRoot);
    }

    /**
     * @author TheGreatWolf
     * @reason Improve / Fix HMs
     */
    @Overwrite
    public static MeshDefinition createMesh(CubeDeformation deformation, boolean slim) {
        MeshDefinition meshDef = HumanoidModel.createMesh(deformation, 0.0F);
        PartDefinition root = meshDef.getRoot();
        root.addOrReplaceChild("ear",
                               CubeListBuilderEv.create().requestFix().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, deformation),
                               PartPose.ZERO);
        root.addOrReplaceChild("cloak", CubeListBuilderEv.create().requestFix()
                                                         .texOffs(0, 0)
                                                         .addBox(-5.0F, -16.0F, 0.0F, 10.0F, 16.0F, 1.0F, deformation, 1.0F, 0.5F),
                               PartPose.offset(0.0F, 24.0F, 2.0F));
        if (slim) {
            root.addOrReplaceChild("left_arm", CubeListBuilderEv.create().requestFix()
                                                                .texOffs(32, 48)
                                                                .addBox(-2.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F, deformation),
                                   PartPose.offset(-5.0F, 22.0F, 0.0F));
            root.addOrReplaceChild("right_arm", CubeListBuilderEv.create().requestFix()
                                                                 .texOffs(40, 16)
                                                                 .addBox(-1.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F, deformation),
                                   PartPose.offset(5.0F, 22.0F, 0.0F));
            root.addOrReplaceChild("left_sleeve", CubeListBuilderEv.create().requestFix()
                                                                   .texOffs(48, 48)
                                                                   .addBox(-2.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F,
                                                                           deformation.extend(0.25F)),
                                   PartPose.offset(-5.0F, 22.0F, 0.0F));
            root.addOrReplaceChild("right_sleeve", CubeListBuilderEv.create().requestFix()
                                                                    .texOffs(40, 32)
                                                                    .addBox(-1.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F,
                                                                            deformation.extend(0.25F)),
                                   PartPose.offset(5.0F, 22.0F, 0.0F));
        }
        else {
            root.addOrReplaceChild("left_arm", CubeListBuilderEv.create().requestFix()
                                                                .texOffs(32, 48)
                                                                .addBox(-3.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                   PartPose.offset(-5.0F, 22.0F, 0.0F));
            root.addOrReplaceChild("left_sleeve", CubeListBuilderEv.create().requestFix()
                                                                   .texOffs(48, 48)
                                                                   .addBox(-3.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F,
                                                                           deformation.extend(0.25F)),
                                   PartPose.offset(-5.0F, 22.0F, 0.0F));
            root.addOrReplaceChild("right_sleeve", CubeListBuilderEv.create().requestFix()
                                                                    .texOffs(40, 32)
                                                                    .addBox(-1.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F,
                                                                            deformation.extend(0.25F)),
                                   PartPose.offset(5.0F, 22.0F, 0.0F));
        }
        root.addOrReplaceChild("left_leg",
                               CubeListBuilderEv.create().requestFix().texOffs(16, 48).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                               PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_pants", CubeListBuilderEv.create().requestFix()
                                                              .texOffs(0, 48)
                                                              .addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(0.25F)),
                               PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("right_pants", CubeListBuilderEv.create().requestFix()
                                                               .texOffs(0, 32)
                                                               .addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(0.25F)),
                               PartPose.offset(1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("jacket", CubeListBuilderEv.create().requestFix()
                                                          .texOffs(16, 32)
                                                          .addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, deformation.extend(0.25F)),
                               PartPose.offset(0.0F, 24.0F, 0.0F));
        return meshDef;
    }

    @Override
    public HM cape() {
        return (HM) (Object) this.cloak;
    }

    @Override
    public HM clothesArmL() {
        return (HM) (Object) this.leftSleeve;
    }

    @Override
    public HM clothesArmR() {
        return (HM) (Object) this.rightSleeve;
    }

    @Override
    public HM clothesBody() {
        return (HM) (Object) this.jacket;
    }

    @Override
    public HM clothesLegL() {
        return (HM) (Object) this.leftPants;
    }

    @Override
    public HM clothesLegR() {
        return (HM) (Object) this.rightPants;
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
}
