package tgw.evolution.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(BlockItem.class)
public abstract class Mixin_M_BlockItem extends Item {

    public Mixin_M_BlockItem(Properties properties) {
        super(properties);
    }

    @Overwrite
    public boolean canPlace(BlockPlaceContext context, BlockState state) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (!this.mustSurvive() || state.canSurvive_(context.getLevel(), x, y, z)) {
            return context.getLevel().isUnobstructed_(state, x, y, z, player);
        }
        return false;
    }

    @Shadow
    protected abstract SoundEvent getPlaceSound(BlockState blockState);

    @Shadow
    protected abstract @Nullable BlockState getPlacementState(BlockPlaceContext blockPlaceContext);

    @Shadow
    protected abstract boolean mustSurvive();

    @Overwrite
    public InteractionResult place(BlockPlaceContext context) {
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }
        BlockPlaceContext updatedContext = this.updatePlacementContext(context);
        if (updatedContext == null) {
            return InteractionResult.FAIL;
        }
        BlockState placementState = this.getPlacementState(updatedContext);
        if (placementState == null) {
            return InteractionResult.FAIL;
        }
        if (!this.placeBlock(updatedContext, placementState)) {
            return InteractionResult.FAIL;
        }
        BlockPos pos = updatedContext.getClickedPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        Level level = updatedContext.getLevel();
        Player player = updatedContext.getPlayer();
        ItemStack stack = updatedContext.getItemInHand();
        BlockState stateAtPos = level.getBlockState_(x, y, z);
        if (stateAtPos.is(placementState.getBlock())) {
            stateAtPos = this.updateBlockStateFromTag(pos, level, stack, stateAtPos);
            this.updateCustomBlockEntityTag(pos, level, player, stack, stateAtPos);
            stateAtPos.getBlock().setPlacedBy(level, pos, stateAtPos, player, stack);
            if (player instanceof ServerPlayer p) {
                CriteriaTriggers.PLACED_BLOCK.trigger_(p, x, y, z, stack);
            }
        }
        SoundType soundType = stateAtPos.getSoundType();
        level.playSound(player, pos, this.getPlaceSound(stateAtPos), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F,
                        soundType.getPitch() * 0.8F);
        level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Overwrite
    public boolean placeBlock(BlockPlaceContext context, BlockState state) {
        BlockPos pos = context.getClickedPos();
        return context.getLevel().setBlock_(pos.getX(), pos.getY(), pos.getZ(), state,
                                            BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.RENDER_MAINTHREAD);
    }

    @Shadow
    protected abstract BlockState updateBlockStateFromTag(BlockPos blockPos,
                                                          Level level,
                                                          ItemStack itemStack,
                                                          BlockState blockState);

    @Shadow
    protected abstract boolean updateCustomBlockEntityTag(BlockPos blockPos,
                                                          Level level,
                                                          @Nullable Player player,
                                                          ItemStack itemStack,
                                                          BlockState blockState);

    @Shadow
    public abstract @Nullable BlockPlaceContext updatePlacementContext(BlockPlaceContext blockPlaceContext);

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = this.place(new BlockPlaceContext(player, hand, player.getItemInHand(hand), hitResult));
        if (!result.consumesAction() && this.isEdible()) {
            InteractionResult secondaryResult = this.use(level, player, hand).getResult();
            return secondaryResult == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : secondaryResult;
        }
        return result;
    }
}
