package tgw.evolution.items;

import net.minecraft.world.entity.player.Player;
import tgw.evolution.init.EvolutionStats;

/**
 * Used to write {@code Throwable} in the item tooltip
 */
public interface IThrowable {

    default void addStat(Player player) {
        player.awardStat(EvolutionStats.ITEMS_THROWN);
    }

    boolean isCancelable();
}
