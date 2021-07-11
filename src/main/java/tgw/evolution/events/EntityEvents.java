package tgw.evolution.events;

import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.passive.horse.TraderLlamaEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
import tgw.evolution.capabilities.inventory.PlayerInventoryCapability;
import tgw.evolution.capabilities.inventory.PlayerInventoryCapabilityProvider;
import tgw.evolution.capabilities.thirst.PlayerThirstCapabilityProvider;
import tgw.evolution.entities.EntityGenericCreature;
import tgw.evolution.entities.IAgressive;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.*;
import tgw.evolution.inventory.extendedinventory.ContainerExtendedHandler;
import tgw.evolution.network.PacketCSPlayerFall;
import tgw.evolution.network.PacketSCChangeTickrate;
import tgw.evolution.network.PacketSCRemoveEffect;
import tgw.evolution.network.PacketSCUpdateCameraTilt;
import tgw.evolution.util.*;
import tgw.evolution.util.damage.*;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.MethodHandler;

import java.util.*;

public class EntityEvents {

    public static final WindVector WIND = new WindVector();
    private static final MethodHandler<Entity, Void> SET_POSE_METHOD = new MethodHandler<>(Entity.class, "func_213301_b", Pose.class);
    private static final FieldHandler<LivingEntity, CombatTracker> COMBAT_TRACKER_FIELD = new FieldHandler<>(LivingEntity.class, "field_94063_bt");
    private static final FieldHandler<PlayerAbilities, Float> PLAYER_SPEED_FIELD = new FieldHandler<>(PlayerAbilities.class, "field_75097_g");
    private static final Random RANDOM = new Random();
    private static final Set<DamageSource> IGNORED_DAMAGE_SOURCES = Util.make(Sets.newHashSet(), set -> {
        set.add(EvolutionDamage.DROWN);
        set.add(EvolutionDamage.FALL);
        set.add(EvolutionDamage.FALLING_ROCK);
        set.add(EvolutionDamage.IN_FIRE);
        set.add(EvolutionDamage.IN_WALL);
        set.add(EvolutionDamage.ON_FIRE);
        set.add(EvolutionDamage.VOID);
        set.add(EvolutionDamage.WALL_IMPACT);
        set.add(EvolutionDamage.WATER_IMPACT);
    });
    private static final Set<DamageSource> IGNORED_VANILLA_SOURCES = Util.make(Sets.newHashSet(), set -> {
        set.add(DamageSource.DROWN);
        set.add(DamageSource.FALL);
        set.add(DamageSource.IN_WALL);
        set.add(DamageSource.OUT_OF_WORLD);
    });
    private final Map<DamageSource, EquipmentSlotType> damageMultipliers = new WeakHashMap<>();
    private final Map<UUID, Integer> playerTimeSinceLastHit = new HashMap<>();

    public static void calculateFallDamage(Entity entity, double velocity, double distanceOfSlowDown, boolean isWater) {
        //Convert from m/t to m/s
        velocity *= 20;
        double legHeight = PlayerHelper.LEG_HEIGHT;
        double baseMass = PlayerHelper.MASS;
        if (entity instanceof EntityGenericCreature) {
            EntityGenericCreature creature = (EntityGenericCreature) entity;
            legHeight = creature.getLegHeight();
            baseMass = creature.getMass();
        }
        distanceOfSlowDown += legHeight;
        double totalMass = baseMass;
        if (entity instanceof PlayerEntity) {
            IAttributeInstance massAttribute = ((PlayerEntity) entity).getAttribute(EvolutionAttributes.MASS);
            baseMass = massAttribute.getBaseValue();
            totalMass = massAttribute.getValue();
        }
        double kineticEnergy = totalMass * velocity * velocity / 2;
        double forceOfImpact = kineticEnergy / distanceOfSlowDown;
        double area = entity.getWidth() * entity.getWidth();
        double pressureOfFall = forceOfImpact / area;
        double maxSupportedPressure = baseMass / (area * 0.035);
        double deltaPressure = MathHelper.clampMin(pressureOfFall - maxSupportedPressure, 0);
        float amount = (float) Math.pow(deltaPressure, 1.7) / 750_000;
        if (amount >= 1) {
            if (isWater) {
                entity.attackEntityFrom(EvolutionDamage.WATER_IMPACT, amount);
            }
            else {
                entity.attackEntityFrom(EvolutionDamage.FALL, amount);
            }
        }
    }

    public static void calculateWaterFallDamage(Entity entity) {
        BlockPos pos = entity.getPosition();
        IFluidState fluidState = entity.world.getFluidState(pos);
        IFluidState fluidStateDown = entity.world.getFluidState(pos.down());
        double distanceOfSlowDown = fluidState.getLevel() * 0.062_5;
        if (!fluidStateDown.isEmpty()) {
            distanceOfSlowDown += 1;
            IFluidState fluidStateDown2 = entity.world.getFluidState(pos.down(2));
            if (!fluidStateDown2.isEmpty()) {
                distanceOfSlowDown += 1;
            }
        }
        double velocity = entity.getMotion().y;
        calculateFallDamage(entity, velocity, distanceOfSlowDown, true);
    }

    private static float getStepHeight(PlayerEntity player) {
        IAttributeInstance mass = player.getAttribute(EvolutionAttributes.MASS);
        int baseMass = (int) mass.getBaseValue();
        int totalMass = (int) mass.getValue();
        int equipMass = totalMass - baseMass;
        float stepHeight = 1.062_5f - equipMass * 0.001_14f;
        return MathHelper.clampMin(stepHeight, 0.6f);
    }

    private static void spawnDrops(ItemStack stack, World worldIn, BlockPos pos) {
        double xOffset = worldIn.getRandom().nextFloat() * 0.7F + 0.65F;
        double yOffset = worldIn.getRandom().nextFloat() * 0.7F + 0.65F;
        double zOffset = worldIn.getRandom().nextFloat() * 0.7F + 0.65F;
        ItemEntity entity = new ItemEntity(worldIn, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, stack);
        entity.setPickupDelay(10);
        worldIn.addEntity(entity);
    }

    private static int strangeRespawnFunc(int spawnArea) {
        return spawnArea <= 16 ? spawnArea - 1 : 17;
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(Evolution.getResource("extended_inventory"), new PlayerInventoryCapabilityProvider());
            event.addCapability(Evolution.getResource("thirst"), new PlayerThirstCapabilityProvider());
        }
    }

    @SubscribeEvent
    public void cloneCapabilitiesEvent(PlayerEvent.Clone event) {
        try {
            ContainerExtendedHandler handler = (ContainerExtendedHandler) event.getOriginal()
                                                                               .getCapability(PlayerInventoryCapability.CAPABILITY_EXTENDED_INVENTORY)
                                                                               .orElseThrow(IllegalStateException::new);
            CompoundNBT nbt = handler.serializeNBT();
            ContainerExtendedHandler handlerClone = (ContainerExtendedHandler) event.getPlayer()
                                                                                    .getCapability(PlayerInventoryCapability.CAPABILITY_EXTENDED_INVENTORY)
                                                                                    .orElseThrow(IllegalStateException::new);
            handlerClone.deserializeNBT(nbt);
        }
        catch (Exception e) {
            Evolution.LOGGER.error("Could not clone player [" + event.getOriginal().getName() + "] extended inventory when changing dimensions");
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
        living.getAttribute(LivingEntity.ENTITY_GRAVITY).setBaseValue(Gravity.gravity(living.world.getDimension()));
        if (living instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) living;
            PLAYER_SPEED_FIELD.set(player.abilities, (float) PlayerHelper.WALK_FORCE);
        }
        //TODO
//        else {
//            //Makes the Living Entities able to step up one block, instead of jumping (it looks better)
//            entity.stepHeight = 1.0625F;
//        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            EntityPlayerCorpse corpse = new EntityPlayerCorpse(player);
            if (!player.world.getGameRules().get(GameRules.KEEP_INVENTORY).get()) {
                corpse.setInventory(player);
            }
            player.world.addEntity(corpse);
        }
    }

    @SubscribeEvent
    public void onEntityKnockedBack(LivingKnockBackEvent event) {
        event.setCanceled(true);
        LivingEntity entity = event.getEntityLiving();
        double knockbackResistance = entity.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getValue();
        if (knockbackResistance == 1) {
            return;
        }
        float strength = event.getStrength();
        double xRatio = event.getRatioX();
        double zRatio = event.getRatioZ();
        entity.isAirBorne = true;
        Vec3d motion = entity.getMotion();
        Vec3d knockbackVec = new Vec3d(xRatio, 0, zRatio).normalize().scale(strength);
        if (knockbackResistance > 0) {
            knockbackVec = knockbackVec.scale(1 - knockbackResistance);
        }
        entity.setMotion(motion.x - knockbackVec.x, 0, motion.z - knockbackVec.z);
        entity.velocityChanged = true;
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
        if (event.getWorld().isRemote) {
            return;
        }
        PlayerEntity player = event.getEntityPlayer();
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
                                                                                player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue());
        if (rayTraceResult != null) {
            player.attackTargetEntityWithCurrentItem(rayTraceResult.getEntity());
            player.resetCooldown();
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntityLiving().world.isRemote) {
            return;
        }
        LivingEntity hitEntity = event.getEntityLiving();
        DamageSource source = event.getSource();
        if (!(source instanceof DamageSourceEv)) {
            if (!IGNORED_VANILLA_SOURCES.contains(source)) {
                Evolution.LOGGER.debug("Canceling vanilla damage source: {}", source.damageType);
            }
            if (source == DamageSource.OUT_OF_WORLD) {
                hitEntity.attackEntityFrom(EvolutionDamage.VOID, event.getAmount() * 5.0f);
            }
            else if (source == DamageSource.DROWN) {
                hitEntity.attackEntityFrom(EvolutionDamage.DROWN, 10.0f);
            }
            event.setCanceled(true);
            return;
        }
        if (IGNORED_DAMAGE_SOURCES.contains(source)) {
            return;
        }
        float damage = event.getAmount();
        Evolution.LOGGER.debug("amount = " + damage);
        //Raytracing projectile damage
        if (source instanceof DamageSourceEntityIndirect && source.isProjectile()) {
            if (hitEntity instanceof PlayerEntity) {
                EquipmentSlotType hitLocation = PlayerHelper.getPartByPosition(source.getImmediateSource().getBoundingBox().minY,
                                                                               (PlayerEntity) hitEntity);
                ((DamageSourceEntityIndirect) source).setHitLocation(hitLocation);
                Evolution.LOGGER.debug("hitLocation ranged = {}", hitLocation);
            }
            return;
        }
        //Raytracing melee damage
        if (source instanceof DamageSourceEntity) {
            Entity trueSource = source.getTrueSource();
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
                    EquipmentSlotType type = PlayerHelper.getPartByPosition(rayTrace.getHitVec().y, (PlayerEntity) hitEntity);
                    Evolution.LOGGER.debug("type = {}", type);
                    this.damageMultipliers.put(source, type);
                }
            }
            else {
                rayTracedEntity.attackEntityFrom(source, damage);
                event.setCanceled(true);
            }
            return;
        }
        Evolution.LOGGER.debug("Damage Source not calculated: " + source.damageType);
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof PlayerEntity) && !(entity instanceof EntityGenericCreature)) {
            return;
        }
        double velocity = entity.getMotion().y;
        double fallDistanceSlowDown = 1 - event.getDamageMultiplier();
        if (entity instanceof PlayerEntity) {
            if (entity.world.isRemote) {
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
        if (event.getEntityLiving().world.isRemote) {
            return;
        }
        DamageSource source = event.getSource();
        if (event.getEntityLiving() instanceof PlayerEntity) {
            EquipmentSlotType hitPart = this.damageMultipliers.remove(source);
            if (source instanceof IHitLocation) {
                hitPart = ((IHitLocation) source).getHitLocation();
            }
            EvolutionDamage.Type type = EvolutionDamage.Type.GENERIC;
            if (source instanceof DamageSourceEv) {
                type = ((DamageSourceEv) source).getType();
            }
            event.setAmount(PlayerHelper.getDamage(hitPart, (PlayerEntity) event.getEntityLiving(), event.getAmount(), type));
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof PlayerEntity) && !(entity instanceof EntityGenericCreature)) {
            return;
        }
        //Removes damage immunity
        entity.hurtResistantTime = 0;
        //Sets the combat tracker
        CombatTrackerEv combatTracker = (CombatTrackerEv) entity.getCombatTracker();
        if (entity.isOnLadder()) {
            combatTracker.setFallSuffixBlock(entity.world.getBlockState(new BlockPos(entity.posX, entity.getBoundingBox().minY, entity.posZ))
                                                         .getBlock());
        }
        else if (entity.onGround) {
            combatTracker.setFallSuffixBlock(null);
        }
        //Deals damage inside blocks
        if (entity.isEntityInsideOpaqueBlock()) {
            entity.attackEntityFrom(EvolutionDamage.IN_WALL, 5.0F);
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
        if (!event.getPlayer().getHeldItemMainhand().canHarvestBlock(event.getState())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                                       new PacketSCChangeTickrate(TickrateChanger.getCurrentTickrate()));
    }

    @SubscribeEvent
    public void onPlayerRespawns(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        player.clearElytraFlying();
        player.extinguish();
        ServerWorld world = (ServerWorld) player.world;
        BlockPos bedPos = player.getBedLocation(player.dimension);
        //noinspection ConstantConditions
        if (bedPos != null) {
            Optional<Vec3d> optional = PlayerEntity.func_213822_a(world, bedPos, false);
            if (optional.isPresent()) {
                return;
            }
        }
        BlockPos worldSpawnPos = world.getSpawnPoint();
        if (world.dimension.hasSkyLight() && world.getWorldInfo().getGameType() != GameType.ADVENTURE) {
            int spawnRadius = Math.max(0, player.server.func_184108_a(world));
            int worldBorder = MathHelper.floor(world.getWorldBorder().getClosestDistance(worldSpawnPos.getX(), worldSpawnPos.getZ()));
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
                    player.moveToBlockPosAndAngles(spawnPos, 0.0F, 0.0F);
                    if (world.areCollisionShapesEmpty(player)) {
                        player.setPositionAndUpdate(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
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
            player.getFoodStats().setFoodLevel(20);
            if (player.isCreative()) {
                player.getAttribute(PlayerEntity.REACH_DISTANCE).setBaseValue(12);
            }
            else {
                player.getAttribute(PlayerEntity.REACH_DISTANCE).setBaseValue(PlayerHelper.REACH_DISTANCE);
            }
            //Handles Player health regeneration
            if (!player.world.isRemote && player.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
                UUID uuid = player.getUniqueID();
                if (player.hurtTime > 0) {
                    this.playerTimeSinceLastHit.put(uuid, 0);
                }
                else {
                    int time = this.playerTimeSinceLastHit.getOrDefault(uuid, 0) + 1;
                    if (time == 100) {
                        this.playerTimeSinceLastHit.put(uuid, 0);
                        if (player.shouldHeal()) {
                            float currentHealth = MathHelper.clampMin(player.getHealth(), 1);
                            float healAmount = currentHealth / 100;
                            player.setHealth(currentHealth + healAmount);
                        }
                    }
                    else {
                        this.playerTimeSinceLastHit.put(uuid, time);
                    }
                }
            }
        }
        else if (event.phase == TickEvent.Phase.END) {
            if (player.isSneaking()) {
                player.setSprinting(false);
            }
            if (Evolution.PRONED_PLAYERS.getOrDefault(player.getUniqueID(), false)) {
                player.setSprinting(false);
                player.stepHeight = getStepHeight(player);
                SET_POSE_METHOD.call(player, Pose.SWIMMING);
            }
            else {
                player.stepHeight = 0.6f;
            }
            if (!player.world.isRemote) {
                //Put off torches in Water
                if (player.areEyesInFluid(FluidTags.WATER)) {
                    ItemStack mainHand = player.getHeldItemMainhand();
                    ItemStack offHand = player.getHeldItemOffhand();
                    boolean torch = false;
                    if (mainHand.getItem() == EvolutionItems.torch.get()) {
                        int count = mainHand.getCount();
                        player.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(EvolutionItems.torch_unlit.get(), count));
                        torch = true;
                    }
                    if (offHand.getItem() == EvolutionItems.torch.get()) {
                        int count = offHand.getCount();
                        player.setItemStackToSlot(EquipmentSlotType.OFFHAND, new ItemStack(EvolutionItems.torch_unlit.get(), count));
                        torch = true;
                    }
                    if (torch) {
                        player.world.playSound(null,
                                               player.getPosition(),
                                               SoundEvents.BLOCK_FIRE_EXTINGUISH,
                                               SoundCategory.BLOCKS,
                                               1.0F,
                                               2.6F + (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.8F);
                    }
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
                                       new PacketSCRemoveEffect(event.getPotionEffect().getPotion()));
    }

    @SubscribeEvent
    public void onPotionRemoved(PotionEvent.PotionRemoveEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayerEntity)) {
            return;
        }
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getEntityLiving()),
                                       new PacketSCRemoveEffect(event.getPotionEffect().getPotion()));
    }

    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
        EvolutionCommands.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            MobEntity entity = (MobEntity) event.getEntity();
            if (entity.getAttackTarget() != null && event.getTarget() != null) {
                if (entity.isPotionActive(Effects.BLINDNESS)) {
                    int effectLevel = 3 - entity.getActivePotionEffect(Effects.BLINDNESS).getAmplifier();
                    if (effectLevel < 0) {
                        effectLevel = 0;
                    }
                    if (entity.getDistance(event.getTarget()) > effectLevel) {
                        entity.setAttackTarget(null);
                    }
                }
            }
        }
    }
}
