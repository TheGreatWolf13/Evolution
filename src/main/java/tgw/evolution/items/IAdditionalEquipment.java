package tgw.evolution.items;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.inventory.AdditionalSlotType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IAdditionalEquipment {

    Reference2ObjectMap<Attribute, AttributeModifier> getAttributes(ItemStack stack);

    @Nullable
    SoundEvent getEquipSound();

    @Nonnull
    AdditionalSlotType getValidSlot();
}
