package tgw.evolution.mixin;

import net.minecraft.block.WoodType;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.model.RenderMaterial;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Atlases.class)
public abstract class AtlasesMixin {

    @Shadow
    @Final
    public static Map<WoodType, RenderMaterial> SIGN_MATERIALS;

    // Instantiating a RenderMaterial every time a sign tries to grab a texture identifier causes a significant
    // performance impact as no RenderLayer will ever be cached for the sprite. Minecraft already maintains a
    // WoodType -> RenderMaterial cache but for some reason doesn't use it.
    @Inject(method = "signTexture", at = @At("HEAD"), cancellable = true)
    private static void preGetSignTextureId(WoodType type, CallbackInfoReturnable<RenderMaterial> ci) {
        if (SIGN_MATERIALS != null) {
            RenderMaterial sprite = SIGN_MATERIALS.get(type);
            //noinspection VariableNotUsedInsideIf
            if (type != null) {
                ci.setReturnValue(sprite);
            }
        }
    }
}
