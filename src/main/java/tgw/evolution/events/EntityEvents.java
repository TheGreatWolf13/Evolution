package tgw.evolution.events;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.passive.horse.TraderLlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.IClimbable;
import tgw.evolution.capabilities.SerializableCapabilityProvider;
import tgw.evolution.capabilities.inventory.CapabilityExtendedInventory;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.thirst.IThirst;
import tgw.evolution.capabilities.thirst.ThirstStats;
import tgw.evolution.entities.EntityGenericCreature;
import tgw.evolution.entities.IAgressive;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.*;
import tgw.evolution.inventory.extendedinventory.ContainerExtendedHandler;
import tgw.evolution.network.*;
import tgw.evolution.util.*;
import tgw.evolution.util.damage.*;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.MethodHandler;

import java.util.*;

public class EntityEvents {

    public static final WindVector WIND = new WindVector();
    public static final Map<UUID, SkinType> SKIN_TYPE = new HashMap<>();
    private static final MethodHandler<Entity, Void> SET_POSE_METHOD = new MethodHandler<>(Entity.class, "func_213301_b", Pose.class);
    private static final FieldHandler<LivingEntity, CombatTracker> COMBAT_TRACKER_FIELD = new FieldHandler<>(LivingEntity.class, "field_94063_bt");
    private static final Random RANDOM = new Random();
    private static final Set<DamageSource> IGNORED_DAMAGE_SOURCES = Util.make(Sets.newHashSet(), set -> {
        set.add(EvolutionDamage.DEHYDRATION);
        set.add(EvolutionDamage.DROWN);
        set.add(EvolutionDamage.FALL);
        set.add(EvolutionDamage.FALLING_ROCK);
        set.add(EvolutionDamage.IN_FIRE);
        set.add(EvolutionDamage.IN_WALL);
        set.add(EvolutionDamage.ON_FIRE);
        set.add(EvolutionDamage.VOID);
        set.add(EvolutionDamage.WALL_IMPACT);
        set.add(EvolutionDamage.WATER_IMPACT);
        set.add(EvolutionDamage.WATER_INTOXICATION);
    });
    private static final Set<DamageSource> IGNORED_VANILLA_SOURCES = Util.make(Sets.newHashSet(), set -> {
        set.add(DamageSource.DROWN);
        set.add(DamageSource.FALL);
        set.add(DamageSource.IN_WALL);
        set.add(DamageSource.OUT_OF_WORLD);
    });
    private final Map<DamageSource, EquipmentSlotType> damageMultipliers = new WeakHashMap<>();
    //TODO replace with capability
    private final Map<UUID, Integer> playerTimeSinceLastHit = new HashMap<>();

    public static void calculateFallDamage(LivingEntity entity, double velocity, double distanceOfSlowDown, boolean isWater) {
        //Convert from m/t to m/s
        velocity *= 20;
        double legHeight = PlayerHelper.LEG_HEIGHT;
        double baseMass = PlayerHelper.MASS;
        if (entity instanceof EntityGenericCreature) {
            EntityGenericCreature<?> creature = (EntityGenericCreature<?>) entity;
            legHeight = creature.getLegHeight();
            baseMass = creature.getBaseMass();
        }
        distanceOfSlowDown += legHeight;
        double totalMass = baseMass;
        if (entity instanceof PlayerEntity) {
            ModifiableAttributeInstance massAttribute = entity.getAttribute(EvolutionAttributes.MASS.get());
            baseMass = massAttribute.getBaseValue();
            totalMass = massAttribute.getValue();
        }
        double kineticEnergy = totalMass * velocity * velocity / 2;
        double forceOfImpact = kineticEnergy / distanceOfSlowDown;
        double area = entity.getBbWidth() * entity.getBbWidth();
        double pressureOfFall = forceOfImpact / area;
        double maxSupportedPressure = baseMass / (area * 0.035);
        double deltaPressure = MathHelper.clampMin(pressureOfFall - maxSupportedPressure, 0);
        float amount = (float) Math.pow(deltaPressure, 1.7) / 750_000;
        if (amount >= 1) {
            if (isWater) {
                entity.hurt(EvolutionDamage.WATER_IMPACT, amount);
            }
            else {
                entity.hurt(EvolutionDamage.FALL, amount);
            }
        }
    }

    public static void calculateWaterFallDamage(LivingEntity entity) {
        BlockPos pos = entity.blockPosition();
        FluidState fluidState = entity.level.getFluidState(pos);
        FluidState fluidStateDown = entity.level.getFluidState(pos.below());
        double distanceOfSlowDown = fluidState.getAmount() * 0.062_5;
        if (!fluidStateDown.isEmpty()) {
            distanceOfSlowDown += 1;
            FluidState fluidStateDown2 = entity.level.getFluidState(pos.below(2));
            if (!fluidStateDown2.isEmpty()) {
                distanceOfSlowDown += 1;
            }
        }
        double velocity = entity.getDeltaMovement().y;
        calculateFallDamage(entity, velocity, distanceOfSlowDown, true);
    }

    private static float getStepHeight(PlayerEntity player) {
        ModifiableAttributeInstance mass = player.getAttribute(EvolutionAttributes.MASS.get());
        int baseMass = (int) mass.getBaseValue();
        int totalMass = (int) mass.getValue();
        int equipMass = totalMass - baseMass;
        float stepHeight = 1.062_5f - equipMass * 0.001_14f;
        return MathHelper.clampMin(stepHeight, 0.6f);
    }

    private static void spawnDrops(ItemStack stack, World world, BlockPos pos) {
        double xOffset = world.getRandom().nextFloat() * 0.7F + 0.65F;
        double yOffset = world.getRandom().nextFloat() * 0.7F + 0.65F;
        double zOffset = world.getRandom().nextFloat() * 0.7F + 0.65F;
        ItemEntity entity = new ItemEntity(world, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, stack);
        entity.setPickUpDelay(10);
        world.addFreshEntity(entity);
    }

    private static int strangeRespawnFunc(int spawnArea) {
        return spawnArea <= 16 ? spawnArea - 1 : 17;
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(Evolution.getResource("extended_inventory"),
                                new SerializableCapabilityProvider<>(CapabilityExtendedInventory.INSTANCE, new ContainerExtendedHandler()));
            event.addCapability(Evolution.getResource("thirst"), new SerializableCapabilityProvider<>(CapabilityThirst.INSTANCE, new ThirstStats()));
        }
    }

    @SubscribeEvent
    public void cloneCapabilitiesEvent(PlayerEvent.Clone event) {
        if (event.getOriginal().level.isClientSide) {
            return;
        }
        try {
            ContainerExtendedHandler handler = (ContainerExtendedHandler) event.getOriginal()
                                                                               .getCapability(CapabilityExtendedInventory.INSTANCE)
                                                                               .orElseThrow(IllegalStateException::new);
            CompoundNBT nbt = handler.serializeNBT();
            ContainerExtendedHandler handlerClone = (ContainerExtendedHandler) event.getPlayer()
                                                                                    .getCapability(CapabilityExtendedInventory.INSTANCE)
                                                                                    .orElseThrow(IllegalStateException::new);
            handlerClone.deserializeNBT(nbt);
        }
        catch (Exception e) {
            Evolution.LOGGER.error("Could not clone player [" + event.getOriginal().getName() + "] extended inventory when changing dimensions");
        }
        try {
            ThirstStats handler = (ThirstStats) event.getOriginal().getCapability(CapabilityThirst.INSTANCE).orElseThrow(IllegalStateException::new);
            CompoundNBT nbt = handler.serializeNBT();
            ThirstStats handlerClone = (ThirstStats) event.getPlayer()
                                                          .getCapability(CapabilityThirst.INSTANCE)
                                                          .orElseThrow(IllegalStateException::new);
            handlerClone.deserializeNBT(nbt);
        }
        catch (Exception e) {
            Evolution.LOGGER.error("Could not clone player [" + event.getOriginal().getName() + "] thirst data when changing dimensions");
        }
    }

    @SubscribeEvent
    public void onEntityCreated(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        //Prevents Phantoms, Trader Llamas and Wandering Traders from spawning
        if (entity instanceof PhantomEntity || entity instanceof TraderLlamaEntity || entity instanceof WanderingTraderEntity) {
            event.setCanceled(true);
            return;
        }
        if (!(entity instanceof PlayerEntity) && !(entity instanceof EntityGenericCreature)) {
            return;
        }
        LivingEntity living = (LivingEntity) entity;
        //Changes the combat tracker
        COMBAT_TRACKER_FIELD.set(living, new CombatTrackerEv(living));
        //Sets the gravity of the Living Entities and the Player to be that of the dimension they're in
        if (living instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) living;
            if (player instanceof ClientPlayerEntity) {
                if (player.equals(Evolution.PROXY.getClientPlayer())) {
                    EvolutionNetwork.INSTANCE.sendToServer(new PacketCSSkinType());
                }
            }
            else if (player instanceof ServerPlayerEntity) {
                Collection<EffectInstance> effects = player.getActiveEffects();
                if (!effects.isEmpty()) {
                    PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player);
                    for (EffectInstance instance : effects) {
                        //noinspection ObjectAllocationInLoop
                        EvolutionNetwork.INSTANCE.send(target, new PacketSCAddEffect(instance, PacketSCAddEffect.Logic.ADD));
                    }
                }
            }
        }
        //TODO
//        else {
//            //Makes the Living Entities able to step up one block, instead of jumping (it looks better)
//            entity.stepHeight = 1.0625F;
//        }
    }

    @SubscribeEvent
    public void onEntityKnockedBack(LivingKnockBackEvent event) {
        event.setCanceled(true);
        LivingEntity entity = event.getEntityLiving();
        double knockbackResistance = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
        if (knockbackResistance == 1) {
            return;
        }
        float strength = event.getStrength();
        double xRatio = event.getRatioX();
        double zRatio = event.getRatioZ();
        entity.hasImpulse = true;
        Vector3d motion = entity.getDeltaMovement();
        Vector3d knockbackVec = new Vector3d(xRatio, 0, zRatio).normalize().scale(strength);
        if (knockbackResistance > 0) {
            knockbackVec = knockbackVec.scale(1 - knockbackResistance);
        }
        entity.setDeltaMovement(motion.x - knockbackVec.x, 0, motion.z - knockbackVec.z);
        entity.hurtMarked = true;
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            //noinspection VariableNotUsedInsideIf
            if (player.connection != null) {
                EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCUpdateCameraTilt(player));
            }
        }
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getWorld().isClientSide) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        if (player == null) {
            return;
        }
        //Makes players able to hit through non-collidable blocks
        BlockState state = event.getWorld().getBlockState(event.getPos());
        if (!state.getCollisionShape(event.getWorld(), event.getPos()).isEmpty()) {
            return;
        }
        EntityRayTraceResult rayTraceResult = MathHelper.rayTraceEntityFromEyes(player,
                                                                                1,
                                                                                player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue());
        if (rayTraceResult != null) {
            player.attack(rayTraceResult.getEntity());
            player.resetAttackStrengthTicker();
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntityLiving().level.isClientSide) {
            return;
        }
        LivingEntity hitEntity = event.getEntityLiving();
        DamageSource source = event.getSource();
        if (!(source instanceof DamageSourceEv)) {
            if (!IGNORED_VANILLA_SOURCES.contains(source)) {
                Evolution.LOGGER.debug("Canceling vanilla damage source: {}", source.msgId);
            }
            if (source == DamageSource.OUT_OF_WORLD) {
                hitEntity.hurt(EvolutionDamage.VOID, event.getAmount() * 5.0f);
            }
            else if (source == DamageSource.DROWN) {
                hitEntity.hurt(EvolutionDamage.DROWN, 10.0f);
            }
            else if ("mob".equals(source.msgId)) {
                hitEntity.hurt(EvolutionDamage.causeMobDamage((LivingEntity) source.getEntity()), event.getAmount());
            }
            event.setCanceled(true);
            return;
        }
        if (!hitEntity.isAlive()) {
            event.setCanceled(true);
            return;
        }
        float damage = event.getAmount();
        if (hitEntity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) hitEntity;
            EvolutionDamage.Type damageType = ((DamageSourceEv) source).getType();
            ResourceLocation resLoc = EvolutionStats.DAMAGE_TAKEN_RAW.get(damageType);
            if (resLoc != null) {
                PlayerHelper.addStat(player, resLoc, damage);
            }
            if (source instanceof DamageSourceEntity) {
                if (source instanceof DamageSourceEntityIndirect) {
                    PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_RAW.get(EvolutionDamage.Type.RANGED), damage);
                }
                else {
                    PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_RAW.get(EvolutionDamage.Type.MELEE), damage);
                }
            }
            PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_RAW.get(EvolutionDamage.Type.TOTAL), damage);
        }
        if (IGNORED_DAMAGE_SOURCES.contains(source)) {
            return;
        }
        Evolution.LOGGER.debug("amount = " + damage);
        //Raytracing projectile damage
        if (source instanceof DamageSourceEntityIndirect && source.isProjectile()) {
            if (hitEntity instanceof PlayerEntity) {
                EquipmentSlotType hitLocation = PlayerHelper.getPartByPosition(source.getDirectEntity().getBoundingBox().minY,
                                                                               (PlayerEntity) hitEntity);
                ((DamageSourceEntityIndirect) source).setHitLocation(hitLocation);
                Evolution.LOGGER.debug("hitLocation ranged = {}", hitLocation);
            }
            return;
        }
        //Raytracing melee damage
        if (source instanceof DamageSourceEntity) {
            Entity trueSource = source.getEntity();
            if (trueSource instanceof PlayerEntity) {
                return;
            }
            double range = 3;
            if (trueSource instanceof IAgressive) {
                range = ((IAgressive) trueSource).getReach();
            }
            EntityRayTraceResult rayTrace = MathHelper.rayTraceEntityFromEyes(trueSource, 1.0F, range);
            if (rayTrace == null) {
                event.setCanceled(true);
                return;
            }
            Entity rayTracedEntity = rayTrace.getEntity();
            if (hitEntity.equals(rayTracedEntity)) {
                if (hitEntity instanceof PlayerEntity) {
                    EquipmentSlotType type = PlayerHelper.getPartByPosition(rayTrace.getLocation().y, (PlayerEntity) hitEntity);
                    Evolution.LOGGER.debug("type = {}", type);
                    this.damageMultipliers.put(source, type);
                }
            }
            else {
                rayTracedEntity.hurt(source, damage);
                event.setCanceled(true);
            }
            return;
        }
        Evolution.LOGGER.debug("Damage Source not calculated: " + source.msgId);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }
        LivingEntity killed = event.getEntityLiving();
        LivingEntity killer = killed.getKillCredit();
        if (killed instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) killed;
            EntityPlayerCorpse corpse = new EntityPlayerCorpse(player);
            if (!player.level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).get()) {
                corpse.setInventory(player);
            }
            player.level.addFreshEntity(corpse);
            if (killer != null) {
                killer.killed((ServerWorld) killer.level, player);
            }
            PlayerHelper.takeStat(player, Stats.CUSTOM.get(EvolutionStats.TIME_SINCE_LAST_DEATH));
            PlayerHelper.takeStat(player, Stats.CUSTOM.get(EvolutionStats.TIME_SINCE_LAST_REST));
            player.awardStat(EvolutionStats.DEATHS);
            DamageSource src = event.getSource();
            if (src instanceof DamageSourceEv) {
                ResourceLocation stat = EvolutionStats.DEATH_SOURCE.get(src.msgId);
                if (stat == null) {
                    if (!"fall_damage".equals(src.msgId)) {
                        Evolution.LOGGER.warn("Unknown stat for {}", src);
                    }
                }
                else {
                    player.awardStat(stat);
                }
            }
        }
        if (killer instanceof ServerPlayerEntity) {
            EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) killer), new PacketSCHitmarker(true));
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntityLiving();
        double velocity = entity.getDeltaMovement().y;
        double fallDistanceSlowDown = 1 - event.getDamageMultiplier();
        if (entity instanceof PlayerEntity) {
            if (entity.level.isClientSide) {
                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerFall(velocity, fallDistanceSlowDown));
            }
            return;
        }
        calculateFallDamage(entity, velocity, fallDistanceSlowDown, false);
    }

    @SubscribeEvent
    public void onLivingHeal(LivingHealEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving().level.isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            EquipmentSlotType hitPart = this.damageMultipliers.remove(source);
            if (source instanceof IHitLocation) {
                hitPart = ((IHitLocation) source).getHitLocation();
            }
            EvolutionDamage.Type type = EvolutionDamage.Type.GENERIC;
            if (source instanceof DamageSourceEv) {
                type = ((DamageSourceEv) source).getType();
            }
            float computedDamage = PlayerHelper.getDamage(hitPart, player, event.getAmount(), type);
            if (computedDamage > 0) {
                float statDamage = computedDamage;
                if (statDamage > player.getHealth() + player.getAbsorptionAmount()) {
                    statDamage = player.getHealth() + player.getAbsorptionAmount();
                }
                ResourceLocation resLoc = EvolutionStats.DAMAGE_TAKEN_ACTUAL.get(type);
                if (resLoc != null) {
                    PlayerHelper.addStat(player, resLoc, statDamage);
                }
                if (source instanceof DamageSourceEntity) {
                    if (source instanceof DamageSourceEntityIndirect) {
                        PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_ACTUAL.get(EvolutionDamage.Type.RANGED), statDamage);
                    }
                    else {
                        PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_ACTUAL.get(EvolutionDamage.Type.MELEE), statDamage);
                    }
                    PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN.get(), source.getEntity().getType(), statDamage);
                }
                PlayerHelper.addStat(player, EvolutionStats.DAMAGE_TAKEN_ACTUAL.get(EvolutionDamage.Type.TOTAL), statDamage);
            }
            event.setAmount(computedDamage);
        }
        Entity trueSource = source.getEntity();
        if (trueSource instanceof ServerPlayerEntity) {
            EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) trueSource), new PacketSCHitmarker(false));
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
            ((PlayerEntity) event.getEntityLiving()).awardStat(EvolutionStats.JUMPS);
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof PlayerEntity) && !(entity instanceof EntityGenericCreature)) {
            return;
        }
        //Removes damage immunity
        entity.invulnerableTime = 0;
        //Sets the combat tracker
        CombatTrackerEv combatTracker = (CombatTrackerEv) entity.getCombatTracker();
        if (entity.onClimbable() && !entity.isOnGround()) {
            entity.setSprinting(false);
            BlockPos pos = new BlockPos(entity.getX(), entity.getBoundingBox().minY, entity.getZ());
            BlockState state = entity.level.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof IClimbable && !entity.level.isClientSide) {
                float sweepAngle = ((IClimbable) block).getSweepAngle();
                if (!Float.isNaN(sweepAngle)) {
                    float yaw = MathHelper.wrapDegrees(entity.getViewYRot(1.0f));
                    yaw = MathHelper.clampAngle(yaw, sweepAngle, ((IClimbable) block).getDirection(state));
                    entity.yRot = yaw;
                    entity.yRotO = yaw;
                }
            }
            combatTracker.setFallSuffixBlock(block);
        }
        else if (entity.isOnGround()) {
            combatTracker.setFallSuffixBlock(null);
        }
        //Deals damage inside blocks
        if (entity.isInWall()) {
            if (entity.tickCount % 10 == 0) {
                entity.hurt(EvolutionDamage.IN_WALL, 5.0F);
            }
        }
    }

    /**
     * Cancels the default player attack. Attack is calculated in {@link PlayerHelper#performAttack(PlayerEntity, Entity, Hand, double)}
     */
    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPlayerBreakBlock(PlayerEvent.BreakSpeed event) {
        //If the block is breakable by hand, do nothing
        if (event.getState().getHarvestLevel() <= HarvestLevel.HAND) {
            return;
        }
        //Prevents players from breaking blocks if their tool cannot harvest the block
        if (!event.getPlayer().getMainHandItem().isCorrectToolForDrops(event.getState())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                                       new PacketSCChangeTickrate(TickrateChanger.getCurrentTickrate()));
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        event.getPlayer().awardStat(EvolutionStats.LEAVE_GAME);
    }

    @SubscribeEvent
    public void onPlayerRespawns(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        player.getStats().markAllDirty();
        player.clearFire();
        ServerWorld world = (ServerWorld) player.level;
        Optional<BlockPos> bedPos = player.getSleepingPos();
        if (bedPos.isPresent()) {
            return;
        }
        BlockPos worldSpawnPos = world.getSharedSpawnPos();
        if (world.dimensionType().hasSkyLight() && world.getServer().getWorldData().getGameType() != GameType.ADVENTURE) {
            int spawnRadius = Math.max(0, player.server.getSpawnRadius(world));
            int worldBorder = MathHelper.floor(world.getWorldBorder().getDistanceToBorder(worldSpawnPos.getX(), worldSpawnPos.getZ()));
            if (worldBorder < spawnRadius) {
                spawnRadius = worldBorder;
            }
            if (worldBorder <= 1) {
                spawnRadius = 1;
            }
            long spawnDiameter = spawnRadius * 2L + 1;
            long spawnArea = spawnDiameter * spawnDiameter;
            int spawnAreaCasted = spawnArea > 2_147_483_647L ? Integer.MAX_VALUE : (int) spawnArea;
            int spawnAreaClamped = strangeRespawnFunc(spawnAreaCasted);
            int randomInt = RANDOM.nextInt(spawnAreaCasted);
            for (int l1 = 0; l1 < spawnAreaCasted; ++l1) {
                int i2 = (randomInt + spawnAreaClamped * l1) % spawnAreaCasted;
                int j2 = i2 % (spawnRadius * 2 + 1);
                int k2 = i2 / (spawnRadius * 2 + 1);
                BlockPos spawnPos = WorldEvents.findSpawn(world, worldSpawnPos.getX() + j2 - spawnRadius, worldSpawnPos.getZ() + k2 - spawnRadius);
                if (spawnPos != null) {
                    player.moveTo(spawnPos, 0.0F, 0.0F);
                    if (world.isUnobstructed(player)) {
                        player.setPosAndOldPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        if (event.phase == TickEvent.Phase.START) {
            player.getFoodData().setFoodLevel(20);
            if (player.isCreative()) {
                player.getAttribute(ForgeMod.REACH_DISTANCE.get()).setBaseValue(12);
            }
            else {
                player.getAttribute(ForgeMod.REACH_DISTANCE.get()).setBaseValue(PlayerHelper.REACH_DISTANCE);
            }
            //Handles Status Updates
            if (!player.level.isClientSide) {
                //Handles Player health regeneration
                if (player.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
                    UUID uuid = player.getUUID();
                    if (player.hurtTime > 0) {
                        this.playerTimeSinceLastHit.put(uuid, 0);
                    }
                    else {
                        int time = this.playerTimeSinceLastHit.getOrDefault(uuid, 0) + 1;
                        if (time == 100) {
                            this.playerTimeSinceLastHit.put(uuid, 0);
                            if (player.isHurt()) {
                                float currentHealth = MathHelper.clampMin(player.getHealth(), 1);
                                float healAmount = currentHealth / 100;
                                player.setHealth(player.getHealth() + healAmount);
                            }
                        }
                        else {
                            this.playerTimeSinceLastHit.put(uuid, time);
                        }
                    }
                }
                if (player.isHurt() && player.hasEffect(Effects.REGENERATION)) {
                    EffectInstance instance = player.getEffect(Effects.REGENERATION);
                    int timer = 50 >> instance.getAmplifier();
                    if (timer < 1) {
                        timer = 1;
                    }
                    if (instance.getDuration() % timer == 0) {
                        player.setHealth(player.getHealth() + 1);
                    }
                }
                //Ticks Player Thirst system
                if (!player.isCreative() && !player.isSpectator()) {
                    IThirst thirst = player.getCapability(CapabilityThirst.INSTANCE).orElseThrow(IllegalStateException::new);
                    thirst.tick((ServerPlayerEntity) player);
                }
            }
        }
        else if (event.phase == TickEvent.Phase.END) {
            player.awardStat(EvolutionStats.TIME_PLAYED);
            if (player.getPose() == Pose.CROUCHING) {
                player.setSprinting(false);
                player.awardStat(EvolutionStats.TIME_SNEAKING);
            }
            if (!player.isSleeping()) {
                if (player.isAlive()) {
                    player.awardStat(EvolutionStats.TIME_SINCE_LAST_REST);
                }
            }
            else {
                PlayerHelper.takeStat(player, Stats.CUSTOM.get(EvolutionStats.TIME_SINCE_LAST_REST));
            }
            if (player.isAlive()) {
                player.awardStat(EvolutionStats.TIME_SINCE_LAST_DEATH);
            }
            if (Evolution.PRONED_PLAYERS.getOrDefault(player.getId(), false)) {
                SET_POSE_METHOD.call(player, Pose.SWIMMING);
            }
            else {
                player.maxUpStep = 0.6f;
            }
            if (player.getPose() == Pose.SWIMMING && !player.isInWater()) {
                player.setSprinting(false);
                player.maxUpStep = getStepHeight(player);
            }
            if (!player.level.isClientSide) {
                //Put off torches in Water
                if (player.isEyeInFluid(FluidTags.WATER)) {
                    ItemStack mainHand = player.getMainHandItem();
                    ItemStack offHand = player.getOffhandItem();
                    boolean torch = false;
                    if (mainHand.getItem() == EvolutionItems.torch.get()) {
                        int count = mainHand.getCount();
                        player.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(EvolutionItems.torch_unlit.get(), count));
                        torch = true;
                    }
                    if (offHand.getItem() == EvolutionItems.torch.get()) {
                        int count = offHand.getCount();
                        player.setItemSlot(EquipmentSlotType.OFFHAND, new ItemStack(EvolutionItems.torch_unlit.get(), count));
                        torch = true;
                    }
                    if (torch) {
                        player.level.playSound(null,
                                               player.blockPosition(),
                                               SoundEvents.FIRE_EXTINGUISH,
                                               SoundCategory.BLOCKS,
                                               1.0F,
                                               2.6F + (player.level.random.nextFloat() - player.level.random.nextFloat()) * 0.8F);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
            if (event.getOldPotionEffect() == null) {
                EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                                               new PacketSCAddEffect(event.getPotionEffect(), PacketSCAddEffect.Logic.ADD));
            }
            else {
                EffectInstance newEffect = new EffectInstance(event.getOldPotionEffect());
                newEffect.update(event.getPotionEffect());
                boolean isSame = event.getOldPotionEffect().getAmplifier() == newEffect.getAmplifier();
                if (isSame) {
                    EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                                                   new PacketSCAddEffect(newEffect, PacketSCAddEffect.Logic.UPDATE));
                }
                else {
                    EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                                                   new PacketSCAddEffect(newEffect, PacketSCAddEffect.Logic.REPLACE));
                }
            }
        }
    }

    @SubscribeEvent
    public void onPotionExpired(PotionEvent.PotionExpiryEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayerEntity)) {
            return;
        }
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getEntityLiving()),
                                       new PacketSCRemoveEffect(event.getPotionEffect().getEffect()));
    }

    @SubscribeEvent
    public void onPotionRemoved(PotionEvent.PotionRemoveEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayerEntity)) {
            return;
        }
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getEntityLiving()),
                                       new PacketSCRemoveEffect(event.getPotion()));
    }

    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
        EvolutionCommands.register(event.getServer().getCommands().getDispatcher());
    }

    @SubscribeEvent
    public void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            MobEntity entity = (MobEntity) event.getEntity();
            if (entity.getTarget() != null && event.getTarget() != null) {
                if (entity.hasEffect(Effects.BLINDNESS)) {
                    int effectLevel = 3 - entity.getEffect(Effects.BLINDNESS).getAmplifier();
                    if (effectLevel < 0) {
                        effectLevel = 0;
                    }
                    if (entity.distanceTo(event.getTarget()) > effectLevel) {
                        entity.setTarget(null);
                    }
                }
            }
        }
    }
}
