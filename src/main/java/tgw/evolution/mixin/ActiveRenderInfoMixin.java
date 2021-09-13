package tgw.evolution.mixin;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.MathHelper;

@Mixin(ActiveRenderInfo.class)
public abstract class ActiveRenderInfoMixin {

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
