package tgw.evolution.stats;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatisticsManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.Evolution;

public class EvolutionStatisticsManager extends StatisticsManager {

    protected final Object2LongMap<Stat<?>> statsData = Object2LongMaps.synchronize(new Object2LongOpenHashMap<>());

    public EvolutionStatisticsManager() {
        this.statsData.defaultReturnValue(0);
    }

    @Override
    public int getValue(Stat<?> stat) {
        Evolution.LOGGER.debug("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T> int getValue(StatType<T> statType, T stat) {
        Evolution.LOGGER.debug("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public <T> long getValueLong(StatType<T> statType, T stat) {
        return statType.contains(stat) ? this.getValueLong(statType.get(stat)) : 0;
    }

    public long getValueLong(Stat<?> stat) {
        return this.statsData.getLong(stat);
    }

    @Override
    public void increment(PlayerEntity player, Stat<?> stat, int amount) {
        this.setValueLong(stat, this.getValueLong(stat) + amount);
    }

    public void increment(Stat<?> stat, long amount) {
        this.setValueLong(stat, this.getValueLong(stat) + amount);
    }

    @Override
    public void setValue(PlayerEntity player, Stat<?> stat, int amount) {
        Evolution.LOGGER.debug("Wrong stats method, called by {}", Thread.currentThread().getStackTrace()[2]);
    }

    public void setValueLong(Stat<?> stat, long amount) {
        this.statsData.put(stat, amount);
    }
}
