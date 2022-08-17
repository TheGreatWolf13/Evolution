package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.util.constants.BlockFlags;

public final class TEUtils {

    private TEUtils() {
    }

    public static void sendRenderUpdate(BlockEntity tile) {
        tile.setChanged();
        Level world = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        BlockState state = world.getBlockState(pos);
        tile.getLevel().sendBlockUpdated(pos, state, state, BlockFlags.RERENDER);
    }
}
