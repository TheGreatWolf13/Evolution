package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipHeat implements ITooltip {

    private static final TooltipHeat INSTANCE = new TooltipHeat();
    private static final Either<FormattedText, TooltipComponent> EITHER = Either.right(INSTANCE);
    private EvolutionFormatter.Temperature oldFormatter;
    private double oldHeat = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipHeat() {
    }

    public static Either<FormattedText, TooltipComponent> heat(double heat) {
        if (INSTANCE.oldHeat != heat || INSTANCE.oldFormatter != EvolutionConfig.CLIENT.bodyTemperature.get()) {
            INSTANCE.oldHeat = heat;
            INSTANCE.oldFormatter = EvolutionConfig.CLIENT.bodyTemperature.get();
            INSTANCE.text = EvolutionTexts.heatResistance(heat);
        }
        return EITHER;
    }

    @Override
    public int getIconX() {
        return 108;
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
