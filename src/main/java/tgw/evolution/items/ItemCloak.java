package tgw.evolution.items;

import tgw.evolution.inventory.SlotType;

import javax.annotation.Nonnull;

public class ItemCloak extends ItemEv implements IAdditionalEquipment {

    public ItemCloak(Properties properties) {
        super(properties);
    }

    @Nonnull
    @Override
    public SlotType getType() {
        return SlotType.CLOAK;
    }
}
