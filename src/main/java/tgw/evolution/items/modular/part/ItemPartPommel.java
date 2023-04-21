package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartPommel;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.util.math.MathHelper;

public class ItemPartPommel extends ItemPart<PartTypes.Pommel, ItemPartPommel, PartPommel> {

    public ItemPartPommel(Properties properties) {
        super(properties);
    }

    @Override
    protected PartPommel createNew() {
        return new PartPommel();
    }

    @Override
    protected String getCapName() {
        return "pommel";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Pommel pommel = PartTypes.Pommel.getRandom(MathHelper.RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(MathHelper.RANDOM);
        while (!pommel.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(MathHelper.RANDOM);
        }
        return this.newStack(pommel, material);
    }

    @Override
    protected PartPommel getPartCap(ItemStack stack) {
        return PartPommel.get(stack);
    }

    @Override
    protected PartTypes.Pommel[] iterable() {
        return PartTypes.Pommel.VALUES;
    }
}
