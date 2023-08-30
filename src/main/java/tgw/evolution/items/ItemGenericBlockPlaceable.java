package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionStats;

public abstract class ItemGenericBlockPlaceable extends ItemBlock {

    public ItemGenericBlockPlaceable(Block block, Properties builder) {
        super(block, builder);
    }

    public abstract boolean customCondition(Block blockAtPlacing, Block blockClicking);

    public abstract @Nullable BlockState getCustomState(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult);

    public abstract BlockState getSneakingState(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult);

    @Override
    public InteractionResult place(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult, boolean canPlace) {
        if (!canPlace) {
            return InteractionResult.FAIL;
        }
        BlockState stateForPlacement = this.getPlacementState(level, x, y, z, player, hand, hitResult);
        if (player.isSecondaryUseActive()) {
            stateForPlacement = this.getSneakingState(level, x, y, z, player, hand, hitResult);
        }
        if (this.customCondition(level.getBlockState_(x, y, z).getBlock(), level.getBlockStateAtSide(x, y, z, hitResult.getDirection().getOpposite()).getBlock())) {
            stateForPlacement = this.getCustomState(level, x, y, z, player, hand, hitResult);
        }
        if (stateForPlacement == null) {
            return InteractionResult.FAIL;
        }
        if (!this.canPlace(level, x, y, z, player, stateForPlacement)) {
            return InteractionResult.FAIL;
        }
        if (!this.placeBlock(level, x, y, z, stateForPlacement)) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer p) {
            BlockState stateAtPos = level.getBlockState_(x, y, z);
            Block blockAtPos = stateAtPos.getBlock();
            if (blockAtPos == stateForPlacement.getBlock()) {
                CriteriaTriggers.PLACED_BLOCK.trigger_(p, x, y, z, stack);
                p.awardStat(EvolutionStats.BLOCK_PLACED.get(blockAtPos));
            }
        }
        if (player.isSecondaryUseActive()) {
            this.sneakingAction(level, x, y, z, player, hand, hitResult);
        }
        SoundType soundType = stateForPlacement.getSoundType();
        level.playSound(player, x + 0.5, y + 0.5, z + 0.5, this.getPlaceSound(stateForPlacement), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public void sneakingAction(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
    }
}
