package tgw.evolution.items;

import tgw.evolution.inventory.SlotType;

import javax.annotation.Nonnull;

public class ItemTrousers extends ItemEv implements IAdditionalEquipment {

    public ItemTrousers(Properties properties) {
        super(properties);
    }

    @Nonnull
    @Override
    public SlotType getType() {
        return SlotType.LEGS;
    }
}
