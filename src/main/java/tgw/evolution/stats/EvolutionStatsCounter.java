package tgw.evolution.stats;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;
import tgw.evolution.Evolution;

public class EvolutionStatsCounter extends StatsCounter {

    protected final Object2LongMap<Stat<?>> statsData = Object2LongMaps.synchronize(new Object2LongOpenHashMap<>());

    public EvolutionStatsCounter() {
        this.statsData.defaultReturnValue(0);
    }

    @Override
    public int getValue(Stat<?> stat) {
        Evolution.debug("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
        return 0;
    }

    @Override
    public <T> int getValue(StatType<T> statType, T stat) {
        Evolution.debug("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
        return 0;
    }

    public <T> long getValueLong(StatType<T> statType, T stat) {
        return statType.contains(stat) ? this.getValueLong(statType.get(stat)) : 0;
    }

    public long getValueLong(Stat<?> stat) {
        return this.statsData.getLong(stat);
    }

    @Override
    public void increment(Player player, Stat<?> stat, int amount) {
        this.setValueLong(stat, this.getValueLong(stat) + amount);
    }

    public void increment(Stat<?> stat, long amount) {
        this.setValueLong(stat, this.getValueLong(stat) + amount);
    }

    @Override
    public void setValue(Player player, Stat<?> stat, int amount) {
        Evolution.debug("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
    }

    public void setValueLong(Stat<?> stat, long amount) {
        this.statsData.put(stat, amount);
    }
}
