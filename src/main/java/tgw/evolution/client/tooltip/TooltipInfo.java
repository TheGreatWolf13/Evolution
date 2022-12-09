package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipInfo implements ITooltip {

    private static final TooltipInfo INSTANCE = new TooltipInfo();
    private static final Either<FormattedText, TooltipComponent> EITHER = Either.right(INSTANCE);
    private Component text = EvolutionTexts.EMPTY;

    public static Either<FormattedText, TooltipComponent> info(Component info) {
        INSTANCE.text = info;
        return EITHER;
    }

    @Override
    public int getIconX() {
        return 15 * 9;
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
