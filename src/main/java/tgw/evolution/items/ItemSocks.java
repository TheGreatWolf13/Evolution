package tgw.evolution.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.maps.R2OMap;

public class ItemSocks extends ItemGeneric implements IAdditionalEquipment, IHeatResistant, IColdResistant {

    public ItemSocks(Properties properties) {
        super(properties);
    }

    @Override
    public R2OMap<Attribute, AttributeModifier> getAttributes(ItemStack stack) {
        R2OMap<Attribute, AttributeModifier> map = new R2OHashMap<>();
        this.putHeatAttributes(map, stack, SlotType.CLOTHES_FEET);
        this.putColdAttributes(map, stack, SlotType.CLOTHES_FEET);
        return map;
    }

    @Override
    public double getColdResistance() {
        return 2;
    }

    @Override
    public @Nullable SoundEvent getEquipSound() {
        //TODO implementation
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    public double getHeatResistance() {
        return -2;
    }

    @Override
    public AdditionalSlotType getValidSlot() {
        return AdditionalSlotType.CLOTHES_FEET;
    }
}
