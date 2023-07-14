package tgw.evolution.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchItem;

@Mixin(Item.class)
public abstract class MixinItem implements PatchItem {

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return this.getDefaultAttributeModifiers(slot);
    }

    @Override
    public int getDamage(ItemStack stack) {
        if (!stack.hasTag()) {
            return 0;
        }
        assert stack.getTag() != null;
        return stack.getTag().getInt("Damage");
    }

    @Shadow
    public abstract Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot);

    @Shadow
    public abstract int getMaxDamage();

    @Override
    public int getMaxDamage(ItemStack stack) {
        return this.getMaxDamage();
    }
}
