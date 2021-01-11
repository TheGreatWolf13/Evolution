package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IFireSource;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;

import java.util.List;

public class ItemStick extends ItemBlock {

    public ItemStick(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_STICK_LIT);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFireSource && ((IFireSource) block).isFireSource(state)) {
            Chunk chunk = world.getChunkAt(pos);
            if (ChunkStorageCapability.remove(chunk, EnumStorage.OXYGEN, 1)) {
                ChunkStorageCapability.add(chunk, EnumStorage.CARBON_DIOXIDE, 1);
                PlayerEntity player = context.getPlayer();
                context.getItem().shrink(1);
                ItemStack stack = ItemTorch.createStack(world, 1);
                if (!player.inventory.addItemStackToInventory(stack)) {
                    BlockUtils.dropItemStack(world, pos, stack);
                }
                world.playSound(player, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, world.rand.nextFloat() * 0.7F + 0.3F);
                player.addStat(Stats.ITEM_CRAFTED.get(EvolutionItems.torch.get()));
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.FAIL;
        }
        return super.onItemUse(context);
    }
}
