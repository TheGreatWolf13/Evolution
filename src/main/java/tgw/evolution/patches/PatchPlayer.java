package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface PatchPlayer extends PatchLivingEntity {

    float getDestroySpeed(BlockState state, BlockPos pos);

    double getMotionX();

    double getMotionY();

    double getMotionZ();

    boolean isCrawling();

    boolean isMoving();

    void setCrawling(boolean crawling);
}
