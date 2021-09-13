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
    protected BlockState getPlacementState(BlockItemUseContext context) {
        BlockState wallState = this.wallBlock.getStateForPlacement(context);
        BlockState stateForPlacement = null;
        IWorldReader world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction != Direction.UP) {
                BlockState floorState = direction == Direction.DOWN ? this.getBlock().getStateForPlacement(context) : wallState;
                if (floorState != null && floorState.canSurvive(world, pos)) {
                    stateForPlacement = floorState;
                    break;
                }
            }
        }
        return stateForPlacement != null && world.isUnobstructed(stateForPlacement, pos, ISelectionContext.empty()) ? stateForPlacement : null;
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        super.registerBlocks(blockToItemMap, item);
        blockToItemMap.put(this.wallBlock, item);
    }

    @Override
    public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item item) {
        super.removeFromBlockToItemMap(blockToItemMap, item);
        blockToItemMap.remove(this.wallBlock);
    }
}
