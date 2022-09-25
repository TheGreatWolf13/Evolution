package tgw.evolution.capabilities.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.MaterialInstance;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.items.modular.part.ItemPartPole;

public class PartPole extends PartGrab<PartTypes.Pole, ItemPartPole, PartPole> {

    public static final PartPole DUMMY = new PartPole();

    public PartPole() {
        super(PartTypes.Pole.NULL);
    }

    public static PartPole get(ItemStack stack) {
        return (PartPole) EvolutionCapabilities.getCapability(stack, CapabilityModular.PART, DUMMY);
    }

    @Override
    public String getDescriptionId() {
        return "item.evolution.part.pole." + this.type.getName() + "." + this.material.getMaterial().getName();
    }

    @Override
    protected PartTypes.Pole getType(byte id) {
        return PartTypes.Pole.byId(id);
    }

    @Override
    public void init(PartTypes.Pole type, ItemMaterial material) {
        if (!material.isAllowedBy(type)) {
            throw new IllegalStateException("Material " + material + " does not allow PoleType " + type);
        }
        this.set(type, new MaterialInstance(material));
    }

    @Override
    public boolean isBroken() {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isSimilar(PartPole part) {
        if (this.type != part.type) {
            return false;
        }
        return this.material.isSimilar(part.material);
    }
}
