package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionCreativeTabs;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.MathHelper;

public class ItemClayMolded extends ItemBlock {

    public final boolean single;

    public ItemClayMolded(Block block) {
        this(block, false);
    }

    public ItemClayMolded(Block block, boolean single) {
        super(block, new Properties().tab(EvolutionCreativeTabs.MISC));
        this.single = single;
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (context.isSecondaryUseActive()) {
            if (!context.canPlace()) {
                return InteractionResult.FAIL;
            }
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos.below());
            if (!state.isFaceSturdy(level, pos, Direction.UP)) {
                return InteractionResult.FAIL;
            }
            CollisionContext selectionContext = context.getPlayer() == null ? CollisionContext.empty() : CollisionContext.of(context.getPlayer());
            if (!level.isUnobstructed(EvolutionBlocks.PIT_KILN.get().defaultBlockState(), pos, selectionContext)) {
                return InteractionResult.FAIL;
            }
            if (!level.setBlock(pos, EvolutionBlocks.PIT_KILN.get().defaultBlockState(),
                                BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.RERENDER)) {
                return InteractionResult.FAIL;
            }
            TEPitKiln tile = (TEPitKiln) level.getBlockEntity(pos);
            if (this.single) {
                tile.setNWStack(context.getItemInHand());
                tile.setSingle(true);
                level.playSound(context.getPlayer(), pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
                return InteractionResult.SUCCESS;
            }
            int x = MathHelper.getIndex(2, 0, 16, (context.getClickLocation().x - pos.getX()) * 16);
            int z = MathHelper.getIndex(2, 0, 16, (context.getClickLocation().z - pos.getZ()) * 16);
            if (x == 0) {
                if (z == 0) {
                    tile.setNWStack(context.getItemInHand());
                    level.playSound(context.getPlayer(), pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
                    return InteractionResult.SUCCESS;
                }
                tile.setSWStack(context.getItemInHand());
                level.playSound(context.getPlayer(), pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
                return InteractionResult.SUCCESS;
            }
            if (z == 0) {
                tile.setNEStack(context.getItemInHand());
                level.playSound(context.getPlayer(), pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
                return InteractionResult.SUCCESS;
            }
            tile.setSEStack(context.getItemInHand());
            level.playSound(context.getPlayer(), pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.75F);
            return InteractionResult.SUCCESS;
        }
        return super.place(context);
    }
}
