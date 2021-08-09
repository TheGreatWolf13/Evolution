package tgw.evolution.items;

import net.minecraft.entity.player.PlayerEntity;
import tgw.evolution.init.EvolutionStats;

/**
 * Used to write {@code Throwable} in the item tooltip
 */
public interface IThrowable {

    default void addStat(PlayerEntity player) {
        player.addStat(EvolutionStats.ITEMS_THROWN);
    }
}
