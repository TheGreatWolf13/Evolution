package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartHalfHead;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.util.math.MathHelper;

public class ItemPartHalfHead extends ItemPart<PartTypes.HalfHead, ItemPartHalfHead, PartHalfHead> {

    public ItemPartHalfHead(Properties properties) {
        super(properties);
    }

    @Override
    public PartHalfHead createNew() {
        return new PartHalfHead();
    }

    @Override
    protected String getCapName() {
        return "halfhead";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.HalfHead halfHead = PartTypes.HalfHead.getRandom(MathHelper.RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(MathHelper.RANDOM);
        while (!halfHead.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(MathHelper.RANDOM);
        }
        return this.newStack(halfHead, material);
    }

    @Override
    protected PartHalfHead getPartCap() {
        return new PartHalfHead();
    }

    @Override
    protected PartTypes.HalfHead[] iterable() {
        return PartTypes.HalfHead.VALUES;
    }
}
