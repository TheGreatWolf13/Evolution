package tgw.evolution.patches;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public interface PatchItem {

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return ImmutableMultimap.of();
    }

    default int getDamage(ItemStack stack) {
        return 0;
    }

    default int getMaxDamage(ItemStack stack) {
        return 0;
    }

    default void onUsingTick(ItemStack stack, LivingEntity player, int useRemaining) {
    }
}
