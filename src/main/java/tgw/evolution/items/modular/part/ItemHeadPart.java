package tgw.evolution.items.modular.part;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.capabilities.modular.part.HeadPart;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.ItemMaterial;

public class ItemHeadPart extends ItemPart<PartTypes.Head, HeadPart> {

    public ItemHeadPart(Item.Properties properties) {
        super(properties);
    }

    @Override
    public IPart<PartTypes.Head> createNew() {
        return new HeadPart();
    }

    @Override
    public String getCapName() {
        return "head";
    }

    @Override
    public IPart<PartTypes.Head> getPartCap(ItemStack stack) {
        return stack.getCapability(CapabilityModular.PART).orElse(HeadPart.DUMMY);
    }

    @Override
    protected boolean isAllowedBy(PartTypes.Head head, ItemMaterial material) {
        return material.isAllowedBy(head);
    }

    @Override
    protected PartTypes.Head[] iterable() {
        return PartTypes.Head.VALUES;
    }

    @Override
    protected void setupNewPart(HeadPart part, PartTypes.Head head, ItemMaterial material) {
        part.set(head, new MaterialInstance(material));
        part.sharp();
    }
}
