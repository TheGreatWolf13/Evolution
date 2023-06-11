package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.Evolution;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.PacketCSPlayerFall;
import tgw.evolution.network.PacketSCHitmarker;
import tgw.evolution.network.PacketSCMovement;
import tgw.evolution.patches.IPlayerPatch;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.collection.RList;
import tgw.evolution.util.constants.SkinType;
import tgw.evolution.util.damage.DamageSourceEntity;
import tgw.evolution.util.damage.DamageSourceEntityIndirect;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.AABBMutable;
import tgw.evolution.util.math.MathHelper;

import java.util.List;
import java.util.Optional;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IPlayerPatch {

    @Unique
    private final AABBMutable hitboxForTouching = new AABBMutable();
    @Shadow
    public float bob;
    @Shadow
    @Nullable
    public AbstractContainerMenu containerMenu;
    @Shadow
    @Final
    public InventoryMenu inventoryMenu;
    @Shadow
    public float oBob;
    @Shadow
    public int takeXpDelay;
    @Shadow
    protected FoodData foodData;
    @Shadow
    protected int jumpTriggerTime;
    @Shadow
    @Final
    private Abilities abilities;
    @Shadow
    @Final
    private ItemCooldowns cooldowns;
    @Shadow
    @Nullable
    private Pose forcedPose;
    @Shadow
    @Final
    private Inventory inventory;
    @Unique
    private boolean isCrawling;
    @Unique
    private boolean isMoving;
    @Shadow
    private ItemStack lastItemInMainHand;
    @Unique
    private double motionX;
    @Unique
    private double motionY;
    @Unique
    private double motionZ;
    @Shadow
    private int sleepCounter;

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    /**
     * @author The Great Wolf
     * @reason Handle Evolution damage system and stats.
     */
    @Override
    @Overwrite
    protected void actuallyHurt(DamageSource source, float amount) {
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
                //noinspection ConstantConditions
                if ((Object) this instanceof ServerPlayer player) {
                    if (source instanceof DamageSourceEv sourceEv) {
                        EvolutionDamage.Type damageType = sourceEv.getType();
                        ResourceLocation resLoc = EvolutionStats.DAMAGE_TAKEN_BY_TYPE.get(damageType);
                        if (resLoc != null) {
                            PlayerHelper.addStat(player, resLoc, damageAfterAbsorp);
                        }
                        if (source instanceof DamageSourceEntity) {
                            if (source instanceof DamageSourceEntityIndirect) {
                                PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_BY_TYPE.get(EvolutionDamage.Type.RANGED), damageAfterAbsorp);
                            }
                            else {
                                PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_BY_TYPE.get(EvolutionDamage.Type.MELEE), damageAfterAbsorp);
                            }
                        }
                        PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_BY_TYPE.get(EvolutionDamage.Type.TOTAL), damageAfterAbsorp);
                    }
                    else {
                        Evolution.warn("Bad damage source: " + source);
                    }
                }
            }
            if (source.getEntity() instanceof ServerPlayer sourcePlayer) {
                EvolutionNetwork.send(sourcePlayer, new PacketSCHitmarker(false));
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Add Evolution hooks.
     */
    @Override
    @Overwrite
    public void aiStep() {
        if (this.jumpTriggerTime > 0) {
            --this.jumpTriggerTime;
        }
        this.inventory.tick();
        this.oBob = this.bob;
        super.aiStep();
        this.flyingSpeed = 0.02F;
        if (this.isSprinting()) {
            this.flyingSpeed += 0.006F;
        }
        this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
        float dBob;
        if (this.onGround && !this.isDeadOrDying() && !this.isSwimming()) {
            dBob = Math.min(0.1F, (float) this.getDeltaMovement().horizontalDistance());
        }
        else {
            dBob = 0.0F;
        }
        this.bob += (dBob - this.bob) * 0.4F;
        if (this.getHealth() > 0.0F && !this.isSpectator()) {
            this.hitboxForTouching.set(this.getBoundingBox());
            //noinspection ConstantConditions
            if (this.isPassenger() && !this.getVehicle().isRemoved()) {
                this.hitboxForTouching.minmax(this.getVehicle().getBoundingBox()).inflateMutable(1, 0, 1);
            }
            else {
                this.hitboxForTouching.inflateMutable(1, 0.5, 1);
            }
            List<Entity> entitiesNearby = this.level.getEntities(this, this.hitboxForTouching);
            RList<Entity> orbs = null;
            for (int i = 0, len = entitiesNearby.size(); i < len; i++) {
                Entity entity = entitiesNearby.get(i);
                if (entity.getType() == EntityType.EXPERIENCE_ORB) {
                    if (orbs == null) {
                        orbs = new RArrayList<>();
                    }
                    orbs.add(entity);
                }
                else if (!entity.isRemoved()) {
                    this.touch(entity);
                }
            }
            if (orbs != null) {
                this.touch(Util.getRandom(orbs, this.random));
            }
        }
        this.playShoulderEntityAmbientSound(this.getShoulderEntityLeft());
        this.playShoulderEntityAmbientSound(this.getShoulderEntityRight());
        if (!this.level.isClientSide && (this.fallDistance > 0.5F || this.isInWater()) ||
            this.abilities.flying ||
            this.isSleeping() ||
            this.isInPowderSnow) {
            this.removeEntitiesOnShoulder();
        }
    }

    @Shadow
    public abstract void awardStat(Stat<?> pStat);

    @Shadow
    public abstract void awardStat(Stat<?> pStat, int pIncrement);

    @Shadow
    public abstract void awardStat(ResourceLocation pStatKey);

    /**
     * @author TheGreatWolf
     * @reason Make fall damage depend on kinetic energy, not fall distance.
     */
    @Override
    @Overwrite
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (this.abilities.mayfly) {
            if (fallDistance > 1 / 16.0f) {
                this.playBlockFallSound();
            }
            return false;
        }
        if (this.level.isClientSide) {
            EvolutionNetwork.sendToServer(new PacketCSPlayerFall(this.getDeltaMovement().y, 1 - multiplier, false));
            return super.causeFallDamage(fallDistance, multiplier, source);
        }
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to use Evolution Stats.
     */
    @Overwrite
    public void checkMovementStatistics(double dx, double dy, double dz) {
        this.isMoving = false;
        this.motionX = dx;
        this.motionY = dy;
        this.motionZ = dz;
        //noinspection ConstantConditions
        if (!this.isPassenger() && (Object) this instanceof ServerPlayer player) {
            float dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 1_000;
            if (dist > 0) {
                PlayerHelper.addStat(player, EvolutionStats.TOTAL_DISTANCE_TRAVELED, dist);
            }
            if (this.isSwimming()) {
                if (dist > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_SWUM, dist);
                }
            }
            else if (this.isEyeInFluid(FluidTags.WATER)) {
                if (dist > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_WALKED_UNDER_WATER, dist);
                }
            }
            else if (this.isInWater()) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_WALKED_ON_WATER, horizontalDist);
                }
            }
            else if (this.onClimbable()) {
                if (dy > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_CLIMBED, (float) (dy * 1_000));
                }
            }
            else if (this.isOnGround()) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    this.isMoving = true;
                    if (this.isSprinting()) {
                        PlayerHelper.addStat(player, EvolutionStats.DISTANCE_SPRINTED, horizontalDist);
                    }
                    else if (this.isCrouching()) {
                        PlayerHelper.addStat(player, EvolutionStats.DISTANCE_CROUCHED, horizontalDist);
                    }
                    else if (this.getPose() == Pose.SWIMMING) {
                        PlayerHelper.addStat(player, EvolutionStats.DISTANCE_PRONE, horizontalDist);
                    }
                    else {
                        PlayerHelper.addStat(player, EvolutionStats.DISTANCE_WALKED, horizontalDist);
                    }
                }
            }
            else if (this.abilities.flying) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_FLOWN, horizontalDist);
                }
            }
            else {
                if (dy < 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_FALLEN, (float) (-dy * 1_000));
                }
                else if (dy > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_JUMPED_VERTICAL, (float) (dy * 1_000));
                }
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    PlayerHelper.addStat(player, EvolutionStats.DISTANCE_JUMPED_HORIZONTAL, horizontalDist);
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to use Evolution Stats.
     */
    @Overwrite
    private void checkRidingStatistics(double dx, double dy, double dz) {
        //noinspection ConstantConditions
        if ((Object) this instanceof ServerPlayer player && this.isPassenger()) {
            float dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 1_000;
            if (dist > 0) {
                PlayerHelper.addStat(player, EvolutionStats.TOTAL_DISTANCE_TRAVELED, dist);
            }
        }
    }

    @Shadow
    public abstract void closeContainer();

    @Override
    public double getBaseAttackDamage() {
        return PlayerHelper.ATTACK_DAMAGE;
    }

    @Override
    public double getBaseHealth() {
        return PlayerHelper.MAX_HEALTH;
    }

    @Override
    public double getBaseMass() {
        return PlayerHelper.MASS;
    }

    @Override
    public double getBaseWalkForce() {
        return PlayerHelper.WALK_FORCE;
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle first person camera.
     */
    @Overwrite
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if ((this.swimAmount > 0 || this.getPose() == Pose.SWIMMING) && this.swimAmount < 1 && this.getPose() == Pose.SWIMMING) {
            return PlayerHelper.SIZE_BY_POSE.getOrDefault(this.getLastPose(), PlayerHelper.STANDING_SIZE);
        }
        return PlayerHelper.SIZE_BY_POSE.getOrDefault(pose, PlayerHelper.STANDING_SIZE);
    }

    @Nullable
    @Override
    public HitboxEntity<Player> getHitboxes() {
        return EntityEvents.SKIN_TYPE.getOrDefault(this.getUUID(), SkinType.STEVE) == SkinType.STEVE ?
               EvolutionEntityHitboxes.PLAYER_STEVE.get((Player) (Object) this) :
               EvolutionEntityHitboxes.PLAYER_ALEX.get((Player) (Object) this);
    }

    @Override
    public double getLungCapacity() {
        return PlayerHelper.LUNG_CAPACITY;
    }

    @Override
    public double getMotionX() {
        return this.motionX;
    }

    @Override
    public double getMotionY() {
        return this.motionY;
    }

    @Override
    public double getMotionZ() {
        return this.motionZ;
    }

    @Shadow
    public abstract CompoundTag getShoulderEntityLeft();

    @Shadow
    public abstract CompoundTag getShoulderEntityRight();

    private float getStepHeightInternal() {
        AttributeInstance massAttribute = this.getAttribute(EvolutionAttributes.MASS.get());
        assert massAttribute != null;
        double baseMass = massAttribute.getBaseValue();
        double totalMass = massAttribute.getValue();
        double equipMass = totalMass - baseMass;
        double stepHeight = 1.062_5f - equipMass * 0.001_14f;
        return (float) Math.max(stepHeight, 0.6);
    }

    @Override
    public double getVolume() {
        return PlayerHelper.VOLUME - PlayerHelper.LUNG_CAPACITY;
    }

    @Override
    public double getVolumeCorrectionFactor() {
        float width = this.getBbWidth();
        if (this.isSwimming() && !this.isFullySubmerged(FluidTags.WATER)) {
            return this.getVolume() / (width * width * this.getBbHeight() * 0.5);
        }
        return this.getVolume() / (width * width * this.getBbHeight());
    }

    @Override
    public boolean hasExtendedInventory() {
        return true;
    }

    /**
     * @author TheGreatWolf
     * @reason Remove damage scaling, as difficulty will be handled differently
     */
    @Override
    @Overwrite
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.abilities.invulnerable && !source.isBypassInvul()) {
            return false;
        }
        this.noActionTime = 0;
        if (this.isDeadOrDying()) {
            return false;
        }
        if (!this.level.isClientSide) {
            this.removeEntitiesOnShoulder();
        }
        return amount > 0 && super.hurt(source, amount);
    }

    @Override
    public boolean isCrawling() {
        return this.isCrawling;
    }

    @Override
    public boolean isDiscrete() {
        return this.isShiftKeyDown() || this.getSwimAmount(1.0f) > 0 && this.isOnGround() && !this.isInWater();
    }

    @Override
    public boolean isMoving() {
        return this.isMoving;
    }

    @Override
    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    protected abstract void moveCloak();

    @Inject(method = "startSleepInBed", at = @At("TAIL"))
    private void onStartSleepInBed(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        if (!this.level.isClientSide) {
            //noinspection ConstantConditions
            EvolutionNetwork.send((ServerPlayer) (Object) this, new PacketSCMovement(0, 0, 0));
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations, check for empty tag to not perform a ton of calculations when no entity is on the shoulder.
     */
    @Overwrite
    private void playShoulderEntityAmbientSound(@Nullable CompoundTag tag) {
        if (tag != null && !tag.isEmpty() && (!tag.contains("Silent") || !tag.getBoolean("Silent")) && this.level.random.nextInt(200) == 0) {
            String id = tag.getString("id");
            Optional<EntityType<?>> entityType = EntityType.byString(id);
            if (entityType.isPresent()) {
                if (entityType.get() == EntityType.PARROT) {
                    if (!Parrot.imitateNearbyMobs(this.level, this)) {
                        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), Parrot.getAmbient(this.level, this.level.random),
                                             this.getSoundSource(), 1.0F, Parrot.getPitch(this.level.random));
                    }
                }
            }
        }
    }

    @Shadow
    protected abstract void removeEntitiesOnShoulder();

    @Shadow
    public abstract void resetAttackStrengthTicker();

    @Override
    public void setCrawling(boolean crawling) {
        this.isCrawling = crawling;
    }

    @Shadow
    public abstract void stopSleepInBed(boolean pWakeImmediatly, boolean pUpdateLevelForSleepingPlayers);

    /**
     * @author TheGreatWolf
     * @reason Add Evolution hooks
     */
    @Override
    @Overwrite
    public void tick() {
        //Event Pre Tick
        EntityEvents.onPlayerTick((Player) (Object) this, TickEvent.Phase.START);
        //Main Tick
        this.noPhysics = this.isSpectator();
        if (this.isSpectator()) {
            this.onGround = false;
        }
        if (this.swimAmount != this.swimAmountO) {
            this.refreshDimensions();
        }
        if (this.takeXpDelay > 0) {
            --this.takeXpDelay;
        }
        if (this.isSleeping()) {
            ++this.sleepCounter;
            if (this.sleepCounter > 100) {
                this.sleepCounter = 100;
            }
            if (!this.level.isClientSide && !ForgeEventFactory.fireSleepingTimeCheck((Player) (Object) this, this.getSleepingPos())) {
                this.stopSleepInBed(false, true);
            }
        }
        else if (this.sleepCounter > 0) {
            ++this.sleepCounter;
            if (this.sleepCounter >= 110) {
                this.sleepCounter = 0;
            }
        }
        this.updateIsUnderwater();
        super.tick();
        if (!this.level.isClientSide && this.containerMenu != null && !this.containerMenu.stillValid((Player) (Object) this)) {
            this.closeContainer();
            this.containerMenu = this.inventoryMenu;
        }
        this.moveCloak();
        double clampedX = Mth.clamp(this.getX(), -29_999_999, 29_999_999);
        double clampedZ = Mth.clamp(this.getZ(), -29_999_999, 29_999_999);
        if (clampedX != this.getX() || clampedZ != this.getZ()) {
            this.setPos(clampedX, this.getY(), clampedZ);
        }
        this.turtleHelmetTick();
        this.cooldowns.tick();
        this.updatePlayerPose();
        if (this.isVisuallyCrawling()) {
            this.setSprinting(false);
            this.maxUpStep = this.getStepHeightInternal();
        }
        else {
            this.maxUpStep = 0.6f;
        }
        //Event Post Tick
        EntityEvents.onPlayerTick((Player) (Object) this, TickEvent.Phase.END);
    }

    @Shadow
    protected abstract void touch(Entity pEntity);

    /**
     * @author TheGreatWolf
     * @reason Replace to handle Evolution's physics
     */
    @Override
    @Overwrite
    public void travel(Vec3 travelVector) {
        if (this.abilities.flying && !this.isPassenger()) {
            double motionY = this.getDeltaMovement().y;
            float jumpMovementFactor = this.flyingSpeed;
            this.flyingSpeed = 4 * this.abilities.getFlyingSpeed() * (this.isSprinting() ? 2 : 1);
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motionY * 0.8, motion.z);
            this.fallDistance = 0.0F;
            super.travel(travelVector);
            this.setSharedFlag(FLAG_FALL_FLYING, false);
            this.flyingSpeed = jumpMovementFactor;
        }
        else {
            super.travel(travelVector);
        }
    }

    @Shadow
    protected abstract void turtleHelmetTick();

    @Shadow
    protected abstract boolean updateIsUnderwater();

    /**
     * @author TheGreatWolf
     * @reason Handle crawling pose
     */
    @Overwrite
    protected void updatePlayerPose() {
        if (this.forcedPose != null) {
            this.setPose(this.forcedPose);
            return;
        }
        if (this.isPassenger()) {
            this.setPose(Pose.STANDING);
        }
        else if (this.canEnterPose(Pose.SWIMMING)) {
            Pose firstPose;
            if (this.isFallFlying()) {
                firstPose = Pose.FALL_FLYING;
            }
            else if (this.isSleeping()) {
                firstPose = Pose.SLEEPING;
            }
            else if (this.isSwimming() || this.isCrawling) {
                firstPose = Pose.SWIMMING;
            }
            else if (this.isAutoSpinAttack()) {
                firstPose = Pose.SPIN_ATTACK;
            }
            else if (this.isShiftKeyDown() && !this.abilities.flying) {
                firstPose = Pose.CROUCHING;
            }
            else {
                firstPose = Pose.STANDING;
            }
            Pose secondPose;
            if (!this.isSpectator() && !this.isPassenger() && !this.canEnterPose(firstPose)) {
                if (this.canEnterPose(Pose.CROUCHING)) {
                    secondPose = Pose.CROUCHING;
                }
                else {
                    secondPose = Pose.SWIMMING;
                }
            }
            else {
                secondPose = firstPose;
            }
            this.setPose(secondPose);
        }
    }
}
