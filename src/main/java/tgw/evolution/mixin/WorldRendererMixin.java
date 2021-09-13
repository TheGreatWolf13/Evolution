package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ClientEvents;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    @Final
    private RenderTypeBuffers renderBuffers;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;checkPoseStack" +
                                                                        "(Lcom/mojang/blaze3d/matrix/MatrixStack;)V", ordinal = 0))
    private void onRenderLevel(MatrixStack matrices,
                               float partialTicks,
                               long finishTimeNano,
                               boolean drawBlockOutline,
                               ActiveRenderInfo renderInfo,
                               GameRenderer gameRenderer,
                               LightTexture lightTexture,
                               Matrix4f proj,
                               CallbackInfo ci) {
        if (!EvolutionConfig.CLIENT.firstPersonRenderer.get() ||
            renderInfo.isDetached() ||
            !renderInfo.getEntity().isAlive() ||
            renderInfo.getEntity() instanceof LivingEntity && ((LivingEntity) renderInfo.getEntity()).isSleeping()) {
            return;
        }
        Vector3d vec3d = renderInfo.getPosition();
        double x = vec3d.x();
        double y = vec3d.y();
        double z = vec3d.z();
        IRenderTypeBuffer.Impl buffer = this.renderBuffers.bufferSource();
        ClientEvents.getInstance().getRenderer().isRenderingPlayer = true;
        this.renderEntity(renderInfo.getEntity(), x, y, z, partialTicks, matrices, buffer);
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
}
