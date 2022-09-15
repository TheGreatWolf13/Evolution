package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartBlade;
import tgw.evolution.capabilities.modular.part.PartTypes;

public class ItemPartBlade extends ItemPart<PartTypes.Blade, ItemPartBlade, PartBlade> {

    public ItemPartBlade(Properties properties) {
        super(properties);
    }

    @Override
    protected PartBlade createNew() {
        return new PartBlade();
    }

    @Override
    protected String getCapName() {
        return "blade";
    }

    @Override
    protected PartBlade getPartCap(ItemStack stack) {
        return PartBlade.get(stack);
    }

    @Override
    protected PartTypes.Blade[] iterable() {
        return PartTypes.Blade.VALUES;
    }
}
