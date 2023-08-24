package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.util.constants.BlockFlags;

public abstract class ItemGenericPlaceable extends ItemEv {

    protected ItemGenericPlaceable(Properties properties) {
        super(properties);
    }

    protected static boolean canPlace(BlockPlaceContext context, BlockState state) {
        Player playerentity = context.getPlayer();
        CollisionContext collisionContext = playerentity == null ? CollisionContext.empty() : CollisionContext.of(playerentity);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) &&
               context.getLevel().isUnobstructed(state, context.getClickedPos(), collisionContext);
    }

    protected static SoundEvent getPlaceSound(BlockState state) {
        return state.getSoundType().getPlaceSound();
    }

    protected static boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return context.getLevel()
                      .setBlock(context.getClickedPos(), state, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.RENDER_MAINTHREAD);
    }

    public abstract boolean customCondition(Block block);

    public abstract @Nullable BlockState getCustomState(BlockPlaceContext context);

    public abstract @Nullable BlockState getSneakingState(BlockPlaceContext context);

    public abstract void sucessPlaceLogic(BlockPlaceContext context);

    public InteractionResult tryPlace(BlockPlaceContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.FAIL;
        }
        BlockState stateForPlacement = null;
        if (context.isSecondaryUseActive()) {
            stateForPlacement = this.getSneakingState(context);
        }
        if (this.customCondition(context.getLevel().getBlockState(context.getClickedPos()).getBlock())) {
            stateForPlacement = this.getCustomState(context);
        }
        if (stateForPlacement == null) {
            return InteractionResult.FAIL;
        }
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }
        if (!canPlace(context, stateForPlacement)) {
            return InteractionResult.FAIL;
        }
        if (!stateForPlacement.canSurvive(context.getLevel(), context.getClickedPos())) {
            return InteractionResult.FAIL;
        }
        if (!placeBlock(context, stateForPlacement)) {
            return InteractionResult.FAIL;
        }
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (context.getPlayer() instanceof ServerPlayer player) {
            ItemStack stack = context.getItemInHand();
            BlockState stateInPos = level.getBlockState(pos);
            Block blockInPos = stateInPos.getBlock();
            if (blockInPos == stateForPlacement.getBlock()) {
                blockInPos.setPlacedBy(level, pos, stateInPos, player, stack);
                CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, stack);
                player.awardStat(EvolutionStats.BLOCK_PLACED.get(blockInPos));
            }
            this.sucessPlaceLogic(context);
            SoundType soundtype = stateInPos.getSoundType();
            player.swing(context.getHand());
            level.playSound(null, pos, getPlaceSound(stateInPos), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = this.tryPlace(new BlockPlaceContext(player, hand, player.getItemInHand(hand), hitResult));
        return result != InteractionResult.SUCCESS && this.isEdible() ? this.use(level, player, hand).getResult() : result;
    }
}
