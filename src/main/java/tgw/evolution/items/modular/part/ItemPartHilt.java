package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartHilt;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.util.math.MathHelper;

public class ItemPartHilt extends ItemPart<PartTypes.Hilt, ItemPartHilt, PartHilt> {

    public ItemPartHilt(Properties properties) {
        super(properties);
    }

    @Override
    protected PartHilt createNew() {
        return new PartHilt();
    }

    @Override
    protected String getCapName() {
        return "hilt";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Hilt hilt = PartTypes.Hilt.getRandom(MathHelper.RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(MathHelper.RANDOM);
        while (!hilt.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(MathHelper.RANDOM);
        }
        return this.newStack(hilt, material);
    }

    @Override
    protected PartHilt getPartCap(ItemStack stack) {
        return PartHilt.get(stack);
    }

    @Override
    protected PartTypes.Hilt[] iterable() {
        return PartTypes.Hilt.VALUES;
    }
}
