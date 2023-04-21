package tgw.evolution.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Score;
import net.minecraftforge.common.util.FakePlayer;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.stats.EvolutionServerStatsCounter;
import tgw.evolution.util.physics.SI;

import java.util.Map;

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
    public static final double WALK_FORCE = 1_000 * SI.NEWTON;
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

    public static void applyDamage(Player player, float amount, EvolutionDamage.Type type, LivingEntity entity) {
        addStat(player, EvolutionStats.DAMAGE_DEALT_BY_TYPE.get(type), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT_BY_TYPE.get(EvolutionDamage.Type.MELEE), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT_BY_TYPE.get(EvolutionDamage.Type.TOTAL), amount);
        addStat(player, EvolutionStats.DAMAGE_DEALT.get(), entity.getType(), amount);
    }

    public static void takeStat(Player player, Stat<?> stat) {
        if (player instanceof ServerPlayer serverPlayer && !(player instanceof FakePlayer)) {
            ((EvolutionServerStatsCounter) serverPlayer.getStats()).setValueLong(stat, 0);
            player.getScoreboard().forAllObjectives(stat, player.getScoreboardName(), Score::reset);
        }
    }
}
