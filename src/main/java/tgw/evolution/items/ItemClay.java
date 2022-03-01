package tgw.evolution.items;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketSCOpenMoldingGui;

import java.util.List;

public class ItemClay extends ItemGenericPlaceable {

    public ItemClay() {
        super(EvolutionItems.propMisc());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
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
        return EvolutionBlocks.MOLDING.get().defaultBlockState();
    }

    @Override
    public void sucessPlaceLogic(BlockPlaceContext context) {
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) context.getPlayer()),
                                       new PacketSCOpenMoldingGui(context.getClickedPos()));
    }
}
