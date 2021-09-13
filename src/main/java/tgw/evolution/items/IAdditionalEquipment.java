package tgw.evolution.items;

import net.minecraft.util.SoundEvent;
import tgw.evolution.inventory.SlotType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IAdditionalEquipment {

    @Nullable
    SoundEvent getEquipSound();

    @Nonnull
    SlotType getType();
}
