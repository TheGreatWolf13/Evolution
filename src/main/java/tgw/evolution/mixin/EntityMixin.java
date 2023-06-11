package tgw.evolution.mixin;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
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
import net.minecraftforge.common.extensions.IForgeEntity;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.blocks.IClimbable;
import tgw.evolution.hooks.LivingHooks;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionBlockTags;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.patches.IFluidPatch;
import tgw.evolution.patches.IHolderReferencePatch;
import tgw.evolution.util.OptionalMutableBlockPos;
import tgw.evolution.util.constants.LevelEvents;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.AABBMutable;
import tgw.evolution.util.math.ChunkPosMutable;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.physics.Physics;
import tgw.evolution.util.physics.SI;
import tgw.evolution.world.util.LevelUtils;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin extends CapabilityProvider<Entity> implements IEntityPatch<Entity>, IForgeEntity {

    @Shadow
    @Final
    protected static EntityDataAccessor<Pose> DATA_POSE;
    @Shadow
    @Final
    private static AABB INITIAL_AABB;
    @Unique
    private final AABBMutable bbForPose = new AABBMutable();
    @Unique
    private final BlockPos.MutableBlockPos eyeBlockPos = new BlockPos.MutableBlockPos();
    @Unique
    private final Vec3d eyePosition = new Vec3d(Vec3d.NULL);
    /**
     * In the future, can be changed to a O2ByteMap to store more flags as needed.
     */
    @Unique
    private final ObjectSet<TagKey<Fluid>> fluidFullySubmerged = new ObjectOpenHashSet<>();
    @Unique
    private final BlockPos.MutableBlockPos frictionPos = new BlockPos.MutableBlockPos();
    @Unique
    private final BlockPos.MutableBlockPos landingPos = new BlockPos.MutableBlockPos();
    @Unique
    private final Vec3d partialEyePosition = new Vec3d(Vec3d.NULL);
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
    @Shadow
    @Final
    protected SynchedEntityData entityData;
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
    protected boolean wasEyeInWater;
    @Shadow
    protected boolean wasTouchingWater;
    @Shadow
    private AABB bb;
    @Shadow
    private BlockPos blockPosition;
    @Unique
    private boolean cachedFluidOnEyes;
    @Shadow
    private ChunkPos chunkPosition;
    @Shadow
    private Vec3 deltaMovement;
    @Shadow
    private EntityDimensions dimensions;
    @Shadow
    private float eyeHeight;
    @Shadow
    @javax.annotation.Nullable
    private BlockState feetBlockState;
    @Shadow
    @Final
    private Set<TagKey<Fluid>> fluidOnEyes;
    @Unique
    private float lastPartialTickEyePosition;
    @Nullable
    @Unique
    private Pose lastPose;
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

    @Shadow
    public static Vec3 collideBoundingBox(@Nullable Entity pEntity,
                                          Vec3 pVec,
                                          AABB pCollisionBox,
                                          Level pLevel,
                                          List<VoxelShape> pPotentialHits) {
        throw new AbstractMethodError();
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

    @Shadow
    public abstract void absMoveTo(double pX, double pY, double pZ);

    /**
     * @author TheGreatWolf
     * @reason Make it so xRot is not hard limited.
     */
    @Overwrite
    public void absMoveTo(double x, double y, double z, float yRot, float xRot) {
        this.absMoveTo(x, y, z);
        this.setYRot(yRot % 360.0F);
        float xDelta = LivingHooks.xDelta((Entity) (Object) this, 1.0f);
        this.setXRot(Mth.clamp(xRot, -90.0F - xDelta, 90.0F - xDelta) % 360.0F);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    /**
     * @author TheGreatWolf
     * @reason Add fire immunity checks, improve performance
     */
    @Overwrite
    public void baseTick() {
        this.eyePosition.set(Vec3d.NULL);
        this.partialEyePosition.set(Vec3d.NULL);
        this.cachedFluidOnEyes = false;
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
                    this.hurt(EvolutionDamage.ON_FIRE, 2.5f);
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
    public boolean canEnterPose(Pose pPose) {
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
    protected abstract void doWaterSplashEffect();

    /**
     * @author TheGreatWolf
     * @reason Use cached pos
     */
    @Overwrite
    public BlockPos eyeBlockPosition() {
        Vec3 eyePosition = this.getEyePosition();
        return this.eyeBlockPos.set(eyePosition.x, eyePosition.y, eyePosition.z);
    }

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
    public abstract float getBbHeight();

    @Shadow
    public abstract float getBbWidth();

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

    @Unique
    @CanIgnoreReturnValue
    private Vec3d getCameraPosition(float partialTicks) {
        if (partialTicks == 1.0f) {
            return MathHelper.getRelativeEyePosition((Entity) (Object) this, 1.0f, this.eyePosition).addMutable(this.position);
        }
        double x = Mth.lerp(partialTicks, this.xo, this.getX());
        double y = Mth.lerp(partialTicks, this.yo, this.getY());
        double z = Mth.lerp(partialTicks, this.zo, this.getZ());
        return MathHelper.getRelativeEyePosition((Entity) (Object) this, partialTicks, this.partialEyePosition).addMutable(x, y, z);
    }

    @Shadow
    @Nullable
    public abstract Entity getControllingPassenger();

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract EntityDimensions getDimensions(Pose pPose);

    @Shadow
    protected abstract float getEyeHeight(Pose pPose, EntityDimensions pDimensions);

    @Shadow
    public abstract float getEyeHeight();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations, use proper eye position
     */
    @Overwrite
    public final Vec3 getEyePosition() {
        if (this.eyePosition.isNull()) {
            this.getCameraPosition(1.0f);
        }
        return this.eyePosition;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations, use proper eye position
     */
    @Overwrite
    public final Vec3 getEyePosition(float partialTicks) {
        if (partialTicks == 1.0f) {
            return this.getEyePosition();
        }
        if (this.partialEyePosition.isNull() || this.lastPartialTickEyePosition != partialTicks) {
            this.getCameraPosition(partialTicks);
            this.lastPartialTickEyePosition = partialTicks;
        }
        return this.partialEyePosition;
    }

    @Shadow
    public abstract double getEyeY();

    @Shadow
    protected abstract int getFireImmuneTicks();

    @Shadow
    public abstract double getFluidHeight(TagKey<Fluid> pFluidTag);

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
    public @Nullable Pose getLastPose() {
        return this.lastPose;
    }

    @Override
    public double getLegSlowdown() {
        return 0;
    }

    @Override
    public double getLungCapacity() {
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
    public boolean hasAnyFluidInEye() {
        if (this.fluidHeight.isEmpty()) {
            return false;
        }
        if (!this.fluidFullySubmerged.isEmpty()) {
            return true;
        }
        if (!this.cachedFluidOnEyes) {
            this.updateFluidOnEyes();
        }
        return !this.fluidOnEyes.isEmpty();
    }

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
    @Shadow
    public abstract boolean isAddedToWorld();

    @Shadow
    public abstract boolean isCrouching();

    /**
     * @author TheGreatWolf
     * @reason Only calculate eye in fluid when needed
     */
    @Overwrite
    public boolean isEyeInFluid(TagKey<Fluid> fluid) {
        if (fluid == FluidTags.WATER && !this.isInWater()) {
            return false;
        }
        if (this.isFullySubmerged(fluid)) {
            return true;
        }
        if (!this.cachedFluidOnEyes) {
            this.updateFluidOnEyes();
        }
        return this.fluidOnEyes.contains(fluid);
    }

    @Override
    @Unique
    public boolean isFullySubmerged(TagKey<Fluid> fluid) {
        return this.fluidFullySubmerged.contains(fluid);
    }

    @Shadow
    protected abstract boolean isHorizontalCollisionMinor(Vec3 pDeltaMovement);

    @Override
    public boolean isInAnyFluid() {
        return !this.fluidHeight.isEmpty();
    }

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
        float dWidth = this.dimensions.width * 0.4F;
        double minX = this.getX() - dWidth;
        double minY = this.getEyeY() - 0.5E-6;
        double minZ = this.getZ() - dWidth;
        double maxX = this.getX() + dWidth;
        double maxY = this.getEyeY() + 0.5E-6;
        double maxZ = this.getZ() + dWidth;
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
    public abstract boolean isSprinting();

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

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;" +
                                                                    "chunkPosition:Lnet/minecraft/world/level/ChunkPos;", opcode = Opcodes.PUTFIELD))
    private void onInit(Entity instance, ChunkPos value) {
        this.chunkPosition = new ChunkPosMutable(value);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;blockPosition:Lnet/minecraft/core/BlockPos;"
            , opcode = Opcodes.PUTFIELD))
    private void onInit(Entity instance, BlockPos value) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        this.blockPosition = pos.set(value);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;bb:Lnet/minecraft/world/phys/AABB;",
            opcode = Opcodes.PUTFIELD))
    private void onInit(Entity instance, AABB value) {
        this.bb = new AABBMutable(value);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;" +
                                                                    "deltaMovement:Lnet/minecraft/world/phys/Vec3;", opcode = Opcodes.PUTFIELD))
    private void onInitMovement(Entity instance, Vec3 value) {
        this.deltaMovement = new Vec3d(value);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;position:Lnet/minecraft/world/phys/Vec3;",
            opcode = Opcodes.PUTFIELD))
    private void onInitPosition(Entity entity, Vec3 vec) {
        this.position = new Vec3d(vec);
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle camera pos.
     */
    @Overwrite
    public HitResult pick(double distance, float partialTicks, boolean checkFluids) {
        Vec3 camera = this.getEyePosition(partialTicks);
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
    protected abstract void positionRider(Entity pPassenger, Entity.MoveFunction pCallback);

    @Shadow
    public abstract void positionRider(Entity pPassenger);

    @Shadow
    protected abstract void processFlappingMovement();

    @Unique
    private void refreshBoundingBox() {
        float radius = this.dimensions.width / 2;
        double x = this.position.x;
        double y = this.position.y;
        double z = this.position.z;
        this.setBoundingBox(x - radius, y, z - radius, x + radius, y + this.dimensions.height, z + radius);
    }

    /**
     * @author TheGreatWolf
     * @reason Handle dynamic poses
     */
    @SuppressWarnings("ConstantConditions")
    @Overwrite
    public void refreshDimensions() {
        EntityDimensions oldDims = this.dimensions;
        Pose pose = this.getPose();
        EntityDimensions newDims = this.getDimensions(pose);
        this.dimensions = newDims;
        this.eyeHeight = this.getEyeHeight(pose, newDims);
        boolean isSmall = newDims.width <= 4 && newDims.height <= 4;
        this.refreshBoundingBox();
        if (!this.level.isClientSide &&
            !this.firstTick &&
            !this.noPhysics &&
            isSmall &&
            (newDims.width > oldDims.width || newDims.height > oldDims.height) &&
            !((Object) this instanceof Player)) {
            Vec3 center = this.position().add(0, oldDims.height / 2, 0);
            double dWidth = Math.max(0.0F, newDims.width - oldDims.width) + 1.0E-6;
            double dHeight = Math.max(0.0F, newDims.height - oldDims.height) + 1.0E-6;
            VoxelShape shape = Shapes.create(AABB.ofSize(center, dWidth, dHeight, dWidth));
            Optional<Vec3> freePosition = this.level.findFreePosition((Entity) (Object) this, shape, center, newDims.width, newDims.height,
                                                                      newDims.width);
            if (freePosition.isPresent()) {
                this.setPos(freePosition.get().add(0, -newDims.height / 2, 0));
            }
        }
    }

    @Shadow
    public abstract void resetFallDistance();

    /**
     * @author TheGreatWolf
     * @reason Preserve size
     */
    @Overwrite
    public final void setBoundingBox(AABB bb) {
        ((AABBMutable) this.bb).set(bb);
    }

    @Unique
    private void setBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        ((AABBMutable) this.bb).set(minX, minY, minZ, maxX, maxY, maxZ);
    }

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

    /**
     * @author TheGreatWolf
     * @reason Add supporting pos
     */
    @Overwrite
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
        this.updateSupportingBlockPos(onGround);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
        this.refreshBoundingBox();
    }

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

    /**
     * @author TheGreatWolf
     * @reason Record last pose
     */
    @Overwrite
    public void setPose(Pose pose) {
        Pose currentPose = this.getPose();
        if (currentPose != pose) {
            this.lastPose = currentPose;
        }
        this.entityData.set(DATA_POSE, pose);
    }

    @Shadow
    public abstract void setRemainingFireTicks(int pRemainingFireTicks);

    @Shadow
    public abstract void setSharedFlagOnFire(boolean pIsOnFire);

    @Shadow
    public abstract void setSwimming(boolean pSwimming);

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

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public boolean touchingUnloadedChunk() {
        AABB bb = this.getBoundingBox();
        int minX = Mth.floor(bb.minX - 1);
        int maxX = Mth.ceil(bb.maxX + 1);
        int minZ = Mth.floor(bb.minZ - 1);
        int maxZ = Mth.ceil(bb.maxZ + 1);
        return !this.level.hasChunksAt(minX, minZ, maxX, maxZ);
    }

    @Shadow
    protected abstract void tryCheckInsideBlocks();

    @Unique
    private void updateFluidHeightAndDoFluidPushing() {
        if (this.touchingUnloadedChunk()) {
            return;
        }
        AABB bb = this.getBoundingBox();
        int minX = Mth.floor(bb.minX + 0.001);
        int maxX = Mth.ceil(bb.maxX - 0.001);
        int minY = Mth.floor(bb.minY - 0.001);
        int maxY = Mth.ceil(bb.maxY + 0.001);
        int minZ = Mth.floor(bb.minZ + 0.001);
        int maxZ = Mth.ceil(bb.maxZ - 0.001);
        boolean pushedByFluid = this.isPushedByFluid();
        double flowX = 0;
        double flowY = 0;
        double flowZ = 0;
        int maxSubmerged = (maxX - minX) * (maxZ - minZ);
        int submerged = 0;
        tgw.evolution.util.physics.Fluid lastSubmergedFluid = null;
        Vec3d flow = null;
        BlockPos.MutableBlockPos flowPos = null;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        double velX = 0;
        double velY = 0;
        double velZ = 0;
        double mult = 0; //0.5 * coefDrag * mass
        double dx = 0;
        double dy = 0;
        double dz = 0;
        double sx = 0; //Reciprocal of sizeX
        double sy = 0; //Reciprocal of sizeY
        double sz = 0; //Reciprocal of sizeZ
        tgw.evolution.util.physics.Fluid fallFluid = null;
        boolean fallDamage = false;
        if (pushedByFluid) {
            flow = new Vec3d(Vec3.ZERO);
            flowPos = new BlockPos.MutableBlockPos();
            Vec3 deltaMovement = this.getDeltaMovement();
            velX = deltaMovement.x;
            velY = deltaMovement.y;
            velZ = deltaMovement.z;
            //noinspection ConstantConditions
            mult = 0.5 * Physics.coefOfDrag((Entity) (Object) this) / ((Object) this instanceof LivingEntity living ?
                                                                       living.getAttributeValue(EvolutionAttributes.MASS.get()) : this.getBaseMass());
            sx = 1 / (bb.maxX - bb.minX);
            sy = 1 / (bb.maxY - bb.minY);
            sz = 1 / (bb.maxZ - bb.minZ);
        }
        for (int x = minX; x < maxX; ++x) {
            mutablePos.setX(x);
            if (pushedByFluid) {
                dx = Math.min(x + 1, bb.maxX) - Math.max(x, bb.minX);
            }
            for (int z = minZ; z < maxZ; ++z) {
                mutablePos.setZ(z);
                if (pushedByFluid) {
                    dz = Math.min(z + 1, bb.maxZ) - Math.max(z, bb.minZ);
                }
                for (int y = minY; y < maxY; ++y) {
                    mutablePos.setY(y);
                    FluidState fluidState = this.level.getFluidState(mutablePos);
                    tgw.evolution.util.physics.Fluid fluid = null;
                    double localHeight = 0;
                    boolean needsSemiCalc = false;
                    double localSemiHeight = 0;
                    if (!fluidState.isEmpty()) {
                        double selfHeight = fluidState.getHeight(this.level, mutablePos);
                        localHeight = y + selfHeight;
                        if (localHeight >= bb.minY) {
                            IFluidPatch patch = (IFluidPatch) fluidState.getType();
                            fluid = patch.fluid();
                            if (pushedByFluid) {
                                patch.getFlow(this.level, mutablePos, fluidState, flowPos, flow.set(Vec3.ZERO))
                                     .scaleMutable(patch.getFlowStrength(this.level.dimensionType()));
                            }
                            if (localHeight >= bb.maxY) {
                                localHeight = bb.maxY;
                                if (lastSubmergedFluid == null) {
                                    lastSubmergedFluid = fluid;
                                }
                                if (lastSubmergedFluid == fluid) {
                                    ++submerged;
                                }
                            }
                            else if (selfHeight < 1 && pushedByFluid) {
                                //Block has top part of air at the top of aabb (aabb is semi on fluid, semi on air)
                                //Calculated via loop
                                needsSemiCalc = true;
                                localSemiHeight = selfHeight;
                            }
                            double newHeight = localHeight - bb.minY;
                            if (newHeight > this.fluidHeight.getDouble(fluid.tag())) {
                                this.fluidHeight.put(fluid.tag(), newHeight);
                            }
                        }
                        else if (pushedByFluid) {
                            //Block has top part of air at bottom of aabb (aabb is fully on air)
                            fluid = tgw.evolution.util.physics.Fluid.AIR;
                            localHeight = y + 1;
                            //TODO wind
                            flow.set(Vec3.ZERO);
                        }
                    }
                    else if (pushedByFluid) {
                        //Block is fully air
                        fluid = tgw.evolution.util.physics.Fluid.AIR;
                        localHeight = y + 1;
                        //TODO wind
                        flow.set(Vec3.ZERO);
                    }
                    double properY = y;
                    boolean pushing = pushedByFluid;
                    while (pushing) {
                        dy = localHeight - Math.max(properY, bb.minY);
                        double density = fluid.density();
                        double localFlowX = flow.x;
                        double localFlowY = flow.y;
                        double localFlowZ = flow.z;
                        double relVelX = localFlowX - velX;
                        double relVelY = localFlowY - velY;
                        double relVelZ = localFlowZ - velZ;
                        double dv = dx * dy * dz;
                        double mul = mult * density * dv;
                        double dragX = Math.signum(relVelX) * relVelX * relVelX * mul * sx;
                        double dragY = Math.signum(relVelY) * relVelY * relVelY * mul * sy;
                        double dragZ = Math.signum(relVelZ) * relVelZ * relVelZ * mul * sz;
                        mul = sx * sy * sz * dv;
                        double maxDrag = Math.abs(relVelX) * mul;
                        if (Math.abs(dragX) > maxDrag) {
                            dragX = Math.signum(dragX) * maxDrag;
                        }
                        maxDrag = Math.abs(relVelY) * mul;
                        if (Math.abs(dragY) > maxDrag) {
                            dragY = Math.signum(dragY) * maxDrag;
                        }
                        if (fallFluid == null) {
                            if (fluid != tgw.evolution.util.physics.Fluid.AIR && fluid != tgw.evolution.util.physics.Fluid.VACUUM) {
                                if (!(fluid == tgw.evolution.util.physics.Fluid.WATER && this.wasTouchingWater)) {
                                    fallFluid = fluid;
                                    fallDamage = true;
                                }
                            }
                        }
                        maxDrag = Math.abs(relVelZ) * mul;
                        if (Math.abs(dragZ) > maxDrag) {
                            dragZ = Math.signum(dragZ) * maxDrag;
                        }
                        flowX += dragX;
                        flowY += dragY;
                        flowZ += dragZ;
                        if (needsSemiCalc) {
                            needsSemiCalc = false;
                            fluid = tgw.evolution.util.physics.Fluid.AIR;
                            localHeight = y + 1;
                            properY = y + localSemiHeight;
                            //TODO wind
                            flow.set(Vec3.ZERO);
                        }
                        else {
                            pushing = false;
                        }
                    }
                }
            }
        }
        //noinspection ConstantConditions
        if (fallDamage && (Object) this instanceof LivingEntity living) {
            LivingHooks.calculateFluidFallDamage(living, fallFluid);
        }
        ((Vec3d) this.getDeltaMovement()).addMutable(flowX, flowY, flowZ);
        if (submerged >= maxSubmerged) {
            this.fluidFullySubmerged.add(lastSubmergedFluid.tag());
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Redirect
     */
    @Overwrite
    public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> fluid, double motionScale) {
        throw new IllegalStateException("Do not call! Call updateFluidHeightAndDoFluidPushing() instead and check if fluidHeight > 0");
    }

    /**
     * @author TheGreatWolf
     * @reason Use proper eye position.
     */
    @Overwrite
    private void updateFluidOnEyes() {
        this.cachedFluidOnEyes = true;
        this.wasEyeInWater = this.isEyeInFluid(FluidTags.WATER);
        this.fluidOnEyes.clear();
        Vec3 eyePosition = this.getEyePosition();
        double eyeY = eyePosition.y;
        Entity vehicle = this.getVehicle();
        if (vehicle instanceof Boat boat) {
            if (!boat.isUnderWater() && boat.getBoundingBox().maxY >= eyeY && boat.getBoundingBox().minY <= eyeY) {
                return;
            }
        }
        BlockPos eyePos = this.eyeBlockPosition();
        FluidState fluidstate = this.level.getFluidState(eyePos);
        double height = eyePos.getY() + fluidstate.getHeight(this.level, eyePos);
        if (height > eyeY) {
            this.fluidOnEyes.addAll(((IHolderReferencePatch<Fluid>) fluidstate.getType().builtInRegistryHolder()).getTags());
        }
    }

    /**
     * @return Whether the entity is in a fluid.
     * @author TheGreatWolf
     * @reason Simplify to a single calculation
     */
    @Overwrite
    protected boolean updateInWaterStateAndDoFluidPushing() {
        this.fluidHeight.clear();
        this.fluidFullySubmerged.clear();
        this.updateFluidHeightAndDoFluidPushing();
        this.updateInWaterStateAndDoWaterCurrentPushing();
        return this.isInWater() || this.fluidHeight.getDouble(FluidTags.LAVA) > 0;
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify to a single calculation
     */
    @Overwrite
    protected void updateInWaterStateAndDoWaterCurrentPushing() {
        if (this.getVehicle() instanceof Boat) {
            this.wasTouchingWater = false;
        }
        else if (this.fluidHeight.getDouble(FluidTags.WATER) > 0) {
            if (!this.wasTouchingWater && !this.firstTick) {
                this.doWaterSplashEffect();
            }
            this.resetFallDistance();
            this.wasTouchingWater = true;
            this.clearFire();
        }
        else {
            this.wasTouchingWater = false;
        }
    }

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

    /**
     * @author TheGreatWolf
     * @reason Make it possible to start swimming when not submerged.
     */
    @Overwrite
    public void updateSwimming() {
        if (this.isSwimming()) {
            this.setSwimming(this.isSprinting() && this.isInWater() && !this.isPassenger());
        }
        else {
            this.setSwimming(this.isSprinting() &&
                             !this.isPassenger() &&
                             this.level.getFluidState(this.blockPosition).is(FluidTags.WATER));
        }
    }
}
