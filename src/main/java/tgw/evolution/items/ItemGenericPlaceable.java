package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCHandAnimation;
import tgw.evolution.util.BlockFlags;

import javax.annotation.Nullable;

public abstract class ItemGenericPlaceable extends ItemEv {

    protected ItemGenericPlaceable(Properties properties) {
        super(properties);
    }

    protected static boolean canPlace(BlockItemUseContext context, BlockState state) {
        PlayerEntity playerentity = context.getPlayer();
        ISelectionContext selectionContext = playerentity == null ? ISelectionContext.empty() : ISelectionContext.of(playerentity);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) &&
               context.getLevel().isUnobstructed(state, context.getClickedPos(), selectionContext);
    }

    protected static SoundEvent getPlaceSound(BlockState state, World world, BlockPos pos, PlayerEntity entity) {
        return state.getSoundType(world, pos, entity).getPlaceSound();
    }

    protected static boolean placeBlock(BlockItemUseContext context, BlockState state) {
        return context.getLevel().setBlock(context.getClickedPos(), state, BlockFlags.NOTIFY_UPDATE_AND_RERENDER);
    }

    public abstract boolean customCondition(Block block);

    @Nullable
    public abstract BlockState getCustomState(BlockItemUseContext context);

    @Nullable
    public abstract BlockState getSneakingState(BlockItemUseContext context);

    public abstract void sucessPlaceLogic(BlockItemUseContext context);

    public ActionResultType tryPlace(BlockItemUseContext context) {
        if (context == null) {
            return ActionResultType.FAIL;
        }
        if (context.getLevel().isClientSide) {
            return ActionResultType.FAIL;
        }
        BlockState stateForPlacement = null;
        if (context.isSecondaryUseActive()) {
            stateForPlacement = this.getSneakingState(context);
        }
        if (this.customCondition(context.getLevel().getBlockState(context.getClickedPos()).getBlock())) {
            stateForPlacement = this.getCustomState(context);
        }
        if (stateForPlacement == null) {
            return ActionResultType.FAIL;
        }
        if (!context.canPlace()) {
            return ActionResultType.FAIL;
        }
        if (!canPlace(context, stateForPlacement)) {
            return ActionResultType.FAIL;
        }
        if (!stateForPlacement.canSurvive(context.getLevel(), context.getClickedPos())) {
            return ActionResultType.FAIL;
        }
        if (!placeBlock(context, stateForPlacement)) {
            return ActionResultType.FAIL;
        }
        BlockPos pos = context.getClickedPos();
        World world = context.getLevel();
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState stateInPos = world.getBlockState(pos);
        Block blockInPos = stateInPos.getBlock();
        if (blockInPos == stateForPlacement.getBlock()) {
            blockInPos.setPlacedBy(world, pos, stateInPos, player, stack);
            CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, stack);
        }
        this.sucessPlaceLogic(context);
        SoundType soundtype = stateInPos.getSoundType(world, pos, context.getPlayer());
        player.swing(context.getHand());
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCHandAnimation(context.getHand()));
        world.playSound(null,
                        pos,
                        getPlaceSound(stateInPos, world, pos, context.getPlayer()),
                        SoundCategory.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F,
                        soundtype.getPitch() * 0.8F);
        stack.shrink(1);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        ActionResultType resultType = this.tryPlace(new BlockItemUseContext(context));
        return resultType != ActionResultType.SUCCESS && this.isEdible() ?
               this.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult() :
               resultType;
    }
}
