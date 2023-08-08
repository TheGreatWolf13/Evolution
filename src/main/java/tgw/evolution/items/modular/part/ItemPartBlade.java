package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartBlade;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;

public class ItemPartBlade extends ItemPart<PartTypes.Blade, ItemPartBlade, PartBlade> {

    public ItemPartBlade(Properties properties) {
        super(properties);
    }

    @Override
    public PartBlade createNew() {
        return new PartBlade();
    }

    @Override
    protected String getCapName() {
        return "blade";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Blade blade = PartTypes.Blade.getRandom(RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(RANDOM);
        while (!blade.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(RANDOM);
        }
        return this.newStack(blade, material);
    }

    @Override
    protected PartBlade getPartCap() {
        return new PartBlade();
    }

    @Override
    protected PartTypes.Blade[] iterable() {
        return PartTypes.Blade.VALUES;
    }
}
