package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartPole;
import tgw.evolution.capabilities.modular.part.PartTypes;

public class ItemPartPole extends ItemPart<PartTypes.Pole, ItemPartPole, PartPole> {

    public ItemPartPole(Properties properties) {
        super(properties);
    }

    @Override
    protected PartPole createNew() {
        return new PartPole();
    }

    @Override
    protected String getCapName() {
        return "pole";
    }

    @Override
    protected PartPole getPartCap(ItemStack stack) {
        return PartPole.get(stack);
    }

    @Override
    protected PartTypes.Pole[] iterable() {
        return PartTypes.Pole.VALUES;
    }
}
