package tgw.evolution.hooks;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.ISoftBlock;
import tgw.evolution.entities.EntityGenericCreature;
import tgw.evolution.entities.IEntityMass;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.items.IEvolutionItem;
import tgw.evolution.network.PacketCSImpactDamage;
import tgw.evolution.network.PacketSCParrySound;
import tgw.evolution.util.*;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.MethodHandler;

public final class LivingEntityHooks {

    private static final FieldHandler<LivingEntity, PlayerEntity> ATTACKING_PLAYER = new FieldHandler<>(LivingEntity.class, "field_70717_bb");
    private static final FieldHandler<LivingEntity, Integer> RECENTLY_HIT = new FieldHandler<>(LivingEntity.class, "field_70718_bc");
    private static final MethodHandler<LivingEntity, SoundEvent> GET_DEATH_SOUND = new MethodHandler<>(LivingEntity.class, "func_184615_bR");
    private static final MethodHandler<LivingEntity, Float> GET_SOUND_VOLUME = new MethodHandler<>(LivingEntity.class, "func_70599_aP");
    private static final MethodHandler<LivingEntity, Float> GET_SOUND_PITCH = new MethodHandler<>(LivingEntity.class, "func_70647_i");
    private static final MethodHandler<LivingEntity, Void> PLAY_HURT_SOUND = new MethodHandler<>(LivingEntity.class,
                                                                                                 "func_184581_c",
                                                                                                 DamageSource.class);
    private static final FieldHandler<LivingEntity, DamageSource> LAST_DAMAGE_SOURCE = new FieldHandler<>(LivingEntity.class, "field_189750_bF");
    private static final FieldHandler<LivingEntity, Long> LAST_DAMAGE_STAMP = new FieldHandler<>(LivingEntity.class, "field_189751_bG");

    private LivingEntityHooks() {
    }

    /**
     * Hooks from {@link LivingEntity#attackEntityFrom(DamageSource, float)}
     */
    @EvolutionHook
    public static boolean attackEntityFrom(LivingEntity entity, DamageSource source, float amount) {
        if (!ForgeHooks.onLivingAttack(entity, source, amount)) {
            return false;
        }
        if (entity.isInvulnerableTo(source)) {
            return false;
        }
        if (entity.world.isRemote) {
            return false;
        }
        if (entity.getHealth() <= 0.0F) {
            return false;
        }
        if (source.isFireDamage() && entity.isPotionActive(Effects.FIRE_RESISTANCE)) {
            return false;
        }
        if (entity.isSleeping() && !entity.world.isRemote) {
            entity.wakeUp();
        }
        entity.setIdleTime(0);
        float f = amount;
        if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) &&
            !entity.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()) {
            entity.getItemStackFromSlot(EquipmentSlotType.HEAD)
                  .damageItem((int) (amount * 4.0F + entity.getRNG().nextFloat() * amount * 2.0F),
                              entity,
                              breaker -> breaker.sendBreakAnimation(EquipmentSlotType.HEAD));
            amount *= 0.75F;
        }
        boolean damageBlocked = false;
        boolean parry = false;
        boolean parrySuccess = false;
        float amountBlocked = 0.0F;
        if (amount > 0.0F && canBlockDamageSource(entity, source)) {
            damageShield(entity, amount);
            amountBlocked = amount;
            amount = 0.0F;
            if (!source.isProjectile()) {
                Entity immediateSource = source.getImmediateSource();
                if (immediateSource instanceof LivingEntity) {
                    blockUsingShield(entity, (LivingEntity) immediateSource);
                }
            }
            damageBlocked = true;
            entity.world.setEntityState(entity, EntityStates.SHIELD_BLOCK_SOUND);
        }
        else if (amount > 0.0f && canParryDamageSource(entity, source)) {
            parry = true;
            int parryTime = getParryTime(entity);
            Evolution.LOGGER.debug("Parry time = {}", parryTime);
            if (parryTime >= 0 && parryTime <= 5) {
                amountBlocked = amount;
                amount = 0.0f;
                if (!source.isProjectile()) {
                    Entity immediateSource = source.getImmediateSource();
                    if (immediateSource instanceof LivingEntity) {
                        blockUsingShield(entity, (LivingEntity) immediateSource);
                    }
                }
                damageBlocked = true;
                parrySuccess = true;
            }
            else {
                amount *= 0.9f;
            }
        }
        entity.limbSwingAmount = 1.5F;
        damageEntity(entity, source, amount);
        entity.maxHurtTime = 10;
        entity.hurtTime = entity.maxHurtTime;
        entity.attackedAtYaw = 0.0F;
        Entity trueSource = source.getTrueSource();
        if (trueSource != null) {
            if (trueSource instanceof LivingEntity) {
                entity.setRevengeTarget((LivingEntity) trueSource);
            }
            if (trueSource instanceof PlayerEntity) {
                RECENTLY_HIT.set(entity, 100);
                ATTACKING_PLAYER.set(entity, (PlayerEntity) trueSource);
            }
            else if (trueSource instanceof TameableEntity) {
                TameableEntity wolf = (TameableEntity) trueSource;
                if (wolf.isTamed()) {
                    RECENTLY_HIT.set(entity, 100);
                    LivingEntity owner = wolf.getOwner();
                    if (owner != null && owner.getType() == EntityType.PLAYER) {
                        ATTACKING_PLAYER.set(entity, (PlayerEntity) owner);
                    }
                    else {
                        ATTACKING_PLAYER.set(entity, null);
                    }
                }
            }
        }
        if (parry) {
            SoundEvent sound;
            if (parrySuccess) {
                sound = EvolutionSounds.PARRY_SUCCESS.get();
            }
            else {
                sound = EvolutionSounds.PARRY_FAIL.get();
            }
            entity.playSound(sound, 0.4f, 0.8F + entity.world.rand.nextFloat() * 0.4F);
            if (entity instanceof ServerPlayerEntity) {
                EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity),
                                               new PacketSCParrySound(parrySuccess));
            }
        }
        else if (!damageBlocked && source instanceof EntityDamageSource && ((EntityDamageSource) source).getIsThornsDamage()) {
            entity.world.setEntityState(entity, EntityStates.THORNS_HIT_SOUND);
        }
        else {
            byte hitSound;
            if (source == EvolutionDamage.DROWN) {
                hitSound = EntityStates.DROWN_HIT_SOUND;
            }
            else if (source.isFireDamage()) {
                hitSound = EntityStates.FIRE_HIT_SOUND;
            }
            else if (source == DamageSource.SWEET_BERRY_BUSH) {
                hitSound = EntityStates.SWEET_BERRY_BUSH_HIT_SOUND;
            }
            else {
                hitSound = EntityStates.GENERIC_HIT_SOUND;
            }
            entity.world.setEntityState(entity, hitSound);
        }
        if (source != EvolutionDamage.DROWN && !damageBlocked) {
            entity.velocityChanged = entity.getRNG().nextDouble() >= entity.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getValue();
        }
        if (trueSource != null) {
            double d1 = trueSource.posX - entity.posX;
            double d0;
            for (d0 = trueSource.posZ - entity.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                d1 = (Math.random() - Math.random()) * 0.01D;
            }
            entity.attackedAtYaw = MathHelper.radToDeg((float) MathHelper.atan2(d0, d1)) - entity.rotationYaw;
            entity.knockBack(trueSource, 0.4F, d1, d0);
        }
        else {
            entity.attackedAtYaw = (int) (Math.random() * 2) * 180;
        }
        if (entity.getHealth() <= 0.0F) {
            if (!checkTotemDeathProtection(entity, source)) {
                SoundEvent deathSound = GET_DEATH_SOUND.call(entity);
                if (deathSound != null) {
                    entity.playSound(deathSound, GET_SOUND_VOLUME.call(entity), GET_SOUND_PITCH.call(entity));
                }
                entity.onDeath(source);
            }
        }
        else {
            PLAY_HURT_SOUND.call(entity, source);
        }
        boolean damageNotBlocked = !damageBlocked;
        if (damageNotBlocked) {
            LAST_DAMAGE_SOURCE.set(entity, source);
            LAST_DAMAGE_STAMP.set(entity, entity.world.getGameTime());
        }
        if (entity instanceof ServerPlayerEntity) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayerEntity) entity, source, f, amount, damageBlocked);
            if (amountBlocked > 0.0F && amountBlocked < Float.MAX_VALUE) {
                ((ServerPlayerEntity) entity).addStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(amountBlocked * 10.0F));
            }
        }
        if (trueSource instanceof ServerPlayerEntity) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayerEntity) trueSource, entity, source, f, amount, damageBlocked);
        }
        return damageNotBlocked;
    }

    private static void blockUsingShield(LivingEntity blocking, LivingEntity blocked) {
        constructKnockBackVector(blocked, blocking);
    }

    private static void calculateWallImpact(LivingEntity entity, double motionX, double motionZ, double mass) {
        double motionXPost = entity.getMotion().x;
        double motionZPost = entity.getMotion().z;
        double deltaSpeedX = Math.abs(motionX) - Math.abs(motionXPost);
        deltaSpeedX *= 20;
        float damage = 0;
        if (deltaSpeedX >= 6) {
            Evolution.LOGGER.debug("x speed = {} m/s", deltaSpeedX);
            double kineticEnergy = 0.5 * deltaSpeedX * deltaSpeedX * mass;
            AxisAlignedBB bb = entity.getBoundingBox();
            double xCoord = motionX >= 0 ? bb.maxX + 0.01 : bb.minX - 0.01;
            int numberOfBlocks = 0;
            double slowDown = 0;
            try (BlockPos.PooledMutableBlockPos minPos = BlockPos.PooledMutableBlockPos.retain(xCoord, bb.minY, bb.minZ);
                 BlockPos.PooledMutableBlockPos maxPos = BlockPos.PooledMutableBlockPos.retain(xCoord, bb.maxY, bb.maxZ);
                 BlockPos.PooledMutableBlockPos changingPos = BlockPos.PooledMutableBlockPos.retain()) {
                if (entity.world.isAreaLoaded(minPos, maxPos)) {
                    for (int j = minPos.getY(); j <= maxPos.getY(); j++) {
                        for (int k = minPos.getZ(); k <= maxPos.getZ(); k++) {
                            numberOfBlocks++;
                            changingPos.setPos(xCoord, j, k);
                            BlockState stateAtPos = entity.world.getBlockState(changingPos);
                            Block blockAtPos = stateAtPos.getBlock();
                            if (blockAtPos instanceof ISoftBlock) {
                                slowDown += ((ISoftBlock) blockAtPos).getSlowdownSide(stateAtPos);
                                ((ISoftBlock) blockAtPos).collision(entity, motionX);
                            }
                            else {
                                slowDown += 1;
                            }
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
            double distanceOfSlowdown = slowDown + entity.getWidth() / 4;
            double forceOfImpact = kineticEnergy / distanceOfSlowdown;
            float area = entity.getHeight() * entity.getWidth();
            double pressure = forceOfImpact / area;
            damage += (float) Math.pow(pressure, 1.6) / 1_750_000;
        }
        double deltaSpeedZ = Math.abs(motionZ) - Math.abs(motionZPost);
        deltaSpeedZ *= 20;
        if (deltaSpeedZ >= 6) {
            Evolution.LOGGER.debug("z speed = {} m/s", deltaSpeedZ);
            double kineticEnergy = 0.5 * deltaSpeedZ * deltaSpeedZ * mass;
            AxisAlignedBB bb = entity.getBoundingBox();
            double zCoord = motionZ >= 0 ? bb.maxZ + 0.01 : bb.minZ - 0.01;
            int numberOfBlocks = 0;
            double slowDown = 0;
            try (BlockPos.PooledMutableBlockPos minPos = BlockPos.PooledMutableBlockPos.retain(bb.minX, bb.minY, zCoord);
                 BlockPos.PooledMutableBlockPos maxPos = BlockPos.PooledMutableBlockPos.retain(bb.maxX, bb.maxY, zCoord);
                 BlockPos.PooledMutableBlockPos changingPos = BlockPos.PooledMutableBlockPos.retain()) {
                if (entity.world.isAreaLoaded(minPos, maxPos)) {
                    for (int i = minPos.getX(); i <= maxPos.getX(); i++) {
                        for (int j = minPos.getY(); j <= maxPos.getY(); j++) {
                            numberOfBlocks++;
                            changingPos.setPos(i, j, zCoord);
                            BlockState stateAtPos = entity.world.getBlockState(changingPos);
                            Block blockAtPos = stateAtPos.getBlock();
                            if (blockAtPos instanceof ISoftBlock) {
                                slowDown += ((ISoftBlock) blockAtPos).getSlowdownSide(stateAtPos);
                                ((ISoftBlock) blockAtPos).collision(entity, motionZ);
                            }
                            else {
                                slowDown += 1;
                            }
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
            double distanceOfSlowdown = slowDown + entity.getWidth() / 4;
            double forceOfImpact = kineticEnergy / distanceOfSlowdown;
            float area = entity.getHeight() * entity.getWidth();
            double pressure = forceOfImpact / area;
            damage += (float) Math.pow(pressure, 1.6) / 1_500_000;
        }
        if (damage >= 1.0f) {
            if (!entity.world.isRemote) {
                entity.attackEntityFrom(EvolutionDamage.WALL_IMPACT, damage);
            }
            else if (entity instanceof PlayerEntity) {
                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSImpactDamage(damage));
            }
        }
    }

    private static boolean canBlockDamageSource(LivingEntity entity, DamageSource source) {
        Entity immediateSource = source.getImmediateSource();
        boolean piercing = false;
        if (immediateSource instanceof AbstractArrowEntity) {
            AbstractArrowEntity arrowEntity = (AbstractArrowEntity) immediateSource;
            if (arrowEntity.getPierceLevel() > 0) {
                piercing = true;
            }
        }
        if (!piercing && !source.isUnblockable() && isActiveItemStackBlocking(entity)) {
            Vec3d damageLocation = source.getDamageLocation();
            if (damageLocation != null) {
                Vec3d look = entity.getLook(1.0F);
                Vec3d damageVec = damageLocation.subtractReverse(new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ))
                                                .normalize();
                return damageVec.dotProduct(look) < 0;
            }
        }
        return false;
    }

    private static boolean canParryDamageSource(LivingEntity entity, DamageSource source) {
        Entity immediateSource = source.getImmediateSource();
        boolean piercing = false;
        if (immediateSource instanceof AbstractArrowEntity) {
            AbstractArrowEntity arrowEntity = (AbstractArrowEntity) immediateSource;
            if (arrowEntity.getPierceLevel() > 0) {
                piercing = true;
            }
        }
        if (!piercing && !source.isUnblockable() && isActiveItemStackParrying(entity)) {
            Vec3d damageLocation = source.getDamageLocation();
            if (damageLocation != null) {
                Vec3d look = entity.getLook(1.0F);
                Vec3d damageVec = damageLocation.subtractReverse(new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ))
                                                .normalize();
                return damageVec.dotProduct(look) < -MathHelper.SQRT_2_OVER_2;
            }
        }
        return false;
    }

    private static boolean checkTotemDeathProtection(LivingEntity entity, DamageSource source) {
        if (source.canHarmInCreative()) {
            return false;
        }
        ItemStack stack = null;
        for (Hand hand : Hand.values()) {
            ItemStack stackInHand = entity.getHeldItem(hand);
            if (stackInHand.getItem() == Items.TOTEM_OF_UNDYING) {
                stack = stackInHand.copy();
                stackInHand.shrink(1);
                break;
            }
        }
        if (stack != null) {
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                player.addStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                CriteriaTriggers.USED_TOTEM.trigger(player, stack);
            }
            entity.setHealth(1.0F);
            entity.clearActivePotions();
            entity.addPotionEffect(new EffectInstance(Effects.REGENERATION, 900, 1));
            entity.addPotionEffect(new EffectInstance(Effects.ABSORPTION, 100, 1));
            entity.world.setEntityState(entity, EntityStates.TOTEM_OF_UNDYING_SOUND);
        }
        return stack != null;
    }

    private static void constructKnockBackVector(LivingEntity blocked, LivingEntity blocking) {
        blocking.knockBack(blocked, 0.5F, blocking.posX - blocked.posX, blocking.posZ - blocked.posZ);
    }

    private static void damageEntity(LivingEntity entity, DamageSource source, float amount) {
        if (!entity.isInvulnerableTo(source)) {
            amount = ForgeHooks.onLivingHurt(entity, source, amount);
            if (amount <= 0) {
                return;
            }
            //TODO
//            amount = entity.applyArmorCalculations(source, amount);
//            amount = entity.applyPotionDamageCalculations(source, amount);
            float totalAmount = amount;
            amount = Math.max(amount - entity.getAbsorptionAmount(), 0.0F);
            entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (totalAmount - amount));
            float absorbedDamage = totalAmount - amount;
            if (absorbedDamage > 0.0F && absorbedDamage < Float.MAX_VALUE && source.getTrueSource() instanceof ServerPlayerEntity) {
                ((PlayerEntity) source.getTrueSource()).addStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(absorbedDamage * 10.0F));
            }
            if (absorbedDamage > 0.0F && absorbedDamage < Float.MAX_VALUE && entity instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) entity).addStat(Stats.DAMAGE_ABSORBED, Math.round(absorbedDamage * 10.0F));
            }
            amount = ForgeHooks.onLivingDamage(entity, source, amount);
            if (amount != 0.0F) {
                float f2 = entity.getHealth();
                entity.getCombatTracker().trackDamage(source, f2, amount);
                entity.setHealth(f2 - amount);
                entity.setAbsorptionAmount(entity.getAbsorptionAmount() - amount);
                if (amount < Float.MAX_VALUE && entity instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) entity).addStat(Stats.DAMAGE_TAKEN, Math.round(amount * 10.0F));
                }
            }
        }
    }

    private static void damageShield(LivingEntity entity, float damage) {
        if (!(entity instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity) entity;
        if (damage >= 3.0F && entity.getActiveItemStack().isShield(entity)) {
            int i = 1 + MathHelper.floor(damage);
            Hand hand = entity.getActiveHand();
            entity.getActiveItemStack().damageItem(i, entity, livingEntity -> {
                livingEntity.sendBreakAnimation(hand);
                ForgeEventFactory.onPlayerDestroyItem(player, entity.getActiveItemStack(), hand);
            });
            if (entity.getActiveItemStack().isEmpty()) {
                if (hand == Hand.MAIN_HAND) {
                    entity.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
                }
                else {
                    entity.setItemStackToSlot(EquipmentSlotType.OFFHAND, ItemStack.EMPTY);
                }
                entity.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + entity.world.rand.nextFloat() * 0.4F);
            }
        }
    }

    private static Vec3d getAbsoluteAcceleration(LivingEntity entity, Vec3d direction, float magnitude) {
        double length = direction.lengthSquared();
        if (length < 1.0E-7) {
            return Vec3d.ZERO;
        }
        if (entity.isSneaking()) {
            if (!(entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying)) {
                magnitude *= 0.3;
            }
        }
        if (entity instanceof PlayerEntity) {
            if (Evolution.PRONED_PLAYERS.getOrDefault(entity.getUniqueID(), false)) {
                magnitude *= 0.3;
            }
        }
        if (entity.isHandActive()) {
            Item activeItem = entity.getActiveItemStack().getItem();
            if (activeItem instanceof IEvolutionItem) {
                magnitude *= ((IEvolutionItem) activeItem).useItemSlowDownRate();
            }
        }
        Vec3d acceleration = direction.normalize();
        double accX = acceleration.x * magnitude;
        double accY = acceleration.y * magnitude;
        double accZ = acceleration.z * magnitude;
        float sinFacing = MathHelper.sinDeg(entity.rotationYaw);
        float cosFacing = MathHelper.cosDeg(entity.rotationYaw);
        return new Vec3d(accX * cosFacing - accZ * sinFacing, accY, accZ * cosFacing + accX * sinFacing);
    }

    public static double getJumpSlowDown(PlayerEntity player) {
        if (player.isCreative()) {
            return 0;
        }
        IAttributeInstance mass = player.getAttribute(EvolutionAttributes.MASS);
        int baseMass = (int) mass.getBaseValue();
        int totalMass = (int) mass.getValue();
        int equipMass = totalMass - baseMass;
        return equipMass * 0.000_2;
    }

    /**
     * Hooks from {@link LivingEntity#getJumpUpwardsMotion()}
     */
    @EvolutionHook
    public static float getJumpUpwardsMotion() {
        return 0.247_5f;
    }

    private static double getMass(Entity entity) {
        if (entity instanceof IEntityMass) {
            return ((IEntityMass) entity).getMass();
        }
        if (entity instanceof PlayerEntity) {
            IAttributeInstance massAttribute = ((PlayerEntity) entity).getAttribute(EvolutionAttributes.MASS);
            return massAttribute.getValue();
        }
        return 1;
    }

    private static int getParryTime(LivingEntity entity) {
        ItemStack stack = entity.getActiveItemStack();
        if (stack.isEmpty()) {
            return -1;
        }
        return stack.getItem().getUseDuration(stack) - entity.getItemInUseCount();
    }

    private static void handleElytraMovement(LivingEntity entity, double gravityAcceleration, DataParameter<Byte> flags) {
        Vec3d motion = entity.getMotion();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        double mass = getMass(entity);
        double drag = Gravity.verticalDrag(entity) / mass;
        double dragX = Math.signum(motionX) * motionX * motionX * drag;
        double dragY = Math.signum(motionY) * motionY * motionY * drag;
        double dragZ = Math.signum(motionZ) * motionZ * motionZ * drag;
        if (motionY > -0.5) {
            entity.fallDistance = 1.0F;
        }
        Vec3d lookVec = entity.getLookVec();
        float pitchInRad = MathHelper.degToRad(entity.rotationPitch);
        double horizLookVecLength = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
        double horizontalSpeed = Math.sqrt(Entity.horizontalMag(motion));
        float cosPitch = MathHelper.cos(pitchInRad);
        cosPitch = (float) (cosPitch * cosPitch * Math.min(1, lookVec.length() / 0.4));
        motionY += gravityAcceleration * (-1 + cosPitch * 0.75);
        if (motionY < 0 && horizLookVecLength > 0) {
            double d3 = motionY * -0.1 * cosPitch;
            motionX += lookVec.x * d3 / horizLookVecLength;
            motionY += d3;
            motionZ += lookVec.z * d3 / horizLookVecLength;
        }
        if (pitchInRad < 0.0F && horizLookVecLength > 0) {
            double d13 = horizontalSpeed * -MathHelper.sin(pitchInRad) * 0.04;
            motionX += -lookVec.x * d13 / horizLookVecLength;
            motionY += d13 * 3.2;
            motionZ += -lookVec.z * d13 / horizLookVecLength;
        }
        if (horizLookVecLength > 0) {
            motionX += (lookVec.x / horizLookVecLength * horizontalSpeed - motion.x) * 0.1;
            motionZ += (lookVec.z / horizLookVecLength * horizontalSpeed - motion.z) * 0.1;
        }
        motionX -= dragX;
        motionY -= dragY;
        motionZ -= dragZ;
        entity.setMotion(motionX, motionY, motionZ);
        entity.move(MoverType.SELF, entity.getMotion());
        if (entity.collidedHorizontally && !entity.world.isRemote) {
            calculateWallImpact(entity, motionX, motionZ, mass);
        }
        if (entity.onGround && !entity.world.isRemote) {
            setFlag(entity, flags, EntityFlags.ELYTRA_FLYING, false);
        }
    }

    private static Vec3d handleLadderMotion(LivingEntity entity, double x, double y, double z) {
        boolean isCreativeFlying = entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying;
        if (entity.isOnLadder() && !isCreativeFlying) {
            entity.fallDistance = 0.0F;
            double newX;
            double newZ;
            if (!entity.onGround) {
                newX = MathHelper.clamp(x, -0.025, 0.025);
                newX *= 0.8;
                newZ = MathHelper.clamp(z, -0.025, 0.025);
                newZ *= 0.8;
            }
            else {
                newX = x;
                newZ = z;
            }
            double newY = y < -0.3 ? y : Math.max(y, entity.isSneaking() ? 0 : -0.15);
            if (newY < 0 && entity.getBlockState().getBlock() != Blocks.SCAFFOLDING && entity.isSneaking() && entity instanceof PlayerEntity) {
                newY = 0;
            }
            return new Vec3d(newX, newY, newZ);
        }
        return new Vec3d(x, y, z);
    }

    private static void handleLavaMovement(LivingEntity entity, Vec3d direction, double gravityAcceleration) {
        double posY = entity.posY;
        entity.moveRelative(0.02F, direction);
        entity.move(MoverType.SELF, entity.getMotion());
        Vec3d motion = entity.getMotion();
        double motionX = motion.x * 0.5;
        double motionY = motion.y * 0.5;
        double motionZ = motion.z * 0.5;
        if (!entity.hasNoGravity()) {
            motionY -= gravityAcceleration / 4;
        }
        if (entity.collidedHorizontally && entity.isOffsetPositionInLiquid(motionX, motionY + 0.6 - entity.posY + posY, motionZ)) {
            motionY = 0.3;
        }
        entity.setMotion(motionX, motionY, motionZ);
    }

    private static void handleNormalMovement(LivingEntity entity,
                                             Vec3d direction,
                                             double gravityAcceleration,
                                             boolean isJumping,
                                             int jumpTicks,
                                             boolean isPlayerFlying,
                                             float slowdown) {
        BlockPos blockBelow = new BlockPos(entity.posX, entity.getBoundingBox().minY - 1, entity.posZ);
        float slipperiness = entity.world.getBlockState(blockBelow).getSlipperiness(entity.world, blockBelow, entity);
        if (Float.compare(slipperiness, 0.6F) < 0.01F) {
            slipperiness = 0.15F;
        }
        if (entity.getSubmergedHeight() > 0) {
            slipperiness += 0.1f;
        }
        float frictionCoef = entity.onGround ? 1.0F - slipperiness : 0.0F;
        Vec3d acceleration = getAbsoluteAcceleration(entity, direction, slowdown * jumpMovementFactor(entity, slipperiness, jumpTicks));
        boolean isActiveWalking = acceleration.x != 0 || acceleration.z != 0;
        Vec3d motion = entity.getMotion();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        if ((entity.collidedHorizontally || isJumping) && entity.isOnLadder()) {
            motionY = BlockUtils.getLadderUpSpeed(entity.getBlockState());
        }
        else if (!entity.hasNoGravity()) {
            if (!isPlayerFlying) {
                motionY -= gravityAcceleration;
            }
        }
        double legSlowDownX = 0;
        double legSlowDownZ = 0;
        double frictionAcc = frictionCoef * gravityAcceleration;
        if (entity.onGround || isPlayerFlying && isActiveWalking) {
            double legSlowDown = legSlowDown(entity);
            if (frictionAcc != 0) {
                legSlowDown *= frictionAcc;
            }
            else {
                legSlowDown *= gravityAcceleration * 0.85;
            }
            legSlowDownX = motionX * legSlowDown;
            legSlowDownZ = motionZ * legSlowDown;
        }
        double mass = getMass(entity);
        double horizontalDrag = Gravity.horizontalDrag(entity) / mass;
        double verticalDrag = Gravity.verticalDrag(entity) / mass;
        double frictionX = 0;
        double frictionZ = 0;
        if (!isActiveWalking) {
            double norm = Math.sqrt(motionX * motionX + motionZ * motionZ);
            if (norm != 0) {
                frictionX = motionX / norm * frictionAcc;
                frictionZ = motionZ / norm * frictionAcc;
            }
            if (Math.abs(motionX) < Math.abs(frictionX)) {
                frictionX = motionX;
            }
            if (Math.abs(motionZ) < Math.abs(frictionZ)) {
                frictionZ = motionZ;
            }
        }
        double dragX = Math.signum(motionX) * motionX * motionX * horizontalDrag;
        if (Math.abs(dragX) > Math.abs(motionX)) {
            dragX = motionX;
        }
        double dragY = Math.signum(motionY) * motionY * motionY * verticalDrag;
        if (Math.abs(dragY) > Math.abs(motionY)) {
            dragY = motionY;
        }
        double dragZ = Math.signum(motionZ) * motionZ * motionZ * horizontalDrag;
        if (Math.abs(dragZ) > Math.abs(motionZ)) {
            dragZ = motionZ;
        }
        motionX += acceleration.x - legSlowDownX - frictionX - dragX;
        motionY += acceleration.y - dragY;
        motionZ += acceleration.z - legSlowDownZ - frictionZ - dragZ;
        if (Math.abs(motionX) < 1e-6) {
            motionX = 0;
        }
        if (Math.abs(motionY) < 1e-6) {
            motionY = 0;
        }
        if (Math.abs(motionZ) < 1e-6) {
            motionZ = 0;
        }
        entity.setMotion(handleLadderMotion(entity, motionX, motionY, motionZ));
        entity.move(MoverType.SELF, entity.getMotion());
        if (entity.collidedHorizontally) {
            calculateWallImpact(entity, motionX, motionZ, mass);
        }
    }

    private static void handleWaterMotion(LivingEntity entity, Vec3d direction, double gravityAcceleration, boolean isJumping, int jumpTicks) {
        if (entity.onGround || jumpTicks > 0) {
            if (entity.getSubmergedHeight() <= 0.4) {
                BlockPos currentBlock = new BlockPos(entity);
                IFluidState fluidState = entity.world.getFluidState(currentBlock);
                int level = fluidState.getLevel();
                float slowdown = 1.0f - 0.05f * level;
                handleNormalMovement(entity, direction, gravityAcceleration, isJumping, jumpTicks, false, slowdown);
                return;
            }
        }
        float waterSpeedMult = 0.04F;
        waterSpeedMult *= (float) entity.getAttribute(LivingEntity.SWIM_SPEED).getValue();
        Vec3d acceleration = getAbsoluteAcceleration(entity, direction, waterSpeedMult);
        Vec3d motion = entity.getMotion();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        double mass = getMass(entity);
        double verticalDrag = Gravity.verticalWaterDrag(entity) / mass;
        double horizontalDrag = entity.isSwimming() ? verticalDrag : Gravity.horizontalWaterDrag(entity) / mass;
        if (entity.collidedHorizontally && entity.isOnLadder()) {
            motionY = 0.2;
        }
        if (!entity.hasNoGravity()) {
            if (entity.isSwimming()) {
                motionY -= gravityAcceleration / 16;
            }
            else {
                motionY -= gravityAcceleration;
            }
        }
        if (entity.collidedHorizontally && entity.isOffsetPositionInLiquid(0, motionY + 1.5, 0)) {
            motionY = 0.2;
            if (entity.getSubmergedHeight() <= 0.4) {
                motionY += 0.2;
                if (entity.world.isRemote) {
                    if (entity.equals(Evolution.PROXY.getClientPlayer())) {
                        ClientEvents.getInstance().jumpTicks = 10;
                    }
                }
            }
        }
        double dragX = Math.signum(motionX) * motionX * motionX * horizontalDrag;
        if (Math.abs(dragX) > Math.abs(motionX / 2)) {
            dragX = motionX / 2;
        }
        double dragY = Math.signum(motionY) * motionY * motionY * verticalDrag;
        if (Math.abs(dragY) > Math.abs(motionY / 2)) {
            dragY = motionY / 2;
            EntityEvents.calculateWaterFallDamage(entity);
        }
        double dragZ = Math.signum(motionZ) * motionZ * motionZ * horizontalDrag;
        if (Math.abs(dragZ) > Math.abs(motionZ / 2)) {
            dragZ = motionZ / 2;
        }
        motionX += acceleration.x - dragX;
        motionY += acceleration.y - dragY;
        motionZ += acceleration.z - dragZ;
        entity.setMotion(motionX, motionY, motionZ);
        entity.move(MoverType.SELF, entity.getMotion());
    }

    private static boolean isActiveItemStackBlocking(LivingEntity entity) {
        ItemStack stack = entity.getActiveItemStack();
        if (entity.isHandActive() && !stack.isEmpty()) {
            Item item = stack.getItem();
            if (!item.isShield(stack, entity)) {
                return false;
            }
            if (item.getUseAction(stack) != UseAction.BLOCK) {
                return false;
            }
            return item.getUseDuration(stack) - entity.getItemInUseCount() >= 2;
        }
        return false;
    }

    private static boolean isActiveItemStackParrying(LivingEntity entity) {
        ItemStack stack = entity.getActiveItemStack();
        if (entity.isHandActive() && !stack.isEmpty()) {
            Item item = stack.getItem();
            if (item.getUseAction(stack) != UseAction.BLOCK) {
                return false;
            }
            return item.getUseDuration(stack) - entity.getItemInUseCount() >= 0;
        }
        return false;
    }

    /**
     * Hooks from {@link LivingEntity#jump()}
     */
    @EvolutionHook
    public static void jump(LivingEntity entity, float jumpUpwardsMotion) {
        float upMotion;
        if (entity.isPotionActive(Effects.JUMP_BOOST)) {
            upMotion = jumpUpwardsMotion * (1.0F + (entity.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1) / 10.0f);
        }
        else {
            upMotion = jumpUpwardsMotion;
        }
        if (entity instanceof PlayerEntity) {
            upMotion -= getJumpSlowDown((PlayerEntity) entity);
        }
        Vec3d motion = entity.getMotion();
        entity.setMotion(motion.x, upMotion, motion.z);
        entity.isAirBorne = true;
        if (entity.world.isRemote) {
            if (entity.equals(Evolution.PROXY.getClientPlayer())) {
                ClientEvents.getInstance().jumpTicks = 10;
            }
        }
        ForgeHooks.onLivingJump(entity);
    }

    private static float jumpMovementFactor(LivingEntity entity, float slipperiness, int jumpTicks) {
        if (entity instanceof PlayerEntity) {
            if (!((PlayerEntity) entity).abilities.isFlying) {
                if (entity.onGround || entity.isOnLadder()) {
                    return entity.getAIMoveSpeed() * (0.15F / slipperiness);
                }
                return jumpTicks > 3 ? 0.075f * entity.getAIMoveSpeed() * (0.15F / slipperiness) : 0;
            }
        }
        return entity.onGround ? entity.getAIMoveSpeed() * (0.15F / slipperiness) : entity.jumpMovementFactor;
    }

    private static double legSlowDown(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return PlayerHelper.LEG_SLOWDOWN;
        }
        if (entity instanceof EntityGenericCreature) {
            return ((EntityGenericCreature) entity).getLegSlowDown();
        }
        return 5.455;
    }

    /**
     * Hooks from {@link LivingEntity#playEquipSound(ItemStack)}
     */
    @EvolutionHook
    public static void playEquipSound(LivingEntity entity, ItemStack stack) {
        if (!stack.isEmpty()) {
            SoundEvent sound = null;
            Item item = stack.getItem();
            if (item instanceof ArmorItem) {
                sound = ((ArmorItem) item).getArmorMaterial().getSoundEvent();
            }
            else if (item == Items.ELYTRA) {
                sound = SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA;
            }
            if (sound != null) {
                entity.playSound(sound, 1.0F, 1.0F);
            }
        }
    }

    private static void setFlag(LivingEntity entity, DataParameter<Byte> flags, int flag, boolean set) {
        byte byteField = entity.getDataManager().get(flags);
        if (set) {
            entity.getDataManager().set(flags, (byte) (byteField | 1 << flag));
        }
        else {
            entity.getDataManager().set(flags, (byte) (byteField & ~(1 << flag)));
        }
    }

    /**
     * Hooks from {@link LivingEntity#travel(Vec3d)}
     */
    @EvolutionHook
    public static void travel(LivingEntity entity, Vec3d direction, boolean isJumping, int jumpTicks, DataParameter<Byte> flags) {
        if (entity.isServerWorld() || entity.canPassengerSteer()) {
            IAttributeInstance gravity = entity.getAttribute(LivingEntity.ENTITY_GRAVITY);
            boolean falling = entity.getMotion().y <= 0;
            if (falling && entity.isPotionActive(Effects.SLOW_FALLING)) {
                if (!gravity.hasModifier(EvolutionAttributes.SLOW_FALLING)) {
                    gravity.applyModifier(EvolutionAttributes.SLOW_FALLING);
                }
                entity.fallDistance = 0.0F;
            }
            else if (gravity.hasModifier(EvolutionAttributes.SLOW_FALLING)) {
                gravity.removeModifier(EvolutionAttributes.SLOW_FALLING);
            }
            double gravityAcceleration = gravity.getValue();
            boolean isPlayerFlying = entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying;
            if (!entity.isInWater() || isPlayerFlying) {
                if (!entity.isInLava() || isPlayerFlying) {
                    if (entity.isElytraFlying()) {
                        handleElytraMovement(entity, gravityAcceleration, flags);
                    }
                    else {
                        handleNormalMovement(entity, direction, gravityAcceleration, isJumping, jumpTicks, isPlayerFlying, 1.0f);
                    }
                }
                else {
                    handleLavaMovement(entity, direction, gravityAcceleration);
                }
            }
            else {
                handleWaterMotion(entity, direction, gravityAcceleration, isJumping, jumpTicks);
            }
        }
        //Controls animations
        entity.prevLimbSwingAmount = entity.limbSwingAmount;
        double deltaPosX = entity.posX - entity.prevPosX;
        double deltaPosZ = entity.posZ - entity.prevPosZ;
        double deltaPosY = entity instanceof IFlyingAnimal ? entity.posY - entity.prevPosY : 0;
        float f8 = MathHelper.sqrt(deltaPosX * deltaPosX + deltaPosY * deltaPosY + deltaPosZ * deltaPosZ) * 4.0F;
        if (f8 > 1.0F) {
            f8 = 1.0F;
        }
        entity.limbSwingAmount += (f8 - entity.limbSwingAmount) * 0.4F;
        entity.limbSwing += entity.limbSwingAmount;
    }
}
