package tgw.evolution.mixin;

import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.client.models.CubeListBuilderEv;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.hitbox.hms.LegacyHMCreeper;

@Mixin(CreeperModel.class)
public abstract class CreeperModelMixin<T extends Entity> extends HierarchicalModel<T> implements LegacyHMCreeper<T> {

    @Shadow
    @Final
    private ModelPart head;

    @Mutable
    @Shadow
    @Final
    private ModelPart leftFrontLeg;

    @Mutable
    @Shadow
    @Final
    private ModelPart leftHindLeg;

    @Mutable
    @Shadow
    @Final
    private ModelPart rightFrontLeg;

    @Mutable
    @Shadow
    @Final
    private ModelPart rightHindLeg;

    /**
     * @author TheGreatWolf
     * @reason Fix HMs
     */
    @Overwrite
    public static LayerDefinition createBodyLayer(CubeDeformation def) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("head",
                               CubeListBuilderEv.create().requestFix().texOffs(0, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 8.0F, 8.0F, def),
                               PartPose.offset(0.0F, 18.0F, 0.0F));
        part.addOrReplaceChild("body",
                               CubeListBuilderEv.create().requestFix().texOffs(16, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, def),
                               PartPose.offset(0.0F, 18.0F, 0.0F));
        CubeListBuilder leg = CubeListBuilderEv.create().requestFix().texOffs(0, 16).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, def);
        part.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(2.0F, 6.0F, 4.0F));
        part.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(-2.0F, 6.0F, 4.0F));
        part.addOrReplaceChild("right_front_leg", leg, PartPose.offset(2.0F, 6.0F, -4.0F));
        part.addOrReplaceChild("left_front_leg", leg, PartPose.offset(-2.0F, 6.0F, -4.0F));
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
    public HM legFR() {
        return (HM) (Object) this.rightFrontLeg;
    }

    @Override
    public HM legHL() {
        return (HM) (Object) this.leftHindLeg;
    }

    @Override
    public HM legHR() {
        return (HM) (Object) this.rightHindLeg;
    }

    /**
     * Why the hell are the legs switched left and right in the original code? This cost me 2 hours.
     */
    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/CreeperModel;" +
                                                                    "rightFrontLeg:Lnet/minecraft/client/model/geom/ModelPart;", opcode =
            Opcodes.PUTFIELD))
    private void onInitLF(CreeperModel instance, ModelPart value) {
        this.leftFrontLeg = value;
    }

    /**
     * Why the hell are the legs switched left and right in the original code? This cost me 2 hours.
     */
    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/CreeperModel;" +
                                                                    "rightHindLeg:Lnet/minecraft/client/model/geom/ModelPart;", opcode =
            Opcodes.PUTFIELD))
    private void onInitLH(CreeperModel instance, ModelPart value) {
        this.leftHindLeg = value;
    }

    /**
     * Why the hell are the legs switched left and right in the original code? This cost me 2 hours.
     */
    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/CreeperModel;" +
                                                                    "leftFrontLeg:Lnet/minecraft/client/model/geom/ModelPart;", opcode =
            Opcodes.PUTFIELD))
    private void onInitRF(CreeperModel instance, ModelPart value) {
        this.rightFrontLeg = value;
    }

    /**
     * Why the hell are the legs switched left and right in the original code? This cost me 2 hours.
     */
    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/CreeperModel;" +
                                                                    "leftHindLeg:Lnet/minecraft/client/model/geom/ModelPart;", opcode =
            Opcodes.PUTFIELD))
    private void onInitRH(CreeperModel instance, ModelPart value) {
        this.rightHindLeg = value;
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
