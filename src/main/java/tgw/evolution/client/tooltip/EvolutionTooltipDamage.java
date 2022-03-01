package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionTexts;

public final class EvolutionTooltipDamage implements IEvolutionTooltip {

    public static final EvolutionTooltipDamage INSTANCE = new EvolutionTooltipDamage();
    private double oldAmount = Double.NaN;
    private EvolutionDamage.Type oldDamage;
    private Component text = EvolutionTexts.EMPTY;

    private EvolutionTooltipDamage() {
    }

    public EvolutionTooltipDamage damage(EvolutionDamage.Type damage, double amount) {
        if (amount != this.oldAmount || damage != this.oldDamage) {
            this.oldAmount = amount;
            this.oldDamage = damage;
            this.text = EvolutionTexts.damage(damage.getTranslationKey(), amount);
        }
        return this;
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
        return 247;
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
