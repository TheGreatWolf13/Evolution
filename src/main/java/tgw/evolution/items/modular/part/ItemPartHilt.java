package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartHilt;
import tgw.evolution.capabilities.modular.part.PartTypes;

public class ItemPartHilt extends ItemPart<PartTypes.Hilt, ItemPartHilt, PartHilt> {

    public ItemPartHilt(Properties properties) {
        super(properties);
    }

    @Override
    protected PartHilt createNew() {
        return new PartHilt();
    }

    @Override
    protected String getCapName() {
        return "hilt";
    }

    @Override
    protected PartHilt getPartCap(ItemStack stack) {
        return PartHilt.get(stack);
    }

    @Override
    protected PartTypes.Hilt[] iterable() {
        return PartTypes.Hilt.VALUES;
    }
}
