package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipFollowUp implements ITooltip {

    private static final TooltipFollowUp INSTANCE = new TooltipFollowUp();
    private static final Either<FormattedText, TooltipComponent> EITHER = Either.right(INSTANCE);
    private int oldFollowUps;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipFollowUp() {
    }

    public static Either<FormattedText, TooltipComponent> followUp(int followUps) {
        if (INSTANCE.oldFollowUps != followUps) {
            INSTANCE.oldFollowUps = followUps;
            INSTANCE.text = EvolutionTexts.followUp(followUps);
        }
        return EITHER;
    }

    @Override
    public int getIconX() {
        return 2 * 9;
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
