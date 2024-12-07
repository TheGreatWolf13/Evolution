package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
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
    protected boolean customCondition(BlockState oldStateAtPos) {
        return false;
    }

    @Override
    protected @Nullable BlockState getCustomState(BlockState stateAtPos) {
        return null;
    }

    @Override
    protected @Nullable BlockState getSneakingState(Player player) {
        return EvolutionBlocks.MOLDING_BLOCK.defaultBlockState();
    }

    @Override
    protected void successPlaceLogic(Level level, int x, int y, int z, Player player, ItemStack stack) {
        Evolution.deprecatedMethod();
        ((ServerPlayer) player).connection.send(new PacketSCOpenMoldingGui(BlockPos.asLong(x, y, z)));
    }

    @Override
    protected boolean usesCustomCondition() {
        return false;
    }
}
