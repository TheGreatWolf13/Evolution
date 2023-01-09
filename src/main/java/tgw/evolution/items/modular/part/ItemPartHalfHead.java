package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartHalfHead;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.Material;
import tgw.evolution.util.math.MathHelper;

public class ItemPartHalfHead extends ItemPart<PartTypes.HalfHead, ItemPartHalfHead, PartHalfHead> {

    public ItemPartHalfHead(Properties properties) {
        super(properties);
    }

    @Override
    protected PartHalfHead createNew() {
        return new PartHalfHead();
    }

    @Override
    protected String getCapName() {
        return "halfhead";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.HalfHead halfHead = PartTypes.HalfHead.getRandom(MathHelper.RANDOM);
        Material material = Material.getRandom(MathHelper.RANDOM);
        while (!halfHead.hasVariantIn(material)) {
            material = Material.getRandom(MathHelper.RANDOM);
        }
        return this.newStack(halfHead, material);
    }

    @Override
    protected PartHalfHead getPartCap(ItemStack stack) {
        return PartHalfHead.get(stack);
    }

    @Override
    protected PartTypes.HalfHead[] iterable() {
        return PartTypes.HalfHead.VALUES;
    }
}
