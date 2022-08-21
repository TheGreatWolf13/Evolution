package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
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
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.util.math.MathHelper;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin extends CapabilityProvider<Entity> implements IEntityProperties, IEntityPatch, INeckPosition {

    @Shadow
    public Level level;
    @Shadow
    public int tickCount;
    @Shadow
    public float xRotO;
    @Shadow
    public float yRotO;
    protected int fireDamageImmunity;
    @Shadow
    protected Object2DoubleMap<Tag<Fluid>> fluidHeight;
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

    @Shadow
    public abstract AABB getBoundingBox();

    @Override
    public float getCameraYOffset() {
        return 0;
    }

    @Override
    public float getCameraZOffset() {
        return 0;
    }

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Override
    public int getFireDamageImmunity() {
        return this.fireDamageImmunity;
    }

    @Override
    public Vec3 getNeckPoint() {
        return Vec3.ZERO;
    }

    @Shadow
    public abstract float getPickRadius();

    @Shadow
    public abstract Pose getPose();

    @Shadow
    public abstract Vec3 getViewVector(float p_20253_);

    @Shadow
    public abstract float getViewXRot(float pPartialTicks);

    @Shadow
    public abstract float getXRot();

    @Shadow
    public abstract float getYRot();

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

    @Shadow
    public abstract boolean isPushedByFluid();

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
        //Prevent moving camera on certain Special Attacks
        if (this instanceof ILivingEntityPatch patch) {
            if (patch.isCameraLocked()) {
                ci.cancel();
            }
        }
        //Climbable limit
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
     * @author TheGreatWolf
     * @reason Overwrite to handle camera pos.
     */
    @Overwrite
    public HitResult pick(double distance, float partialTicks, boolean checkFluids) {
        Vec3 camera = MathHelper.getCameraPosition((Entity) (Object) this, partialTicks);
        Vec3 viewVec = this.getViewVector(partialTicks);
        Vec3 to = camera.add(viewVec.x * distance, viewVec.y * distance, viewVec.z * distance);
        return this.level.clip(new ClipContext(camera, to, ClipContext.Block.OUTLINE, checkFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                                               (Entity) (Object) this));
    }

    @Shadow
    public abstract void setDeltaMovement(Vec3 pMotion);

    @Shadow
    public abstract void setDeltaMovement(double pX, double pY, double pZ);

    @Override
    public void setFireDamageImmunity(int immunity) {
        this.fireDamageImmunity = immunity;
    }

    @Shadow
    public abstract void setXRot(float pXRot);

    @Shadow
    public abstract void setYRot(float pYRot);

    @Shadow
    public abstract boolean touchingUnloadedChunk();

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations
     */
    @Overwrite
    public boolean updateFluidHeightAndDoFluidPushing(Tag<Fluid> fluid, double motionScale) {
        if (this.touchingUnloadedChunk()) {
            return false;
        }
        AABB aabb = this.getBoundingBox();
        int minX = Mth.floor(aabb.minX + 0.001);
        int maxX = Mth.ceil(aabb.maxX - 0.001);
        int minY = Mth.floor(aabb.minY + 0.001);
        int maxY = Mth.ceil(aabb.maxY - 0.001);
        int minZ = Mth.floor(aabb.minZ + 0.001);
        int maxZ = Mth.ceil(aabb.maxZ - 0.001);
        double unMinY = aabb.minY + 0.001;
        double height = 0.0;
        boolean pushedByFluid = this.isPushedByFluid();
        boolean isInFluid = false;
        //Vec3 flow = Vec3.ZERO;
        double flowX = 0.0;
        double flowY = 0.0;
        double flowZ = 0.0;
        int flowCount = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int dx = minX; dx < maxX; ++dx) {
            for (int dy = minY; dy < maxY; ++dy) {
                for (int dz = minZ; dz < maxZ; ++dz) {
                    mutableBlockPos.set(dx, dy, dz);
                    FluidState fluidState = this.level.getFluidState(mutableBlockPos);
                    if (fluidState.is(fluid)) {
                        double localHeight = dy + fluidState.getHeight(this.level, mutableBlockPos);
                        if (localHeight >= unMinY) {
                            isInFluid = true;
                            height = Math.max(localHeight - unMinY, height);
                            if (pushedByFluid) {
                                Vec3 localFlow = fluidState.getFlow(this.level, mutableBlockPos);
                                double localFlowX = localFlow.x;
                                double localFlowY = localFlow.y;
                                double localFlowZ = localFlow.z;
                                if (height < 0.4) {
                                    //localFlow = localFlow.scale(height);
                                    localFlowX *= height;
                                    localFlowY *= height;
                                    localFlowZ *= height;
                                }
                                //flow = flow.add(localFlow);
                                flowX += localFlowX;
                                flowY += localFlowY;
                                flowZ += localFlowZ;
                                ++flowCount;
                            }
                        }
                    }
                }
            }
        }
        //flow.lengthSqr(); (original is length() which computes a sqrt for no reason)
        if (flowX * flowX + flowY * flowY + flowZ * flowZ > 0.0) {
            if (flowCount > 0) {
                //flow = flow.scale(1.0 / flowCount);
                double scale = 1.0 / flowCount;
                flowX *= scale;
                flowY *= scale;
                flowZ *= scale;
            }
            if (!((Object) this instanceof Player)) {
                //flow = flow.normalize();
                double norm = Math.sqrt(flowX * flowX + flowY * flowY + flowZ * flowZ);
                if (norm < 1.0E-4) {
                    flowX = 0.0;
                    flowY = 0.0;
                    flowZ = 0.0;
                }
                else {
                    flowX /= norm;
                    flowY /= norm;
                    flowZ /= norm;
                }
            }
            Vec3 velocity = this.getDeltaMovement();
            //flow = flow.scale(motionScale);
            flowX *= motionScale;
            flowY *= motionScale;
            flowZ *= motionScale;
            //flow.lengthSqr; (original is length() which computes a sqrt for no reason)
            if (Math.abs(velocity.x) < 0.003 &&
                Math.abs(velocity.z) < 0.003 &&
                flowX * flowX + flowY * flowY + flowZ * flowZ < 0.000_020_250_000_000_000_004_5) {
                //flow = flow.normalize().scale(0.004_500_000_000_000_000_5);
                double norm = Math.sqrt(flowX * flowX + flowY * flowY + flowZ * flowZ);
                if (norm < 1.0E-4) {
                    flowX = 0.0;
                    flowY = 0.0;
                    flowZ = 0.0;
                }
                else {
                    flowX /= norm;
                    flowY /= norm;
                    flowZ /= norm;
                }
                flowX *= 0.004_500_000_000_000_000_5;
                flowY *= 0.004_500_000_000_000_000_5;
                flowZ *= 0.004_500_000_000_000_000_5;
            }
            //velocity.add(flow)
            flowX += velocity.x;
            flowY += velocity.y;
            flowZ += velocity.z;
            this.setDeltaMovement(flowX, flowY, flowZ);
        }
        this.fluidHeight.put(fluid, height);
        return isInFluid;
    }
}
