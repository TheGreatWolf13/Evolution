package tgw.evolution.items.modular.part;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartHead;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;

public class ItemPartHead extends ItemPart<PartTypes.Head, ItemPartHead, PartHead> {

    public ItemPartHead(Item.Properties properties) {
        super(properties);
    }

    @Override
    public PartHead createNew() {
        return new PartHead();
    }

    @Override
    protected String getCapName() {
        return "head";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Head head = PartTypes.Head.getRandom(RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(RANDOM);
        while (!head.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(RANDOM);
        }
        return this.newStack(head, material);
    }

    @Override
    protected PartHead getPartCap() {
        return new PartHead();
    }

    @Override
    protected PartTypes.Head[] iterable() {
        return PartTypes.Head.VALUES;
    }
}
