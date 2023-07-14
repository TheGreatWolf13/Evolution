package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.util.math.Vec3d;

@Mixin(RemotePlayer.class)
public abstract class MixinRemotePlayer extends AbstractClientPlayer {

    @Unique private final Vec3d clientVelocity = new Vec3d();
    @Unique private int velocityLerpDivisor;

    public MixinRemotePlayer(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    /**
     * @author TheGreatWolf
     * @reason Add velocity interpolation
     */
    @Override
    @Overwrite
    public void aiStep() {
        if (this.lerpSteps > 0) {
            double newX = this.getX() + (this.lerpX - this.getX()) / this.lerpSteps;
            double newY = this.getY() + (this.lerpY - this.getY()) / this.lerpSteps;
            double newZ = this.getZ() + (this.lerpZ - this.getZ()) / this.lerpSteps;
            this.setYRot(this.getYRot() + (float) Mth.wrapDegrees(this.lerpYRot - this.getYRot()) / this.lerpSteps);
            this.setXRot(this.getXRot() + (float) (this.lerpXRot - this.getXRot()) / this.lerpSteps);
            --this.lerpSteps;
            this.setPos(newX, newY, newZ);
            this.setRot(this.getYRot(), this.getXRot());
        }
        if (this.lerpHeadSteps > 0) {
            this.yHeadRot += (float) (Mth.wrapDegrees(this.lyHeadRot - this.yHeadRot) / this.lerpHeadSteps);
            --this.lerpHeadSteps;
        }
        if (this.velocityLerpDivisor > 0) {
            Vec3 deltaMovement = this.getDeltaMovement();
            ((Vec3d) deltaMovement).addMutable((this.clientVelocity.x - deltaMovement.x) / this.velocityLerpDivisor,
                                               (this.clientVelocity.y - deltaMovement.y) / this.velocityLerpDivisor,
                                               (this.clientVelocity.z - deltaMovement.z) / this.velocityLerpDivisor);
            --this.velocityLerpDivisor;
        }
        this.oBob = this.bob;
        this.updateSwingTime();
        float dBob;
        if (this.onGround && !this.isDeadOrDying()) {
            dBob = (float) Math.min(0.1, this.getDeltaMovement().horizontalDistance());
        }
        else {
            dBob = 0.0F;
        }
        this.bob += (dBob - this.bob) * 0.4F;
        this.level.getProfiler().push("push");
        this.pushEntities();
        this.level.getProfiler().pop();
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        this.clientVelocity.set(x, y, z);
        this.velocityLerpDivisor = this.getType().updateInterval();
    }
}
