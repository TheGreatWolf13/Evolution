package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartPole;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;

public class ItemPartPole extends ItemPart<PartTypes.Pole, ItemPartPole, PartPole> {

    public ItemPartPole(Properties properties) {
        super(properties);
    }

    @Override
    public PartPole createNew() {
        return new PartPole();
    }

    @Override
    protected String getCapName() {
        return "pole";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Pole pole = PartTypes.Pole.getRandom(RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(RANDOM);
        while (!pole.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(RANDOM);
        }
        return this.newStack(pole, material);
    }

    @Override
    protected PartPole getPartCap() {
        return new PartPole();
    }

    @Override
    protected PartTypes.Pole[] iterable() {
        return PartTypes.Pole.VALUES;
    }
}
