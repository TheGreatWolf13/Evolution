package tgw.evolution.items;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionStats;

public interface IThrowable extends ICancelableUse, IMelee, IProjectile {

    default void addStat(Player player) {
        player.awardStat(EvolutionStats.ITEMS_THROWN);
    }

    @Override
    default Component getCancelMessage(Component key) {
        return new TranslatableComponent("evolution.actionbar.cancelThrow", key);
    }

    default boolean isThrowable(ItemStack stack, LivingEntity entity) {
        return true;
    }
}
