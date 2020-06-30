package tgw.evolution.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import tgw.evolution.entities.EntityHook;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.EvolutionStyles;

public class ItemClimbingHook extends ItemEv implements IThrowable {

    public static final ITextComponent TEXT_ACTION = new TranslationTextComponent("evolution.actionbar.hook").setStyle(EvolutionStyles.WHITE);

    public ItemClimbingHook() {
        super(EvolutionItems.propMisc().maxStackSize(1));
    }

    public static float getRelativeChargeStrength(int charge) {
        float f = charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entityLiving;
            int charge = this.getUseDuration(stack) - timeLeft;
            if (charge < 0) {
                return;
            }
            float strength = getRelativeChargeStrength(charge);
            if (strength < 0.1) {
                return;
            }
            if (!worldIn.isRemote) {
                stack.damageItem(1, player, entity -> entity.sendBreakAnimation(entityLiving.getActiveHand()));
                EntityHook hook = new EntityHook(worldIn, player);
                hook.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 0.5f * strength, 1.0F);
                hook.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                worldIn.addEntity(hook);
                worldIn.playMovingSound(null, hook, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                if (!player.abilities.isCreativeMode) {
                    player.inventory.deleteStack(stack);
                }
            }
            player.addStat(Stats.ITEM_USED.get(this));

        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (handIn == Hand.OFF_HAND) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        if (playerIn.getHeldItemOffhand().getItem() != EvolutionItems.rope.get()) {
            playerIn.sendStatusMessage(TEXT_ACTION, true);
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public boolean putEmptyLine() {
        return true;
    }

    @Override
    public int line() {
        return 2;
    }
}
