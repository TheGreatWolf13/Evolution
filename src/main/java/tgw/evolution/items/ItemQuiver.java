package tgw.evolution.items;

import tgw.evolution.inventory.SlotType;

import javax.annotation.Nonnull;

public class ItemQuiver extends ItemEv implements IAdditionalEquipment {

    public ItemQuiver(Properties properties) {
        super(properties);
    }

    @Nonnull
    @Override
    public SlotType getType() {
        return SlotType.TACTICAL;
    }
}
