package tgw.evolution.capabilities.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.items.modular.part.ItemPartHilt;

public class PartHilt extends PartGrab<PartTypes.Hilt, ItemPartHilt, PartHilt> {

    public static final PartHilt DUMMY = new PartHilt();

    public PartHilt() {
        super(PartTypes.Hilt.NULL);
    }

    public static PartHilt get(ItemStack stack) {
        return (PartHilt) EvolutionCapabilities.getCapability(stack, CapabilityModular.PART, DUMMY);
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.hilt." + this.type.getName() + "." + this.material.getMaterial().getName();
    }

    @Override
    protected PartTypes.Hilt getType(byte id) {
        return PartTypes.Hilt.byId(id);
    }

    @Override
    public void init(PartTypes.Hilt type, EvolutionMaterials material) {
        if (!material.isAllowedBy(type)) {
            throw new IllegalStateException("EvolutionMaterials " + material + " does not allow HiltType " + type);
        }
        this.set(type, new MaterialInstance(material));
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isSimilar(PartHilt part) {
        if (this.type != part.type) {
            return false;
        }
        return this.material.isSimilar(part.material);
    }
}
