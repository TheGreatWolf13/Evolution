package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionCreativeTabs;
import tgw.evolution.init.EvolutionStats;

public class ItemFireStarter extends ItemGeneric implements IDurability {

    public ItemFireStarter() {
        super(new Item.Properties().tab(EvolutionCreativeTabs.MISC).durability(10));
    }

    public static boolean canSetFire(LevelReader level, int x, int y, int z) {
        return level.getBlockState_(x, y, z).isAir() && EvolutionBlocks.FIRE.getStateWithAge(level, x, y, z).canSurvive_(level, x, y, z);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (level.isClientSide) {
            return stack;
        }
        float distance = 3.0F;
        if (living instanceof Player) {
            distance = (float) living.getAttributeValue(EvolutionAttributes.REACH_DISTANCE);
        }
        BlockHitResult hitResult = (BlockHitResult) living.pick(distance, 1.0f, false);
        Direction dir = hitResult.getDirection();
        int x = hitResult.posX() + dir.getStepX();
        int y = hitResult.posY() + dir.getStepY();
        int z = hitResult.posZ() + dir.getStepZ();
        if (hitResult.getType() == HitResult.Type.BLOCK && canSetFire(level, x, y, z)) {
            if (level.random.nextInt(3) == 0) {
                level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);
                level.setBlockAndUpdate_(x, y, z, EvolutionBlocks.FIRE.getStateWithAge(level, x, y, z));
            }
            if (living instanceof ServerPlayer player) {
                CriteriaTriggers.PLACED_BLOCK.trigger_(player, x, y, z, stack);
                player.awardStat(EvolutionStats.BLOCK_PLACED.get(EvolutionBlocks.FIRE));
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
    public void onUsingTick(ItemStack stack, LivingEntity player, int useRemaining) {
        if (player.level.isClientSide) {
            return;
        }
        float distance = (float) player.getAttributeValue(EvolutionAttributes.REACH_DISTANCE);
        BlockHitResult hitResult = (BlockHitResult) player.pick(distance, 1.0f, false);
        ((ServerLevel) player.level).sendParticles(ParticleTypes.SMOKE,
                                                   hitResult.posX() + 0.5,
                                                   hitResult.posY() + 1,
                                                   hitResult.posZ() + 0.5,
                                                   5,
                                                   0,
                                                   0.1,
                                                   0,
                                                   0.01);
        if (useRemaining <= 8) {
            ((ServerLevel) player.level).sendParticles(ParticleTypes.LARGE_SMOKE,
                                                       hitResult.posX() + 0.5,
                                                       hitResult.posY() + 1,
                                                       hitResult.posZ() + 0.5,
                                                       2,
                                                       0,
                                                       0.1,
                                                       0,
                                                       0.01);
        }
        if (useRemaining <= 4) {
            ((ServerLevel) player.level).sendParticles(ParticleTypes.FLAME,
                                                       hitResult.posX() + 0.5,
                                                       hitResult.posY() + 1,
                                                       hitResult.posZ() + 0.5,
                                                       2,
                                                       0,
                                                       0.1,
                                                       0,
                                                       0.01);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        float distance = (float) player.getAttributeValue(EvolutionAttributes.REACH_DISTANCE);
        BlockHitResult hitResult = (BlockHitResult) player.pick(distance, 1.0f, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            Direction dir = hitResult.getDirection();
            if (canSetFire(level, hitResult.posX() + dir.getStepX(), hitResult.posY() + dir.getStepY(), hitResult.posZ() + dir.getStepZ())) {
                player.startUsingItem(hand);
                return new InteractionResultHolder<>(InteractionResult.CONSUME, player.getItemInHand(hand));
            }
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction face = hitResult.getDirection();
        if (canSetFire(level, x + face.getStepX(), y + face.getStepY(), z + face.getStepZ())) {
            return InteractionResult.PASS;
        }
        return InteractionResult.FAIL;
    }
}
