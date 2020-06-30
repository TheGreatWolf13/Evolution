package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import tgw.evolution.blocks.BlockMolding;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.EvolutionStyles;

import java.util.List;

public class ItemClay extends ItemPlaceable {

    public ItemClay() {
        super(EvolutionItems.propMisc());
    }

    @Override
    public BlockState getSneakingState(BlockItemUseContext context) {
        return EvolutionBlocks.MOLDING.get().getDefaultState();
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        String text = "evolution.tooltip.clay.mold";
        tooltip.add(new TranslationTextComponent(text).setStyle(EvolutionStyles.INFO));
    }

    @Override
    public boolean customCondition(Block block) {
        return block instanceof BlockMolding;
    }

    @Override
    public BlockState getCustomState(BlockItemUseContext context) {
        return EvolutionBlocks.MOLDING.get().getStateForPlacement(context);
    }

    @Override
    public void sucessPlaceLogic(BlockItemUseContext context) {
        BlockState state = context.getWorld().getBlockState(context.getPos());
        if (state.get(BlockMolding.LAYERS) > 1) {
            TEMolding tile = (TEMolding) context.getWorld().getTileEntity(context.getPos());
            tile.addLayer(state.get(BlockMolding.LAYERS) - 1);
        }
    }
}
