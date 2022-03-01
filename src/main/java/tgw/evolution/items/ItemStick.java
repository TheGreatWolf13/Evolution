package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IFireSource;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;

import java.util.List;

public class ItemStick extends ItemBlock {

    public ItemStick(Block block, Properties builder) {
        super(block, builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_STICK_LIT);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFireSource && ((IFireSource) block).isFireSource(state)) {
            LevelChunk chunk = level.getChunkAt(pos);
            if (CapabilityChunkStorage.remove(chunk, EnumStorage.OXYGEN, 1)) {
                CapabilityChunkStorage.add(chunk, EnumStorage.CARBON_DIOXIDE, 1);
                Player player = context.getPlayer();
                context.getItemInHand().shrink(1);
                ItemStack stack = ItemTorch.createStack(level, 1);
                if (!player.getInventory().add(stack)) {
                    BlockUtils.dropItemStack(level, pos, stack);
                }
                level.playSound(player, pos, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, level.random.nextFloat() * 0.7F + 0.3F);
                player.awardStat(Stats.ITEM_CRAFTED.get(EvolutionItems.torch.get()));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return super.useOn(context);
    }
}
