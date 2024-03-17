package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipStructuralIntegrity implements ITooltip {

    private static final TooltipStructuralIntegrity INSTANCE = new TooltipStructuralIntegrity();
    private int oldIntegrityMax;
    private int oldIntegrityMin;
    private Component text = EvolutionTexts.structuralIntegrity(0, 0);

    public static TooltipComponent setup(int min, int max) {
        if (INSTANCE.oldIntegrityMin != min || INSTANCE.oldIntegrityMax != max) {
            INSTANCE.oldIntegrityMin = min;
            INSTANCE.oldIntegrityMax = max;
            INSTANCE.text = EvolutionTexts.structuralIntegrity(min, max);
        }
        return INSTANCE;
    }

    @Override
    public int getIconX() {
        return 144;
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
