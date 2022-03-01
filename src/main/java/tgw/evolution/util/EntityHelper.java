package tgw.evolution.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.*;
import tgw.evolution.util.hitbox.HitboxType;
import tgw.evolution.util.math.MathHelper;

public final class EntityHelper {

    private EntityHelper() {
    }

    public static void attackEntity(LivingEntity attacker, Entity victim, InteractionHand hand, HitboxType... hitbox) {
        ItemStack attackStack = attacker.getItemInHand(hand);
        Item attackItem = attackStack.getItem();
        if (!(attackStack.isEmpty() || attacker instanceof Player player && !attackItem.onLeftClickEntity(attackStack, player, victim))) {
            return;
        }
        if (victim.isAttackable()) {
            if (!victim.skipAttackInteraction(attacker)) {
                float damage = (float) attacker.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
                if (attackItem instanceof IOffhandAttackable) {
                    damage = (float) (((IOffhandAttackable) attackItem).getAttackDamage(attackStack) +
                                      attacker.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
                }
                if (damage > 0.0F) {
                    int knockbackModifier = 0;
                    if (attackItem instanceof IKnockback) {
                        knockbackModifier += ((IKnockback) attackItem).getLevel();
                    }
                    boolean sprinting = false;
                    if (attacker.isSprinting() && attacker instanceof Player) {
                        attacker.level.playSound(null,
                                                 attacker.getX(),
                                                 attacker.getY(),
                                                 attacker.getZ(),
                                                 SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                                                 attacker.getSoundSource(),
                                                 1.0F,
                                                 1.0F);
                        ++knockbackModifier;
                        sprinting = true;
                    }
                    int heavyModifier = 0;
                    if (attackItem instanceof IHeavyAttack heavyItem) {
                        float heavyChance = heavyItem.getHeavyAttackChance();
                        if (sprinting) {
                            heavyChance *= 2;
                        }
                        if (attacker.getRandom().nextFloat() < heavyChance) {
                            heavyModifier = heavyItem.getHeavyAttackLevel();
                            if (sprinting) {
                                heavyModifier *= 2;
                            }
                            if (attacker.level instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                                                          victim.getX(),
                                                          victim.getY() + victim.getBbHeight() * 0.5F,
                                                          victim.getZ(),
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
                    if (!sprinting && attacker.isOnGround()) {
                        if (attackItem instanceof ISweepAttack) {
                            isSweepAttack = true;
                        }
                    }
                    int fireAspectModifier = 0;
                    if (attackItem instanceof IFireAspect fireItem) {
                        if (attacker.getRandom().nextFloat() < fireItem.getChance()) {
                            fireAspectModifier = fireItem.getLevel();
                        }
                    }
                    float oldHealth = 0.0F;
                    boolean fireAspect = false;
                    if (victim instanceof LivingEntity livingVictim) {
                        oldHealth = livingVictim.getHealth();
                        if (fireAspectModifier > 0 && !livingVictim.isOnFire()) {
                            fireAspect = true;
                            livingVictim.setRemainingFireTicks(1);
                        }
                    }
                    Vec3 targetMotion = victim.getDeltaMovement();
                    EvolutionDamage.Type type = attackItem instanceof IMelee melee ? melee.getDamageType(attackStack) : EvolutionDamage.Type.CRUSHING;
                    DamageSource source;
                    if (attacker instanceof Player player) {
                        source = EvolutionDamage.causePlayerMeleeDamage(player, type, hand);
                    }
                    else {
                        source = EvolutionDamage.causeMobMeleeDamage(attacker, type, hand);
                    }
                    boolean attackSuccessfull = victim.hurt(source, damage);
                    if (attackSuccessfull) {
                        //Knockback calculations
                        if (knockbackModifier > 0) {
                            if (victim instanceof LivingEntity) {
                                ((LivingEntity) victim).knockback(knockbackModifier * 0.5F,
                                                                  MathHelper.sinDeg(attacker.getYRot()),
                                                                  -MathHelper.cosDeg(attacker.getYRot()));
                            }
                            else {
                                victim.push(-MathHelper.sinDeg(attacker.getYRot()) * knockbackModifier * 0.5F,
                                            0,
                                            MathHelper.cosDeg(attacker.getYRot()) * knockbackModifier * 0.5F);
                            }
                            attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6, 1, 0.6));
                        }
                        //Sweep Attack
                        int entitiesHit = 1;
//                        if (isSweepAttack) {
//                            float sweepingDamage = 1.0F + ((ISweepAttack) attackItem).getSweepRatio() * damage;
//                            for (LivingEntity livingEntity : attacker.level.getEntitiesOfClass(LivingEntity.class,
//                                                                                             victim.getBoundingBox().inflate(1, 0.25, 1))) {
//                                if (livingEntity != attacker &&
//                                    livingEntity != victim &&
//                                    !attacker.isAlliedTo(livingEntity) &&
//                                    (!(livingEntity instanceof ArmorStandEntity) || !((ArmorStandEntity) livingEntity).isMarker()) &&
//                                    attacker.distanceToSqr(livingEntity) < 9) {
//                                    livingEntity.knockback(0.4F, MathHelper.sinDeg(attacker.yRot), -MathHelper.cosDeg(attacker.yRot));
//                                    if (livingEntity instanceof PlayerEntity) {
//                                        //noinspection ObjectAllocationInLoop
//                                        livingEntity.hurt(EvolutionDamage.causePlayerMeleeDamage((PlayerEntity) attacker, type, hand),
//                                                          sweepingDamage);
//                                    }
//                                    else {
//                                        //noinspection ObjectAllocationInLoop
//                                        livingEntity.hurt(EvolutionDamage.causePlayerMeleeDamage(player, type, hand), sweepingDamage);
//                                    }
//                                    entitiesHit++;
//                                }
//                            }
//                            player.level.playSound(null,
//                                                   player.getX(),
//                                                   player.getY(),
//                                                   player.getZ(),
//                                                   SoundEvents.PLAYER_ATTACK_SWEEP,
//                                                   player.getSoundSource(),
//                                                   1.0F,
//                                                   1.0F);
//                            player.sweepAttack();
//                        }
                        //Calculated velocity changed
                        if (victim instanceof ServerPlayer player && victim.hurtMarked) {
                            player.connection.send(new ClientboundSetEntityMotionPacket(victim));
                            victim.hurtMarked = false;
                            victim.setDeltaMovement(targetMotion);
                        }
                        //Strong attack particles
                        if (!isSweepAttack) {
                            attacker.level.playSound(null,
                                                     attacker.getX(),
                                                     attacker.getY(),
                                                     attacker.getZ(),
                                                     SoundEvents.PLAYER_ATTACK_STRONG,
                                                     attacker.getSoundSource(),
                                                     1.0F,
                                                     1.0F);
                        }
                        attacker.setLastHurtMob(victim);
                        //Entity parts
                        Entity entity = victim;
                        if (victim instanceof EnderDragonPart enderDragonPart) {
                            entity = enderDragonPart.parentMob;
                        }
                        //Item damage calculation
                        if (attacker instanceof Player player &&
                            !attacker.level.isClientSide &&
                            !attackStack.isEmpty() &&
                            entity instanceof LivingEntity living) {
                            ItemStack copy = attackStack.copy();
                            for (int i = 0; i < entitiesHit; i++) {
                                attackStack.hurtEnemy(living, player);
                                if (attackStack.isEmpty()) {
                                    ForgeEventFactory.onPlayerDestroyItem(player, copy, hand);
                                    attacker.setItemInHand(hand, ItemStack.EMPTY);
                                    break;
                                }
                            }
                        }
                        //Stats and Heart particles
                        if (victim instanceof LivingEntity living) {
                            float damageDealt = oldHealth - living.getHealth();
                            if (attacker instanceof Player player) {
                                PlayerHelper.applyDamageRaw(player, damage, type);
                                PlayerHelper.applyDamageActual(player, damageDealt, type, living);
                            }
                            if (fireAspectModifier > 0) {
                                living.setRemainingFireTicks(fireAspectModifier * 4);
                            }
                            if (attacker.level instanceof ServerLevel serverLevel && damageDealt >= 10.0F) {
                                int heartsToSpawn = (int) (damageDealt * 0.1);
                                serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
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
                        if (attacker instanceof Player) {
                            //TODO add exhaustion
//                            attacker.causeFoodExhaustion(0.1F);
                        }
                    }
                    //Attack fail
                    else {
                        if (attacker instanceof Player) {
                            attacker.level.playSound(null,
                                                     attacker.getX(),
                                                     attacker.getY(),
                                                     attacker.getZ(),
                                                     SoundEvents.PLAYER_ATTACK_NODAMAGE,
                                                     attacker.getSoundSource(),
                                                     1.0F,
                                                     1.0F);
                        }
                        if (fireAspect) {
                            victim.clearFire();
                        }
                    }
                }
            }
        }
    }
}
