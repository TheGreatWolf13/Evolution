package tgw.evolution.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardListenerMixin {

    @Redirect(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;togglePostEffect()V"))
    private void onKeyPress(GameRenderer gameRenderer) {
        //Do nothing. Disables the ability to remove shaders by pressing F4.
    }
}
