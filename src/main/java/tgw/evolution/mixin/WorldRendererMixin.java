package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ClientEvents;

import javax.annotation.Nullable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    @Nullable
    private ShaderGroup entityEffect;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private RenderTypeBuffers renderBuffers;
    private boolean shouldProcessEntityEffects;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/IProfiler;popPush(Ljava/lang/String;)V",
            args = {
            "ldc=destroyProgress"}))
    private void onRenderLevel(MatrixStack matrices,
                               float partialTicks,
                               long finishTimeNano,
                               boolean drawBlockOutline,
                               ActiveRenderInfo camera,
                               GameRenderer gameRenderer,
                               LightTexture lightTexture,
                               Matrix4f proj,
                               CallbackInfo ci) {
        if (this.shouldProcessEntityEffects) {
            this.entityEffect.process(partialTicks);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;checkPoseStack" +
                                                                        "(Lcom/mojang/blaze3d/matrix/MatrixStack;)V", ordinal = 0))
    private void onRenderLevel0(MatrixStack matrices,
                                float partialTicks,
                                long finishTimeNano,
                                boolean drawBlockOutline,
                                ActiveRenderInfo camera,
                                GameRenderer gameRenderer,
                                LightTexture lightTexture,
                                Matrix4f proj,
                                CallbackInfo ci) {
        Entity entity = camera.getEntity();
        this.shouldProcessEntityEffects = false;
        if (!EvolutionConfig.CLIENT.firstPersonRenderer.get() ||
            camera.isDetached() ||
            !entity.isAlive() ||
            entity instanceof LivingEntity && ((LivingEntity) entity).isSleeping()) {
            return;
        }
        Vector3d vec3d = camera.getPosition();
        double x = vec3d.x();
        double y = vec3d.y();
        double z = vec3d.z();
        IRenderTypeBuffer buffer;
        if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(entity)) {
            this.shouldProcessEntityEffects = true;
            OutlineLayerBuffer outlineBuffer = this.renderBuffers.outlineBufferSource();
            buffer = outlineBuffer;
            int teamColor = entity.getTeamColor();
            int red = teamColor >> 16 & 255;
            int green = teamColor >> 8 & 255;
            int blue = teamColor & 255;
            outlineBuffer.setColor(red, green, blue, 255);
        }
        else {
            buffer = this.renderBuffers.bufferSource();
        }
        ClientEvents.getInstance().getRenderer().isRenderingPlayer = true;
        this.renderEntity(entity, x, y, z, partialTicks, matrices, buffer);
        ClientEvents.getInstance().getRenderer().isRenderingPlayer = false;
    }

    @Shadow
    protected abstract void renderEntity(Entity entity,
                                         double camX,
                                         double camY,
                                         double camZ,
                                         float partialTicks,
                                         MatrixStack matrices,
                                         IRenderTypeBuffer buffer);

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/shader/ShaderGroup;process(F)V", ordinal = 0))
    private void renderLevelProxy0(ShaderGroup shaderGroup, float partialTicks) {
        this.shouldProcessEntityEffects = true;
    }

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/shader/Framebuffer;bindWrite(Z)V", ordinal = 2))
    private void renderLevelProxy1(Framebuffer buffer, boolean state) {
    }

    @Shadow
    protected abstract boolean shouldShowEntityOutlines();
}
