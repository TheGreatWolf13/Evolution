package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipMining implements ITooltip {

    private static final TooltipMining INSTANCE = new TooltipMining();
    private static final Either<FormattedText, TooltipComponent> EITHER = Either.right(INSTANCE);
    private double oldMining = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipMining() {
    }

    public static Either<FormattedText, TooltipComponent> mining(double mining) {
        if (INSTANCE.oldMining != mining) {
            INSTANCE.oldMining = mining;
            INSTANCE.text = EvolutionTexts.mining(mining);
        }
        return EITHER;
    }

    @Override
    public int getIconX() {
        return 9;
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
