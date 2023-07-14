package tgw.evolution.capabilities.modular;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.util.collection.lists.EitherList;
import tgw.evolution.util.constants.HarvestLevel;

public interface IModular {

    void appendPartTooltip(CompoundTag tag, EitherList<FormattedText, TooltipComponent> tooltip);

    int getBarColor(CompoundTag tag);

    int getBarWidth(CompoundTag tag);

    String getDescriptionId(CompoundTag tag);

    @HarvestLevel int getHarvestLevel(CompoundTag tag);

    double getMass(CompoundTag tag);

    int getTotalDurabilityDmg(CompoundTag tag);

    int getTotalMaxDurability(CompoundTag tag);

    UseAnim getUseAnimation(CompoundTag tag);

    <E extends LivingEntity> void hurtAndBreak(ItemStack stack,
                                               ItemModular.DamageCause cause,
                                               E entity,
                                               @Nullable EquipmentSlot slot,
                                               @HarvestLevel int harvestLevel);

    boolean isAxe(CompoundTag tag);

    boolean isBarVisible(CompoundTag tag);

    boolean isBroken(CompoundTag tag);

    boolean isHammer(CompoundTag tag);

    boolean isShovel(CompoundTag tag);

    boolean isSword(CompoundTag tag);

    boolean isTwoHanded(CompoundTag tag);

    void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft);

    InteractionResultHolder<ItemStack> use(ItemStack stack, Level level, Player player, InteractionHand hand);
}
