package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public interface IEvolutionTooltip extends TooltipComponent {

    int getIconX();

    int getIconY();

    int getOffsetX();

    Component getText();
}
