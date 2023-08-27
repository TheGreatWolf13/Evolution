package tgw.evolution.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
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
    public boolean customCondition(BlockState stateAtPos) {
        return stateAtPos.getBlock() == EvolutionBlocks.FIREWOOD_PILE;
    }

    @Override
    public @Nullable BlockState getCustomState(BlockState stateAtPos) {
        int count = stateAtPos.getValue(EvolutionBStates.FIREWOOD_COUNT);
        if (count < 16) {
            return stateAtPos.setValue(EvolutionBStates.FIREWOOD_COUNT, count + 1);
        }
        return null;
    }

    @Override
    public @Nullable BlockState getSneakingState(Player player) {
        return EvolutionBlocks.FIREWOOD_PILE.defaultBlockState().setValue(DIRECTION_HORIZONTAL, player.getDirection());
    }

    public WoodVariant getVariant() {
        return this.variant;
    }

    @Override
    public void successPlaceLogic(Level level, int x, int y, int z, Player player, ItemStack stack) {
        if (stack.getItem() instanceof ItemFirewood firewood) {
            if (level.getBlockEntity_(x, y, z) instanceof TEFirewoodPile tile) {
                tile.addFirewood(firewood);
            }
        }
    }
}
