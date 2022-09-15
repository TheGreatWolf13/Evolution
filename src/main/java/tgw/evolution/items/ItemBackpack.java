package tgw.evolution.items;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.inventory.AdditionalSlotType;

public class ItemBackpack extends ItemEv implements IAdditionalEquipment {

    public ItemBackpack(Properties properties) {
        super(properties);
    }

    @Override
    public Reference2ObjectMap<Attribute, AttributeModifier> getAttributes(ItemStack stack) {
        return Reference2ObjectMaps.emptyMap();
    }

    @Override
    @Nullable
    public SoundEvent getEquipSound() {
        //TODO implementation
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    public AdditionalSlotType getValidSlot() {
        return AdditionalSlotType.BACK;
    }
}
