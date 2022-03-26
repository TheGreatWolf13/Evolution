package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.math.MathHelper;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel>
        extends ItemInHandLayer<T, M> {

    public PlayerItemInHandLayerMixin(RenderLayerParent<T, M> p_117183_) {
        super(p_117183_);
    }

    @Redirect(method = "renderArmWithSpyglass", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float inRenderArmWithSpyglass(float value, float min, float max) {
        return Mth.clamp(value, -MathHelper.PI_OVER_2, max);
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderArmWithItem(LivingEntity entity,
                                     ItemStack stack,
                                     ItemTransforms.TransformType transformType,
                                     HumanoidArm arm,
                                     PoseStack matrices,
                                     MultiBufferSource buffer,
                                     int light,
                                     CallbackInfo ci) {
        if (ClientEvents.getInstance().getRenderer().isRenderingPlayer) {
            switch (arm) {
                case RIGHT -> {
                    if (!ClientEvents.getInstance().getRenderer().shouldRenderRightArm) {
                        ci.cancel();
                    }
                }
                case LEFT -> {
                    if (!ClientEvents.getInstance().getRenderer().shouldRenderLeftArm) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
