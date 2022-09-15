package tgw.evolution.items;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.inventory.SlotType;

public class ItemSocks extends ItemEv implements IAdditionalEquipment, IHeatResistant, IColdResistant {

    public ItemSocks(Properties properties) {
        super(properties);
    }

    @Override
    public Reference2ObjectMap<Attribute, AttributeModifier> getAttributes(ItemStack stack) {
        Reference2ObjectMap<Attribute, AttributeModifier> map = new Reference2ObjectOpenHashMap<>();
        this.putHeatAttributes(map, stack, SlotType.FEET);
        this.putColdAttributes(map, stack, SlotType.FEET);
        return map;
    }

    @Override
    public double getColdResistance() {
        return 2;
    }

    @Nullable
    @Override
    public SoundEvent getEquipSound() {
        //TODO implementation
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    public double getHeatResistance() {
        return -2;
    }

    @Override
    public AdditionalSlotType getValidSlot() {
        return AdditionalSlotType.FEET;
    }
}
