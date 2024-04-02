package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

public interface IGrassSpreadable {

    default boolean canReceiveGrass(ServerLevel level, BlockState state, int x, int y, int z) {
        if (level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue_(BlockPos.asLong(x, y + 1, z)) < 12) {
            return false;
        }
        return !level.getBlockState_(x, y + 1, z).isFaceSturdy_(level, x, y + 1, z, Direction.DOWN);
    }

    int getAllowanceCost(BlockState state);

    BlockGenericSpreadable getGrass();
}
