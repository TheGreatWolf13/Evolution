package tgw.evolution.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.util.collection.maps.R2OMap;

public interface IAdditionalEquipment {

    R2OMap<Attribute, AttributeModifier> getAttributes(ItemStack stack);

    @Nullable SoundEvent getEquipSound();

    AdditionalSlotType getValidSlot();
}
