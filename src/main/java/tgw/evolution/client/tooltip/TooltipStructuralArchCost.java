package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public class TooltipStructuralArchCost implements ITooltip {

    private static final TooltipStructuralArchCost INSTANCE = new TooltipStructuralArchCost();
    private int oldMax;
    private int oldMin;
    private Component text = EvolutionTexts.structuralArchCost(0, 0);

    public static TooltipComponent setup(int min, int max) {
        if (INSTANCE.oldMin != min || INSTANCE.oldMax != max) {
            INSTANCE.oldMin = min;
            INSTANCE.oldMax = max;
            INSTANCE.text = EvolutionTexts.structuralArchCost(min, max);
        }
        return INSTANCE;
    }

    @Override
    public int getIconX() {
        return 162;
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
