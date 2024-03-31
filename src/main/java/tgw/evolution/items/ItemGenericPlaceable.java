package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.util.constants.BlockFlags;

public abstract class ItemGenericPlaceable extends ItemGeneric {

    protected ItemGenericPlaceable(Properties properties) {
        super(properties);
    }

    protected static boolean canPlace(LevelAccessor level, int x, int y, int z, Player player, BlockState stateToPlace) {
        return stateToPlace.canSurvive_(level, x, y, z) && level.isUnobstructed_(stateToPlace, x, y, z, player);
    }

    protected static SoundEvent getPlaceSound(BlockState state) {
        return state.getSoundType().getPlaceSound();
    }

    protected static boolean placeBlock(LevelAccessor level, int x, int y, int z, BlockState state) {
        return level.setBlock_(x, y, z, state, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.RENDER_MAINTHREAD);
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult placeResult;
        if (level.getBlockState_(x, y, z).canBeReplaced_(level, x, y, z, player, hand, hitResult)) {
            placeResult = this.place(level, x, y, z, player, hand, hitResult, true);
        }
        else {
            Direction dir = hitResult.getDirection();
            int offX = x + dir.getStepX();
            int offY = y + dir.getStepY();
            int offZ = z + dir.getStepZ();
            placeResult = this.place(level, offX, offY, offZ, player, hand, hitResult, level.getBlockState_(offX, offY, offZ).canBeReplaced_(level, offX, offY, offZ, player, hand, hitResult));
        }
        if (!placeResult.consumesAction() && this.isEdible()) {
            InteractionResult secondaryResult = this.use(level, player, hand).getResult();
            return secondaryResult == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : secondaryResult;
        }
        return placeResult;
    }

    protected abstract boolean customCondition(BlockState oldStateAtPos);

    protected abstract @Nullable BlockState getCustomState(BlockState stateAtPos);

    protected abstract @Nullable BlockState getSneakingState(Player player);

    protected InteractionResult place(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult, boolean canPlace) {
        if (!canPlace) {
            return InteractionResult.FAIL;
        }
        BlockState stateForPlacement = null;
        if (player.isSecondaryUseActive()) {
            stateForPlacement = this.getSneakingState(player);
        }
        if (this.usesCustomCondition()) {
            BlockState oldStateAtPos = level.getBlockState_(x, y, z);
            if (this.customCondition(oldStateAtPos)) {
                stateForPlacement = this.getCustomState(oldStateAtPos);
            }
        }
        if (stateForPlacement == null) {
            return InteractionResult.FAIL;
        }
        if (!canPlace(level, x, y, z, player, stateForPlacement)) {
            return InteractionResult.FAIL;
        }
        if (!stateForPlacement.canSurvive_(level, x, y, z)) {
            return InteractionResult.FAIL;
        }
        if (!placeBlock(level, x, y, z, stateForPlacement)) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer p) {
            BlockState placedState = level.getBlockState_(x, y, z);
            Block placedBlock = placedState.getBlock();
            if (placedBlock == stateForPlacement.getBlock()) {
                placedBlock.setPlacedBy_(level, x, y, z, placedState, p, stack);
                CriteriaTriggers.PLACED_BLOCK.trigger_(p, x, y, z, stack);
                p.awardStat(EvolutionStats.BLOCK_PLACED.get(placedBlock));
            }
        }
        this.successPlaceLogic(level, x, y, z, player, stack);
        stack.shrink(1);
        SoundType soundType = stateForPlacement.getSoundType();
        level.playSound(player, x + 0.5, y + 0.5, z + 0.5, getPlaceSound(stateForPlacement), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected abstract void successPlaceLogic(Level level, int x, int y, int z, Player player, ItemStack stack);

    protected boolean usesCustomCondition() {
        return true;
    }
}
