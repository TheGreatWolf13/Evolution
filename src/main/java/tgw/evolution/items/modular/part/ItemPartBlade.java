package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartBlade;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.util.math.MathHelper;

public class ItemPartBlade extends ItemPart<PartTypes.Blade, ItemPartBlade, PartBlade> {

    public ItemPartBlade(Properties properties) {
        super(properties);
    }

    @Override
    protected PartBlade createNew() {
        return new PartBlade();
    }

    @Override
    protected String getCapName() {
        return "blade";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Blade blade = PartTypes.Blade.getRandom(MathHelper.RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(MathHelper.RANDOM);
        while (!blade.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(MathHelper.RANDOM);
        }
        return this.newStack(blade, material);
    }

    @Override
    protected PartBlade getPartCap(ItemStack stack) {
        return PartBlade.get(stack);
    }

    @Override
    protected PartTypes.Blade[] iterable() {
        return PartTypes.Blade.VALUES;
    }
}
