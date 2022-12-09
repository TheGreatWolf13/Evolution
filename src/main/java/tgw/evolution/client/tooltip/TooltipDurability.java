package tgw.evolution.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;

public final class TooltipDurability implements ITooltip {

    private static final TooltipDurability MAIN = new TooltipDurability(false);
    private static final Either<FormattedText, TooltipComponent> EITHER_MAIN = Either.right(MAIN);
    private static final TooltipDurability[] PARTS = new TooltipDurability[4];
    private static final Either<FormattedText, TooltipComponent>[] EITHER_PARTS = new Either[PARTS.length];

    static {
        for (int i = 0; i < PARTS.length; i++) {
            //noinspection ObjectAllocationInLoop
            TooltipDurability t = new TooltipDurability(true);
            PARTS[i] = t;
            //noinspection ObjectAllocationInLoop
            EITHER_PARTS[i] = Either.right(t);
        }
    }

    private final boolean isPart;
    private String oldDurability;
    private Component text = EvolutionTexts.EMPTY;

    private TooltipDurability(boolean isPart) {
        this.isPart = isPart;
    }

    public static Either<FormattedText, TooltipComponent> durability(String durability) {
        set(MAIN, durability);
        return EITHER_MAIN;
    }

    public static Either<FormattedText, TooltipComponent> part(int index, String durability) {
        set(PARTS[index], durability);
        return EITHER_PARTS[index];
    }

    private static void set(TooltipDurability t, String durability) {
        if (!durability.equals(t.oldDurability)) {
            t.oldDurability = durability;
            t.text = EvolutionTexts.durability(durability);
        }
    }

    public static ClientTooltipComponent setup(ITooltip t) {
        if (t == MAIN) {
            return EvolutionTooltipRenderer.DURABILITY.setTooltip(t);
        }
        for (int i = 0; i < 4; i++) {
            if (t == PARTS[i]) {
                return EvolutionTooltipRenderer.DURABILITY_PARTS[i].setTooltip(t);
            }
        }
        throw new IllegalStateException("Should never reach here!");
    }

    @Override
    public int getIconX() {
        return 72;
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
