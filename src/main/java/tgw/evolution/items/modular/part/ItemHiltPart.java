package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.capabilities.modular.part.HiltPart;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.ItemMaterial;

public class ItemHiltPart extends ItemPart<PartTypes.Hilt, HiltPart> {

    public ItemHiltPart(Properties properties) {
        super(properties);
    }

    @Override
    public IPart<PartTypes.Hilt> createNew() {
        return new HiltPart();
    }

    @Override
    public String getCapName() {
        return "hilt";
    }

    @Override
    public IPart<PartTypes.Hilt> getPartCap(ItemStack stack) {
        return stack.getCapability(CapabilityModular.PART).orElse(HiltPart.DUMMY);
    }

    @Override
    protected boolean isAllowedBy(PartTypes.Hilt hilt, ItemMaterial material) {
        return material.isAllowedBy(hilt);
    }

    @Override
    protected PartTypes.Hilt[] iterable() {
        return PartTypes.Hilt.VALUES;
    }

    @Override
    protected void setupNewPart(HiltPart part, PartTypes.Hilt hilt, ItemMaterial material) {
        part.set(hilt, new MaterialInstance(material));
    }
}
