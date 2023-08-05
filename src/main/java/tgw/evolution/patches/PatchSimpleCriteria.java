package tgw.evolution.patches;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface PatchSimpleCriteria {

    default void trigger_(ServerPlayer player, int x, int y, int z, ItemStack stack) {
        throw new AbstractMethodError();
    }
}
