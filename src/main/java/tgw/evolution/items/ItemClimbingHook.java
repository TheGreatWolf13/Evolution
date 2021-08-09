package tgw.evolution.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;
import tgw.evolution.Evolution;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.entities.projectiles.EntityHook;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.MathHelper;

public class ItemClimbingHook extends ItemEv implements IThrowable {

    public ItemClimbingHook() {
        super(EvolutionItems.propMisc().maxStackSize(1));
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (handIn == Hand.OFF_HAND) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        if (playerIn.getHeldItemOffhand().getItem() != EvolutionItems.rope.get()) {
            playerIn.sendStatusMessage(EvolutionTexts.ACTION_HOOK, true);
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entityLiving;
            int charge = this.getUseDuration(stack) - timeLeft;
            if (charge < 0) {
                return;
            }
            float strength = MathHelper.getRelativeChargeStrength(charge);
            if (strength < 0.1) {
                return;
            }
            if (!world.isRemote) {
                EntityHook hook = new EntityHook(world, player);
                hook.shoot(player, player.rotationPitch, player.rotationYaw, 0.5f * strength, 1.0F);
                hook.pickupStatus = EntityGenericProjectile.PickupStatus.CREATIVE_ONLY;
                world.addEntity(hook);
                world.playMovingSound(null, hook, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                Evolution.usingPlaceholder(player, "sound");
                if (!player.abilities.isCreativeMode) {
                    player.inventory.deleteStack(stack);
                }
            }
            player.addStat(Stats.ITEM_USED.get(this));
            this.addStat(player);
        }
    }
}
