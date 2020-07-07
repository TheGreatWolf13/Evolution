package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ItemBlock extends ItemEv {

    private final Block block;

    public ItemBlock(Block block, Item.Properties builder) {
        super(builder);
        this.block = block;
    }

    private static <T extends Comparable<T>> BlockState getBlockStateFromString(BlockState state, IProperty<T> property, String string) {
        return property.parseValue(string).map(value -> state.with(property, value)).orElse(state);
    }

    public static void setTileEntityNBT(World world, @Nullable PlayerEntity player, BlockPos pos, ItemStack stack) {
        MinecraftServer server = world.getServer();
        if (server == null) {
            return;
        }
        CompoundNBT compound = stack.getChildTag("BlockEntityTag");
        if (compound != null) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) {
                if (!world.isRemote && tile.onlyOpsCanSetNbt() && (player == null || !player.canUseCommandBlock())) {
                    return;
                }
                CompoundNBT placingCompound = tile.write(new CompoundNBT());
                CompoundNBT oldCompound = placingCompound.copy();
                placingCompound.merge(compound);
                placingCompound.putInt("x", pos.getX());
                placingCompound.putInt("y", pos.getY());
                placingCompound.putInt("z", pos.getZ());
                if (!placingCompound.equals(oldCompound)) {
                    tile.read(placingCompound);
                    tile.markDirty();
                }
            }
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ActionResultType actionResult = this.tryPlace(new BlockItemUseContext(context));
        return actionResult != ActionResultType.SUCCESS && this.isFood() ? this.onItemRightClick(context.getWorld(), context.getPlayer(), context.getHand()).getType() : actionResult;
    }

    public ActionResultType tryPlace(BlockItemUseContext context) {
        if (!context.canPlace()) {
            return ActionResultType.FAIL;
        }
        BlockItemUseContext blockUseContext = this.getBlockItemUseContext(context);
        if (blockUseContext == null) {
            return ActionResultType.FAIL;
        }
        BlockState stateForPlacement = this.getStateForPlacement(blockUseContext);
        if (stateForPlacement == null) {
            return ActionResultType.FAIL;
        }
        if (!this.placeBlock(blockUseContext, stateForPlacement)) {
            return ActionResultType.FAIL;
        }
        BlockPos pos = blockUseContext.getPos();
        World world = blockUseContext.getWorld();
        PlayerEntity player = blockUseContext.getPlayer();
        ItemStack stack = blockUseContext.getItem();
        BlockState stateInWorld = world.getBlockState(pos);
        Block blockInWorld = stateInWorld.getBlock();
        if (blockInWorld == stateForPlacement.getBlock()) {
            stateInWorld = this.updateBlockState(pos, world, stack, stateInWorld);
            this.onBlockPlaced(pos, world, player, stack);
            blockInWorld.onBlockPlacedBy(world, pos, stateInWorld, player, stack);
            if (player instanceof ServerPlayerEntity) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
            }
        }
        SoundType soundtype = stateInWorld.getSoundType(world, pos, context.getPlayer());
        world.playSound(player, pos, this.getPlaceSound(stateInWorld, world, pos, context.getPlayer()), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        stack.shrink(1);
        return ActionResultType.SUCCESS;
    }

    //Forge: Sensitive version of BlockItem#getPlaceSound
    protected SoundEvent getPlaceSound(BlockState state, World world, BlockPos pos, PlayerEntity entity) {
        return state.getSoundType(world, pos, entity).getPlaceSound();
    }

    @Nullable
    public BlockItemUseContext getBlockItemUseContext(BlockItemUseContext context) {
        return context;
    }

    protected void onBlockPlaced(BlockPos pos, World worldIn, @Nullable PlayerEntity player, ItemStack stack) {
        setTileEntityNBT(worldIn, player, pos, stack);
    }

    @Nullable
    protected BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = this.getBlock().getStateForPlacement(context);
        return blockstate != null && this.canPlace(context, blockstate) ? blockstate : null;
    }

    private BlockState updateBlockState(BlockPos pos, World world, ItemStack stack, BlockState state) {
        BlockState newState = state;
        CompoundNBT compound = stack.getTag();
        if (compound != null) {
            CompoundNBT blockStateTag = compound.getCompound("BlockStateTag");
            StateContainer<Block, BlockState> stateContainer = state.getBlock().getStateContainer();
            for (String tag : blockStateTag.keySet()) {
                IProperty<?> property = stateContainer.getProperty(tag);
                if (property != null) {
                    String value = blockStateTag.get(tag).getString();
                    newState = getBlockStateFromString(newState, property, value);
                }
            }
        }
        if (newState != state) {
            world.setBlockState(pos, newState, 2);
        }
        return newState;
    }

    protected boolean canPlace(BlockItemUseContext blockUseContext, BlockState state) {
        PlayerEntity player = blockUseContext.getPlayer();
        ISelectionContext selectionContext = player == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(player);
        return (!this.checkPosition() || state.isValidPosition(blockUseContext.getWorld(), blockUseContext.getPos())) && blockUseContext.getWorld().func_217350_a(state, blockUseContext.getPos(), selectionContext);
    }

    protected boolean checkPosition() {
        return true;
    }

    protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
        return context.getWorld().setBlockState(context.getPos(), state, 11);
    }

    @Override
    public String getTranslationKey() {
        return this.getBlock().getTranslationKey();
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            this.getBlock().fillItemGroup(group, items);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        this.getBlock().addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Nullable
    public Block getBlock() {
        return this.block == null ? null : this.block.delegate.get();
    }

    public void addToBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
        blockToItemMap.put(this.getBlock(), itemIn);
    }

    public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
        blockToItemMap.remove(this.getBlock());
    }
}
