package tgw.evolution.items;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.IFireSource;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;

import java.util.List;

public class ItemStick extends ItemBlock {

    public ItemStick(Block block, Properties builder) {
        super(block, builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_STICK_LIT);
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = level.getBlockState_(x, y, z);
        Block block = state.getBlock();
        if (block instanceof IFireSource fire && fire.isFireSource(state)) {
            player.getItemInHand(hand).shrink(1);
            ItemStack stack = ItemTorch.createStack(level, 1);
            if (!player.getInventory().add(stack)) {
                BlockUtils.dropItemStack(level, x, y, z, stack);
            }
            level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F,
                            level.random.nextFloat() * 0.7F + 0.3F);
            player.awardStat(Stats.ITEM_CRAFTED.get(EvolutionItems.TORCH));
            return InteractionResult.SUCCESS;
        }
        return super.useOn_(level, x, y, z, player, hand, hitResult);
    }
}
