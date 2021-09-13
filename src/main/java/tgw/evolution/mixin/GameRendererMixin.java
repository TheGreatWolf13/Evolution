package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.items.IOffhandAttackable;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.PlayerHelper;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * Modify the near clipping plane to improve first person camera.
     */
    @ModifyConstant(method = "getProjectionMatrix", constant = @Constant(floatValue = 0.05f))
    private float modifyGetProjectionMatrix(float original) {
        return 0.006_25f;
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void onApplyBobbing(MatrixStack matrices, float partialTicks, CallbackInfo ci) {
        if (EvolutionConfig.CLIENT.firstPersonRenderer.get()) {
            ci.cancel();
        }
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle Evolution's hitboxes and reach distance.
     */
    @Overwrite
    public void pick(float partialTicks) {
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null) {
            //noinspection VariableNotUsedInsideIf
            if (this.minecraft.level != null) {
                this.minecraft.getProfiler().push("pick");
                double reachDistance = this.minecraft.player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
                Vector3d cameraPos = MathHelper.getCameraPosition(entity, partialTicks);
                ClientEvents.getInstance().setCameraPos(cameraPos);
                this.minecraft.hitResult = MathHelper.rayTraceBlocksFromCamera(entity, cameraPos, partialTicks, reachDistance, false);
                if (this.minecraft.hitResult.getType() == RayTraceResult.Type.BLOCK) {
                    reachDistance = cameraPos.distanceTo(this.minecraft.hitResult.getLocation());
                }
                EntityRayTraceResult leftRayTrace = MathHelper.rayTraceOBBEntityFromEyes(this.minecraft.player,
                                                                                         cameraPos,
                                                                                         partialTicks,
                                                                                         reachDistance);
                if (leftRayTrace != null) {
                    this.minecraft.hitResult = leftRayTrace;
                    this.minecraft.crosshairPickEntity = leftRayTrace.getEntity();
                    ClientEvents.getInstance().leftRayTrace = leftRayTrace;
                    ClientEvents.getInstance().leftPointedEntity = leftRayTrace.getEntity();
                }
                else {
                    this.minecraft.crosshairPickEntity = null;
                    ClientEvents.getInstance().leftRayTrace = null;
                    ClientEvents.getInstance().leftPointedEntity = null;
                }
                if (this.minecraft.player.getOffhandItem().getItem() instanceof IOffhandAttackable) {
                    EntityRayTraceResult rightRayTrace = MathHelper.rayTraceOBBEntityFromEyes(this.minecraft.player,
                                                                                              cameraPos,
                                                                                              partialTicks,
                                                                                              Math.min(reachDistance,
                                                                                                       ((IOffhandAttackable) this.minecraft.player.getOffhandItem()
                                                                                                                                                  .getItem()).getReach() +
                                                                                                       PlayerHelper.REACH_DISTANCE));
                    ClientEvents.getInstance().rightPointedEntity = rightRayTrace == null ? null : rightRayTrace.getEntity();
                    ClientEvents.getInstance().rightRayTrace = rightRayTrace;
                }
                else {
                    ClientEvents.getInstance().rightRayTrace = null;
                    ClientEvents.getInstance().rightPointedEntity = null;
                }
                this.minecraft.getProfiler().pop();
            }
        }
    }
}
