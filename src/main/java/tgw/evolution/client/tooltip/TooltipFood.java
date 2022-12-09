package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipFood implements ITooltip {

    private static final TooltipFood INSTANCE = new TooltipFood();
    private static final Either<FormattedText, TooltipComponent> EITHER = Either.right(INSTANCE);
    private EvolutionFormatter.Food oldFormatter;
    private int oldHunger = Integer.MIN_VALUE;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipFood() {
    }

    public static Either<FormattedText, TooltipComponent> hunger(int hunger) {
        if (hunger != INSTANCE.oldHunger || INSTANCE.oldFormatter != EvolutionConfig.CLIENT.food.get()) {
            INSTANCE.oldHunger = hunger;
            INSTANCE.oldFormatter = EvolutionConfig.CLIENT.food.get();
            INSTANCE.text = EvolutionTexts.food(hunger);
        }
        return EITHER;
    }

    @Override
    public int getIconX() {
        return 81;
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
