package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
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
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_CLAY_MOLD);
    }

    @Override
    public boolean customCondition(Block block) {
        return false;
    }

    @Override
    public BlockState getCustomState(BlockItemUseContext context) {
        return null;
    }

    @Override
    public BlockState getSneakingState(BlockItemUseContext context) {
        return EvolutionBlocks.MOLDING.get().defaultBlockState();
    }

    @Override
    public void sucessPlaceLogic(BlockItemUseContext context) {
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) context.getPlayer()),
                                       new PacketSCOpenMoldingGui(context.getClickedPos()));
    }
}
