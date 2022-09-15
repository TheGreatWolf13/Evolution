package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.extensions.IForgeBlockState;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCHandAnimation;
import tgw.evolution.util.constants.BlockFlags;

import org.jetbrains.annotations.Nullable;

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

    protected static SoundEvent getPlaceSound(IForgeBlockState state, LevelReader level, BlockPos pos, Player entity) {
        return state.getSoundType(level, pos, entity).getPlaceSound();
    }

    protected static boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return context.getLevel().setBlock(context.getClickedPos(), state, BlockFlags.NOTIFY_UPDATE_AND_RERENDER);
    }

    public abstract boolean customCondition(Block block);

    @Nullable
    public abstract BlockState getCustomState(BlockPlaceContext context);

    @Nullable
    public abstract BlockState getSneakingState(BlockPlaceContext context);

    public abstract void sucessPlaceLogic(BlockPlaceContext context);

    public InteractionResult tryPlace(BlockPlaceContext context) {
        if (context == null) {
            return InteractionResult.FAIL;
        }
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
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState stateInPos = level.getBlockState(pos);
        Block blockInPos = stateInPos.getBlock();
        if (blockInPos == stateForPlacement.getBlock()) {
            blockInPos.setPlacedBy(level, pos, stateInPos, player, stack);
            CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, stack);
        }
        this.sucessPlaceLogic(context);
        SoundType soundtype = stateInPos.getSoundType(level, pos, context.getPlayer());
        player.swing(context.getHand());
        EvolutionNetwork.send(player, new PacketSCHandAnimation(context.getHand()));
        level.playSound(null,
                        pos,
                        getPlaceSound(stateInPos, level, pos, context.getPlayer()),
                        SoundSource.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F,
                        soundtype.getPitch() * 0.8F);
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult resultType = this.tryPlace(new BlockPlaceContext(context));
        return resultType != InteractionResult.SUCCESS && this.isEdible() ?
               this.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult() :
               resultType;
    }
}
