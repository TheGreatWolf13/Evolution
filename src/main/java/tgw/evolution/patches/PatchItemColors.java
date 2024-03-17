package tgw.evolution.patches;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.renderer.IItemColor;

public interface PatchItemColors {

    default int getColor_(ItemStack stack, int quad, @Nullable BlockAndTintGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default void register(IItemColor itemColor, ItemLike item) {
        throw new AbstractMethodError();
    }
}
