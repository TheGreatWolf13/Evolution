package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;
import java.util.Map;

public class ItemWallOrFloor extends ItemBlock {

    protected final Block wallBlock;

    public ItemWallOrFloor(Block floorBlock, Block wallBlockIn, Item.Properties propertiesIn) {
        super(floorBlock, propertiesIn);
        this.wallBlock = wallBlockIn;
    }

    @Override
    @Nullable
    protected BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState wallState = this.wallBlock.getStateForPlacement(context);
        BlockState stateForPlacement = null;
        IWorldReader worldIn = context.getWorld();
        BlockPos blockpos = context.getPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction != Direction.UP) {
                BlockState floorState = direction == Direction.DOWN ? this.getBlock().getStateForPlacement(context) : wallState;
                if (floorState != null && floorState.isValidPosition(worldIn, blockpos)) {
                    stateForPlacement = floorState;
                    break;
                }
            }
        }
        return stateForPlacement != null && worldIn.func_217350_a(stateForPlacement, blockpos, ISelectionContext.dummy()) ? stateForPlacement : null;
    }

    @Override
    public void addToBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
        super.addToBlockToItemMap(blockToItemMap, itemIn);
        blockToItemMap.put(this.wallBlock, itemIn);
    }

    @Override
    public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
        super.removeFromBlockToItemMap(blockToItemMap, itemIn);
        blockToItemMap.remove(this.wallBlock);
    }
}
