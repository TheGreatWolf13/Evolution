package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class EvolutionTooltipReach implements IEvolutionTooltip {

    public static final EvolutionTooltipReach INSTANCE = new EvolutionTooltipReach();
    private double oldReach = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private EvolutionTooltipReach() {
    }

    @Override
    public int getIconX() {
        return 18;
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

    public EvolutionTooltipReach reach(double reach) {
        if (this.oldReach != reach) {
            this.oldReach = reach;
            this.text = EvolutionTexts.reach(reach);
        }
        return this;
    }
}
