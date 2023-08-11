package tgw.evolution.client.models.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.client.models.CubeListBuilderEv;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.HMPlayer;

import java.util.random.RandomGenerator;

public class ModelPlayer<T extends LivingEntity> extends ModelHumanoid<T> implements HMPlayer<T> {

    public final ModelPart clothesArmL;
    public final ModelPart clothesArmR;
    public final ModelPart clothesBody;
    public final ModelPart clothesForearmL;
    public final ModelPart clothesForearmR;
    public final ModelPart clothesForelegL;
    public final ModelPart clothesForelegR;
    public final ModelPart clothesLegL;
    public final ModelPart clothesLegR;
    private final ModelPart cape;
    private final OList<ModelPart> parts;
    private final boolean slim;

    public ModelPlayer(ModelPart root, boolean slim) {
        super(root, RenderHelper.RENDER_TYPE_ENTITY_TRANSLUCENT);
        this.slim = slim;
        this.clothesBody = root.getChild("clothes_body");
        this.clothesBody.shouldRenderChildrenEvenWhenNotVisible(true);
        this.clothesArmL = this.clothesBody.getChild("clothes_arm_l");
        this.clothesArmR = this.clothesBody.getChild("clothes_arm_r");
        this.clothesLegL = this.clothesBody.getChild("clothes_leg_l");
        this.clothesLegR = this.clothesBody.getChild("clothes_leg_r");
        this.clothesForearmL = this.clothesArmL.getChild("clothes_forearm_l");
        this.clothesForearmR = this.clothesArmR.getChild("clothes_forearm_r");
        this.clothesForelegL = this.clothesLegL.getChild("clothes_foreleg_l");
        this.clothesForelegR = this.clothesLegR.getChild("clothes_foreleg_r");
        this.cape = root.getChild("cape");
        this.parts = root.getAllParts().collect(OArrayList::new, OArrayList::add, OArrayList::addAll);
        this.bodyParts.add(this.clothesBody);
        this.bodyParts.trimCollection();
    }

    public static MeshDefinition createMesh(CubeDeformation def, boolean slim) {
        CubeDeformation clothesDef = def.extend(0.25F);
        CubeDeformation legsDef = def.extend(0.24F);
        MeshDefinition mesh = ModelHumanoid.createMesh(def, 0);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        root.addOrReplaceChild("cape", CubeListBuilderEv.create().requestFix().texOffs(0, 0).addBox(-5, -16, 0, 10, 16, 1, def, 1, 0.5F), PartPose.offset(0, 24, 2));
        PartDefinition jacket = root.addOrReplaceChild("clothes_body", CubeListBuilderEv.create().requestFix().texOffs(16, 32).addBox(-4, -12, -2, 8, 12, 4, clothesDef), PartPose.offset(0, 24, 0));
        PartPose leftArmPoint = PartPose.offset(-5, -2, 0);
        PartPose rightArmPoint = PartPose.offset(5, -2, 0);
        if (slim) {
            PartPose leftForearmPoint = PartPose.offset(0, -4, 0);
            PartPose rightForearmPoint = PartPose.offset(0, -4, 0);
            PartDefinition armL = body.addOrReplaceChild("arm_l", CubeListBuilderEv.create().texOffs(32, 48).addBoxBend(-2, -4, -2, 3, 6, 4, def, true), leftArmPoint);
            armL.addOrReplaceChild("forearm_l", CubeListBuilderEv.create().texOffs(32, 54).addBoxBend(-2, -6, -2, 3, 6, 4, def, false), leftForearmPoint);
            PartDefinition armR = body.addOrReplaceChild("arm_r", CubeListBuilderEv.create().texOffs(40, 16).addBoxBend(-1, -4, -2, 3, 6, 4, def, true), rightArmPoint);
            armR.addOrReplaceChild("forearm_r", CubeListBuilderEv.create().texOffs(40, 22).addBoxBend(-1, -6, -2, 3, 6, 4, def, false), rightForearmPoint);
            PartDefinition clothesArmL = jacket.addOrReplaceChild("clothes_arm_l", CubeListBuilderEv.create().texOffs(48, 48).addBoxBend(-2, -4, -2, 3, 6, 4, clothesDef, true), leftArmPoint);
            clothesArmL.addOrReplaceChild("clothes_forearm_l", CubeListBuilderEv.create().texOffs(48, 52).addBoxBend(-2, -6, -2, 3, 6, 4, clothesDef, false), leftForearmPoint);
            PartDefinition clothesArmR = jacket.addOrReplaceChild("clothes_arm_r", CubeListBuilderEv.create().texOffs(40, 32).addBoxBend(-1, -4, -2, 3, 6, 4, clothesDef, true), rightArmPoint);
            clothesArmR.addOrReplaceChild("clothes_forearm_r", CubeListBuilderEv.create().texOffs(40, 38).addBoxBend(-1, -6, -2, 3, 6, 4, clothesDef, false), rightForearmPoint);
        }
        else {
            PartPose leftForearmPoint = PartPose.offset(-1, -4, 0);
            PartPose rightForearmPoint = PartPose.offset(1, -4, 0);
            PartDefinition armL = body.addOrReplaceChild("arm_l", CubeListBuilderEv.create().texOffs(32, 48).addBoxBend(-3, -4, -2, 4, 6, 4, def, true), leftArmPoint);
            armL.addOrReplaceChild("forearm_l", CubeListBuilderEv.create().texOffs(32, 54).addBoxBend(-2, -6, -2, 4, 6, 4, def, false), leftForearmPoint);
            PartDefinition clothesArmL = jacket.addOrReplaceChild("clothes_arm_l", CubeListBuilderEv.create().texOffs(48, 48).addBoxBend(-3, -4, -2, 4, 6, 4, clothesDef, true), leftArmPoint);
            clothesArmL.addOrReplaceChild("clothes_forearm_l", CubeListBuilderEv.create().texOffs(48, 54).addBoxBend(-2, -6, -2, 4, 6, 4, clothesDef, false), leftForearmPoint);
            PartDefinition clothesArmR = jacket.addOrReplaceChild("clothes_arm_r", CubeListBuilderEv.create().texOffs(40, 32).addBoxBend(-1, -4, -2, 4, 6, 4, clothesDef, true), rightArmPoint);
            clothesArmR.addOrReplaceChild("clothes_forearm_r", CubeListBuilderEv.create().texOffs(40, 38).addBoxBend(-2, -6, -2, 4, 6, 4, clothesDef, false), rightForearmPoint);
        }
        PartPose leftLegPoint = PartPose.offset(-1.9F, -12, 0);
        PartPose kneePoint = PartPose.offset(0, -6, 0);
        PartDefinition legL = body.addOrReplaceChild("leg_l", CubeListBuilderEv.create().texOffs(16, 48).addBoxBend(-2, -6, -2, 4, 6, 4, def, true), leftLegPoint);
        legL.addOrReplaceChild("foreleg_l", CubeListBuilderEv.create().texOffs(16, 54).addBoxBend(-2, -6, -2, 4, 6, 4, def, false), kneePoint);
        PartDefinition clothesLegL = jacket.addOrReplaceChild("clothes_leg_l", CubeListBuilderEv.create().texOffs(0, 48).addBoxBend(-2, -6, -2, 4, 6, 4, legsDef, true), leftLegPoint);
        clothesLegL.addOrReplaceChild("clothes_foreleg_l", CubeListBuilderEv.create().texOffs(0, 54).addBoxBend(-2, -6, -2, 4, 6, 4, legsDef, false), kneePoint);
        PartDefinition clothesLegR = jacket.addOrReplaceChild("clothes_leg_r", CubeListBuilderEv.create().texOffs(0, 32).addBoxBend(-2, -6, -2, 4, 6, 4, legsDef, true), PartPose.offset(1.9F, -12, 0));
        clothesLegR.addOrReplaceChild("clothes_foreleg_r", CubeListBuilderEv.create().texOffs(0, 38).addBoxBend(-2, -6, -2, 4, 6, 4, legsDef, false), kneePoint);
        return mesh;
    }

    @Override
    public HM cape() {
        return (HM) (Object) this.cape;
    }

    @Override
    public HM clothesArmL() {
        return (HM) (Object) this.clothesArmL;
    }

    @Override
    public HM clothesArmR() {
        return (HM) (Object) this.clothesArmR;
    }

    @Override
    public HM clothesBody() {
        return (HM) (Object) this.clothesBody;
    }

    @Override
    public HM clothesForearmL() {
        return (HM) (Object) this.clothesForearmL;
    }

    @Override
    public HM clothesForearmR() {
        return (HM) (Object) this.clothesForearmR;
    }

    @Override
    public HM clothesForelegL() {
        return (HM) (Object) this.clothesForelegL;
    }

    @Override
    public HM clothesForelegR() {
        return (HM) (Object) this.clothesForelegR;
    }

    @Override
    public HM clothesLegL() {
        return (HM) (Object) this.clothesLegL;
    }

    @Override
    public HM clothesLegR() {
        return (HM) (Object) this.clothesLegR;
    }

    public ModelPart getRandomModelPart(RandomGenerator random) {
        return this.parts.get(random.nextInt(this.parts.size()));
    }

    @Override
    public boolean isSlim() {
        return this.slim;
    }

    public void renderCape(PoseStack matrices, VertexConsumer buffer, int light, int overlay) {
        this.cape.render(matrices, buffer, light, overlay);
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);
        this.clothesArmL.visible = visible;
        this.clothesArmR.visible = visible;
        this.clothesLegL.visible = visible;
        this.clothesLegR.visible = visible;
        this.clothesBody.visible = visible;
        this.cape.visible = visible;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.setup(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack matrices) {
        this.body.translateAndRotate(matrices);
        this.getArm(side).translateAndRotate(matrices);
        this.getForearm(side).translateAndRotate(matrices);
        this.getItem(side).translateAndRotate(matrices);
    }
}
