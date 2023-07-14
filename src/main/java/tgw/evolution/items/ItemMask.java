package tgw.evolution.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.util.collection.maps.R2OMap;

public class ItemMask extends ItemEv implements IAdditionalEquipment {

    public ItemMask(Properties properties) {
        super(properties);
    }

    @Override
    public R2OMap<Attribute, AttributeModifier> getAttributes(ItemStack stack) {
        return R2OMap.emptyMap();
    }

    @Override
    public @Nullable SoundEvent getEquipSound() {
        //TODO implementation
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    public AdditionalSlotType getValidSlot() {
        return AdditionalSlotType.FACE;
    }
}
