package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
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
    public ActionResultType tryPlace(BlockItemUseContext context) {
        if (context.isPlacerSneaking()) {
            if (!context.canPlace()) {
                return ActionResultType.FAIL;
            }
            if (!Block.hasSolidSide(context.getWorld().getBlockState(context.getPos().down()), context.getWorld(), context.getPos(), Direction.UP)) {
                return ActionResultType.FAIL;
            }
            ISelectionContext iselectioncontext = context.getPlayer() == null ?
                                                  ISelectionContext.dummy() :
                                                  ISelectionContext.forEntity(context.getPlayer());
            if (!context.getWorld().func_217350_a(EvolutionBlocks.PIT_KILN.get().getDefaultState(), context.getPos(), iselectioncontext)) {
                return ActionResultType.FAIL;
            }
            if (!context.getWorld().setBlockState(context.getPos(), EvolutionBlocks.PIT_KILN.get().getDefaultState(), 11)) {
                return ActionResultType.FAIL;
            }
            TEPitKiln tile = (TEPitKiln) context.getWorld().getTileEntity(context.getPos());
            if (this.single) {
                tile.setNWStack(context.getItem());
                tile.setSingle(true);
                context.getWorld()
                       .playSound(context.getPlayer(), context.getPos(), SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
                return ActionResultType.SUCCESS;
            }
            int x = MathHelper.getIndex(2, 0, 16, (context.getHitVec().x - context.getPos().getX()) * 16);
            int z = MathHelper.getIndex(2, 0, 16, (context.getHitVec().z - context.getPos().getZ()) * 16);
            if (x == 0) {
                if (z == 0) {
                    tile.setNWStack(context.getItem());
                    context.getWorld()
                           .playSound(context.getPlayer(), context.getPos(), SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
                    return ActionResultType.SUCCESS;
                }
                tile.setSWStack(context.getItem());
                context.getWorld()
                       .playSound(context.getPlayer(), context.getPos(), SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
                return ActionResultType.SUCCESS;
            }
            if (z == 0) {
                tile.setNEStack(context.getItem());
                context.getWorld()
                       .playSound(context.getPlayer(), context.getPos(), SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
                return ActionResultType.SUCCESS;
            }
            tile.setSEStack(context.getItem());
            context.getWorld().playSound(context.getPlayer(), context.getPos(), SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.75F);
            return ActionResultType.SUCCESS;
        }
        return super.tryPlace(context);
    }
}
