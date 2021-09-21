package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import tgw.evolution.blocks.BlockFirewoodPile;
import tgw.evolution.blocks.tileentities.TEFirewoodPile;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.WoodVariant;

import javax.annotation.Nullable;
import java.util.List;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class ItemFirewood extends ItemGenericPlaceable {

    private final WoodVariant variant;

    public ItemFirewood(WoodVariant variant) {
        super(EvolutionItems.propTreesAndWood().stacksTo(16));
        this.variant = variant;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_FIREWOOD_PILE);
    }

    @Override
    public boolean customCondition(Block block) {
        return block instanceof BlockFirewoodPile;
    }

    @Nullable
    @Override
    public BlockState getCustomState(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        int count = state.getValue(EvolutionBStates.FIREWOOD_COUNT);
        if (count < 16) {
            return state.setValue(EvolutionBStates.FIREWOOD_COUNT, count + 1);
        }
        return null;
    }

    @Nullable
    @Override
    public BlockState getSneakingState(BlockItemUseContext context) {
        return EvolutionBlocks.FIREWOOD_PILE.get().defaultBlockState().setValue(DIRECTION_HORIZONTAL, context.getHorizontalDirection());
    }

    public WoodVariant getVariant() {
        return this.variant;
    }

    @Override
    public void sucessPlaceLogic(BlockItemUseContext context) {
        ItemStack stack = context.getItemInHand();
        Item item = stack.getItem();
        if (item instanceof ItemFirewood) {
            World world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            TEFirewoodPile tile = (TEFirewoodPile) world.getBlockEntity(pos);
            tile.addFirewood((ItemFirewood) item);
        }
    }
}
