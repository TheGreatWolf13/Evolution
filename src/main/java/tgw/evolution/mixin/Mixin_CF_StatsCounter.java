package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchStatsCounter;
import tgw.evolution.util.collection.maps.O2LHashMap;
import tgw.evolution.util.collection.maps.O2LMap;

@Mixin(StatsCounter.class)
public abstract class Mixin_CF_StatsCounter implements PatchStatsCounter {

    @Unique private final O2LMap<Stat<?>> statsData;
    @Shadow @Final @DeleteField protected Object2IntMap<Stat<?>> stats;

    @ModifyConstructor
    public Mixin_CF_StatsCounter() {
        this.statsData = new O2LHashMap<>();
        this.statsData.defaultReturnValue(0L);
    }

    @Override
    public O2LMap<Stat<?>> _getMap() {
        return this.statsData;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public int getValue(Stat<?> stat) {
        Evolution.deprecatedMethod();
        return 0;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public <T> int getValue(StatType<T> statType, T object) {
        Evolution.deprecatedMethod();
        return 0;
    }

    @Override
    public long getValue_(Stat<?> stat) {
        synchronized (this) {
            return this.statsData.getLong(stat);
        }
    }

    @Override
    public <T> long getValue_(StatType<T> statType, T stat) {
        if (statType.contains(stat)) {
            synchronized (this) {
                return this.getValue_(statType.get(stat));
            }
        }
        return 0;
    }

    @Override
    public void increment(Stat<?> stat, long amount) {
        synchronized (this) {
            this.setValue_(stat, PatchStatsCounter.properAdd(this.getValue_(stat), amount));
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void increment(Player player, Stat<?> stat, int amount) {
        synchronized (this) {
            this.setValue_(stat, PatchStatsCounter.properAdd(this.getValue_(stat), amount));
        }
    }

    @Override
    public void incrementPartial(Stat<?> stat, float amount) {
        this.increment(stat, (long) amount);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void setValue(Player player, Stat<?> stat, int i) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void setValue_(Stat<?> stat, long amount) {
        synchronized (this) {
            this.statsData.put(stat, amount);
        }
    }
}
