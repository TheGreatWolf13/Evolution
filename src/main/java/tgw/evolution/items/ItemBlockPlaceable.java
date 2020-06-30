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

public abstract class ItemBlockPlaceable extends ItemBlock {

    public ItemBlockPlaceable(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public ActionResultType tryPlace(BlockItemUseContext context) {
        if (context == null) {
            return ActionResultType.FAIL;
        }
        if (context.getWorld().isRemote) {
            return ActionResultType.FAIL;
        }
        if (!context.canPlace()) {
            return ActionResultType.FAIL;
        }
        BlockState stateForPlacement = this.getStateForPlacement(context);
        if (context.isPlacerSneaking()) {
            stateForPlacement = this.getSneakingState(context);
        }
        if (this.customCondition(context.getWorld().getBlockState(context.getPos()).getBlock())) {
            stateForPlacement = this.getCustomState(context);
        }
        if (stateForPlacement == null) {
            return ActionResultType.FAIL;
        }
        if (!this.canPlace(context, stateForPlacement)) {
            return ActionResultType.FAIL;
        }
        if (!stateForPlacement.isValidPosition(context.getWorld(), context.getPos())) {
            return ActionResultType.FAIL;
        }
        if (!this.placeBlock(context, stateForPlacement)) {
            return ActionResultType.FAIL;
        }
        BlockPos pos = context.getPos();
        World worldIn = context.getWorld();
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        ItemStack stack = context.getItem();
        BlockState stateAtPos = worldIn.getBlockState(pos);
        Block blockAtPos = stateAtPos.getBlock();
        if (blockAtPos == stateForPlacement.getBlock()) {
            CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, stack);
        }
        if (player.isSneaking()) {
            this.sneakingAction(context);
        }
        SoundType soundtype = stateAtPos.getSoundType(worldIn, pos, context.getPlayer());
        worldIn.playSound(null, pos, this.getPlaceSound(stateAtPos, worldIn, pos, context.getPlayer()), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        stack.shrink(1);
        player.swingArm(context.getHand());
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCHandAnimation(context.getHand()));
        return ActionResultType.SUCCESS;
    }

    @Override
    protected boolean canPlace(BlockItemUseContext context, BlockState stateForPlacement) {
        PlayerEntity player = context.getPlayer();
        ISelectionContext selectionContext = player == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(player);
        return stateForPlacement.isValidPosition(context.getWorld(), context.getPos()) && context.getWorld().func_217350_a(stateForPlacement, context.getPos(), selectionContext);
    }

    public abstract BlockState getSneakingState(BlockItemUseContext context);

    public abstract boolean customCondition(Block block);

    @Nullable
    public abstract BlockState getCustomState(BlockItemUseContext context);

    public void sneakingAction(BlockItemUseContext context) {
    }
}
