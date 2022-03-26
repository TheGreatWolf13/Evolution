package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.capabilities.modular.part.PolePart;
import tgw.evolution.init.ItemMaterial;

public class ItemPolePart extends ItemPart<PartTypes.Pole, PolePart> {

    public ItemPolePart(Properties properties) {
        super(properties);
    }

    @Override
    public IPart<PartTypes.Pole> createNew() {
        return new PolePart();
    }

    @Override
    public String getCapName() {
        return "pole";
    }

    @Override
    public PolePart getPartCap(ItemStack stack) {
        return PolePart.get(stack);
    }

    @Override
    protected boolean isAllowedBy(PartTypes.Pole pole, ItemMaterial material) {
        return material.isAllowedBy(pole);
    }

    @Override
    protected PartTypes.Pole[] iterable() {
        return PartTypes.Pole.VALUES;
    }

    @Override
    protected void setupNewPart(PolePart part, PartTypes.Pole pole, ItemMaterial material) {
        part.set(pole, new MaterialInstance(material));
    }
}
