package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartHilt;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;

public class ItemPartHilt extends ItemPart<PartTypes.Hilt, ItemPartHilt, PartHilt> {

    public ItemPartHilt(Properties properties) {
        super(properties);
    }

    @Override
    public PartHilt createNew() {
        return new PartHilt();
    }

    @Override
    protected String getCapName() {
        return "hilt";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Hilt hilt = PartTypes.Hilt.getRandom(RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(RANDOM);
        while (!hilt.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(RANDOM);
        }
        return this.newStack(hilt, material);
    }

    @Override
    protected PartHilt getPartCap() {
        return new PartHilt();
    }

    @Override
    protected PartTypes.Hilt[] iterable() {
        return PartTypes.Hilt.VALUES;
    }
}
