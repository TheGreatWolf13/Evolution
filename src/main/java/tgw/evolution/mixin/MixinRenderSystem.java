package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.util.time.Time;

@Mixin(RenderSystem.class)
public abstract class MixinRenderSystem {

    @Shadow @Final public static Vector3f[] shaderLightDirections;
    @Shadow private static Matrix4f projectionMatrix;
    @Shadow private static PoseStack modelViewStack;
    @Shadow private static Matrix4f modelViewMatrix;
    @Shadow private static float shaderGameTime;

    @Overwrite
    public static void _setShaderLights(Vector3f lightingVec0, Vector3f lightingVec1) {
        shaderLightDirections[0].set(lightingVec0.x(), lightingVec0.y(), lightingVec0.z());
        shaderLightDirections[1].set(lightingVec1.x(), lightingVec1.y(), lightingVec1.z());
    }

    @Overwrite(remap = false)
    public static void applyModelViewMatrix() {
        Matrix4f matrix = modelViewStack.last().pose();
        if (!isOnRenderThread()) {
            Matrix4f copy = matrix.copy();
            recordRenderCall(() -> modelViewMatrix.load(copy));
        }
        else {
            modelViewMatrix.load(matrix);
        }
    }

    @Contract(value = " -> _", pure = true)
    @Shadow(remap = false)
    public static boolean isOnRenderThread() {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        shaderLightDirections[0] = new Vector3f();
        shaderLightDirections[1] = new Vector3f();
    }

    @Contract(value = "_ -> _")
    @Shadow
    public static void recordRenderCall(RenderCall renderCall) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Overwrite
    public static void setProjectionMatrix(Matrix4f matrix) {
        if (!isOnRenderThread()) {
            Matrix4f copy = matrix.copy();
            recordRenderCall(() -> projectionMatrix = copy);
        }
        else {
            projectionMatrix = matrix;
        }
    }

    @Overwrite
    public static void setShaderGameTime(long l, float partialTicks) {
        float g = (l % Time.TICKS_PER_DAY + partialTicks) / Time.TICKS_PER_DAY;
        if (!isOnRenderThread()) {
            recordRenderCall(() -> shaderGameTime = g);
        }
        else {
            shaderGameTime = g;
        }
    }
}
