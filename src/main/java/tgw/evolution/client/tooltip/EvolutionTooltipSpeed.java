package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import tgw.evolution.init.EvolutionTexts;

public final class EvolutionTooltipSpeed implements IEvolutionTooltip {

    public static final EvolutionTooltipSpeed INSTANCE = new EvolutionTooltipSpeed();
    private double oldSpeed = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private EvolutionTooltipSpeed() {
    }

    @Override
    public int getIconX() {
        return 63;
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

    public EvolutionTooltipSpeed speed(double speed) {
        if (this.oldSpeed != speed) {
            this.oldSpeed = speed;
            this.text = EvolutionTexts.speed(speed);
        }
        return this;
    }
}
