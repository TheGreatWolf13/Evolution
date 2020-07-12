package tgw.evolution.events;

import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.passive.horse.TraderLlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
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
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.inventory.PlayerInventoryCapability;
import tgw.evolution.capabilities.inventory.PlayerInventoryCapabilityProvider;
import tgw.evolution.entities.CreatureEntity;
import tgw.evolution.entities.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.inventory.ContainerExtendedHandler;
import tgw.evolution.network.PacketCSPlayerFall;
import tgw.evolution.util.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class EntityEvents {

    private static final Method SET_POSE_METHOD = ObfuscationReflectionHelper.findMethod(Entity.class, "func_213301_b", Pose.class);
    private static final Random RANDOM = new Random();
    private final Set<CreatureEntity> jumping = new HashSet<>();
    private final Map<DamageSource, Float> damageMultipliers = new WeakHashMap<>();

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

    public static void calculateFallDamage(Entity entity, double velocity, double distanceOfSlowDown) {
        //Convert from m/t to m/s
        velocity *= 20;
        //TODO entities have a legHeight
        double legHeight = 0.875;
        distanceOfSlowDown += legHeight;
        //TODO entities have a mass attribute
        double baseMass = 70;
        double totalMass = baseMass;
        if (entity instanceof PlayerEntity) {
            IAttributeInstance massAttribute = ((PlayerEntity) entity).getAttribute(EvolutionAttributes.MASS);
            baseMass = massAttribute.getBaseValue();
            totalMass = massAttribute.getValue();
        }
        double kinecticEnergy = totalMass * velocity * velocity / 2;
        double forceOfImpact = kinecticEnergy / distanceOfSlowDown;
        double area = entity.getWidth() * entity.getWidth();
        double pressureOfFall = forceOfImpact / area;
        double maxSupportedPressure = baseMass / (area * 0.035);
        double deltaPressure = MathHelper.clampMin(pressureOfFall - maxSupportedPressure, 0);
        float amount = (float) Math.pow(deltaPressure, 1.7) / 900000;
        entity.attackEntityFrom(EvolutionDamage.FALL, amount);
    }

    private static float getStepHeight(PlayerEntity player) {
        IAttributeInstance mass = player.getAttribute(EvolutionAttributes.MASS);
        int baseMass = (int) mass.getBaseValue();
        int totalMass = (int) mass.getValue();
        int equipMass = totalMass - baseMass;
        float stepHeight = 1.0625f - equipMass * 0.00114f;
        return MathHelper.clampMin(stepHeight, 0.6f);
    }

    private static void calculateWaterFallDamage(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            return;
        }
        BlockPos pos = entity.getPosition();
        IFluidState fluidState = entity.world.getFluidState(pos);
        double distanceOfSlowDown = fluidState.getLevel() * 0.0625;
        double velocity = entity.getMotion().y;
        if (entity instanceof ClientPlayerEntity) {
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerFall(velocity, distanceOfSlowDown));
            return;
        }
        calculateFallDamage(entity, velocity, distanceOfSlowDown);
    }

    @SubscribeEvent
    public void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            //TODO use my own entity classes
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

    @SubscribeEvent
    public void onServerTick(WorldTickEvent event) {
        for (Iterator<CreatureEntity> iterator = this.jumping.iterator(); iterator.hasNext(); ) {
            CreatureEntity entity = iterator.next();
            if (entity.getMotion().y > 0) {
                entity.setMotion(entity.getMotion().x, entity.getMotion().y - 0.03, entity.getMotion().z);
            }
            else {
                iterator.remove();
                entity.setMotion(entity.getMotion().x, entity.getMotion().y - 0.055, entity.getMotion().z);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawns(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        ServerWorld world = (ServerWorld) player.world;
        BlockPos bedPos = player.getBedLocation(player.dimension);
        Optional<Vec3d> optional = PlayerEntity.func_213822_a(world, bedPos, false);
        if (optional.isPresent()) {
            return;
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
            long spawnDiameter = spawnRadius * 2 + 1;
            long spawnArea = spawnDiameter * spawnDiameter;
            int spawnAreaCasted = spawnArea > 2147483647L ? Integer.MAX_VALUE : (int) spawnArea;
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
        EntityRayTraceResult rayTraceResult = MathHelper.rayTraceEntityFromEyes(player, 1, player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue());
        if (rayTraceResult != null) {
            player.attackTargetEntityWithCurrentItem(rayTraceResult.getEntity());
            player.resetCooldown();
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
        if (!(entity instanceof PlayerEntity) && !(entity instanceof CreatureEntity)) {
            return;
        }
        //Sets the gravity of the Living Entities and the Player to be that of the dimension they're in
        ((LivingEntity) entity).getAttribute(LivingEntity.ENTITY_GRAVITY).setBaseValue(Gravity.gravity(entity.world.getDimension()));
        ((LivingEntity) entity).getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).getAttributes().registerAttribute(EvolutionAttributes.MASS);
            ((PlayerEntity) entity).getAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(PlayerHelper.ATTACK_SPEED);
            ((PlayerEntity) entity).getAttribute(PlayerEntity.REACH_DISTANCE).setBaseValue(PlayerHelper.REACH_DISTANCE);
        }
        else {
            //Makes the Living Entities able to step up one block, instead of jumping (it looks better)
            entity.stepHeight = 1.0625F;
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        Entity entity = event.getEntity();
        //TODO my entities too
        if (!(entity instanceof PlayerEntity)) {
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
        calculateFallDamage(entity, velocity, fallDistanceSlowDown);
    }

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (player.isCreative() && player.isSneaking() && player.getHeldItemMainhand().getItem() instanceof BlockItem) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingUpdateEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof PlayerEntity) && !(entity instanceof CreatureEntity)) {
            return;
        }
        //Applies air drag on all living entities
        double horizontalDrag = Gravity.horizontalDrag(entity.world.dimension, entity.getWidth(), entity.getHeight());
        double verticalDrag = Gravity.verticalDrag(entity.world.dimension, entity.getWidth());
        event.getEntityLiving().setMotion(entity.getMotion().mul(horizontalDrag, verticalDrag, horizontalDrag));
        event.getEntityLiving().jumpMovementFactor = 0.015F;
        //Deals with water
        if (!entity.onGround && entity.isInWater()) {
            calculateWaterFallDamage(entity);
        }
        //Reduces the speed of ladders
        if (event.getEntityLiving().isOnLadder()) {
            if (event.getEntityLiving() instanceof PlayerEntity && ((PlayerEntity) event.getEntityLiving()).abilities.isFlying) {
                return;
            }
            Vec3d motion = event.getEntityLiving().getMotion();
            event.getEntityLiving().setMotion(motion.x, motion.y - 0.08, motion.z);
        }
    }

    @SubscribeEvent
    public void onEntityJump(LivingJumpEvent event) {
        if (!(event.getEntity() instanceof CreatureEntity)) {
            return;
        }
        this.jumping.add((CreatureEntity) event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        //Cancels the default player attack. Attack is calculated in PlayerHelper.performAttack(PlayerEntity, Entity, Hand)
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onEntityKnockedBack(LivingKnockBackEvent event) {
        //TODO FIX THIS SHIT KNOCKBACK
        event.setCanceled(true);
        event.getEntity().isAirBorne = true;
        Vec3d motion = event.getEntity().getMotion();
        double knockbackResistance = 0;
        Vec3d knockbackVector = new Vec3d(event.getRatioX(), 0, event.getRatioZ()).normalize().scale(2 * event.getStrength()).scale(1 - knockbackResistance);
        event.getEntity().setMotion(motion.x - knockbackVector.x, motion.y, motion.z - knockbackVector.z);
        event.getEntityLiving().velocityChanged = true;
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
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(Evolution.location("extended_inventory"), new PlayerInventoryCapabilityProvider());
        }
    }

    @SubscribeEvent
    public void cloneCapabilitiesEvent(PlayerEvent.Clone event) {
        try {
            ContainerExtendedHandler handler = (ContainerExtendedHandler) event.getOriginal().getCapability(PlayerInventoryCapability.CAPABILITY_EXTENDED_INVENTORY).orElseThrow(IllegalStateException::new);
            CompoundNBT nbt = handler.serializeNBT();
            ContainerExtendedHandler handlerClone = (ContainerExtendedHandler) event.getPlayer().getCapability(PlayerInventoryCapability.CAPABILITY_EXTENDED_INVENTORY).orElseThrow(IllegalStateException::new);
            handlerClone.deserializeNBT(nbt);
        }
        catch (Exception e) {
            Evolution.LOGGER.error("Could not clone player [" + event.getOriginal().getName() + "] extended inventory when changing dimensions");
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        if (event.getEntity() instanceof PlayerEntity) {
            //Drop contents of extended inventory if keepInventory is off
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (!player.world.getGameRules().get(GameRules.KEEP_INVENTORY).get()) {
                ContainerExtendedHandler handler = (ContainerExtendedHandler) player.getCapability(PlayerInventoryCapability.CAPABILITY_EXTENDED_INVENTORY).orElseThrow(IllegalStateException::new);
                for (int i = 0; i < handler.getSlots(); i++) {
                    spawnDrops(handler.getStackInSlot(i), player.world, player.getPosition());
                    handler.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving().world.isRemote) {
            return;
        }
        if (event.getSource() instanceof DamageSourceMelee) {
            //TODO my entities damage multipliers
            if (event.getEntityLiving() instanceof PlayerEntity) {
                PlayerHelper.reactToDamageType((PlayerEntity) event.getEntityLiving(), ((DamageSourceMelee) event.getSource()).getType());
            }
        }
        if (event.getSource().getTrueSource() instanceof PlayerEntity) {
            event.getEntityLiving().hurtResistantTime = 0;
        }
        if (event.getEntityLiving() instanceof PlayerEntity) {
            Float multiplier = this.damageMultipliers.remove(event.getSource());
            Evolution.LOGGER.debug("damage before = " + event.getAmount());
            Evolution.LOGGER.debug("multiplier = " + multiplier);
            if (multiplier != null) {
                event.setAmount(event.getAmount() * multiplier);
                Evolution.LOGGER.debug("damage after = " + event.getAmount());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.isCreative()) {
            event.player.getAttribute(PlayerEntity.REACH_DISTANCE).setBaseValue(12);
        } else {
            event.player.getAttribute(PlayerEntity.REACH_DISTANCE).setBaseValue(PlayerHelper.REACH_DISTANCE);
        }
        if (event.phase == TickEvent.Phase.END) {
            if (Evolution.PRONED_PLAYERS.getOrDefault(event.player.getUniqueID(), false)) {
                event.player.setSprinting(false);
                event.player.stepHeight = getStepHeight(event.player);
                try {
                    SET_POSE_METHOD.invoke(event.player, Pose.SWIMMING);
                }
                catch (IllegalAccessException | InvocationTargetException e) {
                    Evolution.LOGGER.error("Error when setting player {} to prone: {}", event.player.getDisplayNameAndUUID(), e);
                }
            }
            else {
                event.player.stepHeight = 0.6f;
            }
            if (!event.player.world.isRemote) {
                //Put off torches in Water
                if (event.player.areEyesInFluid(FluidTags.WATER)) {
                    ItemStack mainHand = event.player.getHeldItemMainhand();
                    ItemStack offHand = event.player.getHeldItemOffhand();
                    boolean torch = false;
                    if (mainHand.getItem() == EvolutionItems.torch.get()) {
                        int count = mainHand.getCount();
                        event.player.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(EvolutionItems.stick.get(), count));
                        torch = true;
                    }
                    if (offHand.getItem() == EvolutionItems.torch.get()) {
                        int count = offHand.getCount();
                        event.player.setItemStackToSlot(EquipmentSlotType.OFFHAND, new ItemStack(EvolutionItems.stick.get(), count));
                        torch = true;
                    }
                    if (torch) {
                        event.player.world.playSound(null, event.player.getPosition(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1F, 2.6F + (event.player.world.rand.nextFloat() - event.player.world.rand.nextFloat()) * 0.8F);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntityLiving().world.isRemote) {
            return;
        }
        LivingEntity hitEntity = event.getEntityLiving();
        DamageSource source = event.getSource();
        if (source == EvolutionDamage.FALL) {
            return;
        }
        float damage = event.getAmount();
        Evolution.LOGGER.debug("amount = " + damage);
        if (source == DamageSource.FALL) {
            //TODO cancels for my entities too when they're ready
            if (hitEntity instanceof PlayerEntity) {
                event.setCanceled(true);
            }
            return;
        }
        if (source instanceof IndirectEntityDamageSource && source.isProjectile()) {
            if (hitEntity instanceof PlayerEntity) {
                EquipmentSlotType type = PlayerHelper.getPartByPosition(source.getImmediateSource().getBoundingBox().minY, (PlayerEntity) hitEntity);
                Evolution.LOGGER.debug("type ranged = {}", type);
                this.damageMultipliers.put(source, PlayerHelper.getProjectileModifier(type));
            }
            return;
        }
        if (source instanceof EntityDamageSource) {
            Entity trueSource = source.getTrueSource();
            if (trueSource instanceof PlayerEntity && !(hitEntity instanceof PlayerEntity)) {
                return;
            }
            if (!((EntityDamageSource) source).getIsThornsDamage()) {
                //TODO use range
                EntityRayTraceResult rayTrace = MathHelper.rayTraceEntityFromEyes(trueSource, 1f, 5);
                if (rayTrace == null) {
                    event.setCanceled(true);
                    return;
                }
                Entity rayTracedEntity = rayTrace.getEntity();
                if (hitEntity == rayTracedEntity) {
                    if (hitEntity instanceof PlayerEntity) {
                        EquipmentSlotType type = PlayerHelper.getPartByPosition(rayTrace.getHitVec().y, (PlayerEntity) hitEntity);
                        Evolution.LOGGER.debug("type = {}", type);
                        this.damageMultipliers.put(source, PlayerHelper.getHitMultiplier(type, (PlayerEntity) hitEntity, damage));
                    }
                }
                else {
                    rayTracedEntity.attackEntityFrom(source, damage);
                    event.setCanceled(true);
                }
                return;
            }
        }
        Evolution.LOGGER.debug("Damage Source not calculated: " + source.damageType);
    }
}
