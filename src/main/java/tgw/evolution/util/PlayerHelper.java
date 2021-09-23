package tgw.evolution.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.scoreboard.Score;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.items.*;
import tgw.evolution.network.PacketSCHandAnimation;
import tgw.evolution.stats.EvolutionServerStatisticsManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public final class PlayerHelper {

    /**
     * The base attack speed of the Player, in attacks / s.
     */
    public static final double ATTACK_SPEED = 2;
    /**
     * The base reach distance of the Player, in metres.
     */
    public static final double REACH_DISTANCE = 3.25;
    /**
     * The base Player mass in kg. Used in various kinetic calculations.
     */
    public static final double MASS = 70;
    /**
     * The height of the Player legs in m. Used to calculate fall damage.
     */
    public static final double LEG_HEIGHT = 0.875;
    /**
     * The force the player uses to push its feet against the ground.
     */
    public static final double WALK_FORCE = MathHelper.convertForce(1_000);
    public static final Vector3d NECK_POS_STANDING = new Vector3d(0, 24 / 16.0 * 0.937_5, -1 / 16.0);
    public static final Vector3d NECK_POS_SNEAKING = new Vector3d(0, 1.27 - 4 / 16.0 * 0.937_5, -1 / 16.0);
    public static final Vector3d NECK_POS_CRAWLING = new Vector3d(0, 4.62 / 16.0 * 0.937_5, -2 / 16.0);
    public static final EntitySize STANDING_SIZE = EntitySize.scalable(0.625F, 1.8F);
    public static final Map<Pose, EntitySize> SIZE_BY_POSE = ImmutableMap.<Pose, EntitySize>builder()
                                                                         .put(Pose.STANDING, STANDING_SIZE)
                                                                         .put(Pose.SLEEPING, EntitySize.fixed(0.2F, 0.2F))
                                                                         .put(Pose.FALL_FLYING, EntitySize.scalable(0.625F, 0.625F))
                                                                         .put(Pose.SWIMMING, EntitySize.scalable(0.625F, 0.625F))
                                                                         .put(Pose.SPIN_ATTACK, EntitySize.scalable(0.625F, 0.625F))
                                                                         .put(Pose.CROUCHING, EntitySize.scalable(0.625F, 1.5F))
                                                                         .put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F))
                                                                         .build();
    private static final Random RAND = new Random();

    private PlayerHelper() {
    }

    public static void addStat(PlayerEntity player, @Nonnull ResourceLocation resLoc, float amount) {
        addStat(player, Stats.CUSTOM, resLoc, amount);
    }

    public static <T> void addStat(PlayerEntity player, @Nonnull StatType<T> statType, @Nonnull T type, float amount) {
        if (player instanceof ServerPlayerEntity && !(player instanceof FakePlayer)) {
            Stat<T> stat = statType.get(type);
            EvolutionServerStatisticsManager stats = (EvolutionServerStatisticsManager) ((ServerPlayerEntity) player).getStats();
            stats.incrementPartial(player, stat, amount);
            player.getScoreboard().forAllObjectives(stat, player.getScoreboardName(), score -> score.setScore((int) stats.getValueLong(stat)));
        }
    }

    private static void applyDamageActual(PlayerEntity player, float amount, EvolutionDamage.Type type, LivingEntity entity) {
        addStat(player, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(type), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(EvolutionDamage.Type.MELEE), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(EvolutionDamage.Type.TOTAL), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT.get(), entity.getType(), amount);
    }

    private static void applyDamageRaw(PlayerEntity player, float amount, EvolutionDamage.Type type) {
        addStat(player, EvolutionStats.DAMAGE_DEALT_RAW.get(type), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT_RAW.get(EvolutionDamage.Type.MELEE), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT_RAW.get(EvolutionDamage.Type.TOTAL), amount);
    }

    private static void attackEntity(PlayerEntity player, Entity targetEntity, Hand hand, double rayTraceHeight) {
        ItemStack attackStack = player.getItemInHand(hand);
        Item attackItem = attackStack.getItem();
        if (!(attackStack.isEmpty() || !attackItem.onLeftClickEntity(attackStack, player, targetEntity))) {
            return;
        }
        if (targetEntity.isAttackable()) {
            if (!targetEntity.skipAttackInteraction(player)) {
                float damage = (float) player.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
                if (attackItem instanceof IOffhandAttackable) {
                    damage = (float) (((IOffhandAttackable) attackItem).getAttackDamage() +
                                      player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
                }
                if (damage > 0.0F) {
                    int knockbackModifier = 0;
                    if (attackItem instanceof IKnockback) {
                        knockbackModifier += ((IKnockback) attackItem).getLevel();
                    }
                    boolean sprinting = false;
                    if (player.isSprinting()) {
                        player.level.playSound(null,
                                               player.getX(),
                                               player.getY(),
                                               player.getZ(),
                                               SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                                               player.getSoundSource(),
                                               1.0F,
                                               1.0F);
                        ++knockbackModifier;
                        sprinting = true;
                    }
                    int heavyModifier = 0;
                    if (attackItem instanceof IHeavyAttack) {
                        float heavyChance = ((IHeavyAttack) attackItem).getHeavyAttackChance();
                        if (sprinting) {
                            heavyChance *= 2;
                        }
                        if (RAND.nextFloat() < heavyChance) {
                            heavyModifier = ((IHeavyAttack) attackItem).getHeavyAttackLevel();
                            if (sprinting) {
                                heavyModifier *= 2;
                            }
                            if (player.level instanceof ServerWorld) {
                                ((ServerWorld) player.level).sendParticles(ParticleTypes.ENCHANTED_HIT,
                                                                           targetEntity.getX(),
                                                                           targetEntity.getY() + targetEntity.getBbHeight() * 0.5F,
                                                                           targetEntity.getZ(),
                                                                           10,
                                                                           0.5,
                                                                           0,
                                                                           0.5,
                                                                           0.1);
                            }
                        }
                    }
                    damage *= 1 + heavyModifier / 10.0f;
                    boolean isSweepAttack = false;
                    if (!sprinting && player.isOnGround()) {
                        if (attackItem instanceof ISweepAttack) {
                            isSweepAttack = true;
                        }
                    }
                    int fireAspectModifier = 0;
                    if (attackItem instanceof IFireAspect) {
                        if (RAND.nextFloat() < ((IFireAspect) attackItem).getChance()) {
                            fireAspectModifier = ((IFireAspect) attackItem).getLevel();
                        }
                    }
                    float oldHealth = 0.0F;
                    boolean fireAspect = false;
                    if (targetEntity instanceof LivingEntity) {
                        oldHealth = ((LivingEntity) targetEntity).getHealth();
                        if (fireAspectModifier > 0 && !targetEntity.isOnFire()) {
                            fireAspect = true;
                            targetEntity.setRemainingFireTicks(1);
                        }
                    }
                    Vector3d targetMotion = targetEntity.getDeltaMovement();
                    EvolutionDamage.Type type = attackItem instanceof IMelee ? ((IMelee) attackItem).getDamageType() : EvolutionDamage.Type.CRUSHING;
                    DamageSource source;
                    if (targetEntity instanceof PlayerEntity) {
                        EquipmentSlotType slot = EquipmentSlotType.LEGS;
                        if (!Double.isNaN(rayTraceHeight)) {
                            slot = getPartByPosition(rayTraceHeight, (PlayerEntity) targetEntity);
                        }
                        source = EvolutionDamage.causePVPMeleeDamage(player, type, hand, slot);
                    }
                    else {
                        source = EvolutionDamage.causePlayerMeleeDamage(player, type, hand);
                    }
                    boolean attackSuccessfull = targetEntity.hurt(source, damage);
                    if (attackSuccessfull) {
                        //Knockback calculations
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof LivingEntity) {
                                ((LivingEntity) targetEntity).knockback(knockbackModifier * 0.5F,
                                                                        MathHelper.sinDeg(player.yRot),
                                                                        -MathHelper.cosDeg(player.yRot));
                            }
                            else {
                                targetEntity.push(-MathHelper.sinDeg(player.yRot) * knockbackModifier * 0.5F,
                                                  0,
                                                  MathHelper.cosDeg(player.yRot) * knockbackModifier * 0.5F);
                            }
                            player.setDeltaMovement(player.getDeltaMovement().multiply(0.6, 1, 0.6));
                        }
                        //Sweep Attack
                        int entitiesHit = 1;
                        if (isSweepAttack) {
                            float sweepingDamage = 1.0F + ((ISweepAttack) attackItem).getSweepRatio() * damage;
                            for (LivingEntity livingEntity : player.level.getEntitiesOfClass(LivingEntity.class,
                                                                                             targetEntity.getBoundingBox().inflate(1, 0.25, 1))) {
                                if (livingEntity != player &&
                                    livingEntity != targetEntity &&
                                    !player.isAlliedTo(livingEntity) &&
                                    (!(livingEntity instanceof ArmorStandEntity) || !((ArmorStandEntity) livingEntity).isMarker()) &&
                                    player.distanceToSqr(livingEntity) < 9) {
                                    livingEntity.knockback(0.4F, MathHelper.sinDeg(player.yRot), -MathHelper.cosDeg(player.yRot));
                                    if (livingEntity instanceof PlayerEntity) {
                                        //noinspection ObjectAllocationInLoop
                                        livingEntity.hurt(EvolutionDamage.causePVPMeleeDamage(player, type, hand, EquipmentSlotType.LEGS),
                                                          sweepingDamage);
                                    }
                                    else {
                                        //noinspection ObjectAllocationInLoop
                                        livingEntity.hurt(EvolutionDamage.causePlayerMeleeDamage(player, type, hand), sweepingDamage);
                                    }
                                    entitiesHit++;
                                }
                            }
                            player.level.playSound(null,
                                                   player.getX(),
                                                   player.getY(),
                                                   player.getZ(),
                                                   SoundEvents.PLAYER_ATTACK_SWEEP,
                                                   player.getSoundSource(),
                                                   1.0F,
                                                   1.0F);
                            player.sweepAttack();
                        }
                        //Calculated velocity changed
                        if (targetEntity instanceof ServerPlayerEntity && targetEntity.hurtMarked) {
                            ((ServerPlayerEntity) targetEntity).connection.send(new SEntityVelocityPacket(targetEntity));
                            targetEntity.hurtMarked = false;
                            targetEntity.setDeltaMovement(targetMotion);
                        }
                        //Strong attack particles
                        if (!isSweepAttack) {
                            player.level.playSound(null,
                                                   player.getX(),
                                                   player.getY(),
                                                   player.getZ(),
                                                   SoundEvents.PLAYER_ATTACK_STRONG,
                                                   player.getSoundSource(),
                                                   1.0F,
                                                   1.0F);
                        }
                        player.setLastHurtMob(targetEntity);
                        //Entity parts
                        Entity entity = targetEntity;
                        if (targetEntity instanceof EnderDragonPartEntity) {
                            entity = ((EnderDragonPartEntity) targetEntity).parentMob;
                        }
                        //Item damage calculation
                        if (!player.level.isClientSide && !attackStack.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = attackStack.copy();
                            for (int i = 0; i < entitiesHit; i++) {
                                attackStack.hurtEnemy((LivingEntity) entity, player);
                                if (attackStack.isEmpty()) {
                                    ForgeEventFactory.onPlayerDestroyItem(player, copy, hand);
                                    player.setItemInHand(hand, ItemStack.EMPTY);
                                    break;
                                }
                            }
                        }
                        //Stats and Heart particles
                        if (targetEntity instanceof LivingEntity) {
                            LivingEntity living = (LivingEntity) targetEntity;
                            float damageDealt = oldHealth - living.getHealth();
                            applyDamageRaw(player, damage, type);
                            applyDamageActual(player, damageDealt, type, living);
                            if (fireAspectModifier > 0) {
                                living.setRemainingFireTicks(fireAspectModifier * 4);
                            }
                            if (player.level instanceof ServerWorld && damageDealt >= 10.0F) {
                                int heartsToSpawn = (int) (damageDealt * 0.1);
                                ((ServerWorld) player.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                                                                           living.getX(),
                                                                           living.getY() + living.getBbHeight() * 0.5F,
                                                                           living.getZ(),
                                                                           heartsToSpawn,
                                                                           0.5,
                                                                           0,
                                                                           0.5,
                                                                           0.1);
                            }
                        }
                        player.causeFoodExhaustion(0.1F);
                    }
                    //Attack fail
                    else {
                        player.level.playSound(null,
                                               player.getX(),
                                               player.getY(),
                                               player.getZ(),
                                               SoundEvents.PLAYER_ATTACK_NODAMAGE,
                                               player.getSoundSource(),
                                               1.0F,
                                               1.0F);
                        if (fireAspect) {
                            targetEntity.clearFire();
                        }
                    }
                }
            }
        }
    }

    private static float chestHit(PlayerEntity player, float damage, EvolutionDamage.Type type) {
        damage *= 1.25f;
        //TODO
        return damage;
    }

    private static float footHit(PlayerEntity player, float damage, EvolutionDamage.Type type) {
        damage *= 0.75f;
        //TODO
        return damage;
    }

    private static float fullHit(PlayerEntity player, float damage, EvolutionDamage.Type type) {
        Evolution.LOGGER.debug("{} received {}HP of {} damage", player.getScoreboardName(), damage, type);
        //TODO
        return damage;
    }

    public static float getDamage(@Nullable EquipmentSlotType slot, PlayerEntity player, float damage, EvolutionDamage.Type type) {
        if (slot == null) {
            return fullHit(player, damage, type);
        }
        Evolution.LOGGER.debug("{} received {}HP of {} damage on {}", player.getScoreboardName(), damage, type, slot);
        switch (slot) {
            case HEAD:
                return headHit(player, damage, type);
            case CHEST:
                return chestHit(player, damage, type);
            case LEGS:
                return legHit(player, damage, type);
            case FEET:
                return footHit(player, damage, type);
        }
        return damage;
    }

    @Nullable
    public static EquipmentSlotType getPartByPosition(double y, PlayerEntity player) {
        double yRelativistic = y - player.getY();
        switch (player.getPose()) {
            case CROUCHING:
                if (yRelativistic <= 0.25) {
                    return EquipmentSlotType.FEET;
                }
                if (MathHelper.rangeInclusive(yRelativistic, 0.25, 0.625)) {
                    return EquipmentSlotType.LEGS;
                }
                if (MathHelper.rangeInclusive(yRelativistic, 0.625, 1.125)) {
                    return EquipmentSlotType.CHEST;
                }
                if (yRelativistic >= 1.125) {
                    return EquipmentSlotType.HEAD;
                }
                return null;
            case DYING:
                return null;
            case FALL_FLYING:
            case SLEEPING:
            case SPIN_ATTACK:
            case SWIMMING:
                return EquipmentSlotType.LEGS;
            default:
                if (yRelativistic <= 0.375) {
                    return EquipmentSlotType.FEET;
                }
                if (MathHelper.rangeInclusive(yRelativistic, 0.375, 0.89)) {
                    return EquipmentSlotType.LEGS;
                }
                if (MathHelper.rangeInclusive(yRelativistic, 0.89, 1.415)) {
                    return EquipmentSlotType.CHEST;
                }
                if (yRelativistic >= 1.415) {
                    return EquipmentSlotType.HEAD;
                }
                return null;
        }
    }

    private static float headHit(PlayerEntity player, float damage, EvolutionDamage.Type type) {
        damage *= 1.75f;
        //TODO
        return damage;
    }

    private static float legHit(PlayerEntity player, float damage, EvolutionDamage.Type type) {
        //TODO
        return damage;
    }

    private static void lungeEntity(PlayerEntity player,
                                    Entity targetEntity,
                                    Hand hand,
                                    double rayTraceHeight,
                                    ItemStack lungeStack,
                                    float strength) {
        Item lungeItem = lungeStack.getItem();
        if (targetEntity.isAttackable()) {
            if (!targetEntity.skipAttackInteraction(player)) {
                float damage = (float) player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
                if (lungeItem instanceof IMelee) {
                    damage += ((IMelee) lungeItem).getAttackDamage();
                }
                damage *= 1.2;
                damage *= strength;
                if (damage > 0.0F) {
                    int knockbackModifier = 0;
                    if (lungeItem instanceof IKnockback) {
                        knockbackModifier += ((IKnockback) lungeItem).getLevel();
                    }
                    if (player.isSprinting()) {
                        player.level.playSound(null,
                                               player.getX(),
                                               player.getY(),
                                               player.getZ(),
                                               SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                                               player.getSoundSource(),
                                               1.0F,
                                               1.0F);
                        ++knockbackModifier;
                    }
                    int fireAspectModifier = 0;
                    if (lungeItem instanceof IFireAspect) {
                        if (RAND.nextFloat() < ((IFireAspect) lungeItem).getChance()) {
                            fireAspectModifier = ((IFireAspect) lungeItem).getLevel();
                        }
                    }
                    float oldHealth = 0.0F;
                    boolean fireAspect = false;
                    if (targetEntity instanceof LivingEntity) {
                        oldHealth = ((LivingEntity) targetEntity).getHealth();
                        if (fireAspectModifier > 0 && !targetEntity.isOnFire()) {
                            fireAspect = true;
                            targetEntity.setRemainingFireTicks(1);
                        }
                    }
                    Vector3d targetMotion = targetEntity.getDeltaMovement();
                    EvolutionDamage.Type type = EvolutionDamage.Type.PIERCING;
                    DamageSource source;
                    if (targetEntity instanceof PlayerEntity) {
                        EquipmentSlotType slot = EquipmentSlotType.LEGS;
                        if (!Double.isNaN(rayTraceHeight)) {
                            slot = getPartByPosition(rayTraceHeight, (PlayerEntity) targetEntity);
                        }
                        source = EvolutionDamage.causePVPMeleeDamage(player, type, hand, slot);
                    }
                    else {
                        source = EvolutionDamage.causePlayerMeleeDamage(player, type, hand);
                    }
                    boolean attackSuccessfull = targetEntity.hurt(source, damage);
                    if (attackSuccessfull) {
                        //Knockback calculations
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof LivingEntity) {
                                ((LivingEntity) targetEntity).knockback(knockbackModifier * 0.5F,
                                                                        MathHelper.sinDeg(player.yRot),
                                                                        -MathHelper.cosDeg(player.yRot));
                            }
                            else {
                                targetEntity.push(-MathHelper.sinDeg(player.yRot) * knockbackModifier * 0.5,
                                                  0.0,
                                                  MathHelper.cosDeg(player.yRot) * knockbackModifier * 0.5);
                            }
                            player.setDeltaMovement(player.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                        }
                        //Calculated velocity changed
                        if (targetEntity instanceof ServerPlayerEntity && targetEntity.hurtMarked) {
                            ((ServerPlayerEntity) targetEntity).connection.send(new SEntityVelocityPacket(targetEntity));
                            targetEntity.hurtMarked = false;
                            targetEntity.setDeltaMovement(targetMotion);
                        }
                        //Strong attack particles
                        player.level.playSound(null,
                                               player.getX(),
                                               player.getY(),
                                               player.getZ(),
                                               SoundEvents.PLAYER_ATTACK_STRONG,
                                               player.getSoundSource(),
                                               1.0F,
                                               1.0F);
                        player.setLastHurtMob(targetEntity);
                        //Entity parts
                        Entity entity = targetEntity;
                        if (targetEntity instanceof EnderDragonPartEntity) {
                            entity = ((EnderDragonPartEntity) targetEntity).parentMob;
                        }
                        //Item damage calculation
                        if (!player.level.isClientSide && !lungeStack.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = lungeStack.copy();
                            lungeStack.hurtEnemy((LivingEntity) entity, player);
                            if (lungeStack.isEmpty()) {
                                ForgeEventFactory.onPlayerDestroyItem(player, copy, hand);
                            }
                        }
                        //Stats and Heart particles
                        if (targetEntity instanceof LivingEntity) {
                            LivingEntity living = (LivingEntity) targetEntity;
                            float damageDealt = oldHealth - living.getHealth();
                            applyDamageRaw(player, damage, type);
                            applyDamageActual(player, damageDealt, type, living);
                            if (fireAspectModifier > 0) {
                                living.setRemainingFireTicks(fireAspectModifier * 4);
                            }
                            if (player.level instanceof ServerWorld && damageDealt >= 10.0F) {
                                int heartsToSpawn = (int) (damageDealt * 0.1);
                                ((ServerWorld) player.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                                                                           living.getX(),
                                                                           living.getY() + living.getBbHeight() * 0.5,
                                                                           living.getZ(),
                                                                           heartsToSpawn,
                                                                           0.5,
                                                                           0.0,
                                                                           0.5,
                                                                           0.1);
                            }
                        }
                        player.causeFoodExhaustion(0.1F);
                    }
                    //Attack fail
                    else {
                        player.level.playSound(null,
                                               player.getX(),
                                               player.getY(),
                                               player.getZ(),
                                               SoundEvents.PLAYER_ATTACK_NODAMAGE,
                                               player.getSoundSource(),
                                               1.0F,
                                               1.0F);
                        if (fireAspect) {
                            targetEntity.clearFire();
                        }
                    }
                }
            }
        }
    }

    public static void performAttack(PlayerEntity player, @Nullable Entity entity, Hand hand, double rayTraceHeight) {
        if (hand == Hand.OFF_HAND) {
            Item offhandItem = player.getOffhandItem().getItem();
            if (!(offhandItem instanceof IOffhandAttackable)) {
                return;
            }
        }
        swingArm(player, hand);
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new PacketSCHandAnimation(hand));
        if (entity != null) {
            attackEntity(player, entity, hand, rayTraceHeight);
        }
    }

    public static void performLunge(PlayerEntity player,
                                    @Nullable Entity entity,
                                    Hand hand,
                                    double rayTraceHeight,
                                    ItemStack lungeStack,
                                    float strength) {
        if (entity != null && lungeStack.getItem() instanceof ILunge) {
            lungeEntity(player, entity, hand, rayTraceHeight, lungeStack, strength);
        }
    }

    private static void swingArm(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.onEntitySwing(player)) {
            return;
        }
        player.swingTime = -1;
        player.swinging = true;
        player.swingingArm = hand;
        if (player.level instanceof ServerWorld) {
            ((ServerWorld) player.level).getChunkSource().broadcastAndSend(player, new SAnimateHandPacket(player, hand == Hand.MAIN_HAND ? 0 : 3));
        }
    }

    public static void takeStat(PlayerEntity player, Stat<?> stat) {
        if (player instanceof ServerPlayerEntity && !(player instanceof FakePlayer)) {
            ((EvolutionServerStatisticsManager) ((ServerPlayerEntity) player).getStats()).setValueLong(stat, 0);
            player.getScoreboard().forAllObjectives(stat, player.getScoreboardName(), Score::reset);
        }
    }

    //TODO temp
    public static double tempDoubleValueY() {
        return 0;
    }

    //TODO temp
    public static double tempDoubleValueZ() {
        return 0;
    }

    //TODO temp
    public static void tempTranslationAbsolute(AbstractClientPlayerEntity player, MatrixStack matrices, float partialTicks) {

    }

    //TODO temp
    public static void tempTranslationRelative(AbstractClientPlayerEntity player, MatrixStack matrices, float partialTicks) {
        float pitch = player.getViewXRot(partialTicks);
        float sinPitch = Math.abs(MathHelper.sinDeg(pitch));
        float cosPitch = MathHelper.cosDeg(pitch);
        if (pitch <= 0) {
            matrices.translate(0, -1 * sinPitch - 1.35 * cosPitch * cosPitch, -0.2 * sinPitch + 0.2 * cosPitch);
        }
        else {
            matrices.translate(0, -1.75 * sinPitch - 1.35 * cosPitch * cosPitch, -0.2 * sinPitch + 0.2 * cosPitch);
        }
    }

    //TODO temp
    public static Vector3d tempVector3d() {
        return new Vector3d(0, 4.62 / 16.0 * 0.937_5, -2 / 16.0);
    }
}
