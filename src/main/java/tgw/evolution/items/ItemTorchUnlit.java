package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IFireSource;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;

import javax.annotation.Nullable;
import java.util.List;

import static tgw.evolution.init.EvolutionBStates.LIT;

public class ItemTorchUnlit extends ItemWallOrFloor {

    public ItemTorchUnlit(Properties properties) {
        super(EvolutionBlocks.TORCH.get(), EvolutionBlocks.WALL_TORCH.get(), properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_TORCH_RELIT);
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockItemUseContext context) {
        BlockState wallState = this.wallBlock.getStateForPlacement(context);
        BlockState stateForPlacement = null;
        IWorldReader world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction != Direction.UP) {
                BlockState floorState = direction == Direction.DOWN ? this.getBlock().getStateForPlacement(context) : wallState;
                if (floorState != null && floorState.canSurvive(world, blockpos)) {
                    stateForPlacement = floorState.setValue(LIT, false);
                    break;
                }
            }
        }
        return stateForPlacement != null && world.isUnobstructed(stateForPlacement, blockpos, ISelectionContext.empty()) ? stateForPlacement : null;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFireSource && ((IFireSource) block).isFireSource(state)) {
            Chunk chunk = world.getChunkAt(pos);
            if (CapabilityChunkStorage.remove(chunk, EnumStorage.OXYGEN, 1)) {
                CapabilityChunkStorage.add(chunk, EnumStorage.CARBON_DIOXIDE, 1);
                PlayerEntity player = context.getPlayer();
                context.getItemInHand().shrink(1);
                ItemStack stack = ItemTorch.createStack(world, 1);
                if (!player.inventory.add(stack)) {
                    BlockUtils.dropItemStack(world, pos, stack);
                }
                world.playSound(player, pos, SoundEvents.FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, world.random.nextFloat() * 0.7F + 0.3F);
                player.awardStat(Stats.ITEM_CRAFTED.get(EvolutionItems.torch.get()));
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.FAIL;
        }
        return super.useOn(context);
    }
}
