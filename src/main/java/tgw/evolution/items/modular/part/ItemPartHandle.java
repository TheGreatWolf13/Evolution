package tgw.evolution.items.modular.part;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartHandle;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.util.math.MathHelper;

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
        PartTypes.Handle handle = PartTypes.Handle.getRandom(MathHelper.RANDOM);
        EvolutionMaterials material = EvolutionMaterials.getRandom(MathHelper.RANDOM);
        while (!handle.hasVariantIn(material)) {
            material = EvolutionMaterials.getRandom(MathHelper.RANDOM);
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
