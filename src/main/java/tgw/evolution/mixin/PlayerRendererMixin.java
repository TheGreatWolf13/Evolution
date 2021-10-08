package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

    public PlayerRendererMixin(EntityRendererManager rendererManager, PlayerModel<AbstractClientPlayerEntity> entityModel, float shadowSize) {
        super(rendererManager, entityModel, shadowSize);
    }

    @Inject(method = "scale", at = @At("TAIL"))
    private void onScale(AbstractClientPlayerEntity player, MatrixStack matrices, float partialTicks, CallbackInfo ci) {
        switch (player.getPose()) {
            case STANDING:
            case CROUCHING: {
                matrices.translate(0, 0, 1 / 16.0);
                break;
            }
            case SWIMMING: {
                if (!player.isInWater()) {
                    matrices.translate(0, 9 / 16.0, -0.5 / 16.0);
                }
                break;
            }
        }
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to change overlay to render hurt effect.
     */
    @Overwrite
    private void renderHand(MatrixStack matrices,
                            IRenderTypeBuffer buffer,
                            int packedLight,
                            AbstractClientPlayerEntity player,
                            ModelRenderer arm,
                            ModelRenderer armwear) {
        PlayerModel<AbstractClientPlayerEntity> playerModel = this.getModel();
        this.setModelProperties(player);
        playerModel.attackTime = 0.0F;
        playerModel.crouching = false;
        playerModel.swimAmount = 0.0F;
        playerModel.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        arm.xRot = 0.0F;
        arm.render(matrices,
                   buffer.getBuffer(RenderType.entitySolid(player.getSkinTextureLocation())),
                   packedLight,
                   LivingRenderer.getOverlayCoords(player, 0));
        armwear.xRot = 0.0F;
        armwear.render(matrices,
                       buffer.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation())),
                       packedLight,
                       LivingRenderer.getOverlayCoords(player, 0));
    }

    @Shadow
    protected abstract void setModelProperties(AbstractClientPlayerEntity p_177137_1_);

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to improve first person camera.
     */
    @Override
    @Overwrite
    protected void setupRotations(AbstractClientPlayerEntity player, MatrixStack matrices, float ageInTicks, float rotationYaw, float partialTicks) {
        float swimAmount = player.getSwimAmount(partialTicks);
        if (player.isFallFlying()) {
            super.setupRotations(player, matrices, ageInTicks, rotationYaw, partialTicks);
            float f1 = player.getFallFlyingTicks() + partialTicks;
            float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!player.isAutoSpinAttack()) {
                matrices.mulPose(Vector3f.XP.rotationDegrees(f2 * (-90.0F - player.xRot)));
            }
            Vector3d viewVec = player.getViewVector(partialTicks);
            Vector3d motion = player.getDeltaMovement();
            double horizMotionSqr = Entity.getHorizontalDistanceSqr(motion);
            double horizViewSqr = Entity.getHorizontalDistanceSqr(viewVec);
            if (horizMotionSqr > 0 && horizViewSqr > 0) {
                double d2 = (motion.x * viewVec.x + motion.z * viewVec.z) / Math.sqrt(horizMotionSqr * horizViewSqr);
                double d3 = motion.x * viewVec.z - motion.z * viewVec.x;
                matrices.mulPose(Vector3f.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
            }
        }
        else if (swimAmount > 0.0F) {
            super.setupRotations(player, matrices, ageInTicks, rotationYaw, partialTicks);
            float desiredXRot = player.isInWater() ? -90.0F - player.xRot : -90.0F;
            float interpXRot = MathHelper.lerp(swimAmount, 0.0F, desiredXRot);
            if (player.isVisuallySwimming()) {
                if (!player.isInWater()) {
                    //Crawling pose
                    matrices.mulPose(Vector3f.XP.rotationDegrees(interpXRot));
                    matrices.translate(0, -1, 0.3);
                }
                else {
                    //Swimming pose
                    matrices.translate(0, 0.4, 0);
                    matrices.mulPose(Vector3f.XP.rotationDegrees(interpXRot));
                    matrices.translate(0, -1.4, -0.25);
                }
            }
            else {
                matrices.mulPose(Vector3f.XP.rotationDegrees(interpXRot));
                matrices.translate(0, -1.3, 0);
            }
        }
        else {
            super.setupRotations(player, matrices, ageInTicks, rotationYaw, partialTicks);
        }
    }
}
