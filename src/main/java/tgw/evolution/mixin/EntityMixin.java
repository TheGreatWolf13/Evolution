package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListenerRegistrar;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.blocks.IClimbable;
import tgw.evolution.init.EvolutionBlockTags;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.OptionalMutableBlockPos;
import tgw.evolution.util.constants.LevelEvents;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.AABBMutable;
import tgw.evolution.util.math.ChunkPosMutable;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.physics.SI;
import tgw.evolution.world.util.LevelUtils;

import java.util.List;
import java.util.Random;

@Mixin(Entity.class)
public abstract class EntityMixin extends CapabilityProvider<Entity> implements IEntityPatch<Entity> {

    @Unique
    private final AABBMutable bbForPose = new AABBMutable();
    @Unique
    private final Vec3d eyePosition = new Vec3d();
    @Unique
    private final BlockPos.MutableBlockPos frictionPos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos landingPos = new BlockPos.MutableBlockPos();
    @Unique
    private final OptionalMutableBlockPos supportingPos = new OptionalMutableBlockPos();
    @Unique
    private final Vec3d viewVector = new Vec3d();
    @Shadow
    public float fallDistance;
    @Shadow
    public float flyDist;
    @Shadow
    public boolean horizontalCollision;
    @Shadow
    public boolean isInPowderSnow;
    @Shadow
    public Level level;
    @Shadow
    public boolean minorHorizontalCollision;
    @Shadow
    public float moveDist;
    @Shadow
    public boolean noPhysics;
    @Shadow
    public int tickCount;
    @Shadow
    public boolean verticalCollision;
    @Shadow
    public boolean verticalCollisionBelow;
    @Shadow
    public float walkDist;
    @Shadow
    public float walkDistO;
    @Shadow
    public boolean wasInPowderSnow;
    @Shadow
    public boolean wasOnFire;
    @Shadow
    public float xRotO;
    @Shadow
    public double xo;
    @Shadow
    public float yRotO;
    @Shadow
    public double yo;
    @Shadow
    public double zo;
    @Shadow
    protected int boardingCooldown;
    @Unique
    protected int fireDamageImmunity;
    @Shadow
    protected boolean firstTick;
    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;
    @Unique
    protected boolean hasCollidedOnX;
    @Unique
    protected boolean hasCollidedOnZ;
    @Shadow
    protected boolean onGround;
    @Shadow
    @Final
    protected Random random;
    @Shadow
    protected Vec3 stuckSpeedMultiplier;
    @Shadow
    private BlockPos blockPosition;
    @Shadow
    private ChunkPos chunkPosition;
    @Shadow
    private Vec3 deltaMovement;
    @Shadow
    private EntityDimensions dimensions;
    @Shadow
    @javax.annotation.Nullable
    private BlockState feetBlockState;
    @Shadow
    private EntityInLevelCallback levelCallback;
    @Shadow
    private float nextStep;
    @Shadow
    private Vec3 position;
    @Shadow
    private int remainingFireTicks;
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

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private static Component removeAction(Component name) {
        MutableComponent mutable = name.plainCopy().setStyle(name.getStyle().withClickEvent(null));
        List<Component> siblings = name.getSiblings();
        for (int i = 0, l = siblings.size(); i < l; i++) {
            mutable.append(removeAction(siblings.get(i)));
        }
        return mutable;
    }

    /**
     * @author TheGreatWolf
     * @reason Add fire immunity checks, improve performance
     */
    @Overwrite
    public void baseTick() {
        //Count down fire immunity
        if (this.fireDamageImmunity > 0) {
            this.fireDamageImmunity--;
        }
        this.level.getProfiler().push("entityBaseTick");
        this.feetBlockState = null;
        //noinspection ConstantConditions
        if (this.isPassenger() && this.getVehicle().isRemoved()) {
            this.stopRiding();
        }
        if (this.boardingCooldown > 0) {
            --this.boardingCooldown;
        }
        this.walkDistO = this.walkDist;
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.handleNetherPortal();
        if (this.canSpawnSprintParticle()) {
            this.spawnSprintParticle();
        }
        this.wasInPowderSnow = this.isInPowderSnow;
        this.isInPowderSnow = false;
        this.updateInWaterStateAndDoFluidPushing();
        this.updateFluidOnEyes();
        this.updateSwimming();
        if (this.level.isClientSide) {
            this.clearFire();
        }
        else if (this.remainingFireTicks > 0) {
            if (this.fireImmune()) {
                this.setRemainingFireTicks(this.remainingFireTicks - 4);
                if (this.remainingFireTicks < 0) {
                    this.clearFire();
                }
            }
            else {
                if (this.remainingFireTicks % 20 == 0 && !this.isInLava()) {
                    //Change to evolution damage checking for fire immunity
                    if (this.fireDamageImmunity <= 0) {
                        this.hurt(EvolutionDamage.ON_FIRE, 2.5f);
                    }
                }
                this.setRemainingFireTicks(this.remainingFireTicks - 1);
            }
            if (this.getTicksFrozen() > 0) {
                this.setTicksFrozen(0);
                this.level.levelEvent(null, LevelEvents.FIRE_EXTINGUISH, this.blockPosition, 1);
            }
        }
        if (this.isInLava()) {
            this.lavaHurt();
            //Remove fall distance as it is not used
        }
        this.checkOutOfWorld();
        if (!this.level.isClientSide) {
            this.setSharedFlagOnFire(this.remainingFireTicks > 0);
        }
        this.firstTick = false;
        this.level.getProfiler().pop();
    }

    @Shadow
    public abstract BlockPos blockPosition();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    protected final Vec3 calculateViewVector(float xRot, float yRot) {
        float pitch = xRot * Mth.DEG_TO_RAD;
        float yaw = yRot * Mth.DEG_TO_RAD;
        float cosYaw = Mth.cos(yaw);
        float sinYaw = -Mth.sin(yaw);
        float cosPitch = Mth.cos(pitch);
        float sinPitch = Mth.sin(pitch);
        return this.viewVector.set(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
    }

    @Unique
    private boolean canClimb(BlockState state, BlockPos pos) {
        return state.is(BlockTags.CLIMBABLE) ||
               state.is(Blocks.POWDER_SNOW) ||
               state.getBlock() instanceof IClimbable climbable && climbable.isClimbable(state, this.level, pos, (Entity) (Object) this);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    protected boolean canEnterPose(Pose pPose) {
        return this.level.noCollision((Entity) (Object) this, ((AABBMutable) this.getBoundingBoxForPose(pPose)).deflateMutable(1.0E-7));
    }

    @Shadow
    public abstract boolean canSpawnSprintParticle();

    @Shadow
    protected abstract void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos);

    @Shadow
    protected abstract void checkInsideBlocks();

    @Shadow
    public abstract void checkOutOfWorld();

    @Shadow
    public abstract void clearFire();

    @Shadow
    protected abstract Vec3 collide(Vec3 pVec);

    @Shadow
    public abstract boolean fireImmune();

    @Shadow
    public abstract void gameEvent(GameEvent pEvent);

    @Override
    public double getBaseAttackDamage() {
        return 2.5;
    }

    @Override
    public double getBaseHealth() {
        return 20;
    }

    @Override
    public double getBaseMass() {
        return 1;
    }

    @Override
    public double getBaseWalkForce() {
        return 1_000 * SI.NEWTON;
    }

    @Shadow
    protected abstract float getBlockSpeedFactor();

    @Shadow
    public abstract AABB getBoundingBox();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    protected AABB getBoundingBoxForPose(Pose pose) {
        EntityDimensions dim = this.getDimensions(pose);
        float f = dim.width / 2.0F;
        return this.bbForPose.set(this.getX() - f, this.getY(), this.getZ() - f, this.getX() + f, this.getY() + dim.height, this.getZ() + f);
    }

    @Shadow
    @Nullable
    public abstract Entity getControllingPassenger();

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract EntityDimensions getDimensions(Pose pPose);

    @Shadow
    public abstract float getEyeHeight();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public final Vec3 getEyePosition() {
        return this.eyePosition.set(this.getX(), this.getEyeY(), this.getZ());
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public final Vec3 getEyePosition(float partialTicks) {
        double x = Mth.lerp(partialTicks, this.xo, this.getX());
        double y = Mth.lerp(partialTicks, this.yo, this.getY()) + this.getEyeHeight();
        double z = Mth.lerp(partialTicks, this.zo, this.getZ());
        return this.eyePosition.set(x, y, z);
    }

    @Shadow
    public abstract double getEyeY();

    @Override
    public int getFireDamageImmunity() {
        return this.fireDamageImmunity;
    }

    @Shadow
    protected abstract int getFireImmuneTicks();

    @Override
    public float getFrictionModifier() {
        return 2.0f;
    }

    @Override
    public BlockPos getFrictionPos() {
        return this.getPosWithYOffset(0.500_000_1f, this.frictionPos);
    }

    @Shadow
    @Nullable
    public abstract GameEventListenerRegistrar getGameEventListenerRegistrar();

    @Override
    public @Nullable HitboxEntity<Entity> getHitboxes() {
        return null;
    }

    @Override
    public double getLegSlowdown() {
        return 0;
    }

    @Shadow
    protected abstract Entity.MovementEmission getMovementEmission();

    /**
     * @author TheGreatWolf
     * @reason Use proper stepping pos // This is called landingPos on Yarn
     */
    @Overwrite
    public BlockPos getOnPos() {
        return this.getPosWithYOffset(0.2f, this.landingPos);
    }

    @Shadow
    public abstract float getPickRadius();

    @Unique
    private BlockPos getPosWithYOffset(float offset, BlockPos.MutableBlockPos pos) {
        if (this.supportingPos.isPresent()) {
            if (offset > 1.0E-5f) {
                BlockState state = this.level.getBlockState(this.supportingPos.get());
                //TODO use proper tags or classes
                if (offset <= 0.5 && state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS) || state.getBlock() instanceof FenceGateBlock) {
                    return pos.set(this.supportingPos.get());
                }
                return pos.set(this.supportingPos.get()).setY(Mth.floor(this.position.y - offset));
            }
            return pos.set(this.supportingPos.get());
        }
        return pos.set(Mth.floor(this.position.x), Mth.floor(this.position.y - offset), Mth.floor(this.position.z));
    }

    @Shadow
    public abstract Pose getPose();

    @Unique
    private BlockPos getStepSoundPos(BlockPos pos) {
        BlockPos blockPos = pos.above();
        BlockState blockState = this.level.getBlockState(blockPos);
        if (blockState.is(BlockTags.INSIDE_STEP_SOUND_BLOCKS) || blockState.is(EvolutionBlockTags.BLOCKS_COMBINED_STEP_SOUND)) {
            return blockPos;
        }
        return pos;
    }

    @Override
    public BlockPos getSteppingPos() {
        if (this.supportingPos.isPresent()) {
            return this.supportingPos.get();
        }
        return this.getPosWithYOffset(1e-5f, new BlockPos.MutableBlockPos());
    }

    @Shadow
    public abstract int getTicksFrozen();

    @Shadow
    @Nullable
    public abstract Entity getVehicle();

    @Shadow
    public abstract Vec3 getViewVector(float p_20253_);

    @Shadow
    public abstract float getViewXRot(float pPartialTicks);

    @Override
    public double getVolume() {
        return 0;
    }

    @Override
    @Unique
    public double getVolumeCorrectionFactor() {
        float width = this.dimensions.width;
        return this.getVolume() / (width * width * this.dimensions.height);
    }

    @Shadow
    public abstract double getX(double pScale);

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract float getXRot();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract float getYRot();

    @Shadow
    public abstract double getZ(double pScale);

    @Shadow
    public abstract double getZ();

    @Shadow
    protected abstract void handleNetherPortal();

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

    @Shadow
    public abstract boolean isAddedToWorld();

    @Shadow
    public abstract boolean isCrouching();

    @Shadow
    protected abstract boolean isHorizontalCollisionMinor(Vec3 pDeltaMovement);

    @Shadow
    public abstract boolean isInLava();

    /**
     * @author TheGreatWolf
     * @reason Prevent physics from being calculated on the client. Also avoid allocations when possible.
     */
    @Overwrite
    public boolean isInWall() {
        if (this.noPhysics || this.level.isClientSide) {
            return false;
        }
        float dWidth = this.dimensions.width * 0.8F / 2;
        Vec3 eyePos = this.getEyePosition();
        double minX = eyePos.x - dWidth;
        double minY = eyePos.y - 0.5E-6;
        double minZ = eyePos.z - dWidth;
        double maxX = eyePos.x + dWidth;
        double maxY = eyePos.y + 0.5E-6;
        double maxZ = eyePos.z + dWidth;
        int x0 = Mth.floor(minX);
        int y0 = Mth.floor(minY);
        int z0 = Mth.floor(minZ);
        int x1 = Mth.floor(maxX);
        int y1 = Mth.floor(maxY);
        int z1 = Mth.floor(maxZ);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        VoxelShape comparing = null;
        for (int x = x0; x <= x1; x++) {
            mutablePos.setX(x);
            for (int y = y0; y <= y1; y++) {
                mutablePos.setY(y);
                for (int z = z0; z <= z1; z++) {
                    mutablePos.setZ(z);
                    BlockState state = this.level.getBlockState(mutablePos);
                    if (!state.isAir() && state.isSuffocating(this.level, mutablePos)) {
                        if (comparing == null) {
                            comparing = Shapes.create(minX, minY, minZ, maxX, maxY, maxZ);
                        }
                        if (Shapes.joinIsNotEmpty(state.getCollisionShape(this.level, mutablePos).move(x, y, z), comparing, BooleanOp.AND)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Shadow
    public abstract boolean isInWater();

    @Shadow
    public abstract boolean isInWaterRainOrBubble();

    @Shadow
    public abstract boolean isOnFire();

    @Shadow
    public abstract boolean isOnGround();

    @Shadow
    public abstract boolean isPassenger();

    @Shadow
    public abstract boolean isPushedByFluid();

    @Shadow
    public abstract boolean isRemoved();

    @Shadow
    public abstract boolean isSteppingCarefully();

    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    public abstract boolean isVehicle();

    @Shadow
    public abstract void lavaHurt();

    @Shadow
    protected abstract Vec3 limitPistonMovement(Vec3 pPos);

    @Shadow
    protected abstract Vec3 maybeBackOffFromEdge(Vec3 pVec, MoverType pMover);

    /**
     * @author TheGreatWolf
     * @reason Improve performance
     */
    @Overwrite
    public void move(MoverType type, Vec3 deltaMovement) {
        if (this.noPhysics) {
            this.setPos(this.getX() + deltaMovement.x, this.getY() + deltaMovement.y, this.getZ() + deltaMovement.z);
            return;
        }
        this.wasOnFire = this.isOnFire();
        if (type == MoverType.PISTON) {
            deltaMovement = this.limitPistonMovement(deltaMovement);
            if (deltaMovement.equals(Vec3.ZERO)) {
                return;
            }
        }
        this.level.getProfiler().push("move");
        if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7) {
            deltaMovement = deltaMovement.multiply(this.stuckSpeedMultiplier);
            this.stuckSpeedMultiplier = Vec3.ZERO;
            this.setDeltaMovement(Vec3.ZERO);
        }
        deltaMovement = this.maybeBackOffFromEdge(deltaMovement, type);
        Vec3 allowedMovement = this.collide(deltaMovement);
        double d0 = allowedMovement.lengthSqr();
        if (d0 > 1.0E-7) {
            if (this.fallDistance != 0.0F && d0 >= 1.0) {
                BlockHitResult blockHitResult = this.level.clip(
                        new ClipContext(this.position(), this.position().add(allowedMovement), ClipContext.Block.FALLDAMAGE_RESETTING,
                                        ClipContext.Fluid.WATER, (Entity) (Object) this));
                if (blockHitResult.getType() != HitResult.Type.MISS) {
                    this.resetFallDistance();
                }
            }
            this.setPos(this.getX() + allowedMovement.x, this.getY() + allowedMovement.y, this.getZ() + allowedMovement.z);
        }
        this.level.getProfiler().pop();
        this.hasCollidedOnX = !Mth.equal(deltaMovement.x, allowedMovement.x);
        this.hasCollidedOnZ = !Mth.equal(deltaMovement.z, allowedMovement.z);
        this.level.getProfiler().push("rest");
        this.horizontalCollision = this.hasCollidedOnX || this.hasCollidedOnZ;
        this.verticalCollision = deltaMovement.y != allowedMovement.y;
        this.verticalCollisionBelow = this.verticalCollision && deltaMovement.y < 0;
        if (this.horizontalCollision) {
            this.minorHorizontalCollision = this.isHorizontalCollisionMinor(allowedMovement);
        }
        else {
            this.minorHorizontalCollision = false;
        }
        this.setOnGround(this.verticalCollisionBelow);
        BlockPos landingPos = this.getOnPos();
        BlockState landingState = this.level.getBlockState(landingPos);
        this.checkFallDamage(allowedMovement.y, this.isOnGround(), landingState, landingPos);
        if (this.isRemoved()) {
            this.level.getProfiler().pop();
            return;
        }
        if (this.horizontalCollision) {
            Vec3 updatedDeltaMovement = this.getDeltaMovement();
            this.setDeltaMovement(this.hasCollidedOnX ? 0.0 : updatedDeltaMovement.x, updatedDeltaMovement.y,
                                  this.hasCollidedOnZ ? 0.0 : updatedDeltaMovement.z);
        }
        Block block = landingState.getBlock();
        if (deltaMovement.y != allowedMovement.y) {
            block.updateEntityAfterFallOn(this.level, (Entity) (Object) this);
        }
        if (this.onGround && !this.isSteppingCarefully()) {
            block.stepOn(this.level, landingPos, landingState, (Entity) (Object) this);
        }
        Entity.MovementEmission movementEmission = this.getMovementEmission();
        if (movementEmission.emitsAnything() && !this.isPassenger()) {
            double dx = allowedMovement.x;
            double dy = allowedMovement.y;
            double dz = allowedMovement.z;
            this.flyDist += allowedMovement.length() * 0.6;
            BlockPos steppingPos = this.getSteppingPos();
            BlockState steppingState = this.level.getBlockState(steppingPos);
            boolean canClimb = this.canClimb(steppingState, steppingPos);
            if (!canClimb) {
                dy = 0.0;
            }
            this.walkDist += allowedMovement.horizontalDistance() * 0.5;
            this.moveDist += Math.sqrt(dx * dx + dy * dy + dz * dz) * 0.5;
            if (this.moveDist > this.nextStep && !steppingState.isAir()) {
                boolean landingIsStepping = landingPos.equals(steppingPos);
                boolean stepOnBlock = this.stepOnBlock(landingPos, landingState, movementEmission.emitsSounds(), landingIsStepping, deltaMovement);
                if (!landingIsStepping) {
                    stepOnBlock |= this.stepOnBlock(steppingPos, steppingState, false, movementEmission.emitsEvents(), deltaMovement);
                }
                if (stepOnBlock) {
                    this.moveDist -= this.nextStep;
                    if (this.moveDist > this.nextStep) {
                        this.moveDist %= this.nextStep;
                    }
                    this.nextStep = this.nextStep();
                }
                else if (this.isInWater()) {
                    if (movementEmission.emitsSounds()) {
                        this.moveDist -= this.nextStep;
                        if (this.moveDist > this.nextStep) {
                            this.moveDist %= this.nextStep;
                        }
                        this.nextStep = this.nextStep();
                        this.playSwimSound();
                    }
                    if (movementEmission.emitsEvents()) {
                        this.gameEvent(GameEvent.SWIM);
                    }
                }
            }
            else if (steppingState.isAir()) {
                this.processFlappingMovement();
            }
        }
        this.tryCheckInsideBlocks();
        float blockSpeedFactor = this.getBlockSpeedFactor();
        ((Vec3d) this.getDeltaMovement()).multiplyMutable(blockSpeedFactor, 1.0, blockSpeedFactor);
        if (this.level.getBlockStatesIfLoaded(this.getBoundingBox().deflate(1.0E-6)).noneMatch(s -> s.is(BlockTags.FIRE) || s.is(Blocks.LAVA))) {
            if (this.remainingFireTicks <= 0) {
                this.setRemainingFireTicks(-this.getFireImmuneTicks());
            }
            if (this.wasOnFire && (this.isInPowderSnow || this.isInWaterRainOrBubble())) {
                this.playEntityOnFireExtinguishedSound();
            }
        }
        if (this.isOnFire() && (this.isInPowderSnow || this.isInWaterRainOrBubble())) {
            this.setRemainingFireTicks(-this.getFireImmuneTicks());
        }
        this.level.getProfiler().pop();
    }

    @Shadow
    public abstract void moveRelative(float pAmount, Vec3 pRelative);

    /**
     * @author TheGreatWolf
     * @reason Remove dependency from moveDist, as it's subtracted when reached, prevents precision loss.
     */
    @Overwrite
    protected float nextStep() {
        return 1;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityType entityType, Level level, CallbackInfo ci) {
        this.position = new Vec3d();
        this.deltaMovement = new Vec3d();
        this.blockPosition = new BlockPos.MutableBlockPos();
        this.chunkPosition = new ChunkPosMutable();
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getShort(Ljava/lang/String;)S", ordinal = 0))
    private void onLoad(CompoundTag nbt, CallbackInfo ci) {
        this.fireDamageImmunity = nbt.getByte("FireImmunity");
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putShort(Ljava/lang/String;S)V", ordinal
            = 0))
    private void onSaveWithoutId(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        nbt.putByte("FireImmunity", (byte) this.fireDamageImmunity);
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle camera pos.
     */
    @Overwrite
    public HitResult pick(double distance, float partialTicks, boolean checkFluids) {
        Vec3 camera = MathHelper.getCameraPosition((Entity) (Object) this, partialTicks).asImmutable();
        Vec3 viewVec = this.getViewVector(partialTicks);
        Vec3 to = camera.add(viewVec.x * distance, viewVec.y * distance, viewVec.z * distance);
        return this.level.clip(new ClipContext(camera, to, ClipContext.Block.OUTLINE, checkFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                                               (Entity) (Object) this));
    }

    @Shadow
    protected abstract void playAmethystStepSound(BlockState pState);

    @Unique
    private void playCombinationStepSounds(BlockState primaryState, BlockState secondaryState) {
        SoundType primarySoundType = primaryState.getSoundType();
        SoundType secondarySoundType = secondaryState.getSoundType();
        this.playSound(primarySoundType.getStepSound(), primarySoundType.getVolume() * 0.15f, primarySoundType.getPitch());
        this.playSound(secondarySoundType.getStepSound(), secondarySoundType.getVolume() * 0.05f, secondarySoundType.getPitch() * 0.8f);
    }

    @Shadow
    protected abstract void playEntityOnFireExtinguishedSound();

    @Shadow
    public abstract void playSound(SoundEvent pSound, float pVolume, float pPitch);

    @Shadow
    protected abstract void playStepSound(BlockPos pPos, BlockState pState);

    @Unique
    private void playStepSounds(BlockPos pos, BlockState state) {
        BlockPos blockPos = this.getStepSoundPos(pos);
        //noinspection ConstantConditions
        if ((Object) this instanceof Player && !pos.equals(blockPos)) {
            BlockState blockState = this.level.getBlockState(blockPos);
            if (blockState.is(EvolutionBlockTags.BLOCKS_COMBINED_STEP_SOUND)) {
                this.playCombinationStepSounds(blockState, state);
            }
            else {
                this.playStepSound(blockPos, blockState);
            }
        }
        else {
            this.playStepSound(pos, state);
        }
        this.playAmethystStepSound(state);
    }

    @Unique
    private void playSwimSound() {
        Entity entity;
        Entity controller;
        float mult;
        if (this.isVehicle() && (controller = this.getControllingPassenger()) != null) {
            entity = controller;
            mult = 0.4f;
        }
        else {
            entity = (Entity) (Object) this;
            mult = 0.35f;
        }
        Vec3 velocity = entity.getDeltaMovement();
        float vol = Math.min(1.0f, (float) Math.sqrt(velocity.x * velocity.x * 0.2 + velocity.y * velocity.y + velocity.z * velocity.z * 0.2) * mult);
        this.playSwimSound(vol);
    }

    @Shadow
    protected abstract void playSwimSound(float pVolume);

    @Shadow
    public abstract Vec3 position();

    @Shadow
    public abstract void positionRider(Entity pPassenger);

    @Shadow
    protected abstract void processFlappingMovement();

    @Shadow
    public abstract void resetFallDistance();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void setDeltaMovement(Vec3 motion) {
        ((Vec3d) this.deltaMovement).set(motion);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void setDeltaMovement(double x, double y, double z) {
        ((Vec3d) this.deltaMovement).set(x, y, z);
    }

    @Override
    public void setFireDamageImmunity(int immunity) {
        this.fireDamageImmunity = immunity;
    }

    /**
     * @author TheGreatWolf
     * @reason Add supporting pos
     */
    @Overwrite
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
        this.updateSupportingBlockPos(onGround);
    }

    @Shadow
    public abstract void setPos(double p_20210_, double p_20211_, double p_20212_);

    @Shadow
    public abstract void setPos(Vec3 pPos);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public final void setPosRaw(double x, double y, double z) {
        if (this.position.x != x || this.position.y != y || this.position.z != z) {
            ((Vec3d) this.position).set(x, y, z);
            int i = Mth.floor(x);
            int j = Mth.floor(y);
            int k = Mth.floor(z);
            if (i != this.blockPosition.getX() || j != this.blockPosition.getY() || k != this.blockPosition.getZ()) {
                ((BlockPos.MutableBlockPos) this.blockPosition).set(i, j, k);
                this.feetBlockState = null;
                if (SectionPos.blockToSectionCoord(i) != this.chunkPosition.x || SectionPos.blockToSectionCoord(k) != this.chunkPosition.z) {
                    ((ChunkPosMutable) this.chunkPosition).set(this.blockPosition);
                }
            }
            this.levelCallback.onMove();
            GameEventListenerRegistrar gameEventListenerRegistrar = this.getGameEventListenerRegistrar();
            if (gameEventListenerRegistrar != null) {
                gameEventListenerRegistrar.onListenerMove(this.level);
            }
        }
        if (this.isAddedToWorld() && !this.level.isClientSide && !this.isRemoved()) {
            this.level.getChunk((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4); // Forge - ensure target chunk is loaded.
        }
    }

    @Shadow
    public abstract void setRemainingFireTicks(int pRemainingFireTicks);

    @Shadow
    public abstract void setSharedFlagOnFire(boolean pIsOnFire);

    @Shadow
    public abstract void setTicksFrozen(int pTicksFrozen);

    @Shadow
    public abstract void setXRot(float pXRot);

    @Shadow
    public abstract void setYRot(float pYRot);

    /**
     * @author TheGreatWolf
     * @reason Fix incorrect particles displaying
     */
    @Overwrite
    protected void spawnSprintParticle() {
        if (!this.isOnGround()) {
            return;
        }
        BlockPos landingPos = this.getOnPos();
        BlockState landingState = this.level.getBlockState(landingPos);
        if (landingState.addRunningEffects(this.level, landingPos, (Entity) (Object) this)) {
            return;
        }
        BlockPos above = landingPos.above();
        BlockState aboveState = this.level.getBlockState(above);
        if (aboveState.is(EvolutionBlockTags.BLOCKS_COMBINED_STEP_PARTICLE)) {
            landingState = aboveState;
        }
        if (landingState.getRenderShape() != RenderShape.INVISIBLE) {
            Vec3 velocity = this.getDeltaMovement();
            BlockPos pos = this.blockPosition();
            double x = this.getX() + (this.random.nextDouble() - 0.5) * this.dimensions.width;
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * this.dimensions.width;
            if (pos.getX() != landingPos.getX()) {
                x = MathHelper.clamp(x, landingPos.getX(), landingPos.getX() + 1.0);
            }
            if (pos.getZ() != landingPos.getZ()) {
                z = MathHelper.clamp(z, landingPos.getZ(), landingPos.getZ() + 1.0);
            }
            this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, landingState), x, this.getY() + 0.1, z,
                                   velocity.x * -4.0, 1.5, velocity.z * -4.0);
        }
    }

    @Unique
    private boolean stepOnBlock(BlockPos pos, BlockState state, boolean playSound, boolean emitEvent, Vec3 movement) {
        if (state.isAir()) {
            return false;
        }
        boolean climbable = this.canClimb(state, pos);
        if ((this.isOnGround() || climbable || this.isCrouching() && movement.y == 0.0) && !this.isSwimming()) {
            if (playSound) {
                this.playStepSounds(pos, state);
            }
            if (emitEvent) {
                this.level.gameEvent((Entity) (Object) this, GameEvent.STEP, this.blockPosition());
            }
            return true;
        }
        return false;
    }

    @Shadow
    public abstract void stopRiding();

    @Shadow
    public abstract boolean touchingUnloadedChunk();

    @Shadow
    protected abstract void tryCheckInsideBlocks();

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations
     */
    @Overwrite
    public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> fluid, double motionScale) {
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
            //noinspection ConstantConditions
            if (!((Object) this instanceof Player)) {
                //flow = flow.normalize();
                double norm = Mth.fastInvSqrt(flowX * flowX + flowY * flowY + flowZ * flowZ);
                if (norm > 1.0E4) {
                    flowX = 0.0;
                    flowY = 0.0;
                    flowZ = 0.0;
                }
                else {
                    flowX *= norm;
                    flowY *= norm;
                    flowZ *= norm;
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
                double norm = Mth.fastInvSqrt(flowX * flowX + flowY * flowY + flowZ * flowZ);
                if (norm > 1.0E4) {
                    flowX = 0.0;
                    flowY = 0.0;
                    flowZ = 0.0;
                }
                else {
                    flowX *= norm;
                    flowY *= norm;
                    flowZ *= norm;
                }
                flowX *= 0.004_5;
                flowY *= 0.004_5;
                flowZ *= 0.004_5;
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

    @Shadow
    protected abstract void updateFluidOnEyes();

    @Shadow
    protected abstract boolean updateInWaterStateAndDoFluidPushing();

    @Unique
    private void updateSupportingBlockPos(boolean onGround) {
        if (onGround) {
            AABB bb = this.getBoundingBox();
            LevelUtils.findSupportingBlockPos(this.level, (Entity) (Object) this, bb.minX, bb.minY - 1.0E-6, bb.minZ, bb.maxX, bb.minY, bb.maxZ,
                                              this.supportingPos);
        }
        else {
            this.supportingPos.remove();
        }
    }

    @Shadow
    public abstract void updateSwimming();
}
