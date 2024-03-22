package tgw.evolution.items.modular;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.modular.IModular;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.items.IDurability;
import tgw.evolution.items.IMass;
import tgw.evolution.items.ItemGeneric;
import tgw.evolution.patches.PatchItem;
import tgw.evolution.util.collection.lists.custom.EitherList;
import tgw.evolution.util.constants.HarvestLevel;

public abstract class ItemModular<M extends IModular> extends ItemGeneric implements IDurability, IMass, PatchItem {

    public ItemModular(Properties builder) {
        super(builder);
    }

    protected static boolean verifyStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.hasTag();
    }

    @Override
    public boolean canBeDepleted() {
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        this.putMassAttributes(builder, stack, SlotType.byEquipment(slot));
        return builder.build();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (!verifyStack(stack)) {
            return 0;
        }
        //noinspection ConstantConditions
        return this.getModular().getBarColor(stack.getTag());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (!verifyStack(stack)) {
            return 0;
        }
        //noinspection ConstantConditions
        return this.getModular().getBarWidth(stack.getTag());
    }

    @Override
    public int getDamage(ItemStack stack) {
        if (!this.canBeDepleted()) {
            return 0;
        }
        if (!verifyStack(stack)) {
            return 0;
        }
        //noinspection ConstantConditions
        return this.getModular().getTotalDurabilityDmg(stack.getTag());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if (!verifyStack(stack)) {
            return "null";
        }
        //noinspection ConstantConditions
        return this.getModular().getDescriptionId(stack.getTag());
    }

    @Override
    public double getMass(ItemStack stack) {
        if (!verifyStack(stack)) {
            return 0;
        }
        //noinspection ConstantConditions
        return this.getModular().getMass(stack.getTag());
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        if (!verifyStack(stack)) {
            return 0;
        }
        //noinspection ConstantConditions
        return this.getModular().getTotalMaxDurability(stack.getTag());
    }

    protected abstract M getModular();

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (!verifyStack(stack)) {
            return UseAnim.NONE;
        }
        //noinspection ConstantConditions
        return this.getModular().getUseAnimation(stack.getTag());
    }

    public final <E extends LivingEntity> void hurtAndBreak(ItemStack stack,
                                                            DamageCause cause,
                                                            E entity,
                                                            @Nullable EquipmentSlot slot,
                                                            @HarvestLevel int harvestLevel) {
        if (!verifyStack(stack)) {
            return;
        }
        this.getModular().hurtAndBreak(stack, cause, entity, slot, harvestLevel);
    }

    public boolean isAxe(ItemStack stack) {
        if (!verifyStack(stack)) {
            return false;
        }
        //noinspection ConstantConditions
        return this.getModular().isAxe(stack.getTag());
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (!verifyStack(stack)) {
            return false;
        }
        //noinspection ConstantConditions
        return this.getModular().isBarVisible(stack.getTag());
    }

    public boolean isHammer(ItemStack stack) {
        if (!verifyStack(stack)) {
            return false;
        }
        //noinspection ConstantConditions
        return this.getModular().isHammer(stack.getTag());
    }

    public boolean isShovel(ItemStack stack) {
        if (!verifyStack(stack)) {
            return false;
        }
        //noinspection ConstantConditions
        return this.getModular().isShovel(stack.getTag());
    }

    public void makeTooltip(EitherList<FormattedText, TooltipComponent> tooltip, ItemStack stack) {
        if (!verifyStack(stack)) {
            return;
        }
        //noinspection ConstantConditions
        this.getModular().appendPartTooltip(stack.getTag(), tooltip);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!verifyStack(stack)) {
            return;
        }
        this.getModular().releaseUsing(stack, level, entity, timeLeft);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!verifyStack(stack)) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        return this.getModular().use(stack, level, player, hand);
    }

    @Override
    public boolean usesModularRendering() {
        return true;
    }

    public enum DamageCause {
        BREAK_BLOCK,
        BREAK_BAD_BLOCK,
        HIT_BLOCK,
        HIT_ENTITY
    }
}
