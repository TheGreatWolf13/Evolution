package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.models.CubeListBuilderEv;
import tgw.evolution.util.hitbox.hms.HMSkeleton;

@Mixin(SkeletonModel.class)
public abstract class SkeletonModelMixin<T extends Mob & RangedAttackMob> extends HumanoidModel<T> implements HMSkeleton<T> {

    public SkeletonModelMixin(ModelPart pRoot) {
        super(pRoot);
    }

    /**
     * @author TheGreatWolf
     * @reason Fix HMs.
     */
    @Overwrite
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_arm", CubeListBuilderEv.create().requestFix().texOffs(40, 16).addBox(-1, -10, -1, 2, 12, 2),
                               PartPose.offset(5, 22, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilderEv.create().requestFix().texOffs(40, 16).mirror().addBox(-1, -10, -1, 2, 12, 2),
                               PartPose.offset(-5, 22, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilderEv.create().requestFix().texOffs(0, 16).addBox(-1, -12, -1, 2, 12, 2),
                               PartPose.offset(2, 12, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilderEv.create().requestFix().texOffs(0, 16).mirror().addBox(-1, -12, -1, 2, 12, 2),
                               PartPose.offset(-2, 12, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /**
     * @author TheGreatWolf
     * @reason Use HMs.
     */
    @Override
    @Overwrite
    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        this.prepare(entity, limbSwing, limbSwingAmount, partialTick);
    }

    /**
     * @author TheGreatWolf
     * @reason Use HMs.
     */
    @Override
    @Overwrite
    public void setupAnim(T entity, float limbSwing, float limgSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.setup(entity, limbSwing, limgSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    /**
     * @author TheGreatWolf
     * @reason Fix HMs.
     */
    @Override
    @Overwrite
    public void translateToHand(HumanoidArm arm, PoseStack matrices) {
        float delta = arm == HumanoidArm.RIGHT ? -1.0F : 1.0F;
        ModelPart modelpart = this.getArm(arm);
        modelpart.x += delta;
        modelpart.translateAndRotate(matrices);
        modelpart.x -= delta;
    }
}
