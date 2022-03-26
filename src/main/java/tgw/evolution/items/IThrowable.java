package tgw.evolution.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionStats;

/**
 * Used to write {@code Throwable} in the item tooltip
 */
public interface IThrowable extends ICancelableUse {

    default void addStat(Player player) {
        player.awardStat(EvolutionStats.ITEMS_THROWN);
    }

    default boolean isThrowable(ItemStack stack) {
        return true;
    }
}
