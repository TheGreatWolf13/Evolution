package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.util.Temperature;

public interface IItemTemperature {

    /**
     * @return The temperature of this item in Kelvin.
     */
    default double getTemperature(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            return stack.getTag().getDouble("Temperature");
        }
        return 0;
    }

    /**
     * @return The black body spectrum color of this item.
     */
    default int getTemperatureColor(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            return stack.getTag().getInt("TempColor");
        }
        return 0;
    }

    /**
     * Sets the temperature of this item in Kelvin.
     */
    default void setTemperature(ItemStack stack, double temperature) {
        stack.getOrCreateTag().putDouble("Temperature", temperature);
        assert stack.getTag() != null;
        stack.getTag().putInt("TempColor", Temperature.getBlackBodySpectrumColor(temperature));
    }
}
