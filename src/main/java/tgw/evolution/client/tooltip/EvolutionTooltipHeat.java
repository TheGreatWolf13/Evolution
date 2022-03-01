package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionTexts;

public final class EvolutionTooltipHeat implements IEvolutionTooltip {

    public static final EvolutionTooltipHeat INSTANCE = new EvolutionTooltipHeat();
    private EvolutionFormatter.Temperature oldFormatter;
    private double oldHeat = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private EvolutionTooltipHeat() {
    }

    @Override
    public int getIconX() {
        return 108;
    }

    @Override
    public int getIconY() {
        return 247;
    }

    @Override
    public int getOffsetX() {
        return 24;
    }

    @Override
    public Component getText() {
        return this.text;
    }

    public EvolutionTooltipHeat heat(double heat) {
        if (this.oldHeat != heat || this.oldFormatter != EvolutionConfig.CLIENT.bodyTemperature.get()) {
            this.oldHeat = heat;
            this.oldFormatter = EvolutionConfig.CLIENT.bodyTemperature.get();
            this.text = EvolutionTexts.heatResistance(heat);
        }
        return this;
    }
}
