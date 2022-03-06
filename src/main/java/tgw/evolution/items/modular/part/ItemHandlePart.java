package tgw.evolution.items.modular.part;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.capabilities.modular.part.HandlePart;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.ItemMaterial;

public class ItemHandlePart extends ItemPart<PartTypes.Handle, HandlePart> {

    public ItemHandlePart(Item.Properties properties) {
        super(properties);
    }

    @Override
    public IPart<PartTypes.Handle> createNew() {
        return new HandlePart();
    }

    @Override
    public String getCapName() {
        return "handle";
    }

    @Override
    public IPart<PartTypes.Handle> getPartCap(ItemStack stack) {
        return stack.getCapability(CapabilityModular.PART).orElse(HandlePart.DUMMY);
    }

    @Override
    protected boolean isAllowedBy(PartTypes.Handle handle, ItemMaterial material) {
        return material.isAllowedBy(handle);
    }

    @Override
    protected PartTypes.Handle[] iterable() {
        return PartTypes.Handle.VALUES;
    }

    @Override
    protected void setupNewPart(HandlePart part, PartTypes.Handle handle, ItemMaterial material) {
        part.set(handle, new MaterialInstance(material));
    }
}
