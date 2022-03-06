package tgw.evolution.items.modular;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.IModular;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.items.IDurability;
import tgw.evolution.items.IMass;
import tgw.evolution.items.ItemEv;

import java.util.function.Consumer;

public abstract class ItemModular extends ItemEv implements IDurability, IMass {

    public ItemModular(Properties builder) {
        super(builder);
    }

    @Override
    public boolean canBeDepleted() {
        return true;
    }

    public void damage(ItemStack stack, DamageCause cause) {
        if (this.canBeDepleted()) {
            this.getModularCap(stack).damage(cause);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        this.putMassAttributes(builder, stack, SlotType.byEquipment(slot));
        return builder.build();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int maxDamage = stack.getMaxDamage();
        float f = Math.max(0.0F, (maxDamage - stack.getDamageValue()) / (float) maxDamage);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - stack.getDamageValue() * 13.0F / stack.getMaxDamage());
    }

    @Override
    public int getDamage(ItemStack stack) {
        if (!this.canBeDepleted()) {
            return 0;
        }
        IModular modular = this.getModularCap(stack);
        return modular.getTotalDurabilityDmg();
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.getModularCap(stack).getDescriptionId();
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public double getMass(ItemStack stack) {
        return this.getModularCap(stack).getMass();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        if (!this.canBeDepleted()) {
            return 0;
        }
        IModular modular = this.getModularCap(stack);
        int durability = modular.getTotalMaxDurability();
        return modular.isBroken() ? durability + 1 : durability;
    }

    public abstract IModular getModularCap(ItemStack stack);

    public abstract <E extends LivingEntity> void hurtAndBreak(ItemStack stack, DamageCause cause, E entity, Consumer<E> onBroken);

    public boolean isAxe(ItemStack stack) {
        return this.getModularCap(stack).isAxe();
    }

    public abstract boolean isBroken(ItemStack stack);

    @Override
    public void setDamage(ItemStack stack, int damage) {
        //Disable vanilla logic
    }

    public enum DamageCause {
        BREAK_BLOCK,
        BREAK_BAD_BLOCK,
        HIT_BLOCK,
        HIT_ENTITY
    }
}
