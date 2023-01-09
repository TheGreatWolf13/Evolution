package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionCreativeTabs;
import tgw.evolution.util.math.MathHelper;

public class ItemFireStarter extends ItemEv implements IDurability {

    public ItemFireStarter() {
        super(new Item.Properties().tab(EvolutionCreativeTabs.MISC).durability(10));
    }

    public static boolean canSetFire(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState fireState = EvolutionBlocks.FIRE.get().getStateForPlacement(level, pos);
        return state.isAir() && fireState.canSurvive(level, pos);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (level.isClientSide) {
            return stack;
        }
        float distance = 3.0F;
        if (living instanceof Player) {
            distance = (float) living.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        }
        BlockHitResult rayTrace = MathHelper.rayTraceBlocksFromEyes(living, 1, distance, false);
        BlockPos pos = rayTrace.getBlockPos();
        BlockPos facingPos = pos.relative(rayTrace.getDirection());
        if (rayTrace.getType() == HitResult.Type.BLOCK && canSetFire(level, facingPos)) {
            if (level.random.nextInt(3) == 0) {
                level.playSound(null, facingPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);
                BlockState state = EvolutionBlocks.FIRE.get().getStateForPlacement(level, facingPos);
                level.setBlockAndUpdate(facingPos, state);
            }
            if (living instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, facingPos, stack);
                stack.hurtAndBreak(1, living, entity -> entity.broadcastBreakEvent(entity.getUsedItemHand()));
            }
            return stack;
        }
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
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
        BlockHitResult hitResult = MathHelper.rayTraceBlocksFromEyes(player, 1, distance, false);
        ((ServerLevel) player.level).sendParticles(ParticleTypes.SMOKE,
                                                   hitResult.getBlockPos().getX() + 0.5,
                                                   hitResult.getBlockPos().getY() + 1,
                                                   hitResult.getBlockPos().getZ() + 0.5,
                                                   5,
                                                   0,
                                                   0.1,
                                                   0,
                                                   0.01);
        if (count <= 8) {
            ((ServerLevel) player.level).sendParticles(ParticleTypes.LARGE_SMOKE,
                                                       hitResult.getBlockPos().getX() + 0.5,
                                                       hitResult.getBlockPos().getY() + 1,
                                                       hitResult.getBlockPos().getZ() + 0.5,
                                                       2,
                                                       0,
                                                       0.1,
                                                       0,
                                                       0.01);
        }
        if (count <= 4) {
            ((ServerLevel) player.level).sendParticles(ParticleTypes.FLAME,
                                                       hitResult.getBlockPos().getX() + 0.5,
                                                       hitResult.getBlockPos().getY() + 1,
                                                       hitResult.getBlockPos().getZ() + 0.5,
                                                       2,
                                                       0,
                                                       0.1,
                                                       0,
                                                       0.01);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        float distance = (float) player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        BlockHitResult hitResult = MathHelper.rayTraceBlocksFromEyes(player, 1, distance, false);
        if (hitResult.getType() == HitResult.Type.BLOCK && canSetFire(level, hitResult.getBlockPos().relative(hitResult.getDirection()))) {
            player.startUsingItem(hand);
            return new InteractionResultHolder<>(InteractionResult.CONSUME, player.getItemInHand(hand));
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (canSetFire(context.getLevel(), context.getClickedPos().relative(context.getClickedFace()))) {
            return InteractionResult.PASS;
        }
        return InteractionResult.FAIL;
    }
}
