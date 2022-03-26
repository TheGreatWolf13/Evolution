package tgw.evolution.mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.init.EvolutionEffects;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

    public LivingEntityRendererMixin(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Inject(method = "isShaking", at = @At("HEAD"), cancellable = true)
    private void onIsShaking(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.hasEffect(EvolutionEffects.SHIVERING.get())) {
            cir.setReturnValue(true);
        }
    }
}
