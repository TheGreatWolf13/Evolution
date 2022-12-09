package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipThrowSpeed implements ITooltip {

    private static final TooltipThrowSpeed INSTANCE = new TooltipThrowSpeed();
    private static final Either<FormattedText, TooltipComponent> EITHER = Either.right(INSTANCE);
    private EvolutionFormatter.Speed oldFormatter;
    private double oldSpeed = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipThrowSpeed() {
    }

    public static Either<FormattedText, TooltipComponent> throwSpeed(double speed) {
        if (INSTANCE.oldSpeed != speed || INSTANCE.oldFormatter != EvolutionConfig.CLIENT.speed.get()) {
            INSTANCE.oldSpeed = speed;
            INSTANCE.oldFormatter = EvolutionConfig.CLIENT.speed.get();
            INSTANCE.text = EvolutionTexts.throwSpeed(speed);
        }
        return EITHER;
    }

    @Override
    public int getIconX() {
        return 14 * 9;
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
