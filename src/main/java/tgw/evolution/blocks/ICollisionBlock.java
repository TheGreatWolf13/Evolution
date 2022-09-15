package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface ICollisionBlock {

    /**
     * @return Whether the speed of the Entity was changed
     */
    boolean collision(Level level, BlockPos pos, Entity entity, double speed, double mass, @Nullable Direction.Axis axis);

    float getSlowdownSide(BlockState state);

    float getSlowdownTop(BlockState state);
}
