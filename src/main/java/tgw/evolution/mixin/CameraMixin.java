package tgw.evolution.mixin;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.math.MathHelper;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", ordinal = 0))
    private void onSetup(BlockGetter world,
                         Entity renderViewEntity,
                         boolean thirdPerson,
                         boolean thirdPersonReverse,
                         float partialTicks,
                         CallbackInfo ci) {
        Vec3 cameraPos = ClientEvents.getInstance().getCameraPos();
        if (cameraPos == null) {
            if (thirdPerson) {
                this.setPosition(Mth.lerp(partialTicks, renderViewEntity.xo, renderViewEntity.getX()),
                                 Mth.lerp(partialTicks, renderViewEntity.yo, renderViewEntity.getY()) + renderViewEntity.getEyeHeight(),
                                 Mth.lerp(partialTicks, renderViewEntity.zo, renderViewEntity.getZ()));
            }
            else {
                float yaw = renderViewEntity.getViewYRot(partialTicks);
                float pitch = renderViewEntity.getViewXRot(partialTicks);
                float cosBodyYaw;
                float sinBodyYaw;
                float sinYaw = MathHelper.sinDeg(yaw);
                float cosYaw = MathHelper.cosDeg(yaw);
                if (renderViewEntity instanceof LivingEntity living) {
                    float bodyYaw = MathHelper.lerpAngles(partialTicks, living.yBodyRotO, living.yBodyRot);
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
                Vec3 neckPoint = ((INeckPosition) renderViewEntity).getNeckPoint();
                float actualYOffset = yOffset * cosPitch - zOffset * sinPitch;
                float horizontalOffset = yOffset * sinPitch + zOffset * cosPitch;
                double x = Mth.lerp(partialTicks, renderViewEntity.xo, renderViewEntity.getX()) - horizontalOffset * sinYaw +
                           neckPoint.x * cosBodyYaw - neckPoint.z * sinBodyYaw;
                double y = Mth.lerp(partialTicks, renderViewEntity.yo, renderViewEntity.getY()) + neckPoint.y + actualYOffset;
                double z = Mth.lerp(partialTicks, renderViewEntity.zo, renderViewEntity.getZ()) +
                           horizontalOffset * cosYaw +
                           neckPoint.x * sinBodyYaw +
                           neckPoint.z * cosBodyYaw;
                this.setPosition(x, y, z);
            }
        }
        else {
            if (thirdPerson) {
                this.setPosition(Mth.lerp(partialTicks, renderViewEntity.xo, renderViewEntity.getX()),
                                 Mth.lerp(partialTicks, renderViewEntity.yo, renderViewEntity.getY()) + renderViewEntity.getEyeHeight(),
                                 Mth.lerp(partialTicks, renderViewEntity.zo, renderViewEntity.getZ()));
            }
            else {
                this.setPosition(cameraPos.x, cameraPos.y, cameraPos.z);
            }
        }
    }

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", ordinal = 0))
    private void updateProxy(Camera renderInfo, double x, double y, double z) {

    }
}
