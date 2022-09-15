package tgw.evolution.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Score;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.items.*;
import tgw.evolution.network.PacketSCHandAnimation;
import tgw.evolution.stats.EvolutionServerStatsCounter;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Units;

import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

public final class PlayerHelper {

    /**
     * The base attack damage of the Player, in HP.
     */
    public static final double ATTACK_DAMAGE = 2.5;
    /**
     * The base attack speed of the Player, in attacks / s.
     */
    public static final double ATTACK_SPEED = 2;
    /**
     * The base reach distance of the Player, in metres.
     */
    public static final double REACH_DISTANCE = 3.5;
    /**
     * The base Player mass in kg. Used in various kinetic calculations.
     */
    public static final double MASS = 70;
    /**
     * The Player max health, in HP.
     */
    public static final double MAX_HEALTH = 100;
    /**
     * The height of the Player legs in m. Used to calculate fall damage.
     */
    public static final double LEG_HEIGHT = 0.875;
    /**
     * The force the player uses to push its feet against the ground.
     */
    public static final double WALK_FORCE = Units.toMSUForce(1_000);
    public static final EntityDimensions STANDING_SIZE = EntityDimensions.scalable(0.65F, 1.8F);
    public static final Map<Pose, EntityDimensions> SIZE_BY_POSE = ImmutableMap.<Pose, EntityDimensions>builder()
                                                                               .put(Pose.STANDING, STANDING_SIZE)
                                                                               .put(Pose.SLEEPING, EntityDimensions.fixed(0.2F, 0.2F))
                                                                               .put(Pose.FALL_FLYING, EntityDimensions.scalable(0.65F, 0.65F))
                                                                               .put(Pose.SWIMMING, EntityDimensions.scalable(0.65F, 0.65F))
                                                                               .put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.65F, 0.65F))
                                                                               .put(Pose.CROUCHING, EntityDimensions.scalable(0.65F, 1.5F))
                                                                               .put(Pose.DYING, EntityDimensions.fixed(0.2F, 0.2F))
                                                                               .build();
    private static final RandomGenerator RAND = new Random();

    private PlayerHelper() {
    }

    public static void addStat(Player player, ResourceLocation resLoc, float amount) {
        addStat(player, Stats.CUSTOM, resLoc, amount);
    }

    public static <T> void addStat(Player player, StatType<T> statType, T type, float amount) {
        if (player instanceof ServerPlayer && !(player instanceof FakePlayer)) {
            Stat<T> stat = statType.get(type);
            EvolutionServerStatsCounter stats = (EvolutionServerStatsCounter) ((ServerPlayer) player).getStats();
            stats.incrementPartial(player, stat, amount);
            player.getScoreboard().forAllObjectives(stat, player.getScoreboardName(), score -> {
                assert score != null;
                score.setScore((int) stats.getValueLong(stat));
            });
        }
    }

    public static void applyDamageActual(Player player, float amount, EvolutionDamage.Type type, LivingEntity entity) {
        addStat(player, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(type), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(EvolutionDamage.Type.MELEE), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT_ACTUAL.get(EvolutionDamage.Type.TOTAL), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT.get(), entity.getType(), amount);
    }

    public static void applyDamageRaw(Player player, float amount, EvolutionDamage.Type type) {
        addStat(player, EvolutionStats.DAMAGE_DEALT_RAW.get(type), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT_RAW.get(EvolutionDamage.Type.MELEE), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT_RAW.get(EvolutionDamage.Type.TOTAL), amount);
    }

    private static void attackEntity(Player player, Entity targetEntity, InteractionHand hand, double rayTraceHeight) {
        ItemStack attackStack = player.getItemInHand(hand);
        Item attackItem = attackStack.getItem();
        if (!(attackStack.isEmpty() || !attackItem.onLeftClickEntity(attackStack, player, targetEntity))) {
            return;
        }
        if (targetEntity.isAttackable()) {
            if (!targetEntity.skipAttackInteraction(player)) {
                float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                if (damage > 0.0F) {
                    int knockbackModifier = 0;
                    if (attackItem instanceof IKnockback knockback) {
                        knockbackModifier += knockback.getLevel();
                    }
                    boolean sprinting = false;
                    if (player.isSprinting()) {
                        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                                               player.getSoundSource(), 1.0F, 1.0F);
                        ++knockbackModifier;
                        sprinting = true;
                    }
                    int heavyModifier = 0;
                    if (attackItem instanceof IHeavyAttack heavyAttack) {
                        float heavyChance = heavyAttack.getHeavyAttackChance();
                        if (sprinting) {
                            heavyChance *= 2;
                        }
                        if (RAND.nextFloat() < heavyChance) {
                            heavyModifier = heavyAttack.getHeavyAttackLevel();
                            if (sprinting) {
                                heavyModifier *= 2;
                            }
                            if (player.level instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, targetEntity.getX(),
                                                          targetEntity.getY() + targetEntity.getBbHeight() * 0.5F, targetEntity.getZ(), 10, 0.5, 0,
                                                          0.5, 0.1);
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
                    if (attackItem instanceof IFireAspect fireAspect) {
                        if (RAND.nextFloat() < fireAspect.getChance()) {
                            fireAspectModifier = fireAspect.getLevel();
                        }
                    }
                    float oldHealth = 0.0F;
                    boolean fireAspect = false;
                    if (targetEntity instanceof LivingEntity living) {
                        oldHealth = living.getHealth();
                        if (fireAspectModifier > 0 && !targetEntity.isOnFire()) {
                            fireAspect = true;
                            targetEntity.setRemainingFireTicks(1);
                        }
                    }
                    Vec3 targetMotion = targetEntity.getDeltaMovement();
                    EvolutionDamage.Type type = attackItem instanceof IMelee melee ?
                                                melee.getDamageType(attackStack, melee.getBasicAttackType(attackStack)) :
                                                EvolutionDamage.Type.CRUSHING;
                    DamageSource source = EvolutionDamage.causePlayerMeleeDamage(player, type);
                    boolean attackSuccessfull = targetEntity.hurt(source, damage);
                    if (attackSuccessfull) {
                        //Knockback calculations
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof LivingEntity living) {
                                living.knockback(knockbackModifier * 0.5F, MathHelper.sinDeg(player.getYRot()), -MathHelper.cosDeg(player.getYRot()));
                            }
                            else {
                                targetEntity.push(-MathHelper.sinDeg(player.getYRot()) * knockbackModifier * 0.5F, 0,
                                                  MathHelper.cosDeg(player.getYRot()) * knockbackModifier * 0.5F);
                            }
                            player.setDeltaMovement(player.getDeltaMovement().multiply(0.6, 1, 0.6));
                        }
                        //Sweep Attack
                        int entitiesHit = 1;
                        if (isSweepAttack) {
                            float sweepingDamage = 1.0F + ((ISweepAttack) attackItem).getSweepRatio() * damage;
                            for (LivingEntity livingEntity : player.level.getEntitiesOfClass(LivingEntity.class,
                                                                                             targetEntity.getBoundingBox().inflate(1, 0.25, 1))) {
                                assert livingEntity != null;
                                if (livingEntity != player &&
                                    livingEntity != targetEntity &&
                                    !player.isAlliedTo(livingEntity) &&
                                    (!(livingEntity instanceof ArmorStand armorStand) || !armorStand.isMarker()) &&
                                    player.distanceToSqr(livingEntity) < 9) {
                                    livingEntity.knockback(0.4F, MathHelper.sinDeg(player.getYRot()), -MathHelper.cosDeg(player.getYRot()));
                                    //noinspection ObjectAllocationInLoop
                                    livingEntity.hurt(EvolutionDamage.causePlayerMeleeDamage(player, type), sweepingDamage);
                                    entitiesHit++;
                                }
                            }
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP,
                                                   player.getSoundSource(), 1.0F, 1.0F);
                            player.sweepAttack();
                        }
                        //Calculated velocity changed
                        if (targetEntity instanceof ServerPlayer serverPlayer && targetEntity.hurtMarked) {
                            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(targetEntity));
                            targetEntity.hurtMarked = false;
                            targetEntity.setDeltaMovement(targetMotion);
                        }
                        //Strong attack particles
                        if (!isSweepAttack) {
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG,
                                                   player.getSoundSource(), 1.0F, 1.0F);
                        }
                        player.setLastHurtMob(targetEntity);
                        //Entity parts
                        Entity entity = targetEntity;
                        if (targetEntity instanceof EnderDragonPart enderDragonPart) {
                            entity = enderDragonPart.parentMob;
                        }
                        //Item damage calculation
                        if (!player.level.isClientSide && !attackStack.isEmpty() && entity instanceof LivingEntity living) {
                            ItemStack copy = attackStack.copy();
                            for (int i = 0; i < entitiesHit; i++) {
                                attackStack.hurtEnemy(living, player);
                                if (attackStack.isEmpty()) {
                                    ForgeEventFactory.onPlayerDestroyItem(player, copy, hand);
                                    player.setItemInHand(hand, ItemStack.EMPTY);
                                    break;
                                }
                            }
                        }
                        //Stats and Heart particles
                        if (targetEntity instanceof LivingEntity living) {
                            float damageDealt = oldHealth - living.getHealth();
                            applyDamageRaw(player, damage, type);
                            applyDamageActual(player, damageDealt, type, living);
                            if (fireAspectModifier > 0) {
                                living.setRemainingFireTicks(fireAspectModifier * 4);
                            }
                            if (player.level instanceof ServerLevel serverLevel && damageDealt >= 10.0F) {
                                int heartsToSpawn = (int) (damageDealt * 0.1);
                                serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, living.getX(), living.getY() + living.getBbHeight() * 0.5F,
                                                          living.getZ(), heartsToSpawn, 0.5, 0, 0.5, 0.1);
                            }
                        }
                        player.causeFoodExhaustion(0.1F);
                    }
                    //Attack fail
                    else {
                        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE,
                                               player.getSoundSource(), 1.0F, 1.0F);
                        if (fireAspect) {
                            targetEntity.clearFire();
                        }
                    }
                }
            }
        }
    }

    private static float chestHit(Player player, float damage, EvolutionDamage.Type type) {
        damage *= 1.25f;
        //TODO
        return damage;
    }

    private static float footHit(Player player, float damage, EvolutionDamage.Type type) {
        damage *= 0.75f;
        //TODO
        return damage;
    }

    private static float fullHit(Player player, float damage, EvolutionDamage.Type type) {
        Evolution.info("{} received {}HP of {} damage", player.getScoreboardName(), damage, type);
        //TODO
        return damage;
    }

    public static float getDamage(@Nullable EquipmentSlot slot, Player player, float damage, EvolutionDamage.Type type) {
        if (slot == null) {
            return fullHit(player, damage, type);
        }
        Evolution.info("{} received {}HP of {} damage on {}", player.getScoreboardName(), damage, type, slot);
        switch (slot) {
            case HEAD -> {
                return headHit(player, damage, type);
            }
            case CHEST -> {
                return chestHit(player, damage, type);
            }
            case LEGS -> {
                return legHit(player, damage, type);
            }
            case FEET -> {
                return footHit(player, damage, type);
            }
        }
        return damage;
    }

    @Nullable
    public static EquipmentSlot getPartByPosition(double y, Player player) {
        double yRelativistic = y - player.getY();
        switch (player.getPose()) {
            case CROUCHING -> {
                if (yRelativistic <= 0.25) {
                    return EquipmentSlot.FEET;
                }
                if (MathHelper.rangeInclusive(yRelativistic, 0.25, 0.625)) {
                    return EquipmentSlot.LEGS;
                }
                if (MathHelper.rangeInclusive(yRelativistic, 0.625, 1.125)) {
                    return EquipmentSlot.CHEST;
                }
                if (yRelativistic >= 1.125) {
                    return EquipmentSlot.HEAD;
                }
                return null;
            }
            case DYING -> {
                return null;
            }
            case FALL_FLYING, SLEEPING, SPIN_ATTACK, SWIMMING -> {
                return EquipmentSlot.LEGS;
            }
            default -> {
                if (yRelativistic <= 0.375) {
                    return EquipmentSlot.FEET;
                }
                if (MathHelper.rangeInclusive(yRelativistic, 0.375, 0.89)) {
                    return EquipmentSlot.LEGS;
                }
                if (MathHelper.rangeInclusive(yRelativistic, 0.89, 1.415)) {
                    return EquipmentSlot.CHEST;
                }
                if (yRelativistic >= 1.415) {
                    return EquipmentSlot.HEAD;
                }
                return null;
            }
        }
    }

    public static Vec3 getSwimmingNeckPoint(float xRot) {
        float cosPitch = MathHelper.cosDeg(xRot);
        float sinPitch = MathHelper.sinDeg(xRot);
        return new Vec3(0, 0.36 - 0.02 * sinPitch - 0.25 * cosPitch, 0.02 * cosPitch - 0.25 * sinPitch);
    }

    private static float headHit(Player player, float damage, EvolutionDamage.Type type) {
        damage *= 1.75f;
        //TODO
        return damage;
    }

    private static float legHit(Player player, float damage, EvolutionDamage.Type type) {
        //TODO
        return damage;
    }

    public static void performAttack(ServerPlayer player, @Nullable Entity entity, InteractionHand hand, double rayTraceHeight) {
        if (hand == InteractionHand.OFF_HAND) {
            return;
        }
        swingArm(player, hand);
        EvolutionNetwork.send(player, new PacketSCHandAnimation(hand));
        if (entity != null) {
            attackEntity(player, entity, hand, rayTraceHeight);
        }
    }

    private static void swingArm(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.onEntitySwing(player)) {
            return;
        }
        player.swingTime = -1;
        player.swinging = true;
        player.swingingArm = hand;
        if (player.level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource()
                       .broadcastAndSend(player, new ClientboundAnimatePacket(player, hand == InteractionHand.MAIN_HAND ?
                                                                                      ClientboundAnimatePacket.SWING_MAIN_HAND :
                                                                                      ClientboundAnimatePacket.SWING_OFF_HAND));
        }
    }

    public static void takeStat(Player player, Stat<?> stat) {
        if (player instanceof ServerPlayer serverPlayer && !(player instanceof FakePlayer)) {
            ((EvolutionServerStatsCounter) serverPlayer.getStats()).setValueLong(stat, 0);
            player.getScoreboard().forAllObjectives(stat, player.getScoreboardName(), Score::reset);
        }
    }
}
