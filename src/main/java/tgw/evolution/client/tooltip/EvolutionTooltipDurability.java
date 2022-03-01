package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import tgw.evolution.init.EvolutionTexts;

public final class EvolutionTooltipDurability implements IEvolutionTooltip {

    public static final EvolutionTooltipDurability MAIN = new EvolutionTooltipDurability(false);
    public static final EvolutionTooltipDurability[] PARTS = new EvolutionTooltipDurability[4];

    static {
        for (int i = 0; i < PARTS.length; i++) {
            //noinspection ObjectAllocationInLoop
            PARTS[i] = new EvolutionTooltipDurability(true);
        }
    }

    private final boolean isPart;
    private String oldDurability;
    private Component text = EvolutionTexts.EMPTY;

    private EvolutionTooltipDurability(boolean isPart) {
        this.isPart = isPart;
    }

    public EvolutionTooltipDurability durability(String durability) {
        if (!durability.equals(this.oldDurability)) {
            this.oldDurability = durability;
            this.text = EvolutionTexts.durability(durability);
        }
        return this;
    }

    @Override
    public int getIconX() {
        return 72;
    }

    @Override
    public int getIconY() {
        return 247;
    }

    @Override
    public int getOffsetX() {
        return this.isPart ? 24 : 12;
    }

    @Override
    public Component getText() {
        return this.text;
    }
}
