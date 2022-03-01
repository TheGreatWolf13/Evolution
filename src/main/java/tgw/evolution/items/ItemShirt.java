package tgw.evolution.items;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.inventory.AdditionalSlotType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemShirt extends ItemEv implements IAdditionalEquipment {

    public ItemShirt(Properties properties) {
        super(properties);
    }

    @Override
    public Reference2ObjectMap<Attribute, AttributeModifier> getAttributes(ItemStack stack) {
        return Reference2ObjectMaps.emptyMap();
    }

    @Nullable
    @Override
    public SoundEvent getEquipSound() {
        //TODO implementation
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Nonnull
    @Override
    public AdditionalSlotType getValidSlot() {
        return AdditionalSlotType.BODY;
    }
}
