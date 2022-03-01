package tgw.evolution.items;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.inventory.SlotType;

import java.util.Map;

public interface IHeatResistant {

    double getHeatResistance();

    default void putHeatAttributes(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, ItemStack stack, SlotType slot) {
        builder.put(EvolutionAttributes.HEAT_RESISTANCE.get(), EvolutionAttributes.heatResistanceModifier(this.getHeatResistance(), slot));
    }

    default void putHeatAttributes(Map<Attribute, AttributeModifier> map, ItemStack stack, SlotType slot) {
        map.put(EvolutionAttributes.HEAT_RESISTANCE.get(), EvolutionAttributes.heatResistanceModifier(this.getHeatResistance(), slot));
    }
}
