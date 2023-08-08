package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartPommel;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;

public class ItemPartPommel extends ItemPart<PartTypes.Pommel, ItemPartPommel, PartPommel> {

    public ItemPartPommel(Properties properties) {
        super(properties);
    }

    @Override
    public PartPommel createNew() {
        return new PartPommel();
    }

    @Override
    protected String getCapName() {
        return "pommel";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Pommel pommel = PartTypes.Pommel.getRandom(RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(RANDOM);
        while (!pommel.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(RANDOM);
        }
        return this.newStack(pommel, material);
    }

    @Override
    protected PartPommel getPartCap() {
        return new PartPommel();
    }

    @Override
    protected PartTypes.Pommel[] iterable() {
        return PartTypes.Pommel.VALUES;
    }
}
