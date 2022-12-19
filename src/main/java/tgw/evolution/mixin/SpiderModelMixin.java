package tgw.evolution.mixin;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.models.CubeListBuilderEv;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.LegacyHMSpider;

@Mixin(SpiderModel.class)
public abstract class SpiderModelMixin<T extends Entity> extends HierarchicalModel<T> implements LegacyHMSpider<T> {

    @Shadow
    @Final
    private ModelPart head;

    @Shadow
    @Final
    private ModelPart leftFrontLeg;
    @Shadow
    @Final
    private ModelPart leftHindLeg;
    @Shadow
    @Final
    private ModelPart leftMiddleFrontLeg;
    @Shadow
    @Final
    private ModelPart leftMiddleHindLeg;
    @Shadow
    @Final
    private ModelPart rightFrontLeg;
    @Shadow
    @Final
    private ModelPart rightHindLeg;
    @Shadow
    @Final
    private ModelPart rightMiddleFrontLeg;
    @Shadow
    @Final
    private ModelPart rightMiddleHindLeg;

    /**
     * @author TheGreatWolf
     * @reason Fix HMs.
     */
    @Overwrite
    public static LayerDefinition createSpiderBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilderEv.create().texOffs(32, 4).requestFix().addBox(-4, -4, -8, 8, 8, 8),
                               PartPose.offset(0, 9, -3));
        root.addOrReplaceChild("body0", CubeListBuilderEv.create().texOffs(0, 0).requestFix().addBox(-3, -3, -3, 6, 6, 6), PartPose.offset(0, 9, 0));
        root.addOrReplaceChild("body1", CubeListBuilderEv.create().texOffs(0, 12).requestFix().addBox(-5, -4, -6, 10, 8, 12),
                               PartPose.offset(0, 9, 9));
        CubeListBuilder legR = CubeListBuilderEv.create().texOffs(18, 0).requestFix().addBox(-1, -1, -1, 16, 2, 2);
        CubeListBuilder legL = CubeListBuilderEv.create().texOffs(18, 0).requestFix().addBox(-15, -1, -1, 16, 2, 2);
        root.addOrReplaceChild("right_hind_leg", legR, PartPose.offset(4, 9, 1));
        root.addOrReplaceChild("left_hind_leg", legL, PartPose.offset(-4, 9, 1));
        root.addOrReplaceChild("right_middle_hind_leg", legR, PartPose.offset(4, 9, 0));
        root.addOrReplaceChild("left_middle_hind_leg", legL, PartPose.offset(-4, 9, 0));
        root.addOrReplaceChild("right_middle_front_leg", legR, PartPose.offset(4, 9, -1));
        root.addOrReplaceChild("left_middle_front_leg", legL, PartPose.offset(-4, 9, -1));
        root.addOrReplaceChild("right_front_leg", legR, PartPose.offset(4, 9, -2));
        root.addOrReplaceChild("left_front_leg", legL, PartPose.offset(-4, 9, -2));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public HM head() {
        return (HM) (Object) this.head;
    }

    @Override
    public HM legFL() {
        return (HM) (Object) this.leftFrontLeg;
    }

    @Override
    public HM legFML() {
        return (HM) (Object) this.leftMiddleFrontLeg;
    }

    @Override
    public HM legFMR() {
        return (HM) (Object) this.rightMiddleFrontLeg;
    }

    @Override
    public HM legFR() {
        return (HM) (Object) this.rightFrontLeg;
    }

    @Override
    public HM legHL() {
        return (HM) (Object) this.leftHindLeg;
    }

    @Override
    public HM legHML() {
        return (HM) (Object) this.leftMiddleHindLeg;
    }

    @Override
    public HM legHMR() {
        return (HM) (Object) this.rightMiddleHindLeg;
    }

    @Override
    public HM legHR() {
        return (HM) (Object) this.rightHindLeg;
    }

    /**
     * @author TheGreatWolf
     * @reason Use HMs.
     */
    @Override
    @Overwrite
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.setup(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
