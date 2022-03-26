package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.capabilities.modular.part.GuardPart;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.ItemMaterial;

public class ItemGuardPart extends ItemPart<PartTypes.Guard, GuardPart> {

    public ItemGuardPart(Properties properties) {
        super(properties);
    }

    @Override
    public IPart<PartTypes.Guard> createNew() {
        return new GuardPart();
    }

    @Override
    public String getCapName() {
        return "guard";
    }

    @Override
    public GuardPart getPartCap(ItemStack stack) {
        return GuardPart.get(stack);
    }

    @Override
    protected boolean isAllowedBy(PartTypes.Guard guard, ItemMaterial material) {
        return material.isAllowedBy(guard);
    }

    @Override
    protected PartTypes.Guard[] iterable() {
        return PartTypes.Guard.VALUES;
    }

    @Override
    protected void setupNewPart(GuardPart part, PartTypes.Guard guard, ItemMaterial material) {
        part.set(guard, new MaterialInstance(material));
    }
}
