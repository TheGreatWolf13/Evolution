package tgw.evolution.capabilities.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;

public class HandlePart extends GrabPart<PartTypes.Handle> {

    public static final HandlePart DUMMY = new HandlePart();

    public HandlePart() {
        this.type = PartTypes.Handle.NULL;
    }

    public static HandlePart get(ItemStack stack) {
        return (HandlePart) stack.getCapability(CapabilityModular.PART).orElse(DUMMY);
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.handle." + this.type.getName() + "." + this.material.getMaterial().getName();
    }

    @Override
    protected PartTypes.Handle getType(String type) {
        return PartTypes.Handle.byName(type);
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }
}
