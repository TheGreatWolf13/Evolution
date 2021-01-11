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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.MathHelper;

public class ItemFireStarter extends ItemEv implements IDurability {

    public ItemFireStarter() {
        super(EvolutionItems.propMisc().maxDamage(10));
    }

    public static boolean canSetFire(IWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockState fireState = EvolutionBlocks.FIRE.get().getStateForPlacement(world, pos);
        return state.isAir() && fireState.isValidPosition(world, pos);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 16;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        float distance = (float) player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
        BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromEyes(player, 1, distance, false);
        if (rayTrace.getType() == RayTraceResult.Type.BLOCK && canSetFire(world, rayTrace.getPos().offset(rayTrace.getFace()))) {
            player.setActiveHand(hand);
            return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
        }
        return new ActionResult<>(ActionResultType.FAIL, player.getHeldItem(hand));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (canSetFire(context.getWorld(), context.getPos().offset(context.getFace()))) {
            return ActionResultType.PASS;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, LivingEntity livingEntity) {
        if (world.isRemote) {
            return stack;
        }
        float distance = 3.0F;
        if (livingEntity instanceof PlayerEntity) {
            distance = (float) livingEntity.getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
        }
        BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromEyes(livingEntity, 1, distance, false);
        BlockPos pos = rayTrace.getPos();
        BlockPos facingPos = pos.offset(rayTrace.getFace());
        if (rayTrace.getType() == RayTraceResult.Type.BLOCK && canSetFire(world, facingPos)) {
            if (random.nextInt(3) == 0) {
                world.playSound(null, facingPos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
                BlockState state = EvolutionBlocks.FIRE.get().getStateForPlacement(world, facingPos);
                world.setBlockState(facingPos, state);
            }
            if (livingEntity instanceof ServerPlayerEntity) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) livingEntity, facingPos, stack);
                stack.damageItem(1, livingEntity, entity -> entity.sendBreakAnimation(entity.getActiveHand()));
            }
            return stack;
        }
        return stack;
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        if (player.world.isRemote) {
            return;
        }
        float distance = (float) player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
        BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromEyes(player, 1, distance, false);
        ((ServerWorld) player.world).spawnParticle(ParticleTypes.SMOKE,
                                                   rayTrace.getPos().getX() + 0.5,
                                                   rayTrace.getPos().getY() + 1,
                                                   rayTrace.getPos().getZ() + 0.5,
                                                   5,
                                                   0,
                                                   0.1,
                                                   0,
                                                   0.01);
        if (count <= 8) {
            ((ServerWorld) player.world).spawnParticle(ParticleTypes.LARGE_SMOKE,
                                                       rayTrace.getPos().getX() + 0.5,
                                                       rayTrace.getPos().getY() + 1,
                                                       rayTrace.getPos().getZ() + 0.5,
                                                       2,
                                                       0,
                                                       0.1,
                                                       0,
                                                       0.01);
        }
        if (count <= 4) {
            ((ServerWorld) player.world).spawnParticle(ParticleTypes.FLAME,
                                                       rayTrace.getPos().getX() + 0.5,
                                                       rayTrace.getPos().getY() + 1,
                                                       rayTrace.getPos().getZ() + 0.5,
                                                       2,
                                                       0,
                                                       0.1,
                                                       0,
                                                       0.01);
        }
    }
}
