package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.capabilities.modular.part.PommelPart;
import tgw.evolution.init.ItemMaterial;

public class ItemPommelPart extends ItemPart<PartTypes.Pommel, PommelPart> {

    public ItemPommelPart(Properties properties) {
        super(properties);
    }

    @Override
    public IPart<PartTypes.Pommel> createNew() {
        return new PommelPart();
    }

    @Override
    public String getCapName() {
        return "pommel";
    }

    @Override
    public IPart<PartTypes.Pommel> getPartCap(ItemStack stack) {
        return stack.getCapability(CapabilityModular.PART).orElse(PommelPart.DUMMY);
    }

    @Override
    protected boolean isAllowedBy(PartTypes.Pommel pommel, ItemMaterial material) {
        return material.isAllowedBy(pommel);
    }

    @Override
    protected PartTypes.Pommel[] iterable() {
        return PartTypes.Pommel.VALUES;
    }

    @Override
    protected void setupNewPart(PommelPart part, PartTypes.Pommel pommel, ItemMaterial material) {
        part.set(pommel, new MaterialInstance(material));
        part.sharp();
    }
}
