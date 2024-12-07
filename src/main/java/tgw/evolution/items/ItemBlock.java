package tgw.evolution.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.IStructural;
import tgw.evolution.client.tooltip.TooltipStructuralArchCost;
import tgw.evolution.client.tooltip.TooltipStructuralBeamCost;
import tgw.evolution.client.tooltip.TooltipStructuralIntegrity;
import tgw.evolution.client.tooltip.TooltipStructureType;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.lists.custom.EitherList;
import tgw.evolution.util.collection.sets.RSet;
import tgw.evolution.util.collection.sets.SimpleEnumSet;
import tgw.evolution.util.constants.BlockFlags;

import java.util.List;
import java.util.Map;

public class ItemBlock extends ItemGeneric {

    private static final RSet<IStructural.BeamType> BEAM_TYPES = new SimpleEnumSet<>(IStructural.BeamType.class);
    protected final Block block;

    public ItemBlock(Block block, Properties builder) {
        super(builder);
        this.block = block;
    }

    public static @Nullable CompoundTag getBlockEntityData(ItemStack stack) {
        return stack.getTagElement("BlockEntityTag");
    }

    public static void updateCustomBlockEntityTag(Level level, Player player, int x, int y, int z, ItemStack stack) {
        MinecraftServer minecraftServer = level.getServer();
        if (minecraftServer == null) {
            return;
        }
        CompoundTag tag = getBlockEntityData(stack);
        if (tag != null) {
            BlockEntity tile = level.getBlockEntity_(x, y, z);
            if (tile != null) {
                if (!level.isClientSide && tile.onlyOpCanSetNbt() && !player.canUseGameMasterBlocks()) {
                    return;
                }
                CompoundTag savedData = tile.saveWithoutMetadata();
                CompoundTag oldData = savedData.copy();
                savedData.merge(tag);
                if (!savedData.equals(oldData)) {
                    tile.load(savedData);
                    tile.setChanged();
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        this.block.appendHoverText(stack, level, list, flag);
    }

    protected boolean canPlace(LevelReader level, int x, int y, int z, Player player, BlockState blockState) {
        return (!this.mustSurvive() || blockState.canSurvive_(level, x, y, z)) && level.isUnobstructed_(blockState, x, y, z, player);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (this.allowdedIn(tab)) {
            this.block.fillItemCategory(tab, list);
        }
    }

    public Block getBlock() {
        return this.block;
    }

    @Override
    public String getDescriptionId() {
        return this.block.getDescriptionId();
    }

    protected SoundEvent getPlaceSound(BlockState state) {
        return state.getSoundType().getPlaceSound();
    }

    protected @Nullable BlockState getPlacementState(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return this.block.getStateForPlacement_(level, x, y, z, player, hand, hitResult);
    }

    public void makeTooltip(EitherList<FormattedText, TooltipComponent> tooltip, boolean expanded) {
        if (this.block instanceof IStructural structural) {
            tooltip.addLeft(EvolutionTexts.EMPTY);
            if (expanded) {
                tooltip.addLeft(EvolutionTexts.TOOLTIP_STRUCTURAL);
                int min = Integer.MAX_VALUE;
                int max = 0;
                int minArch = Integer.MAX_VALUE;
                int maxArch = 0;
                int minBeam = Integer.MAX_VALUE;
                int maxBeam = 0;
                RSet<IStructural.BeamType> types = BEAM_TYPES;
                types.clear();
                types.add(IStructural.BeamType.NONE);
                OList<BlockState> possibleStates = this.block.getStateDefinition().getPossibleStates_();
                for (int i = 0, len = possibleStates.size(); i < len; ++i) {
                    BlockState state = possibleStates.get(i);
                    int integrity = structural.getIntegrity(state);
                    types.add(structural.getBeamType(state));
                    if (integrity < min) {
                        min = integrity;
                    }
                    if (integrity > max) {
                        max = integrity;
                    }
                    int arch = structural.getIncrementForArch(state);
                    if (arch < minArch) {
                        minArch = arch;
                    }
                    if (arch > maxArch) {
                        maxArch = arch;
                    }
                    int beam = structural.getIncrementForBeam(state);
                    if (beam < minBeam) {
                        minBeam = beam;
                    }
                    if (beam > maxBeam) {
                        maxBeam = beam;
                    }
                }
                if (types.contains(IStructural.BeamType.CARDINAL_ARCH)) {
                    types.add(IStructural.BeamType.CARDINAL_BEAM);
                }
                if (types.contains(IStructural.BeamType.X_ARCH)) {
                    types.add(IStructural.BeamType.X_BEAM);
                }
                if (types.contains(IStructural.BeamType.Z_ARCH)) {
                    types.add(IStructural.BeamType.Z_BEAM);
                }
                tooltip.addRight(TooltipStructuralIntegrity.setup(min, max));
                tooltip.addRight(TooltipStructureType.setup(types));
                if (types.contains(IStructural.BeamType.CARDINAL_ARCH) || types.contains(IStructural.BeamType.X_ARCH) || types.contains(IStructural.BeamType.Z_ARCH)) {
                    tooltip.addRight(TooltipStructuralArchCost.setup(minArch, maxArch));
                }
                if (types.contains(IStructural.BeamType.CARDINAL_BEAM) || types.contains(IStructural.BeamType.X_BEAM) || types.contains(IStructural.BeamType.Z_BEAM)) {
                    tooltip.addRight(TooltipStructuralBeamCost.setup(minBeam, maxBeam));
                }
            }
            else {
                tooltip.addLeft(EvolutionTexts.TOOLTIP_SHOW_STRUCTURAL);
            }
        }
    }

    protected boolean mustSurvive() {
        return true;
    }

    public InteractionResult place(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult, boolean canPlace) {
        if (!canPlace) {
            return InteractionResult.FAIL;
        }
        BlockState state = this.getPlacementState(level, x, y, z, player, hand, hitResult);
        if (state == null) {
            return InteractionResult.FAIL;
        }
        if (!this.canPlace(level, x, y, z, player, state)) {
            return InteractionResult.FAIL;
        }
        if (!this.placeBlock(level, x, y, z, state)) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        BlockState stateAtPos = level.getBlockState_(x, y, z);
        if (stateAtPos.is(state.getBlock())) {
            this.updateCustomBlockEntityTag(x, y, z, level, player, stack, stateAtPos);
            Block blockPlaced = stateAtPos.getBlock();
            blockPlaced.setPlacedBy_(level, x, y, z, stateAtPos, player, stack);
            if (player instanceof ServerPlayer p) {
                CriteriaTriggers.PLACED_BLOCK.trigger_(p, x, y, z, stack);
                player.awardStat(EvolutionStats.BLOCK_PLACED.get(blockPlaced));
            }
        }
        SoundType soundType = stateAtPos.getSoundType();
        level.playSound(player, x + 0.5, y + 0.5, z + 0.5, this.getPlaceSound(stateAtPos), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME_PARTIAL;
    }

    protected boolean placeBlock(LevelWriter level, int x, int y, int z, BlockState state) {
        return level.setBlock_(x, y, z, state, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.RENDER_MAINTHREAD);
    }

    public void registerBlocks(Map<Block, Item> map, Item item) {
        map.put(this.block, item);
    }

    protected void updateCustomBlockEntityTag(int x, int y, int z, Level level, Player player, ItemStack stack, BlockState state) {
        updateCustomBlockEntityTag(level, player, x, y, z, stack);
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
}
