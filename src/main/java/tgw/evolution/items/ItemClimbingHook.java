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
        super(EvolutionItems.propMisc().stacksTo(1));
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity entityLiving, int timeLeft) {
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
            if (!world.isClientSide) {
                EntityHook hook = new EntityHook(world, player);
                hook.shoot(player, player.xRot, player.yRot, 0.5f * strength, 1.0F);
                hook.pickupStatus = EntityGenericProjectile.PickupStatus.CREATIVE_ONLY;
                world.addFreshEntity(hook);
                world.playSound(null, hook, SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                Evolution.usingPlaceholder(player, "sound");
                if (!player.abilities.instabuild) {
                    player.inventory.removeItem(stack);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            this.addStat(player);
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == Hand.OFF_HAND) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        if (player.getOffhandItem().getItem() != EvolutionItems.rope.get()) {
            player.displayClientMessage(EvolutionTexts.ACTION_HOOK, true);
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        player.startUsingItem(hand);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}
