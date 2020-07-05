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

import javax.annotation.Nullable;

public abstract class ItemPlaceable extends ItemEv {

    protected ItemPlaceable(Properties properties) {
        super(properties);
    }

    protected static boolean placeBlock(BlockItemUseContext context, BlockState state) {
        return context.getWorld().setBlockState(context.getPos(), state, 11);
    }

    protected static SoundEvent getPlaceSound(BlockState state, World world, BlockPos pos, PlayerEntity entity) {
        return state.getSoundType(world, pos, entity).getPlaceSound();
    }

    protected static boolean canPlace(BlockItemUseContext context, BlockState state) {
        PlayerEntity playerentity = context.getPlayer();
        ISelectionContext iselectioncontext = playerentity == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(playerentity);
        return state.isValidPosition(context.getWorld(), context.getPos()) && context.getWorld().func_217350_a(state, context.getPos(), iselectioncontext);
    }

    public ActionResultType tryPlace(BlockItemUseContext context) {
        if (context == null) {
            return ActionResultType.FAIL;
        }
        if (context.getWorld().isRemote) {
            return ActionResultType.FAIL;
        }
        BlockState stateForPlacement = null;
        if (context.isPlacerSneaking()) {
            stateForPlacement = this.getSneakingState(context);
        }
        if (this.customCondition(context.getWorld().getBlockState(context.getPos()).getBlock())) {
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
        if (!stateForPlacement.isValidPosition(context.getWorld(), context.getPos())) {
            return ActionResultType.FAIL;
        }
        if (!placeBlock(context, stateForPlacement)) {
            return ActionResultType.FAIL;
        }
        BlockPos pos = context.getPos();
        World worldIn = context.getWorld();
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        ItemStack stack = context.getItem();
        BlockState stateInPos = worldIn.getBlockState(pos);
        Block blockInPos = stateInPos.getBlock();
        if (blockInPos == stateForPlacement.getBlock()) {
            blockInPos.onBlockPlacedBy(worldIn, pos, stateInPos, player, stack);
            CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, stack);
        }
        this.sucessPlaceLogic(context);
        SoundType soundtype = stateInPos.getSoundType(worldIn, pos, context.getPlayer());
        player.swingArm(context.getHand());
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCHandAnimation(context.getHand()));
        worldIn.playSound(null, pos, getPlaceSound(stateInPos, worldIn, pos, context.getPlayer()), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        stack.shrink(1);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ActionResultType actionresulttype = this.tryPlace(new BlockItemUseContext(context));
        return actionresulttype != ActionResultType.SUCCESS && this.isFood() ? this.onItemRightClick(context.getWorld(), context.getPlayer(), context.getHand()).getType() : actionresulttype;
    }

    @Nullable
    public abstract BlockState getSneakingState(BlockItemUseContext context);

    public abstract boolean customCondition(Block block);

    @Nullable
    public abstract BlockState getCustomState(BlockItemUseContext context);

    public abstract void sucessPlaceLogic(BlockItemUseContext context);
}
