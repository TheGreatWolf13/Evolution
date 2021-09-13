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
        matrices.translate(0, 0, 1 / 16.0);
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
}
