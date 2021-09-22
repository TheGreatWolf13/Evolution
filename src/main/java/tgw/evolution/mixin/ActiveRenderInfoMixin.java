package tgw.evolution.mixin;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.MathHelper;

@Mixin(ActiveRenderInfo.class)
public abstract class ActiveRenderInfoMixin {

    @Shadow
    private Entity entity;
    @Shadow
    @Final
    private Vector3f forwards;
    @Shadow
    private IBlockReader level;
    @Shadow
    private Vector3d position;

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void onGetMaxZoom(double distance, CallbackInfoReturnable<Double> cir) {
        for (int i = 0; i < 8; ++i) {
            float f = (i & 1) * 2 - 1;
            float f1 = (i >> 1 & 1) * 2 - 1;
            float f2 = (i >> 2 & 1) * 2 - 1;
            f *= 0.1F;
            f1 *= 0.1F;
            f2 *= 0.1F;
            Vector3d from = this.position.add(f, f1, f2);
            //noinspection ObjectAllocationInLoop
            Vector3d to = new Vector3d(this.position.x - this.forwards.x() * distance + f + f2,
                                       this.position.y - this.forwards.y() * distance + f1,
                                       this.position.z - this.forwards.z() * distance + f2);
            //noinspection ObjectAllocationInLoop
            BlockRayTraceResult rayTrace = this.level.clip(new RayTraceContext(from,
                                                                               to,
                                                                               RayTraceContext.BlockMode.VISUAL,
                                                                               RayTraceContext.FluidMode.NONE,
                                                                               this.entity));
            if (rayTrace.getType() != RayTraceResult.Type.MISS && !rayTrace.isInside()) {
                double d0 = rayTrace.getLocation().distanceTo(this.position);
                if (d0 < distance) {
                    distance = d0;
                }
            }
        }
        cir.setReturnValue(distance);
    }

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;setPosition(DDD)V", ordinal = 0))
    private void onSetup(IBlockReader world,
                         Entity renderViewEntity,
                         boolean thirdPerson,
                         boolean thirdPersonReverse,
                         float partialTicks,
                         CallbackInfo ci) {
        Vector3d cameraPos = ClientEvents.getInstance().getCameraPos();
        if (cameraPos == null) {
            float yaw = renderViewEntity.getViewYRot(partialTicks);
            float pitch = renderViewEntity.getViewXRot(partialTicks);
            float cosBodyYaw;
            float sinBodyYaw;
            float sinYaw = MathHelper.sinDeg(yaw);
            float cosYaw = MathHelper.cosDeg(yaw);
            if (renderViewEntity instanceof LivingEntity) {
                float bodyYaw = MathHelper.lerpAngles(partialTicks,
                                                      ((LivingEntity) renderViewEntity).yBodyRotO,
                                                      ((LivingEntity) renderViewEntity).yBodyRot);
                cosBodyYaw = MathHelper.cosDeg(bodyYaw);
                sinBodyYaw = MathHelper.sinDeg(bodyYaw);
            }
            else {
                cosBodyYaw = cosYaw;
                sinBodyYaw = sinYaw;
            }
            float sinPitch = MathHelper.sinDeg(pitch);
            float cosPitch = MathHelper.cosDeg(pitch);
            float zOffset = ((INeckPosition) renderViewEntity).getCameraZOffset();
            float yOffset = ((INeckPosition) renderViewEntity).getCameraYOffset();
            Vector3d neckPoint = ((INeckPosition) renderViewEntity).getNeckPoint();
            float actualYOffset = yOffset * cosPitch - zOffset * sinPitch;
            float horizontalOffset = yOffset * sinPitch + zOffset * cosPitch;
            double x = MathHelper.lerp(partialTicks, renderViewEntity.xo, renderViewEntity.getX()) - horizontalOffset * sinYaw +
                       neckPoint.x * cosBodyYaw - neckPoint.z * sinBodyYaw;
            double y = MathHelper.lerp(partialTicks, renderViewEntity.yo, renderViewEntity.getY()) + neckPoint.y + actualYOffset;
            double z = MathHelper.lerp(partialTicks, renderViewEntity.zo, renderViewEntity.getZ()) +
                       horizontalOffset * cosYaw +
                       neckPoint.x * sinBodyYaw +
                       neckPoint.z * cosBodyYaw;
            this.setPosition(x, y, z);
        }
        else {
            this.setPosition(cameraPos.x, cameraPos.y, cameraPos.z);
        }
    }

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;setPosition(DDD)V", ordinal = 0))
    private void updateProxy(ActiveRenderInfo renderInfo, double x, double y, double z) {

    }
}
