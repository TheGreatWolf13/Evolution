package tgw.evolution.items.modular.part;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartHandle;
import tgw.evolution.capabilities.modular.part.PartTypes;

public class ItemPartHandle extends ItemPart<PartTypes.Handle, ItemPartHandle, PartHandle> {

    public ItemPartHandle(Item.Properties properties) {
        super(properties);
    }

    @Override
    protected PartHandle createNew() {
        return new PartHandle();
    }

    @Override
    protected String getCapName() {
        return "handle";
    }

    @Override
    protected PartHandle getPartCap(ItemStack stack) {
        return PartHandle.get(stack);
    }

    @Override
    protected PartTypes.Handle[] iterable() {
        return PartTypes.Handle.VALUES;
    }
}
