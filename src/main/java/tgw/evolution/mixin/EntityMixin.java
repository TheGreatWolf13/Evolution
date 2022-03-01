package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
import tgw.evolution.entities.IEntityProperties;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.util.math.MathHelper;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin extends CapabilityProvider<Entity> implements IEntityProperties, IEntityPatch, INeckPosition {

    @Shadow
    public Level level;
    @Shadow
    public float xRotO;
    @Shadow
    public float yRotO;
    protected int fireDamageImmunity;
    protected boolean hasCollidedOnX;
    protected boolean hasCollidedOnZ;
    @Shadow
    @Nullable
    private Entity vehicle;
    @Shadow
    private float xRot;
    @Shadow
    private float yRot;

    protected EntityMixin(Class<Entity> baseClass) {
        super(baseClass);
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt" +
                                                                       "(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
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
    public Vec3 getNeckPoint() {
        return Vec3.ZERO;
    }

    @Shadow
    public abstract Pose getPose();

    @Shadow
    public abstract Vec3 getViewVector(float p_20253_);

    @Override
    public final boolean hasCollidedOnXAxis() {
        return this.hasCollidedOnX;
    }

    @Override
    public final boolean hasCollidedOnZAxis() {
        return this.hasCollidedOnZ;
    }

    @Shadow
    public abstract boolean hurt(DamageSource p_19946_, float p_19947_);

    @Override
    public boolean hurtInternal(DamageSource source, float damage) {
        return this.hurt(source, damage);
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

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getShort(Ljava/lang/String;)S", ordinal = 0))
    private void onLoad(CompoundTag nbt, CallbackInfo ci) {
        this.fireDamageImmunity = nbt.getByte("FireImmunity");
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", ordinal
            = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onMove(MoverType type, Vec3 pos, CallbackInfo ci, Vec3 allowedMovement) {
        this.hasCollidedOnX = !MathHelper.epsilonEquals(pos.x, allowedMovement.x);
        this.hasCollidedOnZ = !MathHelper.epsilonEquals(pos.z, allowedMovement.z);
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putShort(Ljava/lang/String;S)V", ordinal
            = 0))
    private void onSaveWithoutId(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        nbt.putByte("FireImmunity", (byte) this.fireDamageImmunity);
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void onTurn(double yaw, double pitch, CallbackInfo ci) {
        if (((IMinecraftPatch) Minecraft.getInstance()).isMultiplayerPaused()) {
            ci.cancel();
            return;
        }
        if (!this.isOnGround()) {
            if ((Object) this instanceof LivingEntity living) {
                boolean isPlayerFlying = (Object) this instanceof Player && ((Player) (Object) this).getAbilities().flying;
                if (living.onClimbable() && !isPlayerFlying) {
                    BlockState state = this.level.getBlockState(this.blockPosition());
                    Block block = state.getBlock();
                    if (block instanceof IClimbable climbable) {
                        float sweepAngle = climbable.getSweepAngle();
                        Direction dir = climbable.getDirection(state);
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
    public HitResult pick(double distance, float partialTicks, boolean checkFluids) {
        Vec3 camera = MathHelper.getCameraPosition((Entity) (Object) this, partialTicks);
        Vec3 viewVec = this.getViewVector(partialTicks);
        Vec3 to = camera.add(viewVec.x * distance, viewVec.y * distance, viewVec.z * distance);
        return this.level.clip(new ClipContext(camera,
                                               to,
                                               ClipContext.Block.OUTLINE,
                                               checkFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                                               (Entity) (Object) this));
    }

    @Override
    public void setFireDamageImmunity(int immunity) {
        this.fireDamageImmunity = immunity;
    }
}
