package tgw.evolution.items;

import tgw.evolution.inventory.SlotType;

import javax.annotation.Nonnull;

public class ItemShirt extends ItemEv implements IAdditionalEquipment {

    public ItemShirt(Properties properties) {
        super(properties);
    }

    @Nonnull
    @Override
    public SlotType getType() {
        return SlotType.BODY;
    }
}
