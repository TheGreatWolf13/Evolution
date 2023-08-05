package tgw.evolution.items;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ItemWallOrFloor extends ItemBlock {

    protected final Block wallBlock;

    public ItemWallOrFloor(Block floorBlock, Block wallBlockIn, Item.Properties propertiesIn) {
        super(floorBlock, propertiesIn);
        this.wallBlock = wallBlockIn;
    }

    @Override
    protected @Nullable BlockState getPlacementState(Level level,
                                                     int x,
                                                     int y,
                                                     int z,
                                                     Player player,
                                                     InteractionHand hand,
                                                     BlockHitResult hitResult) {
        if (hitResult.getDirection() == Direction.UP) {
            return this.block.getStateForPlacement_(level, x, y, z, player, hand, hitResult);
        }
        return this.wallBlock.getStateForPlacement_(level, x, y, z, player, hand, hitResult);
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        super.registerBlocks(blockToItemMap, item);
        blockToItemMap.put(this.wallBlock, item);
    }
}
