package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipCooldown implements ITooltip {

    private static final TooltipCooldown INSTANCE = new TooltipCooldown();
    private static final Either<FormattedText, TooltipComponent> EITHER = Either.right(INSTANCE);
    private double oldSpeed = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipCooldown() {
    }

    public static Either<FormattedText, TooltipComponent> cooldown(double speed) {
        if (INSTANCE.oldSpeed != speed) {
            INSTANCE.oldSpeed = speed;
            INSTANCE.text = EvolutionTexts.cooldown(speed);
        }
        return EITHER;
    }

    @Override
    public int getIconX() {
        return 63;
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
