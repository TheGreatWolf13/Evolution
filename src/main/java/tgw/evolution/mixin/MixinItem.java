package tgw.evolution.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchItem;

@Mixin(Item.class)
public abstract class MixinItem implements PatchItem {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        Evolution.deprecatedMethod();
        return this.canAttackBlock_(state, level, pos.getX(), pos.getY(), pos.getZ(), player);
    }

    @Override
    public boolean canAttackBlock_(BlockState state, Level level, int x, int y, int z, Player player) {
        return true;
    }

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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        Evolution.deprecatedMethod();
        return this.mineBlock_(stack, level, state, pos.getX(), pos.getY(), pos.getZ(), entity);
    }

    @Override
    public boolean mineBlock_(ItemStack stack, Level level, BlockState state, int x, int y, int z, LivingEntity entity) {
        return false;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public InteractionResult useOn(UseOnContext context) {
        Evolution.deprecatedMethod();
        BlockHitResult hitResult = context.getHitResult();
        return this.useOn_(context.getLevel(), hitResult.posX(), hitResult.posY(), hitResult.posZ(), context.getPlayer(), context.getHand(),
                           hitResult);
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }
}
