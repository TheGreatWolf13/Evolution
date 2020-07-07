package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tgw.evolution.blocks.BlockFire;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.MathHelper;

public class ItemFireStarter extends ItemEv implements IDurability {

    public ItemFireStarter() {
        super(EvolutionItems.propMisc().maxDamage(10));
    }

    public static boolean canSetFire(IWorld worldIn, BlockPos pos) {
        BlockState state = worldIn.getBlockState(pos);
        BlockState fireState = ((BlockFire) EvolutionBlocks.FIRE.get()).getStateForPlacement(worldIn, pos);
        return state.isAir() && fireState.isValidPosition(worldIn, pos);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 16;
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        if (player.world.isRemote) {
            return;
        }
        BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromEyes(player, 1, 5, false);
        ((ServerWorld) player.world).spawnParticle(ParticleTypes.SMOKE, rayTrace.getPos().getX() + 0.5, rayTrace.getPos().getY() + 1, rayTrace.getPos().getZ() + 0.5, 5, 0, 0.1, 0, 0.01);
        if (count <= 8) {
            ((ServerWorld) player.world).spawnParticle(ParticleTypes.LARGE_SMOKE, rayTrace.getPos().getX() + 0.5, rayTrace.getPos().getY() + 1, rayTrace.getPos().getZ() + 0.5, 2, 0, 0.1, 0, 0.01);
        }
        if (count <= 4) {
            ((ServerWorld) player.world).spawnParticle(ParticleTypes.FLAME, rayTrace.getPos().getX() + 0.5, rayTrace.getPos().getY() + 1, rayTrace.getPos().getZ() + 0.5, 2, 0, 0.1, 0, 0.01);
        }
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        if (worldIn.isRemote) {
            return stack;
        }
        BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromEyes(entityLiving, 1, 5, false);
        BlockPos pos = rayTrace.getPos();
        BlockPos facingPos = pos.offset(rayTrace.getFace());
        if (canSetFire(worldIn, facingPos)) {
            if (random.nextInt(3) == 0) {
                worldIn.playSound(null, facingPos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
                BlockState state = ((BlockFire) EvolutionBlocks.FIRE.get()).getStateForPlacement(worldIn, facingPos);
                worldIn.setBlockState(facingPos, state);
            }
            if (entityLiving instanceof ServerPlayerEntity) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) entityLiving, facingPos, stack);
                stack.damageItem(1, entityLiving, entity -> entity.sendBreakAnimation(entity.getActiveHand()));
            }
            return stack;
        }
        return stack;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromEyes(playerIn, 1, 5, false);
        if (canSetFire(worldIn, rayTrace.getPos().offset(rayTrace.getFace()))) {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
        }
        return new ActionResult<>(ActionResultType.FAIL, playerIn.getHeldItem(handIn));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (canSetFire(context.getWorld(), context.getPos().offset(context.getFace()))) {
            return ActionResultType.PASS;
        }
        return ActionResultType.FAIL;
    }
}
