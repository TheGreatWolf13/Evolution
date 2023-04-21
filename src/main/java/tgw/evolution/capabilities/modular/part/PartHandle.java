package tgw.evolution.capabilities.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.items.modular.part.ItemPartHandle;

public class PartHandle extends PartGrab<PartTypes.Handle, ItemPartHandle, PartHandle> {

    public static final PartHandle DUMMY = new PartHandle();

    public PartHandle() {
        super(PartTypes.Handle.NULL);
    }

    public static PartHandle get(ItemStack stack) {
        return (PartHandle) EvolutionCapabilities.getCapability(stack, CapabilityModular.PART, DUMMY);
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
    public void init(PartTypes.Handle type, EvolutionMaterials material) {
        if (!material.isAllowedBy(type)) {
            throw new IllegalStateException("EvolutionMaterials " + material + " does not allow HandleType " + type);
        }
        this.set(type, new MaterialInstance(material));
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isSimilar(PartHandle part) {
        if (this.type != part.type) {
            return false;
        }
        return this.material.isSimilar(part.material);
    }
}
