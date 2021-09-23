package tgw.evolution.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tgw.evolution.blocks.IClimbable;
import tgw.evolution.entities.IEntityPatch;
import tgw.evolution.entities.IEntityProperties;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin extends CapabilityProvider<Entity> implements IEntityProperties, IEntityPatch, INeckPosition {

    @Shadow
    public World level;
    @Shadow
    public float xRot;
    @Shadow
    public float xRotO;
    @Shadow
    public float yRot;
    @Shadow
    public float yRotO;
    protected int fireDamageImmunity;
    protected boolean hasCollidedOnX;
    protected boolean hasCollidedOnZ;
    @Shadow
    @Nullable
    private Entity vehicle;

    protected EntityMixin(Class<Entity> baseClass) {
        super(baseClass);
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hurt(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean baseTickProxy(Entity entity, DamageSource source, float amount) {
        if (this.fireDamageImmunity > 0) {
            return false;
        }
        return entity.hurt(EvolutionDamage.ON_FIRE, 2.5f);
    }

    @Shadow
    public abstract BlockPos blockPosition();

    @Override
    public double getBaseMass() {
        return 1;
    }

    @Override
    public float getCameraYOffset() {
        return 0;
    }

    @Override
    public float getCameraZOffset() {
        return 0;
    }

    @Override
    public int getFireDamageImmunity() {
        return this.fireDamageImmunity;
    }

    @Override
    public Vector3d getNeckPoint() {
        return Vector3d.ZERO;
    }

    @Shadow
    public abstract Pose getPose();

    @Shadow
    public abstract Vector3d getViewVector(float partialTicks);

    @Override
    public final boolean hasCollidedOnXAxis() {
        return this.hasCollidedOnX;
    }

    @Override
    public final boolean hasCollidedOnZAxis() {
        return this.hasCollidedOnZ;
    }

    @Shadow
    public abstract boolean isInWater();

    @Shadow
    public abstract boolean isOnGround();

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void onBaseTickPre(CallbackInfo ci) {
        if (this.fireDamageImmunity > 0) {
            this.fireDamageImmunity--;
        }
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundNBT;getShort(Ljava/lang/String;)S", ordinal = 0))
    private void onLoad(CompoundNBT nbt, CallbackInfo ci) {
        this.fireDamageImmunity = nbt.getByte("FireImmunity");
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/IProfiler;push(Ljava/lang/String;)V", ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onMove(MoverType type, Vector3d pos, CallbackInfo ci, Vector3d allowedMovement) {
        this.hasCollidedOnX = !MathHelper.epsilonEquals(pos.x, allowedMovement.x);
        this.hasCollidedOnZ = !MathHelper.epsilonEquals(pos.z, allowedMovement.z);
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundNBT;putShort(Ljava/lang/String;S)V", ordinal
            = 0))
    private void onSaveWithoutId(CompoundNBT nbt, CallbackInfoReturnable<CompoundNBT> cir) {
        nbt.putByte("FireImmunity", (byte) this.fireDamageImmunity);
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void onTurn(double yaw, double pitch, CallbackInfo ci) {
        if (((IMinecraftPatch) Minecraft.getInstance()).isMultiplayerPaused()) {
            ci.cancel();
            return;
        }
        if (!this.isOnGround()) {
            if ((Object) this instanceof LivingEntity) {
                boolean isPlayerFlying = (Object) this instanceof PlayerEntity && ((PlayerEntity) (Object) this).abilities.flying;
                if (((LivingEntity) (Object) this).onClimbable() && !isPlayerFlying) {
                    BlockState state = this.level.getBlockState(this.blockPosition());
                    Block block = state.getBlock();
                    if (block instanceof IClimbable) {
                        float sweepAngle = ((IClimbable) block).getSweepAngle();
                        Direction dir = ((IClimbable) block).getDirection(state);
                        double dPitch = pitch * 0.15;
                        this.xRot += dPitch;
                        double dYaw = yaw * 0.15;
                        this.yRot += dYaw;
                        float partialYaw = this.yRot % 360;
                        this.yRot -= partialYaw;
                        boolean wasNegative = partialYaw < 0;
                        if (wasNegative) {
                            partialYaw += 360;
                        }
                        if (partialYaw >= 180) {
                            partialYaw -= 360;
                        }
                        float newYaw = MathHelper.clampAngle(partialYaw, sweepAngle, dir);
                        if (partialYaw < 0) {
                            partialYaw += 360;
                        }
                        if (newYaw < 0) {
                            newYaw += 360;
                        }
                        if (dir.getAxis() == Direction.Axis.X) {
                            if (partialYaw - newYaw <= -180) {
                                partialYaw += 360;
                            }
                            else if (partialYaw - newYaw >= 180) {
                                newYaw += 360;
                            }
                        }
                        newYaw = (partialYaw + partialYaw + partialYaw + newYaw) / 4;
                        if (dir.getAxis() == Direction.Axis.X) {
                            if (newYaw >= 360) {
                                newYaw -= 360;
                            }
                        }
                        if (wasNegative) {
                            newYaw -= 360;
                        }
                        this.yRot += newYaw;
                        this.xRot = MathHelper.clamp(this.xRot, -90.0F, 90.0F);
                        this.xRotO += dPitch;
                        this.yRotO += dYaw;
                        partialYaw = this.yRotO % 360;
                        this.yRotO -= partialYaw;
                        wasNegative = partialYaw < 0;
                        if (wasNegative) {
                            partialYaw += 360;
                        }
                        if (partialYaw >= 180) {
                            partialYaw -= 360;
                        }
                        newYaw = MathHelper.clampAngle(partialYaw, sweepAngle, dir);
                        if (partialYaw < 0) {
                            partialYaw += 360;
                        }
                        if (newYaw < 0) {
                            newYaw += 360;
                        }
                        if (dir.getAxis() == Direction.Axis.X) {
                            if (partialYaw - newYaw <= -180) {
                                partialYaw += 360;
                            }
                            else if (partialYaw - newYaw >= 180) {
                                newYaw += 360;
                            }
                        }
                        newYaw = (partialYaw + partialYaw + partialYaw + newYaw) / 4;
                        if (dir.getAxis() == Direction.Axis.X) {
                            if (newYaw >= 360) {
                                newYaw -= 360;
                            }
                        }
                        if (wasNegative) {
                            newYaw -= 360;
                        }
                        this.yRotO += newYaw;
                        this.xRotO = MathHelper.clamp(this.xRotO, -90.0F, 90.0F);
                        if (this.vehicle != null) {
                            this.vehicle.onPassengerTurned((Entity) (Object) this);
                        }
                        ci.cancel();
                    }
                }
            }
        }
        else if (this.getPose() == Pose.SWIMMING && !this.isInWater()) {
            double dPitch = pitch * 0.15;
            this.xRot += dPitch;
            double dYaw = yaw * 0.15;
            this.yRot += dYaw;
            this.xRot = MathHelper.clamp(this.xRot, 0.0F, 90.0F);
            this.xRotO += dPitch;
            this.yRotO += dYaw;
            this.xRotO = MathHelper.clamp(this.xRotO, 0.0F, 90.0F);
            if (this.vehicle != null) {
                this.vehicle.onPassengerTurned((Entity) (Object) this);
            }
            ci.cancel();
        }
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle camera pos.
     */
    @Overwrite
    public RayTraceResult pick(double distance, float partialTicks, boolean checkFluids) {
        Vector3d camera = MathHelper.getCameraPosition((Entity) (Object) this, partialTicks);
        Vector3d viewVec = this.getViewVector(partialTicks);
        Vector3d to = camera.add(viewVec.x * distance, viewVec.y * distance, viewVec.z * distance);
        return this.level.clip(new RayTraceContext(camera,
                                                   to,
                                                   RayTraceContext.BlockMode.OUTLINE,
                                                   checkFluids ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE,
                                                   (Entity) (Object) this));
    }

    @Override
    public void setFireDamageImmunity(int immunity) {
        this.fireDamageImmunity = immunity;
    }
}
