package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(DoubleHighBlockItem.class)
public abstract class MixinDoubleHighBlockItem extends BlockItem {

    public MixinDoubleHighBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockState stateToClearTheWay = level.isWaterAt_(x, y + 1, z) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        level.setBlock_(x, y + 1, z, stateToClearTheWay, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | BlockFlags.RENDER_MAINTHREAD | BlockFlags.UPDATE_NEIGHBORS);
        return super.placeBlock(context, state);
    }
}
