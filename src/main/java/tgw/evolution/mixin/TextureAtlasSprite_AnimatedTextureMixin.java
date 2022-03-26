package tgw.evolution.mixin;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.config.EvolutionConfig;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(TextureAtlasSprite.AnimatedTexture.class)
public abstract class TextureAtlasSprite_AnimatedTextureMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void preTick(CallbackInfo ci) {
        if (!EvolutionConfig.CLIENT.animatedTextures.get()) {
            ci.cancel();
        }
    }
}
