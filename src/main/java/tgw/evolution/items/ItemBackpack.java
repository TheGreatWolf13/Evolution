package tgw.evolution.items;

import tgw.evolution.inventory.SlotType;

import javax.annotation.Nonnull;

public class ItemBackpack extends ItemEv implements IAdditionalEquipment {

    public ItemBackpack(Properties properties) {
        super(properties);
    }

    @Nonnull
    @Override
    public SlotType getType() {
        return SlotType.BACK;
    }
}
