package tgw.evolution.items;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.inventory.SlotType;

public interface IMass {

    double getMass(ItemStack stack);

    default void putMassAttributes(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, ItemStack stack, SlotType slot) {
        builder.put(EvolutionAttributes.MASS, EvolutionAttributes.massModifier(this.getMass(stack), slot));
    }
}
