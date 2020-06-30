package tgw.evolution.items;

import tgw.evolution.inventory.SlotType;

import javax.annotation.Nonnull;

public interface IAdditionalEquipment {

    @Nonnull
    SlotType getType();
}
