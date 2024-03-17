package tgw.evolution.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import tgw.evolution.blocks.IStructural;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.collection.sets.RSet;

public final class TooltipStructureType implements ITooltip {

    private static final TooltipStructureType INSTANCE = new TooltipStructureType();
    private Component text = EvolutionTexts.structuralType(RSet.emptySet());

    public static TooltipComponent setup(RSet<IStructural.BeamType> set) {
        INSTANCE.text = EvolutionTexts.structuralType(set);
        return INSTANCE;
    }

    @Override
    public int getIconX() {
        return 153;
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
