package tgw.evolution.client.renderer;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

public interface IItemColor extends ItemColor {

    @Override
    default int getColor(ItemStack stack, int quadIndex) {
        Evolution.deprecatedMethod();
        return this.getColor_(stack, quadIndex, null, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    int getColor_(ItemStack stack, int quadIndex, @Nullable BlockAndTintGetter level, int x, int y, int z);
}
