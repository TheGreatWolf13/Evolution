package tgw.evolution.items.modular.part;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.SerializableCapabilityProvider;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.IPartType;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.items.IDurability;
import tgw.evolution.items.IMass;
import tgw.evolution.items.ItemEv;

import java.util.List;

public abstract class ItemPart<T extends IPartType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>> extends ItemEv
        implements IDurability, IMass {

    public ItemPart(Properties properties) {
        super(properties);
    }

    @Override
    public final boolean canBeDepleted() {
        return true;
    }

    protected abstract P createNew();

    @Override
    public final void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowdedIn(tab)) {
            for (T t : this.iterable()) {
                for (EvolutionMaterials material : EvolutionMaterials.VALUES) {
                    if (t.hasVariantIn(material)) {
                        //noinspection ObjectAllocationInLoop
                        items.add(this.newStack(t, material));
                    }
                }
            }
        }
    }

    @Override
    public final Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        this.putMassAttributes(builder, stack, SlotType.byEquipment(slot));
        return builder.build();
    }

    @Override
    public final int getBarColor(ItemStack stack) {
        int maxDamage = stack.getMaxDamage();
        float f = Math.max(0.0F, (maxDamage - stack.getDamageValue()) / (float) maxDamage);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public final int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - stack.getDamageValue() * 13.0F / stack.getMaxDamage());
    }

    protected abstract String getCapName();

    @Override
    public final int getDamage(ItemStack stack) {
        if (!this.canBeDepleted()) {
            return 0;
        }
        IPart<T, I, P> part = this.getPartCap(stack);
        return part.getDurabilityDmg();
    }

    @Override
    public final String getDescriptionId(ItemStack stack) {
        return this.getPartCap(stack).getDescriptionId();
    }

    @Override
    public final int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public final double getMass(ItemStack stack) {
        return this.getPartCap(stack).getMass();
    }

    @Override
    public final int getMaxDamage(ItemStack stack) {
        if (!this.canBeDepleted()) {
            return 0;
        }
        P part = this.getPartCap(stack);
        int durability = part.getMaxDurability();
        return part.isBroken() ? durability + 1 : durability;
    }

    protected abstract P getPartCap(ItemStack stack);

    @Override
    public final @NotNull ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new SerializableCapabilityProvider<>(CapabilityModular.PART, this.createNew());
    }

    protected abstract T[] iterable();

    public final void makeTooltip(List<Either<FormattedText, TooltipComponent>> tooltip, ItemStack stack, int num) {
        P partCap = this.getPartCap(stack);
        partCap.appendText(tooltip, num);
    }

    @Contract(pure = true, value = "_, _ -> new")
    public final ItemStack newStack(T type, EvolutionMaterials material) {
        ItemStack stack = new ItemStack(this);
        this.getPartCap(stack).init(type, material);
        return stack;
    }
}
