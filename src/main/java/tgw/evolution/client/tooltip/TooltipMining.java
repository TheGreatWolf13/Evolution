package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipMining implements ITooltip {

    private static final TooltipMining INSTANCE = new TooltipMining();
    private double oldMining = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipMining() {
    }

    public static TooltipComponent mining(double mining) {
        if (INSTANCE.oldMining != mining) {
            INSTANCE.oldMining = mining;
            INSTANCE.text = EvolutionTexts.mining(mining);
        }
        return INSTANCE;
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
}
