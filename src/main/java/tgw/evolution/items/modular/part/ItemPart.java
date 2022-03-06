package tgw.evolution.items.modular.part;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.IPartType;
import tgw.evolution.init.ItemMaterial;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.items.IDurability;
import tgw.evolution.items.IMass;
import tgw.evolution.items.ItemEv;

public abstract class ItemPart<T extends IPartType<T>, P extends IPart<T>> extends ItemEv implements IDurability, IMass {

    public ItemPart(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canBeDepleted() {
        return true;
    }

    public abstract IPart<T> createNew();

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowdedIn(tab)) {
            for (T t : this.iterable()) {
                for (ItemMaterial material : ItemMaterial.VALUES) {
                    if (this.isAllowedBy(t, material)) {
                        //noinspection ObjectAllocationInLoop
                        ItemStack stack = new ItemStack(this);
                        P part = (P) this.getPartCap(stack);
                        this.setupNewPart(part, t, material);
                        items.add(stack);
                    }
                }
            }
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

    public abstract String getCapName();

    @Override
    public int getDamage(ItemStack stack) {
        if (!this.canBeDepleted()) {
            return 0;
        }
        IPart<T> part = this.getPartCap(stack);
        return part.getDurabilityDmg();
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.getPartCap(stack).getDescriptionId();
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public double getMass(ItemStack stack) {
        return this.getPartCap(stack).getMass();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        if (!this.canBeDepleted()) {
            return 0;
        }
        P part = (P) this.getPartCap(stack);
        int durability = part.getMaxDurability();
        return part.isBroken() ? durability + 1 : durability;
    }

    public abstract IPart<T> getPartCap(ItemStack stack);

    protected abstract boolean isAllowedBy(T t, ItemMaterial material);

    protected abstract T[] iterable();

    protected abstract void setupNewPart(P part, T t, ItemMaterial material);
}
