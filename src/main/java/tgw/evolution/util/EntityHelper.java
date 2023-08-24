package tgw.evolution.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.items.IFireAspect;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.PatchLivingEntity;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.hitbox.HitboxRegistry;
import tgw.evolution.util.hitbox.HitboxType;

public final class EntityHelper {

    private EntityHelper() {
    }

    public static void attackEntity(LivingEntity attacker, Entity victim, IMelee.IAttackType attackType, long hitboxSet) {
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
            int fireAspectModifier = 0;
            if (item instanceof IFireAspect fireItem) {
                if (attacker.getRandom().nextFloat() < fireItem.fireLevel() * 0.1) {
                    fireAspectModifier = fireItem.fireLevel();
                }
            }
            float oldHealth = 0.0F;
//            boolean fireAspect = false;
            if (victim instanceof LivingEntity livingVictim) {
                oldHealth = livingVictim.getHealth();
//                if (fireAspectModifier > 0 && !livingVictim.isOnFire()) {
//                    fireAspect = true;
//                    livingVictim.setRemainingFireTicks(1);
//                }
            }
            Vec3 targetMotion = victim.getDeltaMovement();
            EvolutionDamage.Type damageType = attackType.getDamageType();
            if (item instanceof IMelee melee) {
                damage *= attackType.getDmgMultiplier(melee, stack);
            }
            DamageSourceEv source;
            if (attacker instanceof Player player) {
                source = EvolutionDamage.causePlayerMeleeDamage(player, damageType);
            }
            else {
                source = EvolutionDamage.causeMobMeleeDamage(attacker, damageType);
            }
            boolean attackSuccessfull = hurt(victim, source, damage, hitboxSet);
            if (attackSuccessfull) {
                //Calculated velocity changed
                if (victim instanceof ServerPlayer player && victim.hurtMarked) {
                    player.connection.send(new ClientboundSetEntityMotionPacket(victim));
                    victim.hurtMarked = false;
                    victim.setDeltaMovement(targetMotion);
                }
                //Strong attack particles
                attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, attacker.getSoundSource(), 1.0F, 1.0F);
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
                        PlayerHelper.applyDamage(player, damageDealt, damageType, living);
                    }
                    if (fireAspectModifier > 0) {
                        living.setRemainingFireTicks(Math.max(fireAspectModifier * 4, living.getRemainingFireTicks()));
                    }
                    if (attacker.level instanceof ServerLevel serverLevel && damageDealt >= 10.0F) {
                        int heartsToSpawn = (int) (damageDealt * 0.1);
                        serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, living.getX(), living.getY() + living.getBbHeight() * 0.5F, living.getZ(), heartsToSpawn, 0.5, 0, 0.5, 0.1);
                    }
                }
            }
            //Attack fail
            else {
                if (attacker instanceof Player) {
                    attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, attacker.getSoundSource(), 1.0F, 1.0F);
                }
//                if (fireAspect) {
//                    victim.clearFire();
//                }
            }
        }
    }

    //TODO make armor calculations on every part and other stuff
    public static boolean hurt(Entity entity, DamageSourceEv source, float amount, long hitboxSet) {
        //All
        Evolution.info("Entity {} took {} damage", entity, amount);
        if ((hitboxSet & 1) != 0) {
            return entity.hurt(source, amount);
        }
        int count = Long.bitCount(hitboxSet);
        float strength = amount;
        amount /= count;
        float totalDamage = 0;
        int alreadyCounted = 0;
        for (int i = 1; i < 64; i++) {
            if ((hitboxSet & 1L << i) != 0) {
                alreadyCounted++;
                HitboxType hitbox = HitboxRegistry.deserialize(entity.getType(), i);
                float damage = amount * hitbox.getMultiplier();
                Evolution.info("    {} damage on {}", damage, hitbox);
                if (entity instanceof PatchLivingEntity patch) {
                    totalDamage += patch.tryHurt(source, damage, strength, hitbox);
                }
                else {
                    totalDamage += damage;
                }
                if (alreadyCounted >= count) {
                    break;
                }
            }
        }
        Evolution.info("    Total = {} damage", totalDamage);
        return entity.hurt(source, totalDamage);
    }
}
