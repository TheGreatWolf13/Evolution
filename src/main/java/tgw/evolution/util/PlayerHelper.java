package tgw.evolution.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
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
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.items.*;
import tgw.evolution.network.PacketSCHandAnimation;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class PlayerHelper {

    /**
     * The base attack speed of the Player, in attacks / s.
     */
    public static final double ATTACK_SPEED = 2;
    /**
     * The base reach distance of the Player, in metres.
     */
    public static final double REACH_DISTANCE = 3.25;
    /**
     * The base Player mass. Used in various kinetic calculations.
     */
    public static final double MASS = 70;
    /**
     * The height of the Player legs. Used to calculate fall damage.
     */
    public static final double LEG_HEIGHT = 0.875;
    /**
     * An acceleration value in m/t^2 that will result in a maximum speed of 4.32 m/s
     */
    public static final double WALK_SPEED = 0.025;
    /**
     * Controls the loss of deceleration because of the motion of your legs.
     */
    public static final double LEG_SLOWDOWN = 0.113_6;
    private static final Random RAND = new Random();

    public static void performAttack(PlayerEntity player, @Nullable Entity entity, Hand hand) {
        if (hand == Hand.OFF_HAND) {
            Item offhandItem = player.getHeldItemOffhand().getItem();
            if (!(offhandItem instanceof IOffhandAttackable)) {
                return;
            }
        }
        swingArm(player, hand);
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new PacketSCHandAnimation(hand));
        if (entity != null) {
            attackEntity(player, entity, hand);
        }
    }

    private static void swingArm(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && stack.onEntitySwing(player)) {
            return;
        }
        player.swingProgressInt = -1;
        player.isSwingInProgress = true;
        player.swingingHand = hand;
        if (player.world instanceof ServerWorld) {
            ((ServerWorld) player.world).getChunkProvider().sendToAllTracking(player, new SAnimateHandPacket(player, hand == Hand.MAIN_HAND ? 0 : 3));
        }
    }

    private static void attackEntity(PlayerEntity player, Entity targetEntity, Hand hand) {
        ItemStack attackStack = player.getHeldItem(hand);
        Item attackItem = attackStack.getItem();
        if (!(attackStack.isEmpty() || !attackItem.onLeftClickEntity(attackStack, player, targetEntity))) {
            return;
        }
        if (targetEntity.canBeAttackedWithItem()) {
            if (!targetEntity.hitByEntity(player)) {
                float damage = (float) player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
                if (attackItem instanceof IOffhandAttackable) {
                    damage = (float) (((IOffhandAttackable) attackItem).getAttackDamage() +
                                      player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());
                }
                //                float enchantmentModifier;
                //                //TODO damage enchantments
                //                if (targetEntity instanceof LivingEntity) {
                //                    enchantmentModifier = EnchantmentHelper.getModifierForCreature(attackStack, ((LivingEntity) targetEntity)
                //                    .getCreatureAttribute());
                //                }
                //                else {
                //                    enchantmentModifier = EnchantmentHelper.getModifierForCreature(attackStack, CreatureAttribute.UNDEFINED);
                //                }
                if (damage > 0.0F /*|| enchantmentModifier > 0.0F*/) {
                    int knockbackModifier = 0;
                    if (attackItem instanceof IKnockback) {
                        knockbackModifier += ((IKnockback) attackItem).getModifier();
                    }
                    boolean sprinting = false;
                    if (player.isSprinting()) {
                        player.world.playSound(null,
                                               player.posX,
                                               player.posY,
                                               player.posZ,
                                               SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
                                               player.getSoundCategory(),
                                               1.0F,
                                               1.0F);
                        ++knockbackModifier;
                        sprinting = true;
                    }
//                    boolean critical = player.fallDistance > 0.0F &&
//                                       !player.onGround &&
//                                       !player.isOnLadder() &&
//                                       !player.isInWater() &&
//                                       !player.isPotionActive(Effects.BLINDNESS) &&
//                                       !player.isPassenger() &&
//                                       targetEntity instanceof LivingEntity;
//                    critical = critical && !player.isSprinting();
//                    CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, targetEntity, critical, critical ? 1.5F : 1.0F);
//                    critical = hitResult != null;
//                    if (critical) {
//                        damage *= hitResult.getDamageModifier();
//                    }
                    /*damage += enchantmentModifier;*/
                    boolean isSweepAttack = false;
                    double distanceWalked = player.distanceWalkedModified - player.prevDistanceWalkedModified;
                    if (/*!critical && */!sprinting && player.onGround && distanceWalked < player.getAIMoveSpeed()) {
                        if (attackItem instanceof ISweepAttack) {
                            isSweepAttack = true;
                        }
                    }
                    int fireAspectModifier = 0;
                    if (attackItem instanceof IFireAspect) {
                        if (RAND.nextFloat() < ((IFireAspect) attackItem).getChance()) {
                            fireAspectModifier = ((IFireAspect) attackItem).getModifier();
                        }
                    }
                    float oldHealth = 0.0F;
                    boolean fireAspect = false;
                    if (targetEntity instanceof LivingEntity) {
                        oldHealth = ((LivingEntity) targetEntity).getHealth();
                        if (fireAspectModifier > 0 && !targetEntity.isBurning()) {
                            fireAspect = true;
                            targetEntity.setFire(1);
                        }
                    }
                    Vec3d targetMotion = targetEntity.getMotion();
                    EvolutionDamage.Type type = attackItem instanceof IMelee ? ((IMelee) attackItem).getDamageType() : EvolutionDamage.Type.GENERIC;
                    boolean attackSuccessfull = targetEntity.attackEntityFrom(EvolutionDamage.causePlayerMeleeDamage(player, type), damage);
                    if (attackSuccessfull) {
                        //Knockback calculations
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof LivingEntity) {
                                ((LivingEntity) targetEntity).knockBack(player,
                                                                        knockbackModifier * 0.5F,
                                                                        MathHelper.sinDeg(player.rotationYaw),
                                                                        -MathHelper.cosDeg(player.rotationYaw));
                            }
                            else {
                                targetEntity.addVelocity(-MathHelper.sinDeg(player.rotationYaw) * knockbackModifier * 0.5F,
                                                         0.1,
                                                         MathHelper.cosDeg(player.rotationYaw) * knockbackModifier * 0.5F);
                            }
                            player.setMotion(player.getMotion().mul(0.6, 1, 0.6));
                        }
                        //Sweep Attack
                        if (isSweepAttack) {
                            float sweepingDamage = 1.0F + ((ISweepAttack) attackItem).getSweepRatio() * damage;
                            for (LivingEntity livingentity : player.world.getEntitiesWithinAABB(LivingEntity.class,
                                                                                                targetEntity.getBoundingBox().grow(1, 0.25, 1))) {
                                if (livingentity != player &&
                                    livingentity != targetEntity &&
                                    !player.isOnSameTeam(livingentity) &&
                                    (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity) livingentity).hasMarker()) &&
                                    player.getDistanceSq(livingentity) < 9.0D) {
                                    livingentity.knockBack(player,
                                                           0.4F,
                                                           MathHelper.sinDeg(player.rotationYaw),
                                                           -MathHelper.cosDeg(player.rotationYaw));
                                    //noinspection ObjectAllocationInLoop
                                    livingentity.attackEntityFrom(DamageSource.causePlayerDamage(player), sweepingDamage);
                                }
                            }
                            player.world.playSound(null,
                                                   player.posX,
                                                   player.posY,
                                                   player.posZ,
                                                   SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                                                   player.getSoundCategory(),
                                                   1.0F,
                                                   1.0F);
                            player.spawnSweepParticles();
                        }
                        //Calculated velocity changed
                        if (targetEntity instanceof ServerPlayerEntity && targetEntity.velocityChanged) {
                            ((ServerPlayerEntity) targetEntity).connection.sendPacket(new SEntityVelocityPacket(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.setMotion(targetMotion);
                        }
                        //Critical particles
//                        if (critical) {
//                            player.world.playSound(null,
//                                                   player.posX,
//                                                   player.posY,
//                                                   player.posZ,
//                                                   SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
//                                                   player.getSoundCategory(),
//                                                   1.0F,
//                                                   1.0F);
//                            player.onCriticalHit(targetEntity);
//                        }
                        //Strong attack particles
                        if (/*!critical && */!isSweepAttack) {
                            player.world.playSound(null,
                                                   player.posX,
                                                   player.posY,
                                                   player.posZ,
                                                   SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
                                                   player.getSoundCategory(),
                                                   1.0F,
                                                   1.0F);
                        }
                        /*if (enchantmentModifier > 0.0F) {
                            player.onEnchantmentCritical(targetEntity);
                        }*/
                        player.setLastAttackedEntity(targetEntity);
                        //Entity parts
                        Entity entity = targetEntity;
                        if (targetEntity instanceof EnderDragonPartEntity) {
                            entity = ((EnderDragonPartEntity) targetEntity).dragon;
                        }
                        //Item damage calculation
                        if (!player.world.isRemote && !attackStack.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = attackStack.copy();
                            attackStack.hitEntity((LivingEntity) entity, player);
                            if (attackStack.isEmpty()) {
                                ForgeEventFactory.onPlayerDestroyItem(player, copy, hand);
                                player.setHeldItem(hand, ItemStack.EMPTY);
                            }
                        }
                        //Stats and Heart particles
                        if (targetEntity instanceof LivingEntity) {
                            float damageDealt = oldHealth - ((LivingEntity) targetEntity).getHealth();
                            player.addStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
                            if (fireAspectModifier > 0) {
                                targetEntity.setFire(fireAspectModifier * 4);
                            }
                            if (player.world instanceof ServerWorld && damageDealt >= 10.0F) {
                                int heartsToSpawn = (int) (damageDealt * 0.1);
                                ((ServerWorld) player.world).spawnParticle(ParticleTypes.DAMAGE_INDICATOR,
                                                                           targetEntity.posX,
                                                                           targetEntity.posY + targetEntity.getHeight() * 0.5F,
                                                                           targetEntity.posZ,
                                                                           heartsToSpawn,
                                                                           0.1,
                                                                           0,
                                                                           0.1,
                                                                           0.2);
                            }
                        }
                        player.addExhaustion(0.1F);
                    }
                    //Attack fail
                    else {
                        player.world.playSound(null,
                                               player.posX,
                                               player.posY,
                                               player.posZ,
                                               SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE,
                                               player.getSoundCategory(),
                                               1.0F,
                                               1.0F);
                        if (fireAspect) {
                            targetEntity.extinguish();
                        }
                    }
                }
            }
        }
    }

    @Nullable
    public static EquipmentSlotType getPartByPosition(double y, PlayerEntity player) {
        double yRelativistic = y - player.posY;
        if (player.isSneaking()) {
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
        }
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

    public static float getDamage(@Nullable EquipmentSlotType slot, PlayerEntity player, float damage, EvolutionDamage.Type type) {
        //TODO damage calculations
        Evolution.LOGGER.debug("{} received {}HP of {} damage on {}", player.getScoreboardName(), damage, type, slot);
        return damage;
    }

    public static void headHit(PlayerEntity player, float damage) {
        float strength = MathHelper.relativize(damage, 0, player.getMaxHealth());
        if (RAND.nextFloat() < strength) {
            player.addPotionEffect(new EffectInstance(Effects.NAUSEA, (int) (300 * strength), 0, true, false, true));
        }
        if (RAND.nextFloat() < strength) {
            player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, (int) (100 * strength), strength > 0.8f ? 1 : 0, true, false, true));
        }
        if (RAND.nextFloat() < strength) {
            player.addPotionEffect(new EffectInstance(EvolutionEffects.DIZZINESS.get(),
                                                      (int) (400 * strength),
                                                      strength > 0.8f ? 1 : 0,
                                                      true,
                                                      false,
                                                      true));
        }
    }
}
