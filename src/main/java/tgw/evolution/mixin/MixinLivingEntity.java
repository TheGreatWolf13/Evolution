package tgw.evolution.mixin;

import com.google.common.base.Objects;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.Evolution;
import tgw.evolution.EvolutionClient;
import tgw.evolution.blocks.IClimbable;
import tgw.evolution.blocks.ICollisionBlock;
import tgw.evolution.blocks.IFallSufixBlock;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.entities.EffectHelper;
import tgw.evolution.entities.EntityUtils;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.hooks.LivingHooks;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionBlockTags;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.items.ICancelableUse;
import tgw.evolution.items.IMelee;
import tgw.evolution.network.*;
import tgw.evolution.patches.PatchLivingEntity;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.maps.R2OMap;
import tgw.evolution.util.constants.EntityStates;
import tgw.evolution.util.damage.DamageSourceEntity;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.damage.EvolutionCombatTracker;
import tgw.evolution.util.hitbox.HitboxType;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.physics.EarthHelper;
import tgw.evolution.util.physics.Fluid;
import tgw.evolution.util.physics.Physics;

import java.util.*;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements PatchLivingEntity {

    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_EFFECT_AMBIENCE_ID;
    @Shadow @Final private static EntityDataAccessor<Integer> DATA_EFFECT_COLOR_ID;
    @Shadow @Final protected static int LIVING_ENTITY_FLAG_IS_USING;
    @Shadow @Final protected static int LIVING_ENTITY_FLAG_OFF_HAND;
    @Shadow @Final private static Logger LOGGER;
    @Mutable @Shadow @Final private Map<MobEffect, MobEffectInstance> activeEffects;
    @Shadow protected float animStep;
    @Shadow protected float animStepO;
    @Shadow public float animationSpeed;
    @Shadow public float attackAnim;
    @Shadow protected int autoSpinAttackTicks;
    @Shadow protected Brain<?> brain;
    @Mutable @Final @Shadow private CombatTracker combatTracker;
    @Shadow public int deathTime;
    @Unique private final EffectHelper effectHelper = new EffectHelper();
    @Shadow private boolean effectsDirty;
    @Unique private byte emergeTicks;
    @Shadow protected int fallFlyTicks;
    @Shadow public float hurtDir;
    @Shadow public int hurtDuration;
    @Shadow public int hurtTime;
    @Unique private boolean isSpecialAttacking;
    @Shadow protected boolean jumping;
    @Shadow private DamageSource lastDamageSource;
    @Shadow private long lastDamageStamp;
    @Shadow protected float lastHurt;
    @Shadow private @Nullable LivingEntity lastHurtByMob;
    @Shadow private int lastHurtByMobTimestamp;
    @Shadow protected @Nullable Player lastHurtByPlayer;
    @Shadow protected int lastHurtByPlayerTime;
    @Shadow private @Nullable LivingEntity lastHurtMob;
    @Shadow private BlockPos lastPos;
    @Shadow protected int lerpHeadSteps;
    @Shadow protected int lerpSteps;
    @Shadow protected double lerpX;
    @Shadow protected double lerpXRot;
    @Shadow protected double lerpY;
    @Shadow protected double lerpYRot;
    @Shadow protected double lerpZ;
    @Shadow protected double lyHeadRot;
    @Shadow protected int noActionTime;
    @Shadow private int noJumpDelay;
    @Shadow public float oAttackAnim;
    @Shadow protected float oRun;
    @Shadow public int removeArrowTime;
    @Shadow public int removeStingerTime;
    @Shadow protected float run;
    @Unique private byte specialAttackFollowUp;
    @Unique private byte specialAttackGracePeriod;
    @Unique private byte specialAttackLockedTicks;
    @Unique private byte specialAttackTime;
    @Unique private @Nullable IMelee.IAttackType specialAttackType;
    @Shadow public float swimAmount;
    @Shadow public float swimAmountO;
    @Shadow public int swingTime;
    @Shadow public boolean swinging;
    @Unique private final Vec3d travelVec = new Vec3d();
    @Shadow protected ItemStack useItem;
    @Shadow protected int useItemRemaining;
    @Shadow public float xxa;
    @Shadow public float yBodyRot;
    @Shadow public float yBodyRotO;
    @Shadow public float yHeadRot;
    @Shadow public float yHeadRotO;
    @Shadow public float yya;
    @Shadow public float zza;

    public MixinLivingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author TheGreatWolf
     * @reason Add attributes
     */
    @Overwrite
    public static AttributeSupplier.Builder createLivingAttributes() {
        return AttributeSupplier.builder()
                                .add(Attributes.MAX_HEALTH)
                                .add(Attributes.KNOCKBACK_RESISTANCE)
                                .add(Attributes.MOVEMENT_SPEED)
                                .add(Attributes.ARMOR)
                                .add(Attributes.ARMOR_TOUGHNESS)
                                .add(EvolutionAttributes.MASS)
                                .add(EvolutionAttributes.FRICTION)
                                .add(EvolutionAttributes.SWIM_SPEED);
    }

    @Unique
    private static byte getState(DamageSource source) {
        if (source == EvolutionDamage.DROWN) {
            return EntityStates.DROWN_HIT_SOUND;
        }
        if (source.isFire()) {
            return EntityStates.FIRE_HIT_SOUND;
        }
        if (source == DamageSource.SWEET_BERRY_BUSH) {
            return EntityStates.SWEET_BERRY_BUSH_HIT_SOUND;
        }
        return EntityStates.GENERIC_HIT_SOUND;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void actuallyHurt(DamageSource source, float amount) {
        if (!this.isInvulnerableTo(source)) {
            if (amount <= 0) {
                return;
            }
            float damageAfterAbsorp = Math.max(amount - this.getAbsorptionAmount(), 0.0F);
            float damageAbsorp = amount - damageAfterAbsorp;
            this.setAbsorptionAmount(this.getAbsorptionAmount() - damageAbsorp);
            if (damageAfterAbsorp != 0.0F) {
                float oldHealth = this.getHealth();
                this.getCombatTracker().recordDamage(source, oldHealth, damageAfterAbsorp);
                this.setHealth(oldHealth - damageAfterAbsorp);
                this.gameEvent(GameEvent.ENTITY_DAMAGED, source.getEntity());
            }
            if (source.getEntity() instanceof ServerPlayer sourcePlayer) {
                sourcePlayer.connection.send(new PacketSCSimpleMessage(Message.S2C.HITMARKER_NORMAL));
            }
        }
    }

    @Override
    public void addAbsorptionSuggestion(float amount) {
        if (amount > 0) {
            float delta = this.effectHelper.addAbsorptionSuggestion(amount);
            if (delta > 0) {
                this.setAbsorptionAmount(this.getAbsorptionAmount() + delta);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Add evolution hooks
     */
    @Override
    @Overwrite
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Health", this.getHealth());
        tag.putShort("HurtTime", (short) this.hurtTime);
        tag.putInt("HurtByTimestamp", this.lastHurtByMobTimestamp);
        tag.putShort("DeathTime", (short) this.deathTime);
        tag.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
        tag.put("Attributes", this.getAttributes().save());
        if (!this.activeEffects.isEmpty()) {
            ListTag list = new ListTag();
            for (MobEffectInstance effect : this.activeEffects.values()) {
                //noinspection ObjectAllocationInLoop
                list.add(effect.save(new CompoundTag()));
            }
            tag.put("ActiveEffects", list);
        }
        tag.putBoolean("FallFlying", this.isFallFlying());
        Optional<BlockPos> sleepingPos = this.getSleepingPos();
        if (sleepingPos.isPresent()) {
            BlockPos pos = sleepingPos.get();
            tag.putInt("SleepingX", pos.getX());
            tag.putInt("SleepingY", pos.getY());
            tag.putInt("SleepingZ", pos.getZ());
        }
        DataResult<Tag> brainStart = this.brain.serializeStart(NbtOps.INSTANCE);
        Optional<Tag> brainResult = brainStart.resultOrPartial(LOGGER::error);
        if (brainResult.isPresent()) {
            tag.put("Brain", brainResult.get());
        }
        tag.put("EffectHelper", this.effectHelper.save());
        ((EvolutionCombatTracker) this.getCombatTracker()).saveAdditional(tag);
    }

    /**
     * @author TheGreatWolf
     * @reason When an effect is overwritten to a hidden effect, its attribute modifiers are removed and readded. However, instead of removing the
     * modifiers based on the old instance of the effect (which has a lower amplifier), the original code removes and readds the attributes based
     * on the new instance (which has a higher amplifier), resulting in weird behaviours.
     */
    @Overwrite
    public boolean addEffect(MobEffectInstance effectInstance, @Nullable Entity entity) {
        if (!this.canBeAffected(effectInstance)) {
            return false;
        }
        MobEffectInstance oldInstance = this.activeEffects.get(effectInstance.getEffect());
        EntityEvents.onEffectAdded((LivingEntity) (Object) this, oldInstance, effectInstance);
        if (oldInstance == null) {
            this.activeEffects.put(effectInstance.getEffect(), effectInstance);
            this.onEffectAdded(effectInstance, entity);
            return true;
        }
        if (oldInstance.updateWithEntity(effectInstance, (LivingEntity) (Object) this)) {
            this.onEffectUpdated(oldInstance, false, entity);
            return true;
        }
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason Add evolution hooks.
     */
    @Overwrite
    public void aiStep() {
        if (this.noJumpDelay > 0) {
            --this.noJumpDelay;
        }
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.setPacketCoordinates(this.getX(), this.getY(), this.getZ());
        }
        if (this.lerpSteps > 0) {
            double x = this.getX() + (this.lerpX - this.getX()) / this.lerpSteps;
            double y = this.getY() + (this.lerpY - this.getY()) / this.lerpSteps;
            double z = this.getZ() + (this.lerpZ - this.getZ()) / this.lerpSteps;
            double ry = Mth.wrapDegrees(this.lerpYRot - this.getYRot());
            this.setYRot(this.getYRot() + (float) ry / this.lerpSteps);
            this.setXRot(this.getXRot() + (float) (this.lerpXRot - this.getXRot()) / this.lerpSteps);
            --this.lerpSteps;
            this.setPos(x, y, z);
            this.setRot(this.getYRot(), this.getXRot());
        }
        if (this.lerpHeadSteps > 0) {
            this.yHeadRot += (float) Mth.wrapDegrees(this.lyHeadRot - this.yHeadRot) / this.lerpHeadSteps;
            --this.lerpHeadSteps;
        }
//        Vec3 velocity = this.getDeltaMovement();
//        double velX = velocity.x;
//        double velY = velocity.y;
//        double velZ = velocity.z;
//        if (Math.abs(velocity.x) < 0.003) {
//            velX = 0;
//        }
//        if (Math.abs(velocity.y) < 0.003) {
//            velY = 0;
//        }
//        if (Math.abs(velocity.z) < 0.003) {
//            velZ = 0;
//        }
//        this.setDeltaMovement(velX, velY, velZ);
        this.level.getProfiler().push("ai");
        if (this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }
        else if (this.isEffectiveAi()) {
            this.level.getProfiler().push("newAi");
            this.serverAiStep();
            this.level.getProfiler().pop();
        }
        this.level.getProfiler().pop();
        this.level.getProfiler().push("jump");
        if (this.jumping) {
            if (this.onGround && !this.isSwimming()) {
                this.jumpFromGround();
                this.noJumpDelay = 10;
            }
        }
        else {
            this.noJumpDelay = 0;
        }
        this.level.getProfiler().pop();
        this.level.getProfiler().push("travel");
        this.xxa *= 0.98F;
        this.zza *= 0.98F;
        this.updateFallFlying();
        this.travel(this.travelVec.set(this.xxa, this.yya, this.zza));
        this.level.getProfiler().pop();
        this.level.getProfiler().push("freezing");
        boolean flag = this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES);
        if (!this.level.isClientSide && !this.isDeadOrDying()) {
            int i = this.getTicksFrozen();
            if (this.isInPowderSnow && this.canFreeze()) {
                this.setTicksFrozen(Math.min(this.getTicksRequiredToFreeze(), i + 1));
            }
            else {
                this.setTicksFrozen(Math.max(0, i - 2));
            }
        }
        this.removeFrost();
        this.tryAddFrost();
        if (!this.level.isClientSide && this.isFullyFrozen() && this.canFreeze()) {
            int j = flag ? 5 : 1;
            this.hurt(DamageSource.FREEZE, j);
        }
        this.level.getProfiler().pop();
        this.level.getProfiler().push("push");
        if (this.autoSpinAttackTicks > 0) {
            --this.autoSpinAttackTicks;
            AABB aabb = this.getBoundingBox();
            this.checkAutoSpinAttack(aabb, this.getBoundingBox());
        }
        this.pushEntities();
        this.level.getProfiler().pop();
        if (!this.level.isClientSide && this.isSensitiveToWater() && this.isInWaterRainOrBubble()) {
            this.hurt(EvolutionDamage.DROWN, 2.5F);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Add evolution hooks
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    @Overwrite
    public void baseTick() {
        this.oAttackAnim = this.attackAnim;
        if (this.firstTick) {
            Optional<BlockPos> sleepingPos = this.getSleepingPos();
            if (sleepingPos.isPresent()) {
                this.setPosToBed(sleepingPos.get());
            }
        }
        if (this.canSpawnSoulSpeedParticle()) {
            this.spawnSoulSpeedParticle();
        }
        super.baseTick();
        this.level.getProfiler().push("livingEntityBaseTick");
        if (this.fireImmune() || this.level.isClientSide) {
            this.clearFire();
        }
        if (this.isAlive()) {
            boolean isPlayer = (Object) this instanceof Player;
            if (this.isInWall()) {
                this.hurt(EvolutionDamage.IN_WALL, 5.0F);
            }
            else if (isPlayer && !this.level.getWorldBorder().isWithinBounds_(this.getBoundingBox())) {
                double dist = this.level.getWorldBorder().getDistanceToBorder(this) + this.level.getWorldBorder().getDamageSafeZone();
                if (dist < 0) {
                    double dpB = this.level.getWorldBorder().getDamagePerBlock();
                    if (dpB > 0) {
                        this.hurt(EvolutionDamage.BORDER, Math.max(1, Mth.floor(-dist * dpB)));
                    }
                }
            }
            if (this.isEyeInFluid(FluidTags.WATER) && !this.level.getBlockState_(this.getEyePosition()).is(Blocks.BUBBLE_COLUMN)) {
                boolean drowns = !this.canBreatheUnderwater() &&
                                 !MobEffectUtil.hasWaterBreathing((LivingEntity) (Object) this) &&
                                 (!isPlayer || !((Player) (Object) this).getAbilities().invulnerable);
                if (drowns) {
                    this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
                    if (this.getAirSupply() == -EvolutionDamage.DROWNING_IMMUNITY) {
                        this.setAirSupply(0);
                        Vec3 vel = this.getDeltaMovement();
                        for (int i = 0; i < 8; ++i) {
                            double dx = this.random.nextDouble() - this.random.nextDouble();
                            double dy = this.random.nextDouble() - this.random.nextDouble();
                            double dz = this.random.nextDouble() - this.random.nextDouble();
                            this.level.addParticle(ParticleTypes.BUBBLE, this.getX() + dx, this.getY() + dy, this.getZ() + dz, vel.x, vel.y, vel.z);
                        }
                        this.hurt(EvolutionDamage.DROWN, 10.0F);
                    }
                }
                if (!this.level.isClientSide && this.isPassenger() && this.getVehicle() != null && !this.getVehicle().rideableUnderWater()) {
                    this.stopRiding();
                }
            }
            else if (this.getAirSupply() < this.getMaxAirSupply()) {
                this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
            }
            if (!this.level.isClientSide) {
                BlockPos pos = this.blockPosition();
                if (!Objects.equal(this.lastPos, pos)) {
                    this.lastPos = pos;
                    this.onChangedBlock(pos);
                }
            }
        }
        if (this.isAlive() && (this.isInWaterRainOrBubble() || this.isInPowderSnow)) {
            if (!this.level.isClientSide && this.wasOnFire) {
                this.playEntityOnFireExtinguishedSound();
            }
            this.clearFire();
        }
        if (this.hurtTime > 0) {
            --this.hurtTime;
        }
        if (this.isDeadOrDying() && this.level.shouldTickDeath(this)) {
            this.tickDeath();
        }
        if (this.lastHurtByPlayerTime > 0) {
            --this.lastHurtByPlayerTime;
        }
        else {
            this.lastHurtByPlayer = null;
        }
        if (this.lastHurtMob != null && !this.lastHurtMob.isAlive()) {
            this.lastHurtMob = null;
        }
        if (this.lastHurtByMob != null) {
            if (!this.lastHurtByMob.isAlive()) {
                this.setLastHurtByMob(null);
            }
            else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
                this.setLastHurtByMob(null);
            }
        }
        this.tickEffects();
        this.animStepO = this.animStep;
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        //Special attacks
        if (this.specialAttackGracePeriod > 0) {
            if (--this.specialAttackGracePeriod == 0) {
                this.specialAttackFollowUp = 0;
            }
        }
        if (this.isSpecialAttacking) {
            assert this.specialAttackType != null;
            int totalTime = this.specialAttackType.getAttackTime();
            if (++this.specialAttackTime >= totalTime) {
                this.stopSpecialAttack(IMelee.StopReason.END);
            }
        }
        else {
            if (this.specialAttackLockedTicks > 0) {
                if (--this.specialAttackLockedTicks == 0) {
                    this.specialAttackTime = 0;
                    this.specialAttackFollowUp = 0;
                }
            }
        }
        this.level.getProfiler().pop();
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to reset animation position.
     */
    @Overwrite
    public void calculateEntityAnimation(LivingEntity entity, boolean flies) {
        entity.animationSpeedOld = entity.animationSpeed;
        if (this.isOnGround() || this.isInWater() || flies) {
            boolean isSwimming = this.isInWater() && this.getPose() == Pose.SWIMMING;
            if (entity.animationSpeed < 0) {
                entity.animationSpeed = -entity.animationSpeed;
            }
            double dx = EarthHelper.deltaBlockCoordinate(entity.getX(), entity.xo);
            double dy = flies || isSwimming ? entity.getY() - entity.yo : 0;
            double dz = EarthHelper.deltaBlockCoordinate(entity.getZ(), entity.zo);
            float dS = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * Mth.PI;
            if (dS == 0 && entity.animationSpeed <= 1E-3) {
                entity.animationPosition = 0;
                entity.moveDist = 0;
            }
            entity.animationSpeed += (dS - entity.animationSpeed) * (Mth.PI / 10);
            if (dx != 0 && dz != 0) {
                if (!isSwimming) {
                    double angle = -Mth.atan2(dx, dz) * Mth.RAD_TO_DEG;
                    //Should be backwards
                    if (Math.abs(Mth.wrapDegrees(entity.getYRot() - angle)) > 91) {
                        entity.animationSpeed = -entity.animationSpeed;
                    }
                }
            }
            else {
                if (entity.animationSpeedOld < 0) {
                    entity.animationSpeed = -entity.animationSpeed;
                }
            }
        }
        else {
            entity.animationSpeed *= 0.9F;
        }
        entity.animationPosition += entity.animationSpeed;
        if (entity.animationPosition >= 4 * Mth.PI) {
            entity.animationPosition -= 4 * Mth.PI;
        }
        else if (entity.animationPosition <= -4 * Mth.PI) {
            entity.animationPosition += 4 * Mth.PI;
        }
    }

    @Unique
    private void calculateWallImpact(double speedX, double speedZ, double mass) {
        double motionXPost = this.getDeltaMovement().x;
        double deltaSpeedX = Math.abs(speedX) - Math.abs(motionXPost);
        //noinspection ConstantConditions
        boolean remote = this.level.isClientSide && (Object) this instanceof Player;
        deltaSpeedX *= 20;
        float damage = 0;
        if (deltaSpeedX >= 6) {
            double kineticEnergy = 0.5 * deltaSpeedX * deltaSpeedX * mass;
            AABB bb = this.getBoundingBox();
            double xCoord = speedX >= 0 ? bb.maxX + 0.01 : bb.minX - 0.01;
            int x = Mth.floor(xCoord);
            int numberOfBlocks = 0;
            double slowDown = 0;
            int y0 = Mth.floor(bb.minY);
            int y1 = Mth.floor(bb.maxY);
            int z0 = Mth.floor(bb.minZ);
            int z1 = Mth.floor(bb.maxZ);
            if (this.level.hasChunksAt(x, y0, z0, x, y1, z1)) {
                for (int y = y0; y <= y1; y++) {
                    for (int z = z0; z <= z1; z++) {
                        numberOfBlocks++;
                        BlockState stateAtPos = this.level.getBlockState_(x, y, z);
                        Block blockAtPos = stateAtPos.getBlock();
                        if (blockAtPos instanceof ICollisionBlock collisionBlock) {
                            slowDown += collisionBlock.getSlowdownSide(stateAtPos);
                            //noinspection ConstantConditions
                            if (remote) {
                                //noinspection ObjectAllocationInLoop
                                EvolutionClient.sendToServer(new PacketCSCollision(BlockPos.asLong(x, y, z), speedX, Direction.Axis.X));
                            }
                        }
                        else {
                            slowDown += 1;
                        }
                    }
                }
            }
            if (numberOfBlocks > 0) {
                slowDown /= numberOfBlocks;
            }
            if (slowDown > 0) {
                slowDown = 1.0 - slowDown;
            }
            double distanceOfSlowdown = slowDown + this.getBbWidth() / 4;
            double forceOfImpact = kineticEnergy / distanceOfSlowdown;
            float area = this.getBbHeight() * this.getBbWidth();
            double stress = forceOfImpact / area;
            damage += (float) Math.pow(stress, 1.6) / 1_750_000;
        }
        double motionZPost = this.getDeltaMovement().z;
        double deltaSpeedZ = Math.abs(speedZ) - Math.abs(motionZPost);
        deltaSpeedZ *= 20;
        if (deltaSpeedZ >= 6) {
            double kineticEnergy = 0.5 * deltaSpeedZ * deltaSpeedZ * mass;
            AABB bb = this.getBoundingBox();
            double zCoord = speedZ >= 0 ? bb.maxZ + 0.01 : bb.minZ - 0.01;
            int z = Mth.floor(zCoord);
            int numberOfBlocks = 0;
            double slowDown = 0;
            int y0 = Mth.floor(bb.minY);
            int y1 = Mth.floor(bb.maxY);
            int x0 = Mth.floor(bb.minX);
            int x1 = Mth.floor(bb.maxX);
            if (this.level.hasChunksAt(x0, y0, z, x1, y1, z)) {
                for (int x = x0; x <= x1; x++) {
                    for (int y = y0; y <= y1; y++) {
                        numberOfBlocks++;
                        BlockState stateAtPos = this.level.getBlockState_(x, y, z);
                        Block blockAtPos = stateAtPos.getBlock();
                        if (blockAtPos instanceof ICollisionBlock collisionBlock) {
                            slowDown += collisionBlock.getSlowdownSide(stateAtPos);
                            //noinspection ConstantConditions
                            if (remote) {
                                //noinspection ObjectAllocationInLoop
                                EvolutionClient.sendToServer(new PacketCSCollision(BlockPos.asLong(x, y, z), speedZ, Direction.Axis.Z));
                            }
                        }
                        else {
                            slowDown += 1;
                        }
                    }
                }
            }
            if (numberOfBlocks > 0) {
                slowDown /= numberOfBlocks;
            }
            if (slowDown > 0) {
                slowDown = 1.0 - slowDown;
            }
            double distanceOfSlowdown = slowDown + this.getBbWidth() / 4;
            double forceOfImpact = kineticEnergy / distanceOfSlowdown;
            float area = this.getBbHeight() * this.getBbWidth();
            double stress = forceOfImpact / area;
            damage += (float) Math.pow(stress, 1.6) / 1_500_000;
        }
        if (damage >= 1.0f) {
            if (!this.level.isClientSide) {
                this.hurt(EvolutionDamage.WALL_IMPACT, damage);
            }
            else //noinspection ConstantConditions
                if ((LivingEntity) (Object) this instanceof Player) {
                    EvolutionClient.sendToServer(new PacketCSImpactDamage(damage));
                }
        }
    }

    @Shadow
    public abstract boolean canBeAffected(MobEffectInstance pPotioneffect);

    @Shadow
    public abstract boolean canBreatheUnderwater();

    @Override
    public boolean canPerformFollowUp(IMelee.IAttackType type) {
        if (this.specialAttackType != type) {
            return false;
        }
        if (!this.isOnGracePeriod()) {
            return false;
        }
        return type.getFollowUps() >= this.specialAttackFollowUp;
    }

    @Shadow
    public abstract boolean canSpawnSoulSpeedParticle();

    /**
     * @author TheGreatWolf
     * @reason Make fall damage depend on kinetic energy, not fall distance.
     */
    @Override
    @Overwrite
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        boolean flag = super.causeFallDamage(fallDistance, multiplier, source);
        float amount = LivingHooks.calculateFallDamage((LivingEntity) (Object) this, this.getDeltaMovement().y, 1 - multiplier, false);
        if (fallDistance > 1 / 16.0f) {
            this.playBlockFallSound();
        }
        if (amount > 1) {
            this.playSound(this.getFallDamageSound((int) amount), 1.0F, 1.0F);
            return true;
        }
        return flag;
    }

    @Shadow
    protected abstract void checkAutoSpinAttack(AABB pBoundingBoxBeforeSpin, AABB pBoundingBoxAfterSpin);

    @Shadow
    protected abstract boolean checkBedExists();

    /**
     * @author TheGreatWolf
     * @reason Use supportingPos
     */
    @Override
    @Overwrite
    public void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (!this.isInWater()) {
            this.updateInWaterStateAndDoWaterCurrentPushing();
        }
        if (!this.level.isClientSide && onGround && this.fallDistance > 0.0F) {
            this.removeSoulSpeed();
            this.tryAddSoulSpeed();
        }
        if (!this.level.isClientSide && this.fallDistance > 3.0F && onGround && !state.isAir()) {
            double entityX = this.getX();
            double entityZ = this.getZ();
            BlockPos entityPos = this.blockPosition();
            if (pos.getX() != entityPos.getX() || pos.getZ() != entityPos.getZ()) {
                double dx = entityX - pos.getX() - 0.5;
                double dz = entityZ - pos.getZ() - 0.5;
                double max = Math.max(Math.abs(dx), Math.abs(dz));
                entityX = pos.getX() + 0.5 + dx / max * 0.5;
                entityZ = pos.getZ() + 0.5 + dz / max * 0.5;
            }
            float j = Mth.ceil(this.fallDistance - 3.0F);
            double k = Math.min(0.2 + j / 15.0, 2.5);
            int count = (int) (150 * k);
            ((ServerLevel) this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), entityX, this.getY(), entityZ,
                                                     count, 0, 0, 0, 0.15);
        }
        super.checkFallDamage(y, onGround, state, pos);
    }

    @Shadow
    protected abstract boolean checkTotemDeathProtection(DamageSource p_21263_);

    @Shadow
    protected abstract void completeUsingItem();

    @Shadow
    protected abstract int decreaseAirSupply(int pCurrentAir);

    @Shadow
    protected abstract void detectEquipmentUpdates();

    @Shadow
    public abstract void die(DamageSource p_21014_);

    @Shadow
    protected abstract void doPush(Entity pEntity);

    @Shadow
    public abstract float getAbsorptionAmount();

    @Override
    public double getAcceleration() {
        double force = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double mass = this.getAttributeValue(EvolutionAttributes.MASS);
        return force / mass;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public Collection<MobEffectInstance> getActiveEffects() {
        //Iterate over the values on the map itself!
        Evolution.deprecatedMethod();
        return this.activeEffects.values();
    }

    @Shadow
    public abstract int getArrowCount();

    @Shadow
    public abstract @Nullable AttributeInstance getAttribute(Attribute p_21052_);

    @Shadow
    public abstract double getAttributeBaseValue(Attribute p_21173_);

    @Shadow
    public abstract double getAttributeValue(Attribute p_21134_);

    @Shadow
    public abstract AttributeMap getAttributes();

    @Override
    public double getBaseMass() {
        return 70;
    }

    @Shadow
    public abstract @Nullable Direction getBedOrientation();

    @Shadow
    public abstract CombatTracker getCombatTracker();

    @Shadow
    protected abstract int getCurrentSwingDuration();

    @Shadow
    protected abstract @Nullable SoundEvent getDeathSound();

    @Shadow
    public abstract @Nullable MobEffectInstance getEffect(MobEffect p_21125_);

    @Override
    public EffectHelper getEffectHelper() {
        return this.effectHelper;
    }

    @Shadow
    protected abstract SoundEvent getFallDamageSound(int pHeight);

    @Override
    public int getFollowUp() {
        return this.specialAttackFollowUp;
    }

    @Override
    public float getFrictionModifier() {
        return (float) this.getAttributeValue(EvolutionAttributes.FRICTION);
    }

    @Shadow
    public abstract float getHealth();

    @Unique
    private double getHeightForNotDrowning() {
        return Math.min(this.getEyeHeight() * 0.9, this.getBbHeight() * 0.75);
    }

    @Shadow
    public abstract ItemStack getItemInHand(InteractionHand interactionHand);

    /**
     * @author TheGreatWolf
     * @reason Replace the method to handle Evolution's physics.
     * Represents the upwards acceleration of the Entity when jumping.
     */
    @Overwrite
    public float getJumpPower() {
        return 0.25f * this.getBlockJumpFactor();
    }

    @Override
    public double getLegSlowdown() {
        return this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) * 0.065;
    }

    @Override
    public short getLightEmission() {
        short lightM = this.getItemInHand(InteractionHand.MAIN_HAND).getLightEmission();
        if (lightM == 0) {
            return this.getItemInHand(InteractionHand.OFF_HAND).getLightEmission();
        }
        if (lightM == 0b1_1111_1_1111_1_1111) {
            return 0b1_1111_1_1111_1_1111;
        }
        short lightO = this.getItemInHand(InteractionHand.OFF_HAND).getLightEmission();
        if (lightO == 0) {
            return lightM;
        }
        if (lightO == 0b1_1111_1_1111_1_1111) {
            return 0b1_1111_1_1111_1_1111;
        }
        return DynamicLights.combine(lightM, lightO);
    }

    @Shadow
    public abstract ItemStack getMainHandItem();

    @Shadow
    public abstract float getMaxHealth();

    @Override
    public int getNoJumpDelay() {
        return this.noJumpDelay;
    }

    @Shadow
    public abstract ItemStack getOffhandItem();

    @Shadow
    public abstract Optional<BlockPos> getSleepingPos();

    @Shadow
    protected abstract float getSoundVolume();

    @Override
    public float getSpecialAttackProgress(float partialTicks) {
        if (this.isOnGracePeriod()) {
            assert this.specialAttackType != null;
            return 1.0f;
        }
        if (this.isLockedInSpecialAttack()) {
            partialTicks = 0.0f;
        }
        assert this.specialAttackType != null;
        return (this.specialAttackTime + partialTicks) / this.specialAttackType.getAttackTime();
    }

    @Override
    public @Nullable IMelee.IAttackType getSpecialAttackType() {
        return this.specialAttackType;
    }

    @Shadow
    public abstract int getStingerCount();

    @Shadow
    public abstract ItemStack getUseItem();

    @Shadow
    public abstract int getUseItemRemainingTicks();

    @Shadow
    public abstract float getVoicePitch();

    @Shadow
    protected abstract float getWaterSlowDown();

    /**
     * @author TheGreatWolf
     * @reason Should never be called, buoyancy will ensure entities that should float will float and entities that should sink will sink.
     */
    @Overwrite
    public void goDownInWater() {
        Evolution.warn("Calling goDownInWater! This method should not be called!");
    }

    @Unique
    private void handleLadderMotion() {
        if (!this.onClimbable()) {
            return;
        }
        //noinspection ConstantConditions
        if ((Object) this instanceof Player && ((Player) (Object) this).getAbilities().flying) {
            return;
        }
        Vec3 deltaMovement = this.getDeltaMovement();
        double speedX = deltaMovement.x;
        double speedY = deltaMovement.y;
        double speedZ = deltaMovement.z;
        BlockState state = this.getFeetBlockState();
        Block block = state.getBlock();
        double dx = 0;
        double dz = 0;
        if (block instanceof IClimbable climbable) {
            double climbableOffset = climbable.getXPos(state);
            if (!Double.isNaN(climbableOffset)) {
                if (climbableOffset < 0) {
                    double temp = this.blockPosition().getX() - climbableOffset + this.getBbWidth() / 2.0;
                    if (temp < this.getX()) {
                        dx = (this.getX() - temp) / 20.0;
                    }
                }
                else if (climbableOffset > 0) {
                    double temp = this.blockPosition().getX() + 1 - climbableOffset - this.getBbWidth() / 2.0;
                    if (temp > this.getX()) {
                        dx = (this.getX() - temp) / 20.0;
                    }
                }
            }
            climbableOffset = climbable.getZPos(state);
            if (!Double.isNaN(climbableOffset)) {
                if (climbableOffset < 0) {
                    double temp = this.blockPosition().getZ() - climbableOffset + this.getBbWidth() / 2.0;
                    if (temp < this.getZ()) {
                        dz = (this.getZ() - temp) / 20.0;
                    }
                }
                else if (climbableOffset > 0) {
                    double temp = this.blockPosition().getZ() + 1 - climbableOffset - this.getBbWidth() / 2.0;
                    if (temp > this.getZ()) {
                        dz = (this.getZ() - temp) / 20.0;
                    }
                }
            }
        }
        this.fallDistance = 1.0F;
        double newX;
        double newZ;
        if (!this.isOnGround()) {
            newX = MathHelper.clamp(speedX, -0.025, 0.025);
            newX *= 0.8;
            newX -= dx;
            newZ = MathHelper.clamp(speedZ, -0.025, 0.025);
            newZ *= 0.8;
            newZ -= dz;
        }
        else {
            newX = speedX;
            newZ = speedZ;
        }
        double newY = speedY < -0.3 ? speedY : Math.max(speedY, this.isCrouching() ? 0 : -0.15);
        //noinspection ConstantConditions
        if (newY < 0 && block != Blocks.SCAFFOLDING && this.isCrouching() && (Object) this instanceof Player) {
            newY = 0;
        }
        this.setDeltaMovement(newX, newY, newZ);
    }

    @SuppressWarnings("ConstantConditions")
    @Unique
    private void handleNormalMovement(Vec3d travelVector, Fluid fluid, double slowdown) {
        Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        double mass = this.getAttributeValue(EvolutionAttributes.MASS);
        double gravity = 0;
        try (Physics physics = Physics.getInstance(this, fluid)) {
            boolean isFlyingPlayer = (Object) this instanceof Player player && player.getAbilities().flying;
            physics.calcAccAbsolute(this, travelVector, physics.calcAccMagnitude(this, slowdown));
            double accX = physics.getAccAbsoluteX();
            double accY = physics.getAccAbsoluteY();
            double accZ = physics.getAccAbsoluteZ();
            if ((this.horizontalCollision || this.jumping) && this.onClimbable()) {
                motionY = BlockUtils.getLadderUpSpeed(this.getFeetBlockState());
            }
            else if (!this.isNoGravity() && !isFlyingPlayer) {
                gravity = physics.calcAccGravity();
                accY += gravity;
            }
            if (this.isAffectedByFluids() && !isFlyingPlayer) {
                accY += physics.calcForceBuoyancy(this) / mass;
            }
            if (this.hasCollidedOnXAxis()) {
                accX = Math.signum(accX) * 0.001;
            }
            if (this.hasCollidedOnZAxis()) {
                accZ = Math.signum(accZ) * 0.001;
            }
            //Pseudo-forces
            double accCoriolisY = isFlyingPlayer ? 0 : physics.calcAccCoriolisY();
            double accCentrifugalY = isFlyingPlayer ? 0 : physics.calcAccCentrifugalY();
            //Dissipative Forces
            double legSlowDownX = 0;
            double legSlowDownZ = 0;
            if (this.isOnGround() || !this.isAffectedByFluids()) {
                double legSlowDown = this.getLegSlowdown();
                legSlowDownX = motionX * legSlowDown;
                legSlowDownZ = motionZ * legSlowDown;
            }
            double frictionX = 0;
            double frictionZ = 0;
            if (this.isOnGround() && !(accX != 0 || accZ != 0) && (motionX != 0 || motionZ != 0)) {
                double norm = Mth.fastInvSqrt(motionX * motionX + motionZ * motionZ);
                double frictionAcc = physics.calcAccNormal() * physics.calcKineticFrictionCoef(this);
                frictionX = motionX * norm * frictionAcc;
                frictionZ = motionZ * norm * frictionAcc;
            }
            double dissipativeX = legSlowDownX + frictionX;
            if (Math.abs(dissipativeX) > Math.abs(motionX)) {
                dissipativeX = motionX;
            }
            double dissipativeZ = legSlowDownZ + frictionZ;
            if (Math.abs(dissipativeZ) > Math.abs(motionZ)) {
                dissipativeZ = motionZ;
            }
            //Update Motion
            motionX += accX - dissipativeX;
            motionY += accY + accCoriolisY + accCentrifugalY;
            motionZ += accZ - dissipativeZ;
            if (Double.isNaN(motionX)) {
                motionX = 0;
            }
            if (Double.isNaN(motionY)) {
                motionY = 0;
            }
            if (Double.isNaN(motionZ)) {
                motionZ = 0;
            }
        }
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.handleLadderMotion();
        double oldY = this.getY();
        this.move(MoverType.SELF, this.getDeltaMovement());
        boolean updatedEmerge = false;
        if (this.horizontalCollision) {
            this.calculateWallImpact(motionX, motionZ, mass);
            if (LivingHooks.hasEmptySpaceForEmerging(this, motionX, motionY, motionZ, oldY)) {
                updatedEmerge = true;
                boolean canEmergeFromFluid = this.isInAnyFluid() && !this.hasAnyFluidInEye();
                if (canEmergeFromFluid) {
                    this.emergeTicks = 7;
                }
                if (this.emergeTicks > 0) {
                    --this.emergeTicks;
                    Vec3 movement = this.getDeltaMovement();
                    motionY = -5 * gravity;
                    this.setDeltaMovement(movement.x, motionY, movement.z);
                }
            }
        }
        if (!updatedEmerge) {
            this.emergeTicks = 0;
        }
    }

    @Shadow
    public abstract boolean hasEffect(MobEffect p_21024_);

    /**
     * @author TheGreatWolf
     * @reason Overwrite to use Evolution Damage Sources.
     */
    @Override
    @Overwrite
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.level.isClientSide) {
            return false;
        }
        if (this.isDeadOrDying()) {
            return false;
        }
        if (source.isFire() && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        }
        amount = ((EvolutionCombatTracker) this.combatTracker).accountForImmunity(source, amount);
        if (amount <= 0) {
            return false;
        }
        if (this.isSleeping() && !this.level.isClientSide) {
            this.stopSleeping();
        }
        this.noActionTime = 0;
        this.animationSpeed = 1.5F;
        this.lastHurt = amount;
        this.actuallyHurt(source, amount);
        this.hurtDuration = 10;
        this.hurtTime = this.hurtDuration;
        this.hurtDir = 0.0F;
        Entity sourceEntity = source.getEntity();
        if (sourceEntity != null) {
            if (sourceEntity instanceof LivingEntity livingSource && !source.isNoAggro()) {
                this.setLastHurtByMob(livingSource);
            }
            if (sourceEntity instanceof Player playerSource) {
                this.lastHurtByPlayerTime = 100;
                this.lastHurtByPlayer = playerSource;
            }
            else if (sourceEntity instanceof TamableAnimal tamable) {
                if (tamable.isTame()) {
                    this.lastHurtByPlayerTime = 100;
                    LivingEntity owner = tamable.getOwner();
                    if (owner instanceof Player playerOwner) {
                        this.lastHurtByPlayer = playerOwner;
                    }
                    else {
                        this.lastHurtByPlayer = null;
                    }
                }
            }
        }
        if (source instanceof EntityDamageSource entitySource && entitySource.isThorns()) {
            this.level.broadcastEntityEvent(this, EntityStates.THORNS_HIT_SOUND);
        }
        else {
            byte state = getState(source);
            this.level.broadcastEntityEvent(this, state);
        }
        if (source instanceof DamageSourceEntity) { //Replace for Evolution Damage
            this.markHurt();
        }
        if (sourceEntity != null) {
            double dx = sourceEntity.getX() - this.getX();
            double dz;
            for (dz = sourceEntity.getZ() - this.getZ(); dx * dx + dz * dz < 1.0E-4; dz = (Math.random() - Math.random()) * 0.01) {
                dx = (Math.random() - Math.random()) * 0.01;
            }
            this.hurtDir = (float) (MathHelper.atan2Deg(dz, dx) - this.getYRot());
            this.knockback(0.4F, dx, dz);
        }
        else {
            this.hurtDir = (int) (Math.random() * 2.0) * 180;
        }
        if (this.isDeadOrDying()) {
            if (!this.checkTotemDeathProtection(source)) {
                SoundEvent soundevent = this.getDeathSound();
                if (soundevent != null) {
                    this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
                }
                this.die(source);
            }
        }
        else {
            this.playHurtSound(source);
        }
        this.lastDamageSource = source;
        this.lastDamageStamp = this.level.getGameTime();
        //noinspection ConstantConditions
        if ((Object) this instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger(serverPlayer, source, amount, amount, false);
            float blockedAmount = 0.0F;
            if (blockedAmount > 0.0F && blockedAmount < 3.402_823_5E37F) {
                serverPlayer.awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(blockedAmount * 10.0F));
            }
        }
        if (sourceEntity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(serverPlayer, this, source, amount, amount, false);
        }
        return true;
    }

    @Shadow
    protected abstract int increaseAirSupply(int pCurrentAir);

    @Override
    public double intrinsicSlowdown() {
        return this.getBbHeight() * 0.5;
    }

    @Shadow
    protected abstract boolean isAffectedByFluids();

    @Override
    @Shadow
    public abstract boolean isAlive();

    @Override
    public boolean isCameraLocked() {
        if (this.isSpecialAttacking() || this.isLockedInSpecialAttack()) {
            assert this.specialAttackType != null;
            return this.specialAttackType.isCameraLocked(this.specialAttackTime);
        }
        return false;
    }

    @Shadow
    public abstract boolean isDeadOrDying();

    @Shadow
    public abstract boolean isEffectiveAi();

    @Shadow
    public abstract boolean isFallFlying();

    @Shadow
    protected abstract boolean isImmobile();

    @Override
    public boolean isInHitTicks() {
        if (!this.isSpecialAttacking()) {
            return false;
        }
        if (this.isOnGracePeriod()) {
            return false;
        }
        assert this.specialAttackType != null;
        return this.specialAttackType.isHitTick(this.specialAttackTime);
    }

    @Override
    public boolean isLateralMotionLocked() {
        if (this.isSpecialAttacking() || this.isLockedInSpecialAttack()) {
            assert this.specialAttackType != null;
            return this.specialAttackType.isLateralMotionLocked(this.specialAttackTime);
        }
        return false;
    }

    @Override
    public boolean isLockedInSpecialAttack() {
        return this.specialAttackLockedTicks > 0;
    }

    @Override
    public boolean isLongitudinalMotionLocked() {
        if (this.isSpecialAttacking() || this.isLockedInSpecialAttack()) {
            assert this.specialAttackType != null;
            return this.specialAttackType.isLongitudinalMotionLocked(this.specialAttackTime);
        }
        return false;
    }

    @Override
    public boolean isOnGracePeriod() {
        return this.specialAttackGracePeriod > 0;
    }

    @Shadow
    public abstract boolean isSensitiveToWater();

    @Shadow
    public abstract boolean isSleeping();

    @Override
    public boolean isSpecialAttacking() {
        return this.isSpecialAttacking;
    }

    @Shadow
    public abstract boolean isUsingItem();

    @Override
    public boolean isVerticalMotionLocked() {
        if (this.isSpecialAttacking() || this.isLockedInSpecialAttack()) {
            assert this.specialAttackType != null;
            return this.specialAttackType.isVerticalMotionLocked(this.specialAttackTime);
        }
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason Replace the method to handle Evolution's physics.
     * Represents the jump force applied during a single tick for the entity to jump.
     */
    @Overwrite
    public void jumpFromGround() {
        float upwardsBaseAcc = this.getJumpPower();
        MobEffectInstance effect = this.getEffect(MobEffects.JUMP);
        if (effect != null) {
            upwardsBaseAcc *= 1.0f + (effect.getAmplifier() + 1) / 10.0f;
        }
        double baseMass = this.getAttributeBaseValue(EvolutionAttributes.MASS);
        double totalMass = this.getAttributeValue(EvolutionAttributes.MASS);
        double upwardsForce = Math.min(baseMass * 1.25, totalMass) * upwardsBaseAcc;
        double upwardsAcc = upwardsForce / totalMass;
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, upwardsAcc, motion.z);
        this.hasImpulse = true;
        this.noJumpDelay = 10;
    }

    /**
     * @author TheGreatWolf
     * @reason Should never be called, buoyancy will ensure entities that should float will float and entities that should sink will sink.
     */
    @Overwrite
    public void jumpInLiquid(TagKey<net.minecraft.world.level.material.Fluid> fluid) {
        Evolution.warn("Calling jumpInLiquid! This method should not be called!");
    }

    /**
     * @author TheGreatWolf
     * @reason Use Evolution damage.
     */
    @Override
    @Overwrite
    public void kill() {
        this.hurt(EvolutionDamage.KILL, Float.MAX_VALUE);
    }

    /**
     * @author TheGreatWolf
     * @reason Send tilt packet
     */
    @Overwrite
    public void knockback(double strength, double x, double z) {
        strength *= 1 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        if (strength > 0) {
            this.hasImpulse = true;
            double length = Math.sqrt(x * x + z * z);
            if (length < 1E-4) {
                x = 0;
                z = 0;
            }
            else {
                x /= length;
                z /= length;
            }
            x *= strength;
            z *= strength;
            Vec3 velocity = this.getDeltaMovement();
            this.setDeltaMovement(velocity.x - x, this.onGround ? velocity.y + strength : velocity.y, velocity.z - z);
            this.hurtMarked = true;
            //noinspection ConstantConditions
            if ((Object) this instanceof ServerPlayer player) {
                player.connection.send(new PacketSCUpdateCameraTilt(player));
            }
        }
    }

    @Shadow
    protected abstract Brain<?> makeBrain(Dynamic<?> pDynamic);

    @Shadow
    protected abstract void onChangedBlock(BlockPos pPos);

    /**
     * @author TheGreatWolf
     * @reason Simplify the method, remove optional overhead (simply check for null if you really need)
     */
    @Overwrite
    public boolean onClimbable() {
        if (this.isSpectator()) {
            return false;
        }
        BlockPos pos = this.blockPosition();
        BlockState state = this.getFeetBlockState();
        return state.getBlock().isLadder(state, this.level, pos.getX(), pos.getY(), pos.getZ(), (LivingEntity) (Object) this);
    }

    @Shadow
    protected abstract void onEffectAdded(MobEffectInstance p_147190_, @Nullable Entity p_147191_);

    @Shadow
    protected abstract void onEffectRemoved(MobEffectInstance pEffect);

    @Shadow
    public abstract void onEffectUpdated(MobEffectInstance p_147192_, boolean p_147193_, @Nullable Entity p_147194_);

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;activeEffects:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit(LivingEntity instance, Map<MobEffect, MobEffectInstance> value) {
        this.activeEffects = new R2OHashMap<>();
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;" +
                                                                    "combatTracker:Lnet/minecraft/world/damagesource/CombatTracker;", opcode = Opcodes.PUTFIELD))
    private void onInit(LivingEntity instance, CombatTracker value) {
        this.combatTracker = new EvolutionCombatTracker((LivingEntity) (Object) this);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void onInit(LivingEntity instance, float pHealth) {
        AttributeInstance massAtr = this.getAttribute(EvolutionAttributes.MASS);
        assert massAtr != null;
        massAtr.setBaseValue(this.getBaseMass());
        AttributeInstance walkForceAtr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        assert walkForceAtr != null;
        walkForceAtr.setBaseValue(this.getBaseWalkForce());
        AttributeInstance healthAtr = this.getAttribute(Attributes.MAX_HEALTH);
        assert healthAtr != null;
        healthAtr.setBaseValue(this.getBaseHealth());
        this.setHealth(this.getMaxHealth());
        AttributeInstance damageAtr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAtr != null) {
            damageAtr.setBaseValue(this.getBaseAttackDamage());
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private @Nullable HashMap onInitRemoveMap() {
        return null;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean onSoulSpeedBlock() {
        return this.level.getBlockState_(this.getFrictionPos()).is(BlockTags.SOUL_SPEED_BLOCKS);
    }

    @Override
    public void performFollowUp() {
        assert this.specialAttackType != null;
        this.startSpecialAttack(this.specialAttackType);
        if (this.specialAttackType == IMelee.BARE_HAND_ATTACK && this.getOffhandItem().isEmpty()) {
            this.specialAttackTime = (byte) (IMelee.BARE_HAND_ATTACK.getAttackTime() / 2);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Use supportingPos
     */
    @Overwrite
    public void playBlockFallSound() {
        if (!this.isSilent()) {
            BlockPos landingPos = this.getOnPos();
            BlockState landingState = this.level.getBlockState_(landingPos.getX(), landingPos.getY(), landingPos.getZ());
            BlockState aboveState = this.level.getBlockState_(landingPos.getX(), landingPos.getY() + 1, landingPos.getZ());
            if (aboveState.is(EvolutionBlockTags.BLOCKS_COMBINED_STEP_PARTICLE)) {
                landingState = aboveState;
            }
            if (!landingState.isAir()) {
                SoundType sound = landingState.getSoundType();
                this.playSound(sound.getFallSound(), sound.getVolume() * 0.5F, sound.getPitch() * 0.75F);
            }
        }
    }

    @Shadow
    protected abstract void playHurtSound(DamageSource p_21160_);

    /**
     * @author TheGreatWolf
     * @reason Prevent physics from being called on the client.
     */
    @Overwrite
    public void pushEntities() {
        if (this.level.isClientSide) {
            List<Player> entities = this.level.getEntities(EntityTypeTest.forClass(Player.class), this.getBoundingBox(),
                                                           EntitySelector.pushableBy(this));
            for (int i = 0, len = entities.size(); i < len; i++) {
                this.doPush(entities.get(i));
            }
            return;
        }
        List<Entity> pushable = this.level.getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this));
        if (!pushable.isEmpty()) {
            int maxCramming = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if (maxCramming > 0 && pushable.size() > maxCramming - 1 && this.random.nextInt(4) == 0) {
                int i = 0;
                for (int k = 0, len = pushable.size(); k < len; ++k) {
                    if (!pushable.get(k).isPassenger()) {
                        ++i;
                    }
                }
                if (i > maxCramming - 1) {
                    this.hurt(DamageSource.CRAMMING, 6.0F);
                }
            }
            for (int l = 0, len = pushable.size(); l < len; ++l) {
                Entity entity = pushable.get(l);
                this.doPush(entity);
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void readAdditionalSaveData(CompoundTag tag) {
        this.setAbsorptionAmount(tag.getFloat("AbsorptionAmount"));
        if (tag.contains("Attributes", Tag.TAG_LIST) && !this.level.isClientSide) {
            this.getAttributes().load(tag.getList("Attributes", Tag.TAG_COMPOUND));
        }
        if (tag.contains("ActiveEffects", Tag.TAG_LIST)) {
            ListTag effects = tag.getList("ActiveEffects", Tag.TAG_COMPOUND);
            for (int i = 0; i < effects.size(); ++i) {
                CompoundTag compound = effects.getCompound(i);
                MobEffectInstance effect = MobEffectInstance.load(compound);
                if (effect != null) {
                    this.activeEffects.put(effect.getEffect(), effect);
                }
            }
        }
        if (tag.contains("Health", Tag.TAG_ANY_NUMERIC)) {
            this.setHealth(tag.getFloat("Health"));
        }
        this.hurtTime = tag.getShort("HurtTime");
        this.deathTime = tag.getShort("DeathTime");
        this.lastHurtByMobTimestamp = tag.getInt("HurtByTimestamp");
        if (tag.contains("Team", Tag.TAG_STRING)) {
            String teamName = tag.getString("Team");
            PlayerTeam team = this.level.getScoreboard().getPlayerTeam(teamName);
            boolean teamExists = team != null && this.level.getScoreboard().addPlayerToTeam(this.getStringUUID(), team);
            if (!teamExists) {
                LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", teamName);
            }
        }
        if (tag.getBoolean("FallFlying")) {
            this.setSharedFlag(7, true);
        }
        if (tag.contains("SleepingX", Tag.TAG_ANY_NUMERIC) && tag.contains("SleepingY", Tag.TAG_ANY_NUMERIC) && tag.contains("SleepingZ", Tag.TAG_ANY_NUMERIC)) {
            BlockPos blockpos = new BlockPos(tag.getInt("SleepingX"), tag.getInt("SleepingY"), tag.getInt("SleepingZ"));
            this.setSleepingPos(blockpos);
            this.entityData.set(DATA_POSE, Pose.SLEEPING);
            if (!this.firstTick) {
                this.setPosToBed(blockpos);
            }
        }
        if (tag.contains("Brain", Tag.TAG_COMPOUND)) {
            this.brain = this.makeBrain(new Dynamic<>(NbtOps.INSTANCE, tag.get("Brain")));
        }
        this.effectHelper.fromNBT(tag.getCompound("EffectHelper"));
        ((EvolutionCombatTracker) this.getCombatTracker()).readAdditional(tag);
    }

    @Override
    public void removeAbsorptionSuggestion(float amount) {
        if (amount > 0) {
            float max = this.effectHelper.removeAbsorptionSuggestion(amount);
            if (this.getAbsorptionAmount() > max) {
                this.setAbsorptionAmount(max);
            }
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "collectEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EquipmentSlot;values()" +
                                                                                      "[Lnet/minecraft/world/entity/EquipmentSlot;"))
    private EquipmentSlot[] removeAllocation() {
        return AdditionalSlotType.SLOTS;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean removeEffect(MobEffect mobEffect) {
        //noinspection ConstantConditions
        if (!this.level.isClientSide && (Object) this instanceof ServerPlayer p) {
            p.connection.send(new PacketSCRemoveEffect(mobEffect));
        }
        MobEffectInstance mobEffectInstance = this.removeEffectNoUpdate(mobEffect);
        if (mobEffectInstance != null) {
            this.onEffectRemoved(mobEffectInstance);
            return true;
        }
        return false;
    }

    @Shadow
    public abstract @Nullable MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect mobEffect);

    @Shadow
    protected abstract void removeFrost();

    @Shadow
    protected abstract void removeSoulSpeed();

    @Shadow
    protected abstract void serverAiStep();

    @Shadow
    public abstract void setAbsorptionAmount(float pAmount);

    @Shadow
    public abstract void setArrowCount(int p_85034_1_);

    @Shadow
    public abstract void setHealth(float pHealth);

    @Shadow
    public abstract void setLastHurtByMob(@Nullable LivingEntity p_70604_1_);

    @Shadow
    protected abstract void setLivingEntityFlag(int i, boolean bl);

    @Shadow
    protected abstract void setPosToBed(BlockPos p_21081_);

    @Shadow
    public abstract void setSleepingPos(BlockPos pPos);

    @Shadow
    public abstract void setStingerCount(int p_226300_1_);

    @Shadow
    protected abstract boolean shouldTriggerItemUseEffects();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void spawnItemParticles(ItemStack stack, int amount) {
        float xRot = -this.getXRot() * Mth.DEG_TO_RAD;
        float cosX = Mth.cos(xRot);
        float sinX = Mth.sin(xRot);
        float yRot = -this.getYRot() * Mth.DEG_TO_RAD;
        float cosY = Mth.cos(yRot);
        float sinY = Mth.sin(yRot);
        Vec3 eyePosition = this.getEyePosition();
        for (int i = 0; i < amount; ++i) {
            double velX = (this.random.nextFloat() - 0.5) * 0.05;
            double velY = this.random.nextFloat() * 0.05;
            double velZ = 0;
            //xRot
            double newY = velY * cosX;
            double newZ = -velY * sinX;
            velY = newY;
            velZ = newZ;
            //yRot
            double newX = velX * cosY + velZ * sinY;
            newZ = velZ * cosY - velX * sinY;
            velX = newX;
            velZ = newZ;
            //
            double x = (this.random.nextFloat() - 0.5) * 0.3;
            double y = -this.random.nextFloat() * 0.5;
            double z = 0.6;
            //xRot
            newY = y * cosX + z * sinX;
            newZ = z * cosX - y * sinX;
            y = newY;
            z = newZ;
            //yRot
            newX = x * cosY + z * sinY;
            newZ = z * cosY - x * sinY;
            x = newX;
            z = newZ;
            //
            x += eyePosition.x;
            y += eyePosition.y;
            z += eyePosition.z;
            //noinspection ObjectAllocationInLoop
            this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), x, y, z, velX, velY + 0.05, velZ);
        }
    }

    @Shadow
    protected abstract void spawnSoulSpeedParticle();

    @Override
    public void startSpecialAttack(IMelee.IAttackType type) {
        this.isSpecialAttacking = true;
        this.specialAttackType = type;
        this.specialAttackTime = 0;
        this.specialAttackGracePeriod = 0;
        if (this.level.isClientSide) {
            //noinspection ConstantConditions
            if ((Object) this instanceof Player) {
                EvolutionClient.sendToServer(new PacketCSSpecialAttackStart(type));
            }
        }
        else {
            ((ServerLevel) this.level).getChunkSource().broadcast(this, new PacketSCSpecialAttackStart((LivingEntity) (Object) this, type));
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Add message for cancelable items
     */
    @Overwrite
    public void startUsingItem(InteractionHand hand) {
        ItemStack stack = this.getItemInHand(hand);
        if (!stack.isEmpty() && !this.isUsingItem()) {
            this.useItem = stack;
            this.useItemRemaining = stack.getUseDuration();
            if (!this.level.isClientSide) {
                this.setLivingEntityFlag(LIVING_ENTITY_FLAG_IS_USING, true);
                this.setLivingEntityFlag(LIVING_ENTITY_FLAG_OFF_HAND, hand == InteractionHand.OFF_HAND);
            }
            else {
                //noinspection ConstantConditions
                if ((Object) this instanceof Player player) {
                    if (stack.getItem() instanceof ICancelableUse cancel && cancel.isCancelable(stack, player)) {
                        player.displayClientMessage(cancel.getCancelMessage(EvolutionClient.getAttackKeyText()), true);
                    }
                }
            }
        }
    }

    @Shadow
    public abstract void stopSleeping();

    @Override
    public void stopSpecialAttack(IMelee.StopReason reason) {
        this.isSpecialAttacking = false;
        if (reason == IMelee.StopReason.HIT_BLOCK) {
            ItemStack stack = this.getMainHandItem();
            SoundEvent sound;
            if (stack.getItem() instanceof IMelee melee) {
                sound = melee.getBlockHitSound(stack);
            }
            else {
                sound = EvolutionSounds.FIST_PUNCHES_BLOCK;
            }
            this.playSound(sound, 0.4f, 0.8F + this.random.nextFloat() * 0.4F);
            this.specialAttackLockedTicks = 10;
        }
        if (!this.isLockedInSpecialAttack()) {
            assert this.specialAttackType != null;
            if (this.specialAttackType.getFollowUps() >= ++this.specialAttackFollowUp) {
                this.specialAttackGracePeriod = 10;
            }
            else {
                this.specialAttackFollowUp = 0;
                this.specialAttackGracePeriod = 0;
            }
        }
        if (this.level.isClientSide) {
            //noinspection ConstantConditions
            if ((Object) this instanceof Player) {
                EvolutionClient.sendToServer(new PacketCSSpecialAttackStop(reason));
            }
        }
        else {
            ((ServerLevel) this.level).getChunkSource().broadcast(this, new PacketSCSpecialAttackStop((LivingEntity) (Object) this, reason));
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to rotate body
     */
    @Override
    @Overwrite
    public void tick() {
        super.tick();
        this.updatingUsingItem();
        this.updateSwimAmount();
        if (!this.level.isClientSide) {
            int i = this.getArrowCount();
            if (i > 0) {
                if (this.removeArrowTime <= 0) {
                    this.removeArrowTime = 20 * (30 - i);
                }
                --this.removeArrowTime;
                if (this.removeArrowTime <= 0) {
                    this.setArrowCount(i - 1);
                }
            }
            int j = this.getStingerCount();
            if (j > 0) {
                if (this.removeStingerTime <= 0) {
                    this.removeStingerTime = 20 * (30 - j);
                }
                --this.removeStingerTime;
                if (this.removeStingerTime <= 0) {
                    this.setStingerCount(j - 1);
                }
            }
            this.detectEquipmentUpdates();
            ((EvolutionCombatTracker) this.getCombatTracker()).tick();
            if (this.tickCount % 20 == 0) {
                this.getCombatTracker().recheckStatus();
            }
            if (this.isSleeping() && !this.checkBedExists()) {
                this.stopSleeping();
            }
        }
        this.aiStep();
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        float dSSq = (float) (dx * dx + dz * dz);
        float f1 = this.yBodyRot;
        this.oRun = this.run;
        float f3 = 0.0F;
        float f2 = 0.0F;
        if (dSSq > 0.002_500_000_2F) {
            f3 = 1.0F;
            f2 = MathHelper.sqrt(dSSq) * 3.0F;
            float f4 = (float) MathHelper.atan2Deg(dz, dx) - 90.0F;
            float f5 = Math.abs(Mth.wrapDegrees(this.getYRot()) - f4);
            if (95.0F < f5 && f5 < 265.0F) {
                f1 = f4 - 180.0F;
            }
            else {
                f1 = f4;
            }
        }
        if (this.attackAnim > 0.0F || LivingHooks.shouldFixRotation((LivingEntity) (Object) this)) {
            f1 = this.getYRot();
        }
        if (!this.onGround) {
            f3 = 0.0F;
        }
        this.run += (f3 - this.run) * 0.3F;
        this.level.getProfiler().push("headTurn");
        f2 = this.tickHeadTurn(f1, f2);
        this.level.getProfiler().popPush("rangeChecks");
        while (this.getYRot() - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }
        while (this.getYRot() - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }
        while (this.yBodyRot - this.yBodyRotO < -180.0F) {
            this.yBodyRotO -= 360.0F;
        }
        while (this.yBodyRot - this.yBodyRotO >= 180.0F) {
            this.yBodyRotO += 360.0F;
        }
        while (this.getXRot() - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }
        while (this.getXRot() - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }
        while (this.yHeadRot - this.yHeadRotO < -180.0F) {
            this.yHeadRotO -= 360.0F;
        }
        while (this.yHeadRot - this.yHeadRotO >= 180.0F) {
            this.yHeadRotO += 360.0F;
        }
        this.level.getProfiler().pop();
        this.animStep += f2;
        if (this.isFallFlying()) {
            ++this.fallFlyTicks;
        }
        else {
            this.fallFlyTicks = 0;
        }
        if (this.isSleeping()) {
            this.setXRot(0.0f);
        }
        if (this.getFeetBlockState().getBlock() instanceof IFallSufixBlock fallSufixBlock) {
            ((EvolutionCombatTracker) this.combatTracker).setLastSuffix(fallSufixBlock);
        }
        else if (this.isOnGround()) {
            ((EvolutionCombatTracker) this.combatTracker).setLastSuffix(null);
        }
    }

    @Shadow
    protected abstract void tickDeath();

    /**
     * @author TheGreatWolf
     * @reason Implement evolution effect system, avoid allocations
     */
    @Overwrite
    public void tickEffects() {
        try {
            boolean canSprint = true;
            boolean canRegen = true;
            float hungerMod = 0.0f;
            float thirstMod = 0.0f;
            double tempMod = 0.0;
            R2OMap<MobEffect, MobEffectInstance> activeEffects = (R2OMap<MobEffect, MobEffectInstance>) this.activeEffects;
            for (long it = activeEffects.beginIteration(); activeEffects.hasNextIteration(it); it = activeEffects.nextEntry(it)) {
                MobEffect effect = activeEffects.getIterationKey(it);
                MobEffectInstance instance = activeEffects.get(effect);
                //noinspection DataFlowIssue
                if (!instance.tick((LivingEntity) (Object) this, null)) {
                    if (!this.level.isClientSide) {
                        EntityEvents.onEffectExpired((LivingEntity) (Object) this, instance);
                        it = activeEffects.removeIteration(it);
                        this.onEffectRemoved(instance);
                    }
                }
                else if (instance.getDuration() % 600 == 0) {
                    this.onEffectUpdated(instance, false, null);
                }
                if (canSprint && effect.disablesSprint()) {
                    canSprint = false;
                }
                if (canRegen && effect.disablesNaturalRegen()) {
                    canRegen = false;
                }
                int lvl = instance.getAmplifier();
                hungerMod += effect.hungerMod(lvl);
                thirstMod += effect.thirstMod(lvl);
                tempMod += effect.tempMod();
            }
            //noinspection ConstantConditions
            this.effectHelper.setCanSprint(canSprint);
            //noinspection ConstantConditions
            this.effectHelper.setCanRegen(canRegen);
            this.effectHelper.setHungerMod(hungerMod);
            this.effectHelper.setThirstMod(thirstMod);
            this.effectHelper.setTemperatureMod(tempMod);
        }
        catch (ConcurrentModificationException e) {
            Evolution.warn("Concurrent modification while iterating through effects", e);
        }
        if (this.effectsDirty) {
            if (!this.level.isClientSide) {
                this.updateInvisibilityStatus();
                this.updateGlowingStatus();
            }
            this.effectsDirty = false;
        }
        int color = this.entityData.get(DATA_EFFECT_COLOR_ID);
        boolean ambient = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
        if (color > 0) {
            boolean shouldShow;
            if (this.isInvisible()) {
                shouldShow = this.random.nextInt(15) == 0;
            }
            else {
                shouldShow = this.random.nextBoolean();
            }
            if (ambient) {
                shouldShow &= this.random.nextInt(5) == 0;
            }
            if (shouldShow) {
                double r = (color >> 16 & 255) / 255.0;
                double g = (color >> 8 & 255) / 255.0;
                double b = (color & 255) / 255.0;
                this.level.addParticle(ambient ? ParticleTypes.AMBIENT_ENTITY_EFFECT : ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), r, g, b);
            }
        }
    }

    @Shadow
    protected abstract float tickHeadTurn(float p_110146_1_, float p_110146_2_);

    /**
     * @author TheGreatWolf
     * @reason Replace to handle Evolution's physics.
     */
    @Overwrite
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            //noinspection ConstantConditions
            if (this.level.isClientSide && (Object) this instanceof Player player) {
                if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                    if (EntityUtils.isPlayerNearUnloadedChunks(player)) {
                        //Prevents players from moving in unloaded chunks, gaining momentum and then taking damage when the ground finally loads.
                        return;
                    }
                }
            }
            if (this.isFallFlying()) {
                //Handle elytra movement
                //TODO
                Vec3 motion = this.getDeltaMovement();
                double motionX = motion.x;
                double motionY = motion.y;
                double motionZ = motion.z;
                double mass = this.getAttributeValue(EvolutionAttributes.MASS);
                this.setDeltaMovement(motionX, motionY, motionZ);
                this.move(MoverType.SELF, this.getDeltaMovement());
                if (this.horizontalCollision && !this.level.isClientSide) {
                    this.calculateWallImpact(motionX, motionZ, mass);
                }
                if (this.isOnGround() && !this.level.isClientSide) {
                    this.setSharedFlag(FLAG_FALL_FLYING, false);
                }
            }
            else {
                this.handleNormalMovement((Vec3d) travelVector, this.isInWater() ? Fluid.WATER : this.isInLava() ? Fluid.LAVA : Fluid.AIR, 1.0f);
            }
        }
        this.calculateEntityAnimation((LivingEntity) (Object) this, this instanceof FlyingAnimal);
    }

    @Shadow
    protected abstract void triggerItemUseEffects(ItemStack itemStack, int i);

    @Shadow
    protected abstract void tryAddFrost();

    @Shadow
    protected abstract void tryAddSoulSpeed();

    @Override
    public float tryHurt(DamageSourceEv source, float amount, float strength, HitboxType hitbox) {
        if (this.isInvulnerableTo(source)) {
            return 0.0f;
        }
        if (this.level.isClientSide) {
            return 0.0f;
        }
        if (this.isDeadOrDying()) {
            return 0.0f;
        }
        if (amount <= 0 || strength <= 0) {
            return 0.0f;
        }
        float maxHealth = this.getMaxHealth();
        //TODO defense and armour against specific damage types
        float woundLimit = hitbox.getWoundPercentage() * maxHealth;
        if (amount > woundLimit) {
            switch (source.getType()) {
                case SLASHING -> {
                    if (hitbox.canBleed()) {
                        //TODO bleeding effect, needs wound system
                    }
                }
                case PIERCING -> {
                    if (hitbox.isLethal()) {
                        float relativeDamage = amount / maxHealth;
                        if (this.random.nextFloat() < 0.5 * relativeDamage) {
                            //Insta-kill
                            if (amount < maxHealth + 1) {
                                amount = maxHealth + 1;
                            }
                        }
                    }
                }
                case CRUSHING -> {
                    if (hitbox.isFracturable()) {
                        //TODO fracture wound, needs wound system
                    }
                }
            }
        }
        return amount;
    }

    @Shadow
    protected abstract void updateFallFlying();

    @Shadow
    protected abstract void updateGlowingStatus();

    @Shadow
    protected abstract void updateInvisibilityStatus();

    /**
     * @author TheGreatWolf
     * @reason Make animation last longer
     */
    @Overwrite
    private void updateSwimAmount() {
        this.swimAmountO = this.swimAmount;
        if (this.isVisuallySwimming()) {
            this.swimAmount = Math.min(1.0F, this.swimAmount + 0.067_5F);
        }
        else {
            this.swimAmount = Math.max(0.0F, this.swimAmount - 0.067_5F);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Fix special attack
     */
    @Overwrite
    public void updateSwingTime() {
        if (!this.swinging && this.swingTime == 0) {
            return;
        }
        int i = this.getCurrentSwingDuration();
        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime >= i) {
                this.swingTime = 0;
                this.swinging = false;
            }
        }
        else {
            this.swingTime = 0;
        }
        this.attackAnim = this.swingTime / (float) i;
    }

    /**
     * @author TheGreatWolf
     * @reason Add hooks
     */
    @Overwrite
    public void updateUsingItem(ItemStack stack) {
        stack.onUseTick(this.level, (LivingEntity) (Object) this, this.getUseItemRemainingTicks());
        if (this.shouldTriggerItemUseEffects()) {
            this.triggerItemUseEffects(stack, 5);
        }
        this.useItem.onUsingTick((LivingEntity) (Object) this, this.useItemRemaining);
        if (--this.useItemRemaining == 0 && !this.level.isClientSide && !stack.useOnRelease()) {
            this.completeUsingItem();
        }
    }

    @Shadow
    protected abstract void updatingUsingItem();
}
