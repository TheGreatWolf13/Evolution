package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.IRockVariant;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketSCOpenKnappingGui;
import tgw.evolution.util.constants.RockVariant;

import java.util.List;

public class ItemRock extends ItemGenericBlockPlaceable implements IRockVariant {

    private final RockVariant variant;

    public ItemRock(Block block, Properties builder, RockVariant variant) {
        super(block, builder);
        this.variant = variant;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        switch (this.variant.getRockType()) {
            case IGNEOUS_EXTRUSIVE -> tooltip.add(EvolutionTexts.TOOLTIP_ROCK_TYPE_IGEXTRUSIVE);
            case IGNEOUS_INTRUSIVE -> tooltip.add(EvolutionTexts.TOOLTIP_ROCK_TYPE_IGINTRUSIVE);
            case METAMORPHIC -> tooltip.add(EvolutionTexts.TOOLTIP_ROCK_TYPE_METAMORPHIC);
            case SEDIMENTARY -> tooltip.add(EvolutionTexts.TOOLTIP_ROCK_TYPE_SEDIMENTARY);
        }
        tooltip.add(EvolutionTexts.TOOLTIP_ROCK_KNAP);
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }

    @Override
    protected boolean customCondition(Block blockAtPlacing, Block blockClicking) {
        return blockClicking instanceof BlockKnapping;
    }

    @Override
    protected @Nullable BlockState getCustomState(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return null;
    }

    @Override
    protected BlockState getSneakingState(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return this.variant.get(EvolutionBlocks.KNAPPING_BLOCKS).defaultBlockState();
    }

    @Override
    protected void sneakingAction(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer p) {
            p.connection.send(new PacketSCOpenKnappingGui(BlockPos.asLong(x, y, z), this.variant));
        }
    }

    @Override
    protected boolean usesCustomCondition() {
        return true;
    }

    @Override
    protected boolean usesSneakingCondition() {
        return true;
    }
}
