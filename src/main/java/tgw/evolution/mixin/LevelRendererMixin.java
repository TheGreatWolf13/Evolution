package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.ILevelRendererPatch;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements ILevelRendererPatch {

    @Shadow
    @Nullable
    private PostChain entityEffect;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private RenderBuffers renderBuffers;
    private boolean shouldProcessEntityEffects;

    @Override
    public RenderBuffers getRenderBuffers() {
        return this.renderBuffers;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack" +
                                                                        "(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0))
    private void onRenderLevel0(PoseStack matrices,
                                float partialTicks,
                                long finishTimeNano,
                                boolean drawBlockOutline,
                                Camera camera,
                                GameRenderer gameRenderer,
                                LightTexture lightTexture,
                                Matrix4f proj,
                                CallbackInfo ci) {
        Entity entity = camera.getEntity();
        this.shouldProcessEntityEffects = false;
        if (!EvolutionConfig.CLIENT.firstPersonRenderer.get() ||
            camera.isDetached() ||
            !entity.isAlive() ||
            entity instanceof LivingEntity living && living.isSleeping()) {
            return;
        }
        Vec3 vec3d = camera.getPosition();
        double x = vec3d.x();
        double y = vec3d.y();
        double z = vec3d.z();
        MultiBufferSource buffer;
        if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(entity)) {
            this.shouldProcessEntityEffects = true;
            OutlineBufferSource outlineBuffer = this.renderBuffers.outlineBufferSource();
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

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush" +
                                                                               "(Ljava/lang/String;)V", args = {"ldc=destroyProgress"}))
    private void onRenderLevel1(PoseStack matrices,
                                float partialTicks,
                                long finishTimeNano,
                                boolean drawBlockOutline,
                                Camera camera,
                                GameRenderer gameRenderer,
                                LightTexture lightTexture,
                                Matrix4f proj,
                                CallbackInfo ci) {
        if (this.shouldProcessEntityEffects) {
            this.entityEffect.process(partialTicks);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getModelViewStack()" +
                                                                        "Lcom/mojang/blaze3d/vertex/PoseStack;", ordinal = 0))
    private void onRenderLevel2(PoseStack matrices,
                                float partialTicks,
                                long finishTimeNano,
                                boolean drawBlockOutline,
                                Camera camera,
                                GameRenderer gameRenderer,
                                LightTexture lightTexture,
                                Matrix4f proj,
                                CallbackInfo ci) {
        if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.MISS) {
            ForgeHooksClient.onDrawHighlight((LevelRenderer) (Object) this,
                                             camera,
                                             this.minecraft.hitResult,
                                             partialTicks,
                                             matrices,
                                             this.renderBuffers.bufferSource());
        }
    }

    @Shadow
    protected abstract void renderEntity(Entity p_109518_,
                                         double p_109519_,
                                         double p_109520_,
                                         double p_109521_,
                                         float p_109522_,
                                         PoseStack p_109523_,
                                         MultiBufferSource p_109524_);

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;process(F)V", ordinal = 0))
    private void renderLevelProxy0(PostChain shaderGroup, float partialTicks) {
        this.shouldProcessEntityEffects = true;
    }

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;bindWrite(Z)V", ordinal = 2))
    private void renderLevelProxy1(RenderTarget buffer, boolean state) {
    }

    @Shadow
    protected abstract boolean shouldShowEntityOutlines();
}
