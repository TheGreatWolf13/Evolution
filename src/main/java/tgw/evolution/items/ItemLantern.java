package tgw.evolution.items;

import net.minecraft.world.item.ItemStack;

public class ItemLantern extends ItemEv {

    private final short lightColor;

    public ItemLantern(int lightColor, Properties properties) {
        super(properties);
        this.lightColor = (short) lightColor;
    }

    @Override
    public short getLightEmission(ItemStack stack) {
        return this.lightColor;
    }
}
