package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipDmgMultiplier implements ITooltip {

    private static final TooltipDmgMultiplier BASIC = new TooltipDmgMultiplier();
    private static final TooltipDmgMultiplier CHARGE = new TooltipDmgMultiplier();
    private static final TooltipDmgMultiplier THROWN = new TooltipDmgMultiplier();
    private static final Either<FormattedText, TooltipComponent> EITHER_BASIC = Either.right(BASIC);
    private static final Either<FormattedText, TooltipComponent> EITHER_CHARGE = Either.right(CHARGE);
    private static final Either<FormattedText, TooltipComponent> EITHER_THROWN = Either.right(THROWN);
    private double oldMult = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipDmgMultiplier() {
    }

    public static Either<FormattedText, TooltipComponent> basic(double mult) {
        setup(BASIC, mult);
        return EITHER_BASIC;
    }

    public static Either<FormattedText, TooltipComponent> charge(double mult) {
        setup(CHARGE, mult);
        return EITHER_CHARGE;
    }

    private static void setup(TooltipDmgMultiplier t, double mult) {
        if (t.oldMult != mult) {
            t.oldMult = mult;
            t.text = EvolutionTexts.dmgMultiplier(mult);
        }
    }

    public static ClientTooltipComponent setup(ITooltip t) {
        if (t == BASIC) {
            return EvolutionTooltipRenderer.DMG_MULT_BASIC.setTooltip(t);
        }
        if (t == CHARGE) {
            return EvolutionTooltipRenderer.DMG_MULT_CHARGE.setTooltip(t);
        }
        return EvolutionTooltipRenderer.DMG_MULT_THROWN.setTooltip(t);
    }

    public static Either<FormattedText, TooltipComponent> thrown(double mult) {
        setup(THROWN, mult);
        return EITHER_THROWN;
    }

    @Override
    public int getIconX() {
        return 6 * 9;
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
