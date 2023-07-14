package tgw.evolution.mixin;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.models.CubeListBuilderEv;
import tgw.evolution.client.util.ModelUtils;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.LegacyHMVillager;

@Mixin(VillagerModel.class)
public abstract class MixinVillagerModel<T extends Entity> extends HierarchicalModel<T> implements LegacyHMVillager<T> {

    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart leftLeg;
    @Shadow @Final private ModelPart rightLeg;

    /**
     * @author TheGreatWolf
     * @reason Fix HMs.
     */
    @Overwrite
    public static MeshDefinition createBodyModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose neckPoint = PartPose.offset(0, 24, 0);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilderEv.create()
                                                                              .texOffs(0, 0)
                                                                              .requestFix()
                                                                              .addBox(-4, 0, -4, 8, 10, 8), neckPoint);
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilderEv.create()
                                                                            .texOffs(32, 0)
                                                                            .requestFix()
                                                                            .addBox(-4, 0, -4, 8, 10, 8, ModelUtils.DEF_05), PartPose.ZERO);
        hat.addOrReplaceChild("hat_rim", CubeListBuilderEv.create().texOffs(30, 47).requestFix().addBox(-8, -8, -6, 16, 16, 1),
                              PartPose.offsetAndRotation(0, 0, 0, 90 * Mth.DEG_TO_RAD, 0, 0));
        head.addOrReplaceChild("nose", CubeListBuilderEv.create().texOffs(24, 0).requestFix().addBox(-1, -1, -6, 2, 4, 2), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilderEv.create()
                                                                              .texOffs(16, 20)
                                                                              .requestFix()
                                                                              .addBox(-4, -12, -3, 8, 12, 6), neckPoint);
        body.addOrReplaceChild("jacket", CubeListBuilderEv.create()
                                                          .texOffs(0, 38)
                                                          .requestFix()
                                                          .addBox(-4, -18, -3, 8, 18, 6, ModelUtils.DEF_05), PartPose.ZERO);
        root.addOrReplaceChild("arms", CubeListBuilderEv.create()
                                                        .texOffs(44, 22)
                                                        .requestFix()
                                                        .addBox(-8, 2, -2, 4, 8, 4, true)
                                                        .texOffs(44, 22)
                                                        .addBox(4, 2, -2, 4, 8, 4)
                                                        .texOffs(40, 38)
                                                        .addBox(-4, 2, -2, 8, 4, 4),
                               PartPose.offsetAndRotation(0, 15.2f, -6.4f, 0.75F, 0, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilderEv.create().texOffs(0, 22).requestFix().addBox(-2, -12, -2, 4, 12, 4),
                               PartPose.offset(-2, 12, 0));
        root.addOrReplaceChild("left_leg",
                               CubeListBuilderEv.create().texOffs(0, 22).requestFix().mirror().addBox(-2, -12, -2, 4, 12, 4),
                               PartPose.offset(2, 12, 0));
        return mesh;
    }

    @Override
    public HM head() {
        return (HM) (Object) this.head;
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
     * @reason Fix HMs.
     */
    @Override
    @Overwrite
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.setup(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
