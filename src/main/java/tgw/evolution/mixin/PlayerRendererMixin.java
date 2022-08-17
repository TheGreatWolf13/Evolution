package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.client.layers.LayerBack;
import tgw.evolution.client.layers.LayerBelt;
import tgw.evolution.patches.IPoseStackPatch;
import tgw.evolution.util.math.MathHelper;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(EntityRendererProvider.Context p_174289_, PlayerModel<AbstractClientPlayer> p_174290_, float p_174291_) {
        super(p_174289_, p_174290_, p_174291_);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructor(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        this.addLayer(new LayerBelt(this));
        this.addLayer(new LayerBack(this));
    }

    @Inject(method = "scale(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("TAIL"))
    private void onScale(AbstractClientPlayer player, PoseStack matrices, float partialTicks, CallbackInfo ci) {
        switch (player.getPose()) {
            case STANDING, CROUCHING -> matrices.translate(0, 0, 1 / 16.0);
            case SWIMMING -> {
                if (!player.isInWater()) {
                    matrices.translate(0, 9 / 16.0, -0.5 / 16.0);
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Overwrite to change overlay to render hurt effect.
     */
    @Overwrite
    private void renderHand(PoseStack matrices,
                            MultiBufferSource buffer,
                            int packedLight,
                            AbstractClientPlayer player,
                            ModelPart arm,
                            ModelPart armwear) {
        PlayerModel<AbstractClientPlayer> playerModel = this.getModel();
        this.setModelProperties(player);
        playerModel.attackTime = 0.0F;
        playerModel.crouching = false;
        playerModel.swimAmount = 0.0F;
        playerModel.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        arm.xRot = 0.0F;
        arm.render(matrices, buffer.getBuffer(RenderType.entitySolid(player.getSkinTextureLocation())), packedLight,
                   LivingEntityRenderer.getOverlayCoords(player, 0));
        armwear.xRot = 0.0F;
        armwear.render(matrices, buffer.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation())), packedLight,
                       LivingEntityRenderer.getOverlayCoords(player, 0));
    }

    @Shadow
    protected abstract void setModelProperties(AbstractClientPlayer p_117819_);

    /**
     * @author TheGreatWolf
     * <p>
     * Overwrite to improve first person camera.
     */
    @Override
    @Overwrite
    protected void setupRotations(AbstractClientPlayer player, PoseStack matrices, float ageInTicks, float rotationYaw, float partialTicks) {
        float swimAmount = player.getSwimAmount(partialTicks);
        IPoseStackPatch matricesExt = MathHelper.getExtendedMatrix(matrices);
        if (player.isFallFlying()) {
            super.setupRotations(player, matrices, ageInTicks, rotationYaw, partialTicks);
            float f1 = player.getFallFlyingTicks() + partialTicks;
            float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!player.isAutoSpinAttack()) {
                matricesExt.mulPoseX(f2 * (-90.0F - player.getXRot()));
            }
            Vec3 viewVec = player.getViewVector(partialTicks);
            Vec3 motion = player.getDeltaMovement();
            double horizMotionSqr = motion.horizontalDistanceSqr();
            double horizViewSqr = viewVec.horizontalDistanceSqr();
            if (horizMotionSqr > 0 && horizViewSqr > 0) {
                double d2 = (motion.x * viewVec.x + motion.z * viewVec.z) / Math.sqrt(horizMotionSqr * horizViewSqr);
                double d3 = motion.x * viewVec.z - motion.z * viewVec.x;
                matricesExt.mulPoseYRad((float) (Math.signum(d3) * Math.acos(d2)));
            }
        }
        else if (swimAmount > 0.0F) {
            super.setupRotations(player, matrices, ageInTicks, rotationYaw, partialTicks);
            float desiredXRot = player.isInWater() ? -90.0F - player.getXRot() : -90.0F;
            float interpXRot = Mth.lerp(swimAmount, 0.0F, desiredXRot);
            if (player.isVisuallySwimming()) {
                if (!player.isInWater()) {
                    //Crawling pose
                    matricesExt.mulPoseX(interpXRot);
                    matrices.translate(0, -1, 0.3);
                }
                else {
                    //Swimming pose
                    matrices.translate(0, 0.4, 0);
                    matricesExt.mulPoseX(interpXRot);
                    matrices.translate(0, -1.4, -0.25);
                }
            }
            else {
                matricesExt.mulPoseX(interpXRot);
                matrices.translate(0, -1.3, 0);
            }
        }
        else {
            super.setupRotations(player, matrices, ageInTicks, rotationYaw, partialTicks);
        }
    }
}
