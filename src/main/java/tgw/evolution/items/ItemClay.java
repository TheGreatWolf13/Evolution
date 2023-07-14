package tgw.evolution.items;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionCreativeTabs;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketSCOpenMoldingGui;

import java.util.List;

public class ItemClay extends ItemGenericPlaceable {

    public ItemClay() {
        super(new Properties().tab(EvolutionCreativeTabs.MISC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_CLAY_MOLD);
    }

    @Override
    public boolean customCondition(Block block) {
        return false;
    }

    @Override
    public BlockState getCustomState(BlockPlaceContext context) {
        return null;
    }

    @Override
    public BlockState getSneakingState(BlockPlaceContext context) {
        return EvolutionBlocks.MOLDING_BLOCK.defaultBlockState();
    }

    @Override
    public void sucessPlaceLogic(BlockPlaceContext context) {
        //noinspection ConstantConditions
        ((ServerPlayer) context.getPlayer()).connection.send(new PacketSCOpenMoldingGui(context.getClickedPos()));
    }
}
