package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartPole;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.Material;
import tgw.evolution.util.math.MathHelper;

public class ItemPartPole extends ItemPart<PartTypes.Pole, ItemPartPole, PartPole> {

    public ItemPartPole(Properties properties) {
        super(properties);
    }

    @Override
    protected PartPole createNew() {
        return new PartPole();
    }

    @Override
    protected String getCapName() {
        return "pole";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Pole pole = PartTypes.Pole.getRandom(MathHelper.RANDOM);
        Material material = Material.getRandom(MathHelper.RANDOM);
        while (!pole.hasVariantIn(material)) {
            material = Material.getRandom(MathHelper.RANDOM);
        }
        return this.newStack(pole, material);
    }

    @Override
    protected PartPole getPartCap(ItemStack stack) {
        return PartPole.get(stack);
    }

    @Override
    protected PartTypes.Pole[] iterable() {
        return PartTypes.Pole.VALUES;
    }
}
