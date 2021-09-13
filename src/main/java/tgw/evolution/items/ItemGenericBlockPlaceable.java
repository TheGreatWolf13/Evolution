package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCHandAnimation;

import javax.annotation.Nullable;

public abstract class ItemGenericBlockPlaceable extends ItemBlock {

    public ItemGenericBlockPlaceable(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    protected boolean canPlace(BlockItemUseContext blockUseContext, BlockState state) {
        PlayerEntity player = blockUseContext.getPlayer();
        ISelectionContext selectionContext = player == null ? ISelectionContext.empty() : ISelectionContext.of(player);
        return state.canSurvive(blockUseContext.getLevel(), blockUseContext.getClickedPos()) &&
               blockUseContext.getLevel().isUnobstructed(state, blockUseContext.getClickedPos(), selectionContext);
    }

    public abstract boolean customCondition(Block blockAtPlacing, Block blockClicking);

    @Nullable
    public abstract BlockState getCustomState(BlockItemUseContext context);

    public abstract BlockState getSneakingState(BlockItemUseContext context);

    @Override
    public ActionResultType place(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (world.isClientSide) {
            return ActionResultType.FAIL;
        }
        if (!context.canPlace()) {
            return ActionResultType.FAIL;
        }
        BlockState stateForPlacement = this.getPlacementState(context);
        if (context.isSecondaryUseActive()) {
            stateForPlacement = this.getSneakingState(context);
        }
        if (this.customCondition(world.getBlockState(pos).getBlock(),
                                 world.getBlockState(pos.relative(context.getClickedFace().getOpposite())).getBlock())) {
            stateForPlacement = this.getCustomState(context);
        }
        if (stateForPlacement == null) {
            return ActionResultType.FAIL;
        }
        if (!this.canPlace(context, stateForPlacement)) {
            return ActionResultType.FAIL;
        }
        if (!stateForPlacement.canSurvive(world, pos)) {
            return ActionResultType.FAIL;
        }
        if (!this.placeBlock(context, stateForPlacement)) {
            return ActionResultType.FAIL;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState stateAtPos = world.getBlockState(pos);
        Block blockAtPos = stateAtPos.getBlock();
        if (blockAtPos == stateForPlacement.getBlock()) {
            CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, stack);
        }
        if (player.isCrouching()) {
            this.sneakingAction(context);
        }
        SoundType soundtype = stateAtPos.getSoundType(world, pos, context.getPlayer());
        world.playSound(null,
                        pos,
                        this.getPlaceSound(stateAtPos, world, pos, context.getPlayer()),
                        SoundCategory.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F,
                        soundtype.getPitch() * 0.8F);
        stack.shrink(1);
        player.swing(context.getHand());
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCHandAnimation(context.getHand()));
        return ActionResultType.SUCCESS;
    }

    public void sneakingAction(BlockItemUseContext context) {
    }
}
