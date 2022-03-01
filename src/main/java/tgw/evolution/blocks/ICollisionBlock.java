package tgw.evolution.blocks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ICollisionBlock {

    void collision(LivingEntity entity, double speed);

    float getSlowdownSide(BlockState state);

    float getSlowdownTop(BlockState state);
}
