package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class EvolutionTooltipMass implements IEvolutionTooltip {

    public static final EvolutionTooltipMass MAIN = new EvolutionTooltipMass(false);
    public static final EvolutionTooltipMass[] PARTS = new EvolutionTooltipMass[4];

    static {
        for (int i = 0; i < PARTS.length; i++) {
            //noinspection ObjectAllocationInLoop
            PARTS[i] = new EvolutionTooltipMass(true);
        }
    }

    private final boolean isPart;
    private EvolutionFormatter.Mass oldFormatter;
    private double oldMass = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private EvolutionTooltipMass(boolean isPart) {
        this.isPart = isPart;
    }

    @Override
    public int getIconX() {
        return 0;
    }

    @Override
    public int getIconY() {
        return EvolutionResources.ICON_9_9;
    }

    @Override
    public int getOffsetX() {
        return this.isPart ? 24 : 12;
    }

    @Override
    public Component getText() {
        return this.text;
    }

    public EvolutionTooltipMass mass(double mass) {
        if (this.oldMass != mass || this.oldFormatter != EvolutionConfig.CLIENT.mass.get()) {
            this.oldMass = mass;
            this.oldFormatter = EvolutionConfig.CLIENT.mass.get();
            this.text = EvolutionTexts.mass(mass);
        }
        return this;
    }
}
