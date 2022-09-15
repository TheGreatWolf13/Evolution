package tgw.evolution.capabilities.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.modular.part.ItemPartHandle;

public class PartHandle extends PartGrab<PartTypes.Handle, ItemPartHandle, PartHandle> {

    public static final PartHandle DUMMY = new PartHandle();

    public PartHandle() {
        super(PartTypes.Handle.NULL);
    }

    public static PartHandle get(ItemStack stack) {
        return (PartHandle) stack.getCapability(CapabilityModular.PART).orElse(DUMMY);
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.handle." + this.type.getName() + "." + this.material.getMaterial().getName();
    }

    @Override
    protected PartTypes.Handle getType(byte id) {
        return PartTypes.Handle.byId(id);
    }

    @Override
    public void init(PartTypes.Handle type, ItemMaterial material) {
        if (!material.isAllowedBy(type)) {
            throw new IllegalStateException("Material " + material + " does not allow HandleType " + type);
        }
        this.set(type, new MaterialInstance(material));
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }
}
