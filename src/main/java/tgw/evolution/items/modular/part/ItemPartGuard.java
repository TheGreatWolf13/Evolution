package tgw.evolution.items.modular.part;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartGuard;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.util.math.MathHelper;

public class ItemPartGuard extends ItemPart<PartTypes.Guard, ItemPartGuard, PartGuard> {

    public ItemPartGuard(Properties properties) {
        super(properties);
    }

    @Override
    protected PartGuard createNew() {
        return new PartGuard();
    }

    @Override
    protected String getCapName() {
        return "guard";
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Guard guard = PartTypes.Guard.getRandom(MathHelper.RANDOM);
        ItemMaterial material = ItemMaterial.getRandom(MathHelper.RANDOM);
        while (!guard.hasVariantIn(material)) {
            material = ItemMaterial.getRandom(MathHelper.RANDOM);
        }
        return this.newStack(guard, material);
    }

    @Override
    protected PartGuard getPartCap(ItemStack stack) {
        return PartGuard.get(stack);
    }

    @Override
    protected PartTypes.Guard[] iterable() {
        return PartTypes.Guard.VALUES;
    }
}
