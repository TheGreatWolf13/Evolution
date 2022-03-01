package tgw.evolution.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IClimbable {

    Direction getDirection(BlockState state);

    float getSweepAngle();

    double getUpSpeed();

    double getXPos(BlockState state);

    double getZPos(BlockState state);
}
