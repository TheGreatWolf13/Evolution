package tgw.evolution.client.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipDmgMultiplier implements ITooltip {

    private static final TooltipDmgMultiplier BASIC = new TooltipDmgMultiplier();
    private static final TooltipDmgMultiplier CHARGE = new TooltipDmgMultiplier();
    private static final TooltipDmgMultiplier THROWN = new TooltipDmgMultiplier();
    private double oldMult = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipDmgMultiplier() {
    }

    public static TooltipComponent basic(double mult) {
        setup(BASIC, mult);
        return BASIC;
    }

    public static TooltipComponent charge(double mult) {
        setup(CHARGE, mult);
        return CHARGE;
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

    public static TooltipComponent thrown(double mult) {
        setup(THROWN, mult);
        return THROWN;
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
