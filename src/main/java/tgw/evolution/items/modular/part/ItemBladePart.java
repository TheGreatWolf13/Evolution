package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.capabilities.modular.part.BladePart;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.ItemMaterial;

public class ItemBladePart extends ItemPart<PartTypes.Blade, BladePart> {

    public ItemBladePart(Properties properties) {
        super(properties);
    }

    @Override
    public IPart<PartTypes.Blade> createNew() {
        return new BladePart();
    }

    @Override
    public String getCapName() {
        return "blade";
    }

    @Override
    public BladePart getPartCap(ItemStack stack) {
        return BladePart.get(stack);
    }

    @Override
    protected boolean isAllowedBy(PartTypes.Blade blade, ItemMaterial material) {
        return material.isAllowedBy(blade);
    }

    @Override
    protected PartTypes.Blade[] iterable() {
        return PartTypes.Blade.VALUES;
    }

    @Override
    protected void setupNewPart(BladePart part, PartTypes.Blade blade, ItemMaterial material) {
        part.set(blade, new MaterialInstance(material));
        part.sharp();
    }
}
