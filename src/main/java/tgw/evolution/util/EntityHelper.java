package tgw.evolution.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.IFireAspect;
import tgw.evolution.items.IKnockback;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.hitbox.HitboxType;
import tgw.evolution.util.math.MathHelper;

public final class EntityHelper {

    private EntityHelper() {
    }

    public static void attackEntity(LivingEntity attacker, Entity victim, IMelee.IAttackType attackType, HitboxType... hitbox) {
        ItemStack stack = attacker.getMainHandItem();
        Item item = stack.getItem();
        if (!victim.isAttackable()) {
            return;
        }
        if (victim.skipAttackInteraction(attacker)) {
            return;
        }
        float damage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (damage > 0.0F) {
            int knockbackModifier = 0;
            if (item instanceof IKnockback knockback) {
                knockbackModifier += knockback.getLevel();
            }
            if (attacker.isSprinting()) {
                attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                                         attacker.getSoundSource(), 1.0F, 1.0F);
                ++knockbackModifier;
            }
            int fireAspectModifier = 0;
            if (item instanceof IFireAspect fireItem) {
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
            EvolutionDamage.Type damageType = EvolutionDamage.Type.CRUSHING;
            if (item instanceof IMelee melee) {
                damageType = melee.getDamageType(stack, attackType);
                damage *= melee.getAttackDamage(stack, attackType);
            }
            DamageSource source;
            if (attacker instanceof Player player) {
                source = EvolutionDamage.causePlayerMeleeDamage(player, damageType);
            }
            else {
                source = EvolutionDamage.causeMobMeleeDamage(attacker, damageType);
            }
            boolean attackSuccessfull = victim.hurt(source, damage);
            if (attackSuccessfull) {
                //Knockback calculations
                if (knockbackModifier > 0) {
                    if (victim instanceof LivingEntity) {
                        ((LivingEntity) victim).knockback(knockbackModifier * 0.5F, MathHelper.sinDeg(attacker.getYRot()),
                                                          -MathHelper.cosDeg(attacker.getYRot()));
                    }
                    else {
                        victim.push(-MathHelper.sinDeg(attacker.getYRot()) * knockbackModifier * 0.5F, 0,
                                    MathHelper.cosDeg(attacker.getYRot()) * knockbackModifier * 0.5F);
                    }
                    attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6, 1, 0.6));
                }
                //Calculated velocity changed
                if (victim instanceof ServerPlayer player && victim.hurtMarked) {
                    player.connection.send(new ClientboundSetEntityMotionPacket(victim));
                    victim.hurtMarked = false;
                    victim.setDeltaMovement(targetMotion);
                }
                //Strong attack particles
                attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_STRONG,
                                         attacker.getSoundSource(), 1.0F, 1.0F);
                attacker.setLastHurtMob(victim);
                //Item damage calculation
                if (attacker instanceof Player player &&
                    !attacker.level.isClientSide &&
                    !stack.isEmpty() &&
                    victim instanceof LivingEntity living) {
                    stack.hurtEnemy(living, player);
                }
                //Stats and Heart particles
                if (victim instanceof LivingEntity living) {
                    float damageDealt = oldHealth - living.getHealth();
                    if (attacker instanceof Player player) {
                        PlayerHelper.applyDamageRaw(player, damage, damageType);
                        PlayerHelper.applyDamageActual(player, damageDealt, damageType, living);
                    }
                    if (fireAspectModifier > 0) {
                        living.setRemainingFireTicks(fireAspectModifier * 4);
                    }
                    if (attacker.level instanceof ServerLevel serverLevel && damageDealt >= 10.0F) {
                        int heartsToSpawn = (int) (damageDealt * 0.1);
                        serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, living.getX(), living.getY() + living.getBbHeight() * 0.5F,
                                                  living.getZ(), heartsToSpawn, 0.5, 0, 0.5, 0.1);
                    }
                }
            }
            //Attack fail
            else {
                if (attacker instanceof Player) {
                    attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE,
                                             attacker.getSoundSource(), 1.0F, 1.0F);
                }
                if (fireAspect) {
                    victim.clearFire();
                }
            }
        }
    }
}
