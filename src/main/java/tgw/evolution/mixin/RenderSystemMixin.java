package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {

    @Shadow
    @Final
    public static Vector3f[] shaderLightDirections;

    /**
     * @author TheGreatWolf
     * @reason Prevent vector leaks.
     */
    @Overwrite
    public static void _setShaderLights(Vector3f lightingVec0, Vector3f lightingVec1) {
        shaderLightDirections[0].set(lightingVec0.x(), lightingVec0.y(), lightingVec0.z());
        shaderLightDirections[1].set(lightingVec1.x(), lightingVec1.y(), lightingVec1.z());
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        shaderLightDirections[0] = new Vector3f();
        shaderLightDirections[1] = new Vector3f();
    }
}
