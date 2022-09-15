package tgw.evolution.items;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.inventory.AdditionalSlotType;

public interface IAdditionalEquipment {

    Reference2ObjectMap<Attribute, AttributeModifier> getAttributes(ItemStack stack);

    @Nullable
    SoundEvent getEquipSound();

    AdditionalSlotType getValidSlot();
}
