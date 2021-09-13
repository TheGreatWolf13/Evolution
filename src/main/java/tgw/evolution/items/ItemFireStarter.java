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
import net.minecraftforge.common.ForgeMod;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.MathHelper;

public class ItemFireStarter extends ItemEv implements IDurability {

    public ItemFireStarter() {
        super(EvolutionItems.propMisc().durability(10));
    }

    public static boolean canSetFire(IWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockState fireState = EvolutionBlocks.FIRE.get().getStateForPlacement(world, pos);
        return state.isAir() && fireState.canSurvive(world, pos);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity livingEntity) {
        if (world.isClientSide) {
            return stack;
        }
        float distance = 3.0F;
        if (livingEntity instanceof PlayerEntity) {
            distance = (float) livingEntity.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        }
        BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromEyes(livingEntity, 1, distance, false);
        BlockPos pos = rayTrace.getBlockPos();
        BlockPos facingPos = pos.relative(rayTrace.getDirection());
        if (rayTrace.getType() == RayTraceResult.Type.BLOCK && canSetFire(world, facingPos)) {
            if (random.nextInt(3) == 0) {
                world.playSound(null, facingPos, SoundEvents.FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
                BlockState state = EvolutionBlocks.FIRE.get().getStateForPlacement(world, facingPos);
                world.setBlockAndUpdate(facingPos, state);
            }
            if (livingEntity instanceof ServerPlayerEntity) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) livingEntity, facingPos, stack);
                stack.hurtAndBreak(1, livingEntity, entity -> entity.broadcastBreakEvent(entity.getUsedItemHand()));
            }
            return stack;
        }
        return stack;
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 16;
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        if (player.level.isClientSide) {
            return;
        }
        float distance = (float) player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromEyes(player, 1, distance, false);
        ((ServerWorld) player.level).sendParticles(ParticleTypes.SMOKE,
                                                   rayTrace.getBlockPos().getX() + 0.5,
                                                   rayTrace.getBlockPos().getY() + 1,
                                                   rayTrace.getBlockPos().getZ() + 0.5,
                                                   5,
                                                   0,
                                                   0.1,
                                                   0,
                                                   0.01);
        if (count <= 8) {
            ((ServerWorld) player.level).sendParticles(ParticleTypes.LARGE_SMOKE,
                                                       rayTrace.getBlockPos().getX() + 0.5,
                                                       rayTrace.getBlockPos().getY() + 1,
                                                       rayTrace.getBlockPos().getZ() + 0.5,
                                                       2,
                                                       0,
                                                       0.1,
                                                       0,
                                                       0.01);
        }
        if (count <= 4) {
            ((ServerWorld) player.level).sendParticles(ParticleTypes.FLAME,
                                                       rayTrace.getBlockPos().getX() + 0.5,
                                                       rayTrace.getBlockPos().getY() + 1,
                                                       rayTrace.getBlockPos().getZ() + 0.5,
                                                       2,
                                                       0,
                                                       0.1,
                                                       0,
                                                       0.01);
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        float distance = (float) player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        BlockRayTraceResult rayTrace = MathHelper.rayTraceBlocksFromEyes(player, 1, distance, false);
        if (rayTrace.getType() == RayTraceResult.Type.BLOCK && canSetFire(world, rayTrace.getBlockPos().relative(rayTrace.getDirection()))) {
            player.startUsingItem(hand);
            return new ActionResult<>(ActionResultType.SUCCESS, player.getItemInHand(hand));
        }
        return new ActionResult<>(ActionResultType.FAIL, player.getItemInHand(hand));
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (canSetFire(context.getLevel(), context.getClickedPos().relative(context.getClickedFace()))) {
            return ActionResultType.PASS;
        }
        return ActionResultType.FAIL;
    }
}
