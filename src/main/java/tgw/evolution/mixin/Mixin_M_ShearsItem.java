package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(ShearsItem.class)
public abstract class Mixin_M_ShearsItem extends Item {

    public Mixin_M_ShearsItem(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean mineBlock_(ItemStack stack, Level level, BlockState state, int x, int y, int z, LivingEntity entity) {
        if (!level.isClientSide && !state.is(BlockTags.FIRE)) {
            stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return state.is(BlockTags.LEAVES) ||
               state.is(Blocks.COBWEB) ||
               state.is(Blocks.GRASS) ||
               state.is(Blocks.FERN) ||
               state.is(Blocks.DEAD_BUSH) ||
               state.is(Blocks.HANGING_ROOTS) ||
               state.is(Blocks.VINE) ||
               state.is(Blocks.TRIPWIRE) ||
               state.is(BlockTags.WOOL) || super.mineBlock_(stack, level, state, x, y, z, entity);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = level.getBlockState_(x, y, z);
        Block block = state.getBlock();
        if (block instanceof GrowingPlantHeadBlock plantHeadBlock) {
            if (!plantHeadBlock.isMaxAge(state)) {
                ItemStack stack = player.getItemInHand(hand);
                if (player instanceof ServerPlayer p) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger_(p, x, y, z, stack);
                }
                level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(new BlockPos(x, y, z), plantHeadBlock.getMaxAgeState(state));
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useOn_(level, x, y, z, player, hand, hitResult);
    }
}
