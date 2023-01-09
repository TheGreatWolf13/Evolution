package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.BlockUtils;
import tgw.evolution.blocks.IFireSource;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionTexts;

import java.util.List;

import static tgw.evolution.init.EvolutionBStates.LIT;

public class ItemTorchUnlit extends ItemWallOrFloor {

    public ItemTorchUnlit(Properties properties) {
        super(EvolutionBlocks.TORCH.get(), EvolutionBlocks.TORCH_WALL.get(), properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(EvolutionTexts.TOOLTIP_TORCH_RELIT);
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState wallState = this.wallBlock.getStateForPlacement(context);
        BlockState stateForPlacement = null;
        LevelReader level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction != Direction.UP) {
                BlockState floorState = direction == Direction.DOWN ? this.getBlock().getStateForPlacement(context) : wallState;
                if (floorState != null && floorState.canSurvive(level, blockpos)) {
                    stateForPlacement = floorState.setValue(LIT, false);
                    break;
                }
            }
        }
        return stateForPlacement != null && level.isUnobstructed(stateForPlacement, blockpos, CollisionContext.empty()) ? stateForPlacement : null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFireSource fireSource && fireSource.isFireSource(state)) {
            LevelChunk chunk = level.getChunkAt(pos);
            Player player = context.getPlayer();
            context.getItemInHand().shrink(1);
            ItemStack stack = ItemTorch.createStack(level, 1);
            assert player != null;
            if (!player.getInventory().add(stack)) {
                BlockUtils.dropItemStack(level, pos, stack);
            }
            level.playSound(player, pos, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, level.random.nextFloat() * 0.7F + 0.3F);
            player.awardStat(Stats.ITEM_CRAFTED.get(EvolutionItems.TORCH.get()));
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
