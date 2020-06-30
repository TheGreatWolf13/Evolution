package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import tgw.evolution.blocks.BlockTorch;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.EvolutionStyles;

import java.util.List;

public class ItemStick extends ItemBlock {

    public ItemStick(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockState state = context.getWorld().getBlockState(context.getPos());
        if (state.getBlock() instanceof BlockTorch && state.get(BlockTorch.LIT)) {
            boolean[] bool = {false};
            ChunkStorageCapability.getChunkStorage(context.getWorld().getChunkAt(context.getPos())).map(chunkStorage -> {
                if (chunkStorage.getElementStored(EnumStorage.OXYGEN) > 0) {
                    bool[0] = true;
                    chunkStorage.removeElement(EnumStorage.OXYGEN, 1);
                    chunkStorage.addElement(EnumStorage.CARBON_DIOXIDE, 1);
                }
                return true;
            }).orElseGet(() -> false);
            if (bool[0]) {
                PlayerEntity player = context.getPlayer();
                context.getItem().shrink(1);
                ItemStack stack = new ItemStack(EvolutionItems.torch.get());
                if (!player.inventory.addItemStackToInventory(stack)) {
                    BlockUtils.dropItemStack(context.getWorld(), context.getPos(), stack);
                }
                context.getWorld().playSound(player, context.getPos(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, context.getWorld().rand.nextFloat() * 0.7F + 0.3F);
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.FAIL;
        }
        return super.onItemUse(context);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        String text = "evolution.tooltip.stick.lit";
        tooltip.add(new TranslationTextComponent(text).setStyle(EvolutionStyles.INFO));
    }
}
