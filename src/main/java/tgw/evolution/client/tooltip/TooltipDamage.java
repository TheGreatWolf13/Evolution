package tgw.evolution.client.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipDamage implements ITooltip {

    private static final TooltipDamage BASIC = new TooltipDamage();
    private static final TooltipDamage CHARGE = new TooltipDamage();
    private static final TooltipDamage THROWN = new TooltipDamage();
    private double oldAmount = Double.NaN;
    private EvolutionDamage.Type oldDamage;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipDamage() {
    }

    public static TooltipComponent basic(EvolutionDamage.Type damage, double amount) {
        setup(BASIC, damage, amount);
        return BASIC;
    }

    public static TooltipComponent charge(EvolutionDamage.Type damage, double amount) {
        setup(CHARGE, damage, amount);
        return CHARGE;
    }

    public static ClientTooltipComponent setup(ITooltip t) {
        if (t == BASIC) {
            return EvolutionTooltipRenderer.DAMAGE_BASIC.setTooltip(t);
        }
        if (t == CHARGE) {
            return EvolutionTooltipRenderer.DAMAGE_CHARGE.setTooltip(t);
        }
        return EvolutionTooltipRenderer.DAMAGE_THROWN.setTooltip(t);
    }

    private static void setup(TooltipDamage t, EvolutionDamage.Type damage, double amount) {
        if (amount != t.oldAmount || damage != t.oldDamage) {
            t.oldAmount = amount;
            t.oldDamage = damage;
            t.text = EvolutionTexts.damage(damage.getTranslationKey(), amount);
        }
    }

    public static TooltipComponent thrown(EvolutionDamage.Type damage, double amount) {
        setup(THROWN, damage, amount);
        return THROWN;
    }

    @Override
    public int getIconX() {
        return switch (this.oldDamage) {
            case CRUSHING -> 36;
            case PIERCING -> 45;
            case SLASHING -> 27;
            default -> 54;
        };
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
