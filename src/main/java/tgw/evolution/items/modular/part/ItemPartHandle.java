package tgw.evolution.items.modular.part;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartHandle;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;

public class ItemPartHandle extends ItemPart<PartTypes.Handle, ItemPartHandle, PartHandle> {

    public ItemPartHandle(Item.Properties properties) {
        super(properties);
    }

    @Override
    public PartHandle createNew() {
        return new PartHandle();
    }

    @Override
    protected String getCapName() {
        return "handle";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Handle handle = PartTypes.Handle.getRandom(RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(RANDOM);
        while (!handle.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(RANDOM);
        }
        return this.newStack(handle, material);
    }

    @Override
    protected PartHandle getPartCap() {
        return new PartHandle();
    }

    @Override
    protected PartTypes.Handle[] iterable() {
        return PartTypes.Handle.VALUES;
    }
}
