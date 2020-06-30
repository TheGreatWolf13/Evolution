package tgw.evolution.items;

import tgw.evolution.inventory.SlotType;

import javax.annotation.Nonnull;

public class ItemSocks extends ItemEv implements IAdditionalEquipment {

    public ItemSocks(Properties properties) {
        super(properties);
    }

    @Nonnull
    @Override
    public SlotType getType() {
        return SlotType.FEET;
    }
}
