package tgw.evolution.patches;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface PatchTriggerInstance {

    default boolean matches_(BlockState state, ServerLevel level, int x, int y, int z, ItemStack stack) {
        throw new AbstractMethodError();
    }
}
