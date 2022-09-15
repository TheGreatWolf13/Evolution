package tgw.evolution.items.modular.part;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartHead;
import tgw.evolution.capabilities.modular.part.PartTypes;

public class ItemPartHead extends ItemPart<PartTypes.Head, ItemPartHead, PartHead> {

    public ItemPartHead(Item.Properties properties) {
        super(properties);
    }

    @Override
    protected PartHead createNew() {
        return new PartHead();
    }

    @Override
    protected String getCapName() {
        return "head";
    }

    @Override
    protected PartHead getPartCap(ItemStack stack) {
        return PartHead.get(stack);
    }

    @Override
    protected PartTypes.Head[] iterable() {
        return PartTypes.Head.VALUES;
    }
}
