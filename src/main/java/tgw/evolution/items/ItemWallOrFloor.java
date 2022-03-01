package tgw.evolution.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

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
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState wallState = this.wallBlock.getStateForPlacement(context);
        BlockState stateForPlacement = null;
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction != Direction.UP) {
                BlockState floorState = direction == Direction.DOWN ? this.getBlock().getStateForPlacement(context) : wallState;
                if (floorState != null && floorState.canSurvive(level, pos)) {
                    stateForPlacement = floorState;
                    break;
                }
            }
        }
        return stateForPlacement != null && level.isUnobstructed(stateForPlacement, pos, CollisionContext.empty()) ? stateForPlacement : null;
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
