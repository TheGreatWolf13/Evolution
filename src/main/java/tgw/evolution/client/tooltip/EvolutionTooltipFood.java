package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionTexts;

public final class EvolutionTooltipFood implements IEvolutionTooltip {

    public static final EvolutionTooltipFood INSTANCE = new EvolutionTooltipFood();
    private EvolutionFormatter.Food oldFormatter;
    private int oldHunger = Integer.MIN_VALUE;
    private Component text = EvolutionTexts.EMPTY;

    private EvolutionTooltipFood() {
    }

    @Override
    public int getIconX() {
        return 81;
    }

    @Override
    public int getIconY() {
        return 247;
    }

    @Override
    public int getOffsetX() {
        return 12;
    }

    @Override
    public Component getText() {
        return this.text;
    }

    public EvolutionTooltipFood hunger(int hunger) {
        if (hunger != this.oldHunger || this.oldFormatter != EvolutionConfig.CLIENT.food.get()) {
            this.oldHunger = hunger;
            this.oldFormatter = EvolutionConfig.CLIENT.food.get();
            this.text = EvolutionTexts.food(hunger);
        }
        return this;
    }
}
