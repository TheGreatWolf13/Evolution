package tgw.evolution.items;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.inventory.SlotType;

import java.util.Map;

public interface IColdResistant {

    double getColdResistance();

    default void putColdAttributes(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, ItemStack stack, SlotType slot) {
        builder.put(EvolutionAttributes.COLD_RESISTANCE.get(), EvolutionAttributes.heatResistanceModifier(this.getColdResistance(), slot));
    }

    default void putColdAttributes(Map<Attribute, AttributeModifier> map, ItemStack stack, SlotType slot) {
        map.put(EvolutionAttributes.COLD_RESISTANCE.get(), EvolutionAttributes.heatResistanceModifier(this.getColdResistance(), slot));
    }
}
