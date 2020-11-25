package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.entities.projectiles.EntityTorch;
import tgw.evolution.util.MathHelper;

public class ItemTorch extends ItemWallOrFloor implements IFireAspect, IThrowable {

    public ItemTorch(Block floorBlock, Block wallBlockIn, Properties propertiesIn) {
        super(floorBlock, wallBlockIn, propertiesIn);
    }

    @Override
    public int getModifier() {
        return 2;
    }

    @Override
    public float getChance() {
        return 0.2f;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (handIn == Hand.OFF_HAND) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
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
            float strength = MathHelper.getRelativeChargeStrength(charge);
            if (strength < 0.1) {
                return;
            }
            if (!worldIn.isRemote) {
                EntityTorch torch = new EntityTorch(worldIn, player);
                torch.shoot(player, player.rotationPitch, player.rotationYaw, 0.6f * strength, 1.0F);
                torch.pickupStatus = EntityGenericProjectile.PickupStatus.CREATIVE_ONLY;
                worldIn.addEntity(torch);
                worldIn.playMovingSound(null, torch, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                if (!player.abilities.isCreativeMode) {
                    stack.shrink(1);
                }
            }
            player.addStat(Stats.ITEM_USED.get(this));
        }
    }
}
