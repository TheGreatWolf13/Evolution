package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipFollowUp implements ITooltip {

    private static final TooltipFollowUp INSTANCE = new TooltipFollowUp();
    private int oldFollowUps;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipFollowUp() {
    }

    public static TooltipComponent followUp(int followUps) {
        if (INSTANCE.oldFollowUps != followUps) {
            INSTANCE.oldFollowUps = followUps;
            INSTANCE.text = EvolutionTexts.followUp(followUps);
        }
        return INSTANCE;
    }

    @Override
    public int getIconX() {
        return 2 * 9;
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
