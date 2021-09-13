package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;

public class ItemClayMolded extends ItemBlock {

    public final boolean single;

    public ItemClayMolded(RegistryObject<Block> block) {
        this(block, false);
    }

    public ItemClayMolded(RegistryObject<Block> block, boolean single) {
        super(block.get(), EvolutionItems.propMisc());
        this.single = single;
    }

    @Override
    public ActionResultType place(BlockItemUseContext context) {
        if (context.isSecondaryUseActive()) {
            if (!context.canPlace()) {
                return ActionResultType.FAIL;
            }
            World world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = world.getBlockState(pos.below());
            if (!state.isFaceSturdy(world, pos, Direction.UP)) {
                return ActionResultType.FAIL;
            }
            ISelectionContext selectionContext = context.getPlayer() == null ? ISelectionContext.empty() : ISelectionContext.of(context.getPlayer());
            if (!world.isUnobstructed(EvolutionBlocks.PIT_KILN.get().defaultBlockState(), pos, selectionContext)) {
                return ActionResultType.FAIL;
            }
            if (!world.setBlock(pos, EvolutionBlocks.PIT_KILN.get().defaultBlockState(), BlockFlags.NOTIFY_UPDATE_AND_RERENDER)) {
                return ActionResultType.FAIL;
            }
            TEPitKiln tile = (TEPitKiln) world.getBlockEntity(pos);
            if (this.single) {
                tile.setNWStack(context.getItemInHand());
                tile.setSingle(true);
                world.playSound(context.getPlayer(), pos, SoundEvents.GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
                return ActionResultType.SUCCESS;
            }
            int x = MathHelper.getIndex(2, 0, 16, (context.getClickLocation().x - pos.getX()) * 16);
            int z = MathHelper.getIndex(2, 0, 16, (context.getClickLocation().z - pos.getZ()) * 16);
            if (x == 0) {
                if (z == 0) {
                    tile.setNWStack(context.getItemInHand());
                    world.playSound(context.getPlayer(), pos, SoundEvents.GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
                    return ActionResultType.SUCCESS;
                }
                tile.setSWStack(context.getItemInHand());
                world.playSound(context.getPlayer(), pos, SoundEvents.GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
                return ActionResultType.SUCCESS;
            }
            if (z == 0) {
                tile.setNEStack(context.getItemInHand());
                world.playSound(context.getPlayer(), pos, SoundEvents.GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
                return ActionResultType.SUCCESS;
            }
            tile.setSEStack(context.getItemInHand());
            world.playSound(context.getPlayer(), pos, SoundEvents.GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
            return ActionResultType.SUCCESS;
        }
        return super.place(context);
    }
}
