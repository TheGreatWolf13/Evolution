package tgw.evolution.items;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import tgw.evolution.inventory.SlotType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemQuiver extends ItemEv implements IAdditionalEquipment {

    public ItemQuiver(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public SoundEvent getEquipSound() {
        //TODO implementation
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Nonnull
    @Override
    public SlotType getType() {
        return SlotType.TACTICAL;
    }
}
