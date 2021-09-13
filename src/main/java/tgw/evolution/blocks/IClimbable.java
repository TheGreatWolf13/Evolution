package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public interface IClimbable {

    Direction getDirection(BlockState state);

    float getSweepAngle();

    double getUpSpeed();

    double getXPos(BlockState state);

    double getZPos(BlockState state);
}
