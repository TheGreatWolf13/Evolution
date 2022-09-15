package tgw.evolution.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
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
        PartDefinition partDef = meshDef.getRoot();
        partDef.addOrReplaceChild("ear",
                                  CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, deformation),
                                  PartPose.ZERO);
        partDef.addOrReplaceChild("cloak", CubeListBuilder.create()
                                                          .texOffs(0, 0)
                                                          .addBox(-5.0F, -16.0F, 0.0F, 10.0F, 16.0F, 1.0F, deformation, 1.0F, 0.5F),
                                  PartPose.offset(0.0F, 24.0F, 2.0F));
        if (slim) {
            partDef.addOrReplaceChild("left_arm", CubeListBuilder.create()
                                                                 .texOffs(32, 48)
                                                                 .addBox(-2.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F, deformation),
                                      PartPose.offset(-5.0F, 22.0F, 0.0F));
            partDef.addOrReplaceChild("right_arm", CubeListBuilder.create()
                                                                  .texOffs(40, 16)
                                                                  .addBox(-1.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F, deformation),
                                      PartPose.offset(5.0F, 22.0F, 0.0F));
            partDef.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                                                                    .texOffs(48, 48)
                                                                    .addBox(-2.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F,
                                                                            deformation.extend(0.25F)),
                                      PartPose.offset(-5.0F, 22.0F, 0.0F));
            partDef.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                                                                     .texOffs(40, 32)
                                                                     .addBox(-1.0F, -10.0F, -2.0F, 3.0F, 12.0F, 4.0F,
                                                                             deformation.extend(0.25F)),
                                      PartPose.offset(5.0F, 22.0F, 0.0F));
        }
        else {
            partDef.addOrReplaceChild("left_arm", CubeListBuilder.create()
                                                                 .texOffs(32, 48)
                                                                 .addBox(-3.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                      PartPose.offset(-5.0F, 22.0F, 0.0F));
            partDef.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                                                                    .texOffs(48, 48)
                                                                    .addBox(-3.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F,
                                                                            deformation.extend(0.25F)),
                                      PartPose.offset(-5.0F, 22.0F, 0.0F));
            partDef.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                                                                     .texOffs(40, 32)
                                                                     .addBox(-1.0F, -10.0F, -2.0F, 4.0F, 12.0F, 4.0F,
                                                                             deformation.extend(0.25F)),
                                      PartPose.offset(5.0F, 22.0F, 0.0F));
        }
        partDef.addOrReplaceChild("left_leg",
                                  CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
                                  PartPose.offset(-1.9F, 12.0F, 0.0F));
        partDef.addOrReplaceChild("left_pants", CubeListBuilder.create()
                                                               .texOffs(0, 48)
                                                               .addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(0.25F)),
                                  PartPose.offset(-1.9F, 12.0F, 0.0F));
        partDef.addOrReplaceChild("right_pants", CubeListBuilder.create()
                                                                .texOffs(0, 32)
                                                                .addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation.extend(0.25F)),
                                  PartPose.offset(1.9F, 12.0F, 0.0F));
        partDef.addOrReplaceChild("jacket", CubeListBuilder.create()
                                                           .texOffs(16, 32)
                                                           .addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, deformation.extend(0.25F)),
                                  PartPose.offset(0.0F, 24.0F, 0.0F));
        return meshDef;
    }

    @Override
    public HM cloak() {
        return (HM) (Object) this.cloak;
    }

    @Override
    public HM ear() {
        return (HM) (Object) this.ear;
    }

    @Override
    public HM jacket() {
        return (HM) (Object) this.jacket;
    }

    @Override
    public HM leftPants() {
        return (HM) (Object) this.leftPants;
    }

    @Override
    public HM leftSleeve() {
        return (HM) (Object) this.leftSleeve;
    }

    @Override
    public HM rightPants() {
        return (HM) (Object) this.rightPants;
    }

    @Override
    public HM rightSleeve() {
        return (HM) (Object) this.rightSleeve;
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
