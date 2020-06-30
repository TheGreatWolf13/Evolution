package tgw.evolution.network;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.Evolution;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.items.IFireAspect;
import tgw.evolution.items.IKnockback;
import tgw.evolution.items.IOffhandAttackable;
import tgw.evolution.items.ISweepAttack;

import java.util.function.Supplier;

public class PacketCSOffhandAttack extends PacketAbstract {

    public PacketCSOffhandAttack() {
        super(LogicalSide.SERVER);
    }

    public static void encode(PacketCSOffhandAttack message, PacketBuffer buffer) {
    }

    public static PacketCSOffhandAttack decode(PacketBuffer buffer) {
        return new PacketCSOffhandAttack();
    }

    public static void handle(PacketCSOffhandAttack packet, Supplier<NetworkEvent.Context> context) {
        if (EvolutionNetwork.checkSide(context, packet)) {
            context.get().enqueueWork(() -> {
                Evolution.LOGGER.debug("attacking with left hand");
                ServerPlayerEntity player = context.get().getSender();
                Item offhandItem = player.getHeldItemOffhand().getItem();
                if (offhandItem instanceof IOffhandAttackable) {
                    Evolution.LOGGER.debug("item is valid");
                    float distance = (float) ((IOffhandAttackable) offhandItem).getReach() + 5;
                    EntityRayTraceResult result = EntityEvents.rayTraceEntity(player, 1f, distance);
                    Evolution.LOGGER.debug("rayTrace = " + result);
                    if (result != null) {
                        Evolution.LOGGER.debug("entity traced = " + result.getEntity());
                        attackEntityWithOffhand(player, result.getEntity());
                    }
                }
                swingArm(player, Hand.OFF_HAND);
                EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCHandAnimation(Hand.OFF_HAND));
            });
            context.get().setPacketHandled(true);
        }
    }

    private static void swingArm(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && stack.onEntitySwing(player)) {
            return;
        }
        if (!player.isSwingInProgress || player.swingProgressInt >= getArmSwingAnimationEnd(player) / 2 || player.swingProgressInt < 0) {
            player.swingProgressInt = -1;
            player.isSwingInProgress = true;
            player.swingingHand = hand;
            if (player.world instanceof ServerWorld) {
                ((ServerWorld) player.world).getChunkProvider().sendToAllTracking(player, new SAnimateHandPacket(player, hand == Hand.MAIN_HAND ? 0 : 3));
            }
        }
    }

    private static int getArmSwingAnimationEnd(PlayerEntity player) {
        if (EffectUtils.hasMiningSpeedup(player)) {
            return 6 - (1 + EffectUtils.getMiningSpeedup(player));
        }
        return player.isPotionActive(Effects.MINING_FATIGUE) ? 6 + (1 + player.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier()) * 2 : 6;
    }

    private static void attackEntityWithOffhand(PlayerEntity player, Entity targetEntity) {
        ItemStack offhandStack = player.getHeldItemOffhand();
        Item offhandItem = player.getHeldItemOffhand().getItem();
        if (!(offhandStack.isEmpty() || !offhandItem.onLeftClickEntity(offhandStack, player, targetEntity))) {
            return;
        }
        if (targetEntity.canBeAttackedWithItem()) {
            if (!targetEntity.hitByEntity(player)) {
                float damage = (float) player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                if (offhandItem instanceof IOffhandAttackable) {
                    damage += ((IOffhandAttackable) offhandItem).getAttackDamage();
                }
                float enchantmentModifier;
                //TODO damage enchantments
                if (targetEntity instanceof LivingEntity) {
                    enchantmentModifier = EnchantmentHelper.getModifierForCreature(offhandStack, ((LivingEntity) targetEntity).getCreatureAttribute());
                }
                else {
                    enchantmentModifier = EnchantmentHelper.getModifierForCreature(offhandStack, CreatureAttribute.UNDEFINED);
                }
                if (damage > 0.0F || enchantmentModifier > 0.0F) {
                    int knockbackModifier = 0;
                    if (offhandItem instanceof IKnockback) {
                        knockbackModifier += ((IKnockback) offhandItem).getModifier();
                    }
                    boolean sprinting = false;
                    if (player.isSprinting()) {
                        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
                        ++knockbackModifier;
                        sprinting = true;
                    }
                    boolean critical = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Effects.BLINDNESS) && !player.isPassenger() && targetEntity instanceof LivingEntity;
                    critical = critical && !player.isSprinting();
                    CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, targetEntity, critical, critical ? 1.5F : 1.0F);
                    critical = hitResult != null;
                    if (critical) {
                        damage *= hitResult.getDamageModifier();
                    }
                    damage += enchantmentModifier;
                    boolean isSweepAttack = false;
                    double distanceWalked = player.distanceWalkedModified - player.prevDistanceWalkedModified;
                    if (!critical && !sprinting && player.onGround && distanceWalked < (double) player.getAIMoveSpeed()) {
                        if (offhandItem instanceof ISweepAttack) {
                            isSweepAttack = true;
                        }
                    }
                    int fireAspectModifier = 0;
                    if (offhandItem instanceof IFireAspect) {
                        fireAspectModifier = ((IFireAspect) offhandItem).getModifier();
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
                    boolean attackSuccessfull = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
                    if (attackSuccessfull) {
                        //Knockback calculations
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof LivingEntity) {
                                ((LivingEntity) targetEntity).knockBack(player, (float) knockbackModifier * 0.5F, MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)), -MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F)));
                            }
                            else {
                                targetEntity.addVelocity(-MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)) * (float) knockbackModifier * 0.5F, 0.1D, MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F)) * (float) knockbackModifier * 0.5F);
                            }
                            player.setMotion(player.getMotion().mul(0.6D, 1.0D, 0.6D));
                        }
                        //Sweep Attack
                        if (isSweepAttack) {
                            float sweepingDamage = 1.0F + ((ISweepAttack) offhandItem).getSweepRatio() * damage;
                            for (LivingEntity livingentity : player.world.getEntitiesWithinAABB(LivingEntity.class, targetEntity.getBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
                                if (livingentity != player && livingentity != targetEntity && !player.isOnSameTeam(livingentity) && (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity) livingentity).hasMarker()) && player.getDistanceSq(livingentity) < 9.0D) {
                                    livingentity.knockBack(player, 0.4F, MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)), -MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F)));
                                    //noinspection ObjectAllocationInLoop
                                    livingentity.attackEntityFrom(DamageSource.causePlayerDamage(player), sweepingDamage);
                                }
                            }
                            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
                            player.spawnSweepParticles();
                        }
                        //Calculated velocity changed
                        if (targetEntity instanceof ServerPlayerEntity && targetEntity.velocityChanged) {
                            ((ServerPlayerEntity) targetEntity).connection.sendPacket(new SEntityVelocityPacket(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.setMotion(targetMotion);
                        }
                        //Critical particles
                        if (critical) {
                            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(targetEntity);
                        }
                        //Strong attack particles
                        if (!critical && !isSweepAttack) {
                            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                        }
                        if (enchantmentModifier > 0.0F) {
                            player.onEnchantmentCritical(targetEntity);
                        }
                        player.setLastAttackedEntity(targetEntity);
                        if (targetEntity instanceof LivingEntity) {
                            //TODO thorns
                            EnchantmentHelper.applyThornEnchantments((LivingEntity) targetEntity, player);
                        }
                        //Entity parts
                        Entity entity = targetEntity;
                        if (targetEntity instanceof EnderDragonPartEntity) {
                            entity = ((EnderDragonPartEntity) targetEntity).dragon;
                        }
                        //Item damage calculation
                        if (!player.world.isRemote && !offhandStack.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = offhandStack.copy();
                            offhandStack.hitEntity((LivingEntity) entity, player);
                            if (offhandStack.isEmpty()) {
                                ForgeEventFactory.onPlayerDestroyItem(player, copy, Hand.OFF_HAND);
                                player.setHeldItem(Hand.OFF_HAND, ItemStack.EMPTY);
                            }
                        }
                        //Stats and Heart particles
                        if (targetEntity instanceof LivingEntity) {
                            float damageDealt = oldHealth - ((LivingEntity) targetEntity).getHealth();
                            player.addStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
                            if (fireAspectModifier > 0) {
                                targetEntity.setFire(fireAspectModifier * 4);
                            }
                            if (player.world instanceof ServerWorld && damageDealt >= 2.0F) {
                                int heartsToSpawn = (int) ((double) damageDealt * 0.5D);
                                ((ServerWorld) player.world).spawnParticle(ParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double) (targetEntity.getHeight() * 0.5F), targetEntity.posZ, heartsToSpawn, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }
                        player.addExhaustion(0.1F);
                    }
                    //Attack fail
                    else {
                        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);
                        if (fireAspect) {
                            targetEntity.extinguish();
                        }
                    }
                }
            }
        }
    }
}
