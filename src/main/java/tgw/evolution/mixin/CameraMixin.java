package tgw.evolution.mixin;

import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.obj.NearPlane;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3d;

@Mixin(Camera.class)
public abstract class CameraMixin {

    private final NearPlane nearPlane = new NearPlane();
    @Shadow
    @Final
    private BlockPos.MutableBlockPos blockPosition;
    @Shadow
    @Final
    private Vector3f forwards;
    @Shadow
    private boolean initialized;
    @Shadow
    @Final
    private Vector3f left;
    @Shadow
    private BlockGetter level;
    @Shadow
    private Vec3 position;
    @Shadow
    @Final
    private Vector3f up;

    public FogType getFluidInCamera() {
        if (!this.initialized) {
            return FogType.NONE;
        }
        FluidState fluidState = this.level.getFluidState(this.blockPosition);
        if (fluidState.is(FluidTags.WATER) && this.position.y < this.blockPosition.getY() + fluidState.getHeight(this.level, this.blockPosition)) {
            return FogType.WATER;
        }
        NearPlane nearPlane = this.setupAndGetNearPlane();
        return nearPlane.getFogType(this.position, this.level);
    }

    @Shadow
    public abstract Camera.NearPlane getNearPlane();

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", ordinal = 0))
    private void onSetup(BlockGetter world,
                         Entity renderViewEntity,
                         boolean thirdPerson,
                         boolean thirdPersonReverse,
                         float partialTicks,
                         CallbackInfo ci) {
        if (thirdPerson) {
            this.setPosition(Mth.lerp(partialTicks, renderViewEntity.xo, renderViewEntity.getX()),
                             Mth.lerp(partialTicks, renderViewEntity.yo, renderViewEntity.getY()) + renderViewEntity.getEyeHeight(),
                             Mth.lerp(partialTicks, renderViewEntity.zo, renderViewEntity.getZ()));
        }
        else {
            Vec3d cameraPos = ClientEvents.getInstance().getCameraPos();
            if (cameraPos.isNull()) {
                cameraPos = MathHelper.getCameraPosition(renderViewEntity, partialTicks);
            }
            this.setPosition(cameraPos.x(), cameraPos.y(), cameraPos.z());
        }
    }

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    private NearPlane setupAndGetNearPlane() {
        Minecraft minecraft = Minecraft.getInstance();
        double aspectRatio = minecraft.getWindow().getWidth() / (double) minecraft.getWindow().getHeight();
        double d1 = Math.tan(minecraft.options.fov * (Mth.PI / 180.0F) / 2.0) * 0.05F;
        double d2 = d1 * aspectRatio;
        this.nearPlane.setup(this.forwards, 0.05, this.left, d2, this.up, d1);
        return this.nearPlane;
    }

    @Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", ordinal = 0))
    private void updateProxy(Camera renderInfo, double x, double y, double z) {
        //Do nothing, as we already did
    }
}
