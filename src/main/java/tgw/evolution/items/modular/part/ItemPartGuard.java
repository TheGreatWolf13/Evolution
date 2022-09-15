package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartGuard;
import tgw.evolution.capabilities.modular.part.PartTypes;

public class ItemPartGuard extends ItemPart<PartTypes.Guard, ItemPartGuard, PartGuard> {

    public ItemPartGuard(Properties properties) {
        super(properties);
    }

    @Override
    protected PartGuard createNew() {
        return new PartGuard();
    }

    @Override
    protected String getCapName() {
        return "guard";
    }

    @Override
    protected PartGuard getPartCap(ItemStack stack) {
        return PartGuard.get(stack);
    }

    @Override
    protected PartTypes.Guard[] iterable() {
        return PartTypes.Guard.VALUES;
    }
}
