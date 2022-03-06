package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.capabilities.modular.part.HalfHeadPart;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.ItemMaterial;

public class ItemHalfHeadPart extends ItemPart<PartTypes.HalfHead, HalfHeadPart> {

    public ItemHalfHeadPart(Properties properties) {
        super(properties);
    }

    @Override
    public IPart<PartTypes.HalfHead> createNew() {
        return new HalfHeadPart();
    }

    @Override
    public String getCapName() {
        return "halfhead";
    }

    @Override
    public IPart<PartTypes.HalfHead> getPartCap(ItemStack stack) {
        return stack.getCapability(CapabilityModular.PART).orElse(HalfHeadPart.DUMMY);
    }

    @Override
    protected boolean isAllowedBy(PartTypes.HalfHead halfHead, ItemMaterial material) {
        return material.isAllowedBy(halfHead);
    }

    @Override
    protected PartTypes.HalfHead[] iterable() {
        return PartTypes.HalfHead.VALUES;
    }

    @Override
    protected void setupNewPart(HalfHeadPart part, PartTypes.HalfHead halfHead, ItemMaterial material) {
        part.set(halfHead, new MaterialInstance(material));
        part.sharp();
    }
}
