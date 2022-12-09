package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipCold implements ITooltip {

    private static final TooltipCold INSTANCE = new TooltipCold();
    private static final Either<FormattedText, TooltipComponent> EITHER = Either.right(INSTANCE);
    private double oldCold = Double.NaN;
    private EvolutionFormatter.Temperature oldFormatter;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipCold() {
    }

    public static Either<FormattedText, TooltipComponent> cold(double cold) {
        if (INSTANCE.oldCold != cold || INSTANCE.oldFormatter != EvolutionConfig.CLIENT.bodyTemperature.get()) {
            INSTANCE.oldCold = cold;
            INSTANCE.oldFormatter = EvolutionConfig.CLIENT.bodyTemperature.get();
            INSTANCE.text = EvolutionTexts.coldResistance(cold);
        }
        return EITHER;
    }

    @Override
    public int getIconX() {
        return 99;
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
