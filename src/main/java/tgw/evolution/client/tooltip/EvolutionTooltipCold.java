package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class EvolutionTooltipCold implements IEvolutionTooltip {

    public static final EvolutionTooltipCold INSTANCE = new EvolutionTooltipCold();
    private double oldCold = Double.NaN;
    private EvolutionFormatter.Temperature oldFormatter;
    private Component text = EvolutionTexts.EMPTY;

    private EvolutionTooltipCold() {
    }

    public EvolutionTooltipCold cold(double cold) {
        if (this.oldCold != cold || this.oldFormatter != EvolutionConfig.CLIENT.bodyTemperature.get()) {
            this.oldCold = cold;
            this.oldFormatter = EvolutionConfig.CLIENT.bodyTemperature.get();
            this.text = EvolutionTexts.coldResistance(cold);
        }
        return this;
    }

    @Override
    public int getIconX() {
        return 99;
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
