package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

public abstract class ItemGenericBlockPlaceable extends ItemBlock {

    public ItemGenericBlockPlaceable(Block block, Properties builder) {
        super(block, builder);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        Player player = context.getPlayer();
        CollisionContext collisionContext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) &&
               context.getLevel().isUnobstructed(state, context.getClickedPos(), collisionContext);
    }

    public abstract boolean customCondition(Block blockAtPlacing, Block blockClicking);

    public abstract @Nullable BlockState getCustomState(BlockPlaceContext context);

    public abstract BlockState getSneakingState(BlockPlaceContext context);

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide) {
            return InteractionResult.FAIL;
        }
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }
        BlockState stateForPlacement = this.getPlacementState(context);
        if (context.isSecondaryUseActive()) {
            stateForPlacement = this.getSneakingState(context);
        }
        if (this.customCondition(level.getBlockState(pos).getBlock(),
                                 level.getBlockState(pos.relative(context.getClickedFace().getOpposite())).getBlock())) {
            stateForPlacement = this.getCustomState(context);
        }
        if (stateForPlacement == null) {
            return InteractionResult.FAIL;
        }
        if (!this.canPlace(context, stateForPlacement)) {
            return InteractionResult.FAIL;
        }
        if (!stateForPlacement.canSurvive(level, pos)) {
            return InteractionResult.FAIL;
        }
        if (!this.placeBlock(context, stateForPlacement)) {
            return InteractionResult.FAIL;
        }
        if (context.getPlayer() instanceof ServerPlayer player) {
            ItemStack stack = context.getItemInHand();
            BlockState stateAtPos = level.getBlockState(pos);
            Block blockAtPos = stateAtPos.getBlock();
            if (blockAtPos == stateForPlacement.getBlock()) {
                CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, stack);
            }
            if (player.isCrouching()) {
                this.sneakingAction(context);
            }
            SoundType soundtype = stateAtPos.getSoundType();
            level.playSound(null,
                            pos,
                            this.getPlaceSound(stateAtPos),
                            SoundSource.BLOCKS,
                            (soundtype.getVolume() + 1.0F) / 2.0F,
                            soundtype.getPitch() * 0.8F);
            stack.shrink(1);
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public void sneakingAction(BlockPlaceContext context) {
    }
}
