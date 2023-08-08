package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartGuard;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;

public class ItemPartGuard extends ItemPart<PartTypes.Guard, ItemPartGuard, PartGuard> {

    public ItemPartGuard(Properties properties) {
        super(properties);
    }

    @Override
    public PartGuard createNew() {
        return new PartGuard();
    }

    @Override
    protected String getCapName() {
        return "guard";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Guard guard = PartTypes.Guard.getRandom(RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(RANDOM);
        while (!guard.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(RANDOM);
        }
        return this.newStack(guard, material);
    }

    @Override
    protected PartGuard getPartCap() {
        return new PartGuard();
    }

    @Override
    protected PartTypes.Guard[] iterable() {
        return PartTypes.Guard.VALUES;
    }
}
