package tgw.evolution.hooks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import tgw.evolution.Evolution;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.util.EntityFlags;
import tgw.evolution.util.PlayerHelper;

public final class PlayerHooks {

    private PlayerHooks() {
    }

    /**
     * Hooks from {@link PlayerEntity#getHurtSound(DamageSource)}
     */
    @EvolutionHook
    public static SoundEvent getHurtSound(DamageSource source) {
        if (source == EvolutionDamage.ON_FIRE) {
            return SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE;
        }
        if (source == EvolutionDamage.DROWN) {
            return SoundEvents.ENTITY_PLAYER_HURT_DROWN;
        }
        return source == DamageSource.SWEET_BERRY_BUSH ? SoundEvents.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH : SoundEvents.ENTITY_PLAYER_HURT;
    }

    /**
     * Hooks from {@link PlayerEntity#registerAttributes()}
     */
    @EvolutionHook
    public static void registerAttributes(PlayerEntity player) {
        player.getAttributes().registerAttribute(EvolutionAttributes.MASS);
        player.getAttributes().registerAttribute(EvolutionAttributes.FRICTION);
        player.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100);
        player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.5);
        player.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(PlayerHelper.ATTACK_SPEED);
        player.getAttribute(PlayerEntity.REACH_DISTANCE).setBaseValue(PlayerHelper.REACH_DISTANCE);
        player.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(PlayerHelper.WALK_FORCE);
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
     * Hooks from {@link PlayerEntity#travel(Vec3d)}
     */
    @EvolutionHook
    public static void travel(PlayerEntity player, Vec3d direction, boolean isJumping, DataParameter<Byte> flags) {
        int jumpTicks = 0;
        if (player.world.isRemote) {
            if (player.equals(Evolution.PROXY.getClientPlayer())) {
                jumpTicks = ClientEvents.getInstance().jumpTicks;
            }
        }
        double posX = player.posX;
        double posY = player.posY;
        double posZ = player.posZ;
        if (player.isSwimming() && !player.isPassenger()) {
            double lookVecY = player.getLookVec().y;
            double d4 = lookVecY < -0.2 ? 0.085 : 0.06;
            if (lookVecY <= 0 ||
                isJumping ||
                !player.world.getBlockState(new BlockPos(player.posX, player.posY + 0.9, player.posZ)).getFluidState().isEmpty()) {
                Vec3d motion = player.getMotion();
                player.setMotion(motion.add(0, (lookVecY - motion.y) * d4, 0));
            }
        }
        if (player.abilities.isFlying && !player.isPassenger()) {
            double motionY = player.getMotion().y;
            float jumpMovementFactor = player.jumpMovementFactor;
            player.jumpMovementFactor = 4 * player.abilities.getFlySpeed() * (player.isSprinting() ? 2 : 1);
            Vec3d motion = player.getMotion();
            player.setMotion(motion.x, motionY * 0.8, motion.z);
            player.fallDistance = 0.0F;
            LivingEntityHooks.travel(player, direction, isJumping, jumpTicks, flags);
            setFlag(player, flags, EntityFlags.ELYTRA_FLYING, false);
            player.jumpMovementFactor = jumpMovementFactor;
        }
        else {
            LivingEntityHooks.travel(player, direction, isJumping, jumpTicks, flags);
        }
        player.addMovementStat(player.posX - posX, player.posY - posY, player.posZ - posZ);
    }
}