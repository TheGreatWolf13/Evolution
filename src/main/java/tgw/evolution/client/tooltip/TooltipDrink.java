package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipDrink implements ITooltip {

    private static final TooltipDrink INSTANCE = new TooltipDrink();
    private static final Either<FormattedText, TooltipComponent> EITHER = Either.right(INSTANCE);
    private EvolutionFormatter.Drink oldFormat;
    private int oldThirst = Integer.MIN_VALUE;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipDrink() {
    }

    public static Either<FormattedText, TooltipComponent> thirst(int thirst) {
        if (INSTANCE.oldThirst != thirst || INSTANCE.oldFormat != EvolutionConfig.CLIENT.drink.get()) {
            INSTANCE.oldThirst = thirst;
            INSTANCE.oldFormat = EvolutionConfig.CLIENT.drink.get();
            INSTANCE.text = EvolutionTexts.drink(thirst);
        }
        return EITHER;
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
