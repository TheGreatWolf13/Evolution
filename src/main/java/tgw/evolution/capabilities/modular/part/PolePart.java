package tgw.evolution.capabilities.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;

public class PolePart extends GrabPart<PartTypes.Pole> {

    public static final PolePart DUMMY = new PolePart();

    public PolePart() {
        this.type = PartTypes.Pole.NULL;
    }

    public static PolePart get(ItemStack stack) {
        return (PolePart) stack.getCapability(CapabilityModular.PART).orElse(DUMMY);
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.pole." + this.type.getName() + "." + this.material.getMaterial().getName();
    }

    @Override
    protected PartTypes.Pole getType(String type) {
        return PartTypes.Pole.byName(type);
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }
}
