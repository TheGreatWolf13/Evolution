package tgw.evolution.hooks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.scoreboard.Score;
import net.minecraft.server.management.PlayerList;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.FakePlayer;
import tgw.evolution.Evolution;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.stats.EvolutionServerStatisticsManager;
import tgw.evolution.util.EntityFlags;
import tgw.evolution.util.PlayerHelper;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;
import java.util.UUID;

public final class PlayerHooks {

    private PlayerHooks() {
    }

    /**
     * Hooks from {@link PlayerEntity#addMountedMovementStat(double, double, double)}, replacing the method.
     */
    @EvolutionHook
    public static void addMountedMovementStat(PlayerEntity player, double dx, double dy, double dz) {
        if (player instanceof ServerPlayerEntity && player.isPassenger()) {
            float dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 1_000;
            if (dist > 0) {
                addStat(player, EvolutionStats.TOTAL_DISTANCE_TRAVELED, dist);
            }
        }
    }

    /**
     * Hooks from {@link PlayerEntity#addMovementStat(double, double, double)}, replacing the method.
     */
    @EvolutionHook
    public static void addMovementStat(PlayerEntity player, double dx, double dy, double dz) {
        if (!player.isPassenger() && player instanceof ServerPlayerEntity) {
            float dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 1_000;
            if (dist > 0) {
                addStat(player, EvolutionStats.TOTAL_DISTANCE_TRAVELED, dist);
            }
            if (player.isSwimming()) {
                if (dist > 0) {
                    addStat(player, EvolutionStats.DISTANCE_SWUM, dist);
                }
            }
            else if (player.areEyesInFluid(FluidTags.WATER, true)) {
                if (dist > 0) {
                    addStat(player, EvolutionStats.DISTANCE_WALKED_UNDER_WATER, dist);
                }
            }
            else if (player.isInWater()) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    addStat(player, EvolutionStats.DISTANCE_WALKED_ON_WATER, horizontalDist);
                }
            }
            else if (player.isOnLadder()) {
                if (dy > 0) {
                    addStat(player, EvolutionStats.DISTANCE_CLIMBED, (float) (dy * 1_000));
                }
            }
            else if (player.onGround) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    if (player.isSprinting()) {
                        addStat(player, EvolutionStats.DISTANCE_SPRINTED, horizontalDist);
                    }
                    else if (player.isSneaking()) {
                        addStat(player, EvolutionStats.DISTANCE_CROUCHED, horizontalDist);
                    }
                    else if (player.getPose() == Pose.SWIMMING) {
                        addStat(player, EvolutionStats.DISTANCE_PRONE, horizontalDist);
                    }
                    else {
                        addStat(player, EvolutionStats.DISTANCE_WALKED, horizontalDist);
                    }
                }
            }
            else if (player.abilities.isFlying) {
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    addStat(player, EvolutionStats.DISTANCE_FLOWN, horizontalDist);
                }
            }
            else {
                if (dy < 0) {
                    addStat(player, EvolutionStats.DISTANCE_FALLEN, (float) (-dy * 1_000));
                }
                else if (dy > 0) {
                    addStat(player, EvolutionStats.DISTANCE_JUMPED_VERTICAL, (float) (dy * 1_000));
                }
                float horizontalDist = MathHelper.sqrt(dx * dx + dz * dz) * 1_000;
                if (horizontalDist > 0) {
                    addStat(player, EvolutionStats.DISTANCE_JUMPED_HORIZONTAL, horizontalDist);
                }
            }
        }
    }

    public static void addStat(PlayerEntity player, @Nonnull ResourceLocation resLoc, float amount) {
        addStat(player, Stats.CUSTOM, resLoc, amount);
    }

    public static <T> void addStat(PlayerEntity player, @Nonnull StatType<T> statType, @Nonnull T type, float amount) {
        if (player instanceof ServerPlayerEntity && !(player instanceof FakePlayer)) {
            Stat<T> stat = statType.get(type);
            EvolutionServerStatisticsManager stats = (EvolutionServerStatisticsManager) ((ServerPlayerEntity) player).getStats();
            stats.incrementPartial(stat, amount);
            player.getWorldScoreboard()
                  .forAllObjectives(stat, player.getScoreboardName(), score -> score.setScorePoints((int) stats.getValueLong(stat)));
        }
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
     * Hooks from {@link net.minecraft.server.management.PlayerList#getPlayerStats(PlayerEntity)}, replacing the method.
     */
    @EvolutionHook
    public static ServerStatisticsManager getPlayerStats(PlayerEntity player, PlayerList list, Map<UUID, ServerStatisticsManager> playerStatFiles) {
        UUID uuid = player.getUniqueID();
        ServerStatisticsManager statisticsManager = playerStatFiles.get(uuid);
        if (statisticsManager == null) {
            File statsFolder = new File(list.getServer().func_71218_a(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), "stats");
            File statsFile = new File(statsFolder, uuid + ".json");
            if (!statsFile.exists()) {
                File statsFileByName = new File(statsFolder, player.getName().getString() + ".json");
                if (statsFileByName.exists() && statsFileByName.isFile()) {
                    //noinspection ResultOfMethodCallIgnored
                    statsFileByName.renameTo(statsFile);
                }
            }
            statisticsManager = new EvolutionServerStatisticsManager(list.getServer(), statsFile);
            playerStatFiles.put(uuid, statisticsManager);
        }
        return statisticsManager;
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

    public static void takeStat(PlayerEntity player, Stat<?> stat) {
        if (player instanceof ServerPlayerEntity && !(player instanceof FakePlayer)) {
            ((EvolutionServerStatisticsManager) ((ServerPlayerEntity) player).getStats()).setValueLong(stat, 0);
            player.getWorldScoreboard().forAllObjectives(stat, player.getScoreboardName(), Score::reset);
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
    }
}