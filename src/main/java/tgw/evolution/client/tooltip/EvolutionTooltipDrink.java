package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class EvolutionTooltipDrink implements IEvolutionTooltip {

    public static final EvolutionTooltipDrink INSTANCE = new EvolutionTooltipDrink();
    private EvolutionFormatter.Drink oldFormat;
    private int oldThirst = Integer.MIN_VALUE;
    private Component text = EvolutionTexts.EMPTY;

    private EvolutionTooltipDrink() {
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

    public EvolutionTooltipDrink thirst(int thirst) {
        if (this.oldThirst != thirst || this.oldFormat != EvolutionConfig.CLIENT.drink.get()) {
            this.oldThirst = thirst;
            this.oldFormat = EvolutionConfig.CLIENT.drink.get();
            this.text = EvolutionTexts.drink(thirst);
        }
        return this;
    }
}
