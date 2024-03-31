package tgw.evolution.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.IWoodVariant;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.constants.WoodVariant;

public class ItemPlank extends ItemGenericBlockPlaceable implements IWoodVariant {

    private final WoodVariant variant;

    public ItemPlank(WoodVariant variant, Properties properties) {
        super(variant.get(EvolutionBlocks.PLANKS), properties);
        this.variant = variant;
    }

    @Override
    public WoodVariant woodVariant() {
        return this.variant;
    }

    @Override
    protected boolean customCondition(Block blockAtPlacing, Block blockClicking) {
        return false;
    }

    @Override
    protected @Nullable BlockState getCustomState(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return null;
    }

    @Override
    protected @Nullable BlockState getSneakingState(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return null;
    }

    @Override
    protected boolean usesCustomCondition() {
        return false;
    }

    @Override
    protected boolean usesSneakingCondition() {
        return false;
    }
}
