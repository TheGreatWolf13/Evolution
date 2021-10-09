package tgw.evolution.items;

import net.minecraft.item.ItemStack;
import tgw.evolution.util.Temperature;

public interface IItemTemperature {

    /**
     * @return The temperature of this item in Kelvin.
     */
    default double getTemperature(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getDouble("Temperature");
        }
        return 0;
    }

    /**
     * @return The black body spectrum color of this item.
     */
    default int getTemperatureColor(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt("TempColor");
        }
        return 0;
    }

    /**
     * Sets the temperature of this item in Kelvin.
     */
    default void setTemperature(ItemStack stack, double temperature) {
        stack.getOrCreateTag().putDouble("Temperature", temperature);
        stack.getTag().putInt("TempColor", Temperature.getBlackBodySpectrumColor(temperature));
    }
}
