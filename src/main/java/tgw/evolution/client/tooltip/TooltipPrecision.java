package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipPrecision implements ITooltip {

    private static final TooltipPrecision INSTANCE = new TooltipPrecision();
    private float precision = Float.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipPrecision() {
    }

    public static TooltipComponent precision(float precision) {
        if (INSTANCE.precision != precision) {
            INSTANCE.precision = precision;
            INSTANCE.text = EvolutionTexts.precision(precision);
        }
        return INSTANCE;
    }

    @Override
    public int getIconX() {
        return 13 * 9;
    }

    @Override
    public int getIconY() {
        return EvolutionResources.ICON_9_9;
    }

    @Override
    public int getOffsetX() {
        return 24;
    }

    @Override
    public Component getText() {
        return this.text;
    }
}
