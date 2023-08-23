package tgw.evolution.client.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipMass implements ITooltip {

    private static final TooltipMass[] PARTS = new TooltipMass[4];
    private static final TooltipMass MAIN = new TooltipMass(false);

    static {
        for (int i = 0; i < PARTS.length; i++) {
            //noinspection ObjectAllocationInLoop
            TooltipMass t = new TooltipMass(true);
            PARTS[i] = t;
        }
    }

    private final boolean isPart;
    private EvolutionFormatter.Mass oldFormatter;
    private double oldMass = Double.NaN;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipMass(boolean isPart) {
        this.isPart = isPart;
    }

    public static TooltipComponent mass(double mass) {
        set(MAIN, mass);
        return MAIN;
    }

    public static TooltipComponent part(int index, double mass) {
        set(PARTS[index], mass);
        return PARTS[index];
    }

    private static void set(TooltipMass t, double mass) {
        if (t.oldMass != mass || t.oldFormatter != EvolutionConfig.MASS.get()) {
            t.oldMass = mass;
            t.oldFormatter = EvolutionConfig.MASS.get();
            t.text = EvolutionTexts.mass(mass);
        }
    }

    public static ClientTooltipComponent setup(ITooltip t) {
        if (t == MAIN) {
            return EvolutionTooltipRenderer.MASS.setTooltip(t);
        }
        for (int i = 0; i < 4; i++) {
            if (t == PARTS[i]) {
                return EvolutionTooltipRenderer.MASS_PARTS[i].setTooltip(t);
            }
        }
        throw new IllegalStateException("Should never reach here!");
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
}
