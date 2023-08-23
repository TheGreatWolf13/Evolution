package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipDrink implements ITooltip {

    private static final TooltipDrink INSTANCE = new TooltipDrink();
    private EvolutionFormatter.Drink oldFormat;
    private int oldThirst = Integer.MIN_VALUE;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipDrink() {
    }

    public static TooltipComponent thirst(int thirst) {
        if (INSTANCE.oldThirst != thirst || INSTANCE.oldFormat != EvolutionConfig.DRINK.get()) {
            INSTANCE.oldThirst = thirst;
            INSTANCE.oldFormat = EvolutionConfig.DRINK.get();
            INSTANCE.text = EvolutionTexts.drink(thirst);
        }
        return INSTANCE;
    }

    @Override
    public int getIconX() {
        return 90;
    }

    @Override
    public int getIconY() {
        return EvolutionResources.ICON_9_9;
    }

    @Override
    public int getOffsetX() {
        return 12;
    }

    @Override
    public Component getText() {
        return this.text;
    }
}
