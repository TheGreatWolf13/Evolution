package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartPommel;
import tgw.evolution.capabilities.modular.part.PartTypes;

public class ItemPartPommel extends ItemPart<PartTypes.Pommel, ItemPartPommel, PartPommel> {

    public ItemPartPommel(Properties properties) {
        super(properties);
    }

    @Override
    protected PartPommel createNew() {
        return new PartPommel();
    }

    @Override
    protected String getCapName() {
        return "pommel";
    }

    @Override
    protected PartPommel getPartCap(ItemStack stack) {
        return PartPommel.get(stack);
    }

    @Override
    protected PartTypes.Pommel[] iterable() {
        return PartTypes.Pommel.VALUES;
    }
}
