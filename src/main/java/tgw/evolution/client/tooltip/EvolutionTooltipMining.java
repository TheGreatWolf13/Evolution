package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class EvolutionTooltipMining implements IEvolutionTooltip {

    public static final EvolutionTooltipMining INSTANCE = new EvolutionTooltipMining();
    private double oldMining = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private EvolutionTooltipMining() {
    }

    @Override
    public int getIconX() {
        return 9;
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

    public EvolutionTooltipMining mining(double mining) {
        if (this.oldMining != mining) {
            this.oldMining = mining;
            this.text = EvolutionTexts.mining(mining);
        }
        return this;
    }
}
