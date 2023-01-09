package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.BlockFirewoodPile;
import tgw.evolution.blocks.tileentities.TEFirewoodPile;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionCreativeTabs;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.constants.WoodVariant;

import java.util.List;

import static tgw.evolution.init.EvolutionBStates.DIRECTION_HORIZONTAL;

public class ItemFirewood extends ItemGenericPlaceable {

    private final WoodVariant variant;

    public ItemFirewood(WoodVariant variant) {
        super(new Properties().tab(EvolutionCreativeTabs.TREES_AND_WOOD).stacksTo(16));
        this.variant = variant;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_FIREWOOD_PILE);
    }

    @Override
    public boolean customCondition(Block block) {
        return block instanceof BlockFirewoodPile;
    }

    @Nullable
    @Override
    public BlockState getCustomState(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        int count = state.getValue(EvolutionBStates.FIREWOOD_COUNT);
        if (count < 16) {
            return state.setValue(EvolutionBStates.FIREWOOD_COUNT, count + 1);
        }
        return null;
    }

    @Nullable
    @Override
    public BlockState getSneakingState(BlockPlaceContext context) {
        return EvolutionBlocks.FIREWOOD_PILE.get().defaultBlockState().setValue(DIRECTION_HORIZONTAL, context.getHorizontalDirection());
    }

    public WoodVariant getVariant() {
        return this.variant;
    }

    @Override
    public void sucessPlaceLogic(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        Item item = stack.getItem();
        if (item instanceof ItemFirewood firewood) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            TEFirewoodPile tile = (TEFirewoodPile) level.getBlockEntity(pos);
            tile.addFirewood(firewood);
        }
    }
}
