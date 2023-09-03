package tgw.evolution.patches;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface PatchItem {

    default boolean canAttackBlock_(BlockState state, Level level, int x, int y, int z, Player player) {
        throw new AbstractMethodError();
    }

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return ImmutableMultimap.of();
    }

    default int getDamage(ItemStack stack) {
        return 0;
    }

    default byte getLightEmission(ItemStack stack) {
        return 0;
    }

    default int getMaxDamage(ItemStack stack) {
        return 0;
    }

    default boolean mineBlock_(ItemStack stack, Level level, BlockState state, int x, int y, int z, LivingEntity entity) {
        throw new AbstractMethodError();
    }

    default void onUsingTick(ItemStack stack, LivingEntity player, int useRemaining) {
    }

    default InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        throw new AbstractMethodError();
    }
}
