package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.PartTypes;

import javax.annotation.Nullable;

public class ItemPart extends ItemEv {

    public ItemPart(Properties properties) {
        super(properties);
    }

    @Nullable
    public PartTypes getPartType(ItemStack stack) {
        return null;
    }
}
